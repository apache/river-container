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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compiler for state machine instances. The "input" to the compiler is actually
 * a class that is annotated with the
 *
 * @RootState annotation.
 *
 */
class StateMachineCompiler {

    private static final Logger log =
            Logger.getLogger(StateMachineCompiler.class.getName(), MessageNames.BUNDLE_NAME);
    Class[] eventInterfaces = null;

    /**
     * Retrieve the event interface, as set by the
     *
     * @RootState annotation.
     * @return The event interface.
     */
    Class[] getEventInterfaces() {
        return eventInterfaces;
    }

    MetaState compile(Class rootStateClass) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        log.log(Level.FINE, MessageNames.BEGINNING_COMPILE, new Object[]{rootStateClass.getName()});
        findEventInterfaces(rootStateClass);
        // First pass: create all metastates
        MetaState rootMetaState = createMetaState(rootStateClass);
        // Second pass: Fill in event methods
        rootMetaState.visitAll(new MetaStateVisitor() {
            @Override
            public void visit(MetaState ms) {
                try {
                    fillInEventMethods(ms);
                    fillInGuardMethods(ms);
                    fillInEntryMethods(ms);
                    fillInExitMethods(ms);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        log.log(Level.FINE, MessageNames.COMPILE_COMPLETED, new Object[]{rootStateClass.getName()});
        return rootMetaState;
    }

    private void findEventInterfaces(Class rootStateClass) {
        RootState rootStateAnnotation = (RootState) rootStateClass.getAnnotation(RootState.class);
        if (rootStateAnnotation == null || rootStateAnnotation.value() == null) {
            throw new RuntimeException("Root state class must specify @RootState(interfaceClass).");
        }
        eventInterfaces = rootStateAnnotation.value();
    }

    MetaState createMetaState(Class stateClass) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return createMetaState(null, stateClass);
    }

    MetaState createMetaState(MetaState parentMetaState, Class stateClass) throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

        MetaState metaState = new MetaState();
        metaState.stateClass = stateClass;
        metaState.parent = parentMetaState;

        processSubstates(metaState);
        return metaState;
    }

    /**
     * Look for fields that are annotated with
     *
     * @State and fill in the required substate info.
     * @param metaState
     */
    private void processSubstates(MetaState metaState) throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        for (Field f : metaState.stateClass.getDeclaredFields()) {
            // Look for fields annotated with @State.
            State ann = (State) f.getAnnotation(State.class);
            if (ann == null) {
                continue;
            }
            SubstateInfo info = new SubstateInfo();
            info.setField(f);
            metaState.substates.add(info);
            if (ann.value() == null) {
                throw new RuntimeException("@State needs a list of possible states");
            }
            info.setPossibleMetaStates(createPossibleMetaStates(metaState, ann.value()).toArray(new MetaState[0]));
            Initial initialAnn = f.getAnnotation(Initial.class);
            if (initialAnn == null) {
                throw new RuntimeException("Need initial state for "
                        + f.getName());
            }
            MetaState initialMetaState = findMetaState(info.getPossibleMetaStates(), initialAnn.value());
            if (initialMetaState == null) {
                throw new RuntimeException("Couldn't find a metastate that corresponds to " + initialAnn.value() + " in " + Utils.format(info.getPossibleMetaStates()));
            }
            info.setInitialMetaState(initialMetaState);

            /* While we're at it, set the active metastate. */
            info.setActiveMetaState(info.getInitialMetaState());
            Retained retainedAnn = f.getAnnotation(Retained.class);
            info.setRetained(retainedAnn != null);

            /* Add the non-retained metastate to the list of transitions-on-entry of the
             * parent metastat.
             */
            if (!info.isRetained()) {
                metaState.entryTransitions.add(new TransitionOnSubstate(info, info.getInitialMetaState()));
            }
        }
    }

