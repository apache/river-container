/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.river.container.hsm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class PlainStateMachineExecutor implements StateMachineExecutor, StateMachineInfo {

    private static final Logger log =
            Logger.getLogger(PlainStateMachineExecutor.class.getName(),
            MessageNames.BUNDLE_NAME);

    /**
     * Convenience method to compile a state machine and instantiate it.
     *
     * @param rootStateClass
     * @return
     */
    public static Object createProxy(Class rootStateClass) {
        StateMachineCompiler compiler = new StateMachineCompiler();
        try {
            MetaState rootState = compiler.compile(rootStateClass);
            instantiate(rootState);
            return createProxy(rootState);
        } catch (StateMachineException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StateMachineException(ex, MessageNames.BUNDLE_NAME, MessageNames.ERROR_CREATING_PROXY);
        }
    }

    /**
     * Convenience method to compile a state machine and instantiate it.
     *
     * @param rootStateClass
     * @return
     */
    public static Object createProxy(Object rootStateInstance) {
        StateMachineCompiler compiler = new StateMachineCompiler();
        try {
            MetaState rootState = compiler.compile(rootStateInstance.getClass());
            rootState.stateInstance = rootStateInstance;
            instantiate(rootState);
            return createProxy(rootState);
        } catch (StateMachineException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StateMachineException(ex, MessageNames.BUNDLE_NAME, MessageNames.ERROR_CREATING_PROXY);
        }
    }

    /**
     * Create a fully-instantiated metastate from a compiled metastate.
     *
     * @param rootState
     */
    public static void instantiate(MetaState rootState) {
        /* Create a user class instance to go with every metastate. */
        rootState.visitAll(new MetaStateVisitor() {
            @Override
            public void visit(MetaState metaState) {
                try {
                    if (metaState.stateInstance != null) {
                        return;
                    }
                    /*
                     * Goal here is to create a stateInstance for each metastate.
                     * In the simple case, the stateClass is a root class, and we can
                     * just instantiate it.
                     */
                    if (metaState.stateClass.getEnclosingClass() == null) {
                        metaState.stateInstance = metaState.stateClass.newInstance();
                    } else {
                        /* OK, we need to get an instance of the enclosing class.
                         * That should be the stateInstance of a parent state.
                         */
                        Object parentInstance = findAParentInstance(metaState, metaState.stateClass.getEnclosingClass());

                        Constructor con = metaState.stateClass.getConstructor(parentInstance.getClass());
                        metaState.stateInstance = con.newInstance(parentInstance);
                    }
                    log.log(Level.FINE, MessageNames.METASTATE_WAS_INSTANTIATED, new Object[]{metaState.stateClass, metaState.stateInstance});
                } catch (Exception ex) {
                    throw new StateMachineException(ex, MessageNames.BUNDLE_NAME, MessageNames.ERROR_INSTANTIATING);
                }
            }
        });
        /* Now set all the initial state instance values. */
        rootState.visitAll(new MetaStateVisitor() {
            @Override
            public void visit(MetaState metaState) {
                try {
                    for (SubstateInfo ssi : metaState.substates) {
                        ssi.setObjectThatHoldsField(metaState.stateInstance);
                        log.log(Level.FINE, MessageNames.SETTING_FIELD_TO, new Object[]{ssi.getField().getName(), metaState.stateInstance, ssi.getInitialMetaState().stateInstance});
                        writeStateField(ssi, metaState);
                    }
                } catch (Exception ex) {
                    throw new StateMachineException(ex, MessageNames.BUNDLE_NAME, MessageNames.ERROR_INSTANTIATING);
                }
            }
        });
    }

    private static void writeStateField(SubstateInfo ssi, MetaState metaState) throws IllegalArgumentException, IllegalAccessException, SecurityException {
        boolean originalAccess = ssi.getField().isAccessible();
        ssi.getField().setAccessible(true);
        ssi.getField().set(ssi.getObjectThatHoldsField(), metaState.stateInstance);
        ssi.getField().setAccessible(originalAccess);
    }

    private static Object findAParentInstance(MetaState metaState, Class enclosingClass) {
        for (MetaState currentState = metaState.parent; currentState != null; currentState = currentState.parent) {
            if (currentState.stateClass == enclosingClass) {
                return currentState.stateInstance;
            }
        }
        throw new StateMachineException(MessageNames.BUNDLE_NAME, MessageNames.NO_PARENT_INSTANCE,
                new Object[]{enclosingClass, metaState.stateInstance});
    }

    public static Object createProxy(MetaState instantiatedMetaState) {
        RootState rootStateAnnotation = (RootState) instantiatedMetaState.stateClass.getAnnotation(RootState.class);
        Class[] eventInterfaces = rootStateAnnotation.value();
        PlainStateMachineExecutor executor = new PlainStateMachineExecutor(instantiatedMetaState);
        executor.activate();
        Object proxy =
                Proxy.newProxyInstance(eventInterfaces[0].getClassLoader(),
                eventInterfaces,
                executor.getInvocationHandler());
        return proxy;
    }
    InvocationHandler invocationHandler = null;

    public InvocationHandler getInvocationHandler() {
        if (invocationHandler == null) {
            invocationHandler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return runEvent(method, args);
                }
            };
        }
        return invocationHandler;
    }

    PlainStateMachineExecutor(MetaState instantiatedMetaState) {
        this.rootMetaState = instantiatedMetaState;
        fillInControllerReferences();
        fillInRootStateReferences();
    }
    MetaState rootMetaState = null;

    private void activate() {
        List<MetaState> initialStates = buildActiveStates();
        runEntryMethods(initialStates);
    }

    private synchronized Object runEvent(Method eventMethod, Object[] args)
            throws Throwable {
        clearOutput();
        clearExceptions();
        clearTransitions();
        List<MetaState> initialStates = buildActiveStates();
        runEventActions(initialStates, eventMethod, args);
        runGuardMethods(initialStates);
        applyTransitions(initialStates);
        List<MetaState> finalStates = buildActiveStates();
        List<MetaState> exiting = new ArrayList<MetaState>();
        List<MetaState> entering = new ArrayList<MetaState>();
        calculateStateDelta(initialStates, finalStates, exiting, entering);
        runExitMethods(exiting);
        runEntryMethods(entering);
        if (hasExceptions()) {
            throw buildOutputException();
        }
        if (eventMethod.getReturnType() != null) {
            return buildOutputValue(eventMethod.getReturnType());
        }
        return null;
    }
    Object output = null;

    private void clearOutput() {
        output = null;
    }

    private Object buildOutputValue(Class returnType) {
        if (returnType==null ) {
            // No output expected.  If there is output, it isn't necessarily an
            // error, so ignore it (i.e. don't throw an exception 
            // if returnType== null and output!=null.
            return null;
        }
        returnType=normalizeReturnType(returnType);
        if (output!= null && ! returnType.isAssignableFrom(output.getClass())) {
            throw new StateMachineException(MessageNames.BUNDLE_NAME, MessageNames.ERROR_INCOMPATIBLE_OUTPUT,
                new Object[] { returnType, output.getClass() });
        }
        return output;
    }
    
    /**
     * Handle return types for primitive values.
     * @param returnType
     * @return 
     */
    private Class normalizeReturnType(Class returnType) {
        if (returnType==int.class) {
            return Integer.class;
        } else if (returnType==float.class) {
            return Float.class;
        } else if (returnType==double.class) {
            return Double.class;
        } else if (returnType==long.class) {
            return Long.class;
        } else if (returnType==short.class) {
            return Short.class;
        } else if (returnType==boolean.class) {
            return Boolean.class;
        } else if (returnType==byte.class) {
            return Byte.class;
        } else if (returnType==char.class) {
            return Character.class;
        }
        return returnType;
    }
    
    List<Throwable> exceptions = new ArrayList<Throwable>();

    private void clearExceptions() {
        exceptions.clear();
    }

    private boolean hasExceptions() {
        return !exceptions.isEmpty();
    }

    private Throwable buildOutputException() {
        if (exceptions.size() == 1) {
            return exceptions.get(0);
        } else {
            return new StateMachineException(MessageNames.BUNDLE_NAME, MessageNames.MULTIPLE_EXCEPTIONS_THROWN,
                    new Object[0]);
        }
    }
    List<TransitionOnSubstate> queuedTransitions = new ArrayList<TransitionOnSubstate>();

    private void clearTransitions() {
        queuedTransitions.clear();
    }

    @Override
    public void queueTransition(TransitionOnSubstate t) {
        log.log(Level.FINER, MessageNames.QUEUED_TRANSITION,
                new Object[]{t.targetMetaState.stateClass.getSimpleName(),
                    t.targetMetaState.parent.stateClass.getSimpleName()});
        queuedTransitions.add(t);
    }

    @Override
    public void output(Object outputObject) {
        output = outputObject;
    }

    @Override
    public void exception(MetaState metaState, Method interfaceMethod, Throwable cause) {
        log.log(Level.FINE, MessageNames.STORING_EXCEPTION, new Object[]{metaState.stateInstance, interfaceMethod.getName(), cause.toString()});
        exceptions.add(cause);
    }

    private List<MetaState> buildActiveStates() {
        return rootMetaState.getActiveMetaStates();
    }

    private void runEventActions(List<MetaState> activeStates, Method eventInterfaceMethod, Object[] args) {
        boolean thereWasAnEventMethod=false;
        for (MetaState ms : activeStates) {
            Operation op = ms.eventMethods.get(eventInterfaceMethod);
            if (op != null) {
                thereWasAnEventMethod=true;
                op.eval(this, args);
            }
        }
        if(!thereWasAnEventMethod) {
            exceptions.add(new IllegalStateException());
        }
    }

    private void runGuardMethods(List<MetaState> activeStates) {
        for (MetaState ms : activeStates) {
            for (Operation op : ms.guardMethods) {
                op.eval(this, null);
            }
        }
    }

    private void runExitMethods(List<MetaState> exitingStates) {
        for (MetaState ms : exitingStates) {
            for (Operation op : ms.exitMethods) {
                op.eval(this, null);
            }
        }
    }

    private void runEntryMethods(List<MetaState> enteringStates) {
        for (MetaState ms : enteringStates) {
            for (Operation op : ms.entryMethods) {
                op.eval(this, null);
            }
        }
    }

    private void applyTransitions(List<MetaState> activeStates) {

        while (!queuedTransitions.isEmpty()) {
            /* Pull a transition. */
            TransitionOnSubstate tos = queuedTransitions.remove(queuedTransitions.size() - 1);
            /* Apply it. */
            applyTransition(tos);
            /* Add any implied transitions to the list of transitions, if the new state is not 
             * currently active. */
            if (!activeStates.contains(tos.targetMetaState)) {
                queuedTransitions.addAll(tos.targetMetaState.entryTransitions);
            }
        }
    }

    private void applyTransition(TransitionOnSubstate tos) {
        try {
            tos.substate.setActiveMetaState(tos.targetMetaState);
            writeStateField(tos.substate, tos.targetMetaState);
            /* Get rid of this - it doesn't work on private fields.  Use the writeStateField() above.
             tos.substate.getField().set(tos.targetMetaState.parent.stateInstance, tos.targetMetaState.stateInstance);
             */
        } catch (Exception ex) {
            StateMachineException sme = new StateMachineException(ex, MessageNames.BUNDLE_NAME, MessageNames.ERROR_APPLYING_TRANSITION,
                    new Object[]{ex.getMessage()});
            //sme.printStackTrace();
            throw sme;
        }
    }

    private void calculateStateDelta(List<MetaState> initialStates, List<MetaState> finalStates, List<MetaState> exiting, List<MetaState> entering) {
        for (MetaState initialState : initialStates) {
            if (!finalStates.contains(initialState)) {
                exiting.add(initialState);
            }
        }
        for (MetaState finalState : finalStates) {
            if (!initialStates.contains(finalState)) {
                entering.add(finalState);
            }
        }
    }

    private void fillInControllerReferences() {
        rootMetaState.visitAll(new MetaStateVisitor() {
            @Override
            public void visit(MetaState metaState) {
                log.fine("Visiting " + metaState + " to fill in controller reference.");
                fillInReferenceFields(metaState, Controller.class, PlainStateMachineExecutor.this);
            }
        });
    }

    private void fillInRootStateReferences() {
        rootMetaState.visitAll(new MetaStateVisitor() {
            @Override
            public void visit(MetaState metaState) {
                log.fine("Visiting " + metaState + " to fill in root state reference.");
                fillInReferenceFields(metaState, RootState.class, rootMetaState.stateInstance);
            }
        });
    }

    private void fillInReferenceFields(MetaState metaState, Class<? extends Annotation> aClass, Object value) {

        for (Field f : metaState.stateClass.getFields()) {

            if (f.getAnnotation(aClass) != null) {
                try {
                    log.fine("Setting field " + metaState.stateInstance + "." + f.getName()
                            + " to " + value);
                    boolean accessible = f.isAccessible();
                    f.setAccessible(true);
                    f.set(metaState.stateInstance, value);
                    f.setAccessible(accessible);
                } catch (Exception ex) {
                    throw new StateMachineException(MessageNames.BUNDLE_NAME,
                            MessageNames.ERROR_INSTANTIATING, new Object[]{ex.getMessage()});

                }
            }
        }
    }

    @Override
    public List<Class> getActiveStates() {
        return rootMetaState.getActiveStates();
    }
}