    private List<MetaState> createPossibleMetaStates(MetaState metaState, Class[] substateClasses) throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        List<MetaState> metaStates = new ArrayList<MetaState>();
        /* For each class, we'll need an instance. */
        for (Class substateClass : substateClasses) {
            metaStates.add(createMetaState(metaState, substateClass));
        }
        return metaStates;

    }

    private MetaState findMetaState(MetaState[] possibleMetaStates, Class value) {
        for (MetaState metaState : possibleMetaStates) {
            if (metaState.stateClass == value) {
                return metaState;
            }
        }
        return null;
    }

    private void fillInEventMethods(MetaState metaState) throws NoSuchMethodException {
        for (Class eventInterface : getEventInterfaces()) {
            for (Method m : eventInterface.getMethods()) {
                /* See if the state class has a method with the same name and 
                 * parameters.
                 */
                Method eventMethod = null;
                try {
                    eventMethod = metaState.stateClass.getMethod(m.getName(), m.getParameterTypes());
                } catch (NoSuchMethodException nsme) {
                    // Silent catch - lets the event method remain null, which we check for.
                    eventMethod=null; //Redundant but keeps PMD happy.
                }
                Operation operation = null;
                if (eventMethod != null) {
                    if (eventMethod.getReturnType() != null && !m.getReturnType().isAssignableFrom(eventMethod.getReturnType())) {
                        throw new RuntimeException("If the event method returns a value, its type must match the event interface's method.  Required"
                                + m.getReturnType() + ", found " + eventMethod.getReturnType());
                    }
                    Transition transition = eventMethod.getAnnotation(Transition.class);
                    // Fill in from here down!
                    // Return value, no transition
                    if (eventMethod.getReturnType() != null && transition == null) {
                        operation = new InvokeAndTransitionOperation(metaState, eventMethod);
                    } // Return value with transition
                    else if (eventMethod.getReturnType() != null && transition != null) {
                        TransitionOnSubstate[] transitions = resolveTransitions(metaState, transition.value());
                        operation = new InvokeAndTransitionOperation(metaState, eventMethod, transitions);
                    } // No return value, no transition
                    else if (eventMethod.getReturnType() == null && transition == null) {
                        operation = new InvokeVoidAndTransitionOperation(metaState, eventMethod);
                    } // No return value, with transition
                    else if (eventMethod.getReturnType() == null && transition != null) {
                        TransitionOnSubstate[] transitions = resolveTransitions(metaState, transition.value());
                        operation = new InvokeVoidAndTransitionOperation(metaState, eventMethod, transitions);
                    }
                }
                metaState.eventMethods.put(m, operation);
            }
        }
    }

    private void fillInGuardMethods(MetaState metaState) {
        Class stateClass = metaState.stateClass;
        for (Method m : stateClass.getMethods()) {
            Guard guard = m.getAnnotation(Guard.class);
            if (guard != null) {
                if (m.getReturnType() != boolean.class) {
                    throw new StateMachineException(MessageNames.BUNDLE_NAME, MessageNames.GUARD_METHOD_DOESNT_RETURN_BOOLEAN,
                            new Object[]{m.toGenericString(), m.getReturnType()});
                }
                TransitionOnSubstate[] transitions = resolveTransitions(metaState, guard.value());
                Operation op = new InvokeGuardOperation(metaState, m, transitions);
                metaState.guardMethods.add(op);
            }

        }
    }

    private void fillInEntryMethods(MetaState metaState) {
        Class stateClass = metaState.stateClass;
        for (Method m : stateClass.getMethods()) {
            OnEntry onEntry = m.getAnnotation(OnEntry.class);
            if (onEntry != null) {
                if (m.getReturnType() != void.class) {
                    throw new StateMachineException(MessageNames.BUNDLE_NAME, MessageNames.ENTRY_METHOD_ISNT_VOID,
                            new Object[]{m.toGenericString(), m.getReturnType()});
                }
                Operation op = new InvokeVoidAndTransitionOperation(metaState, m, new TransitionOnSubstate[0]);
                metaState.entryMethods.add(op);
            }

        }
    }

    private void fillInExitMethods(MetaState metaState) {
        Class stateClass = metaState.stateClass;
        for (Method m : stateClass.getMethods()) {
            OnExit onEntry = m.getAnnotation(OnExit.class);
            if (onEntry != null) {
                if (m.getReturnType() != void.class) {
                    throw new StateMachineException(MessageNames.BUNDLE_NAME, MessageNames.EXIT_METHOD_ISNT_VOID,
                            new Object[]{m.toGenericString(), m.getReturnType()});
                }
                Operation op = new InvokeVoidAndTransitionOperation(metaState, m, new TransitionOnSubstate[0]);
                metaState.exitMethods.add(op);
            }

        }
    }

    /**
     * Convert an array of classes to a set of transitions with reference to a
     * given metastate. Transitions are specified as an array of classes on an
     * event method in a state class. In order to be used, they need to be
     * resolved to a particular substate field related to that metastate. <br>
     * The class referenced can be one of <ul> <li>a class that is a possible
     * state of one of the substates defined on the state class that contains
     * the annotated event method.</li> <li>A class that is a possible peer
     * state to the state class that contains the annotated method. </li> <li>A
     * class that is a possible peer state to an ancester of the state class
     * that contains the annotated method. </li> </ul>
     *
     * @param metaState
     * @param transitionClasses
     * @return
     */
    private TransitionOnSubstate[] resolveTransitions(MetaState metaState, Class[] transitionClasses) {
        List<TransitionOnSubstate> allTransitions = new ArrayList<TransitionOnSubstate>();
        for (Class c : transitionClasses) {
            List<TransitionOnSubstate> transitionsForClass = new ArrayList<TransitionOnSubstate>();
            resolveSubstateTransitions(transitionsForClass, metaState, c);
            resolvePeerAndParentTransitions(transitionsForClass, metaState, c);
            if (transitionsForClass.isEmpty()) {
                throw new StateMachineException(MessageNames.BUNDLE_NAME, MessageNames.CANT_RESOLVE_TRANSITIONS_FOR_CLASS,
                        new Object[]{metaState.stateClass.getName(), c.getName()});
            }
            allTransitions.addAll(transitionsForClass);
        }
        return allTransitions.toArray(new TransitionOnSubstate[0]);
    }

    /**
     * Go through each of the substates and find any metastates that correspond
     * to the given state class. Create a transition for them and add it to the
     * list of transitions.
     *
     * @param transitionsForClass
     * @param metaState
     * @param c
     */
    private void resolveSubstateTransitions(List<TransitionOnSubstate> transitionsForClass, MetaState metaState, Class c) {
        for (SubstateInfo substateInfo : metaState.substates) {
            for (MetaState m : substateInfo.getPossibleMetaStates()) {
                if (m.stateClass == c) {
                    transitionsForClass.add(new TransitionOnSubstate(substateInfo, m));
                }
            }
        }
    }

    private void resolvePeerAndParentTransitions(List<TransitionOnSubstate> transitionsForClass, MetaState metaState, Class c) {
        for (MetaState currentMetaState = metaState.parent; currentMetaState != null; currentMetaState = currentMetaState.parent) {
            resolveSubstateTransitions(transitionsForClass, currentMetaState, c);
        }
    }
}
