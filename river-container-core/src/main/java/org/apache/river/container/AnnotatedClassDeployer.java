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
package org.apache.river.container;

import java.beans.Introspector;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author trasukg
 */
public class AnnotatedClassDeployer implements ContextListener {

    private static final Logger log =
            Logger.getLogger(AnnotatedClassDeployer.class.getName(),
            MessageNames.BUNDLE_NAME);
    private Context context = null;
    private List<DeployedObject> uninitializedObjects =
            new ArrayList<DeployedObject>();
    private Map<String, DeployedObject> initializedObjects =
            new HashMap<String, DeployedObject>();
    private List<DeploymentListener> deploymentListeners =
            new ArrayList<DeploymentListener>();

    public List<DeploymentListener> getDeploymentListeners() {
        return deploymentListeners;
    }

    public void put(String name, Object o) {
        try {
            /*
            Scan the object's class and register any injection needs from it.
             */
            readInObject(name, o);
            /* Attempt to satisfy the object's injection needs from current
            candidates.
             */
            resolve();
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException(
                    ex,
                    MessageNames.ILLEGAL_ARGUMENT_EXCEPTION);
        } catch (IllegalAccessException ex) {
            throw new ConfigurationException(
                    ex,
                    MessageNames.ILLEGAL_ACCESS_EXCEPTION);
        } catch (InvocationTargetException ex) {
            throw new ConfigurationException(
                    ex,
                    MessageNames.INVOCATION_TARGET_EXCEPTION);
        }
    }

    public void remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setContext(Context ctx) {
        this.context = ctx;
    }

    /** 
    When the context notifies us that it is done reading the initialization
    file, etc, then we can inject default values, check for unresolved
    dependencies, etc.
     */
    public void initComplete() {
        checkUnresolvedDependencies();
    }

    @Override
    public void shutDown() {
        for (DeploymentListener l : getDeploymentListeners()) {
            l.shutDown();
        }
    }

    private void checkUnresolvedDependencies() {
        if (uninitializedObjects.isEmpty()) {
            return;
        }

        for (DeployedObject depObj : uninitializedObjects) {
            for (Member m : depObj.getUnresolvedDependencies()) {
                log.log(Level.SEVERE,
                        MessageNames.UNRESOLVED_DEPENDENCY,
                        new Object[]{depObj.getName(),
                            m.getName(), nameOfRequiredObject(m)});
            }
        }
        throw new ConfigurationException(MessageNames.ANNOTATED_OBJECT_DEPLOYER_HAS_UNRESOLVED_DEPENDENCIES);
    }

    /**
    Given an object, see if it is fully resolved (no outstanding dependencies).
    If it is, then call any initialization that may be required, and move it
    to our list of 'ready' objects.
    @param deployed
    @return True if the list of 'ready' objects has been changed.
    @throws IllegalAccessException
    @throws IllegalArgumentException
    @throws InvocationTargetException
     */
    private boolean initializeIfFullyResolved(DeployedObject deployed) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (deployed.getUnresolvedDependencies().isEmpty()) {
            if (deployed.isInitialized() == false) {

                uninitializedObjects.remove(deployed);
                for (Method initMethod : deployed.getInitMethods()) {
                    initMethod.invoke(deployed.getDeployedObject());
                }
                notifyDeploymentListeners(deployed);
                initializedObjects.put(deployed.getName(), deployed);
                if (deployed.getDeployedObject() instanceof DeploymentListener) {
                    deploymentListeners.add((DeploymentListener) deployed.getDeployedObject());
                }
                if (!deployed.getShutdownMethods().isEmpty()) {
                    deploymentListeners.add(new ShutdownRunner(deployed));
                }
                deployed.setInitialized(true);
            }
            return true;
        }
        return false;
    }

    private void notifyDeploymentListeners(DeployedObject deployed) {
        for (DeploymentListener l : deploymentListeners) {
            l.postInit(deployed.getName(), deployed.getDeployedObject());
        }
    }

    private void readInObject(String name, Object o) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        log.log(Level.FINER,
                MessageNames.READING_OBJECT,
                new Object[]{name, o.getClass().getName()});

        /* Get the object's class. */
        Class cls = o.getClass();
        DeployedObject deployed = new DeployedObject();
        deployed.setDeployedObject(o);
        deployed.setName(name);
        List<Member> members = buildMemberList(cls);
        List<Member> unresolved = deployed.getUnresolvedDependencies();
        log.log(Level.FINER,
                MessageNames.READING_OBJECT_MEMBER_COUNT, members.size());

        for (Member m : members) {
            if (isAnnotatedAsInjected(m)) {
                log.log(Level.FINER,
                        MessageNames.READING_OBJECT_ANNOTATED_MEMBER_FOUND, m.toString());
                unresolved.add(m);
            }
            if (isInitMethod(m)) {
                deployed.getInitMethods().add((Method) m);
            }
            if (isShutdownMethod(m)) {
                deployed.getShutdownMethods().add((Method) m);
            }
            if (isAnnotatedAsName(m)) {
                memberSet(deployed.getDeployedObject(), m, deployed.getName());
            }
        }
        /* Add the object
        and list to our unsatisfied group. If it has no dependencies, then it
        will be initialized and moved over to the initialized group when
        resolve() is called.
         */
        uninitializedObjects.add(deployed);
    }

    /**
    It's an init method if it is
    <ul>
    <li>a Method</li>
    <li>is annotated @Init</li>
    <li>takes no parameter</li>
    <li>returns void</li>
    </ul>
    @param m The method to evaluate.
    @return
     */
    private boolean isInitMethod(Member m) {
        AnnotatedElement annEm = (AnnotatedElement) m;
        if (annEm.getAnnotation(Init.class) == null) {
            return false;
        }
        Method method = (Method) m;
        if (method.getReturnType() != void.class) {
            throw new ConfigurationException(MessageNames.INIT_METHOD_NOT_VOID,
                    m.getDeclaringClass().getName(),
                    m.getName(),
                    method.getReturnType());
        }
        if (method.getParameterTypes().length != 0) {
            throw new ConfigurationException(MessageNames.INIT_METHOD_HAS_PARAMETERS,
                    m.getDeclaringClass().getName(),
                    m.getName());
        }
        return true;
    }

    /**
    It's a shutdown method if it is
    <ul>
    <li>a Method</li>
    <li>is annotated @Shutdown</li>
    <li>takes no parameter</li>
    <li>returns void</li>
    </ul>
    @param m The method to evaluate.
    @return
     */
    private boolean isShutdownMethod(Member m) {
        AnnotatedElement annEm = (AnnotatedElement) m;
        if (annEm.getAnnotation(Shutdown.class) == null) {
            return false;
        }
        Method method = (Method) m;
        if (method.getReturnType() != void.class) {
            throw new ConfigurationException(MessageNames.SHUTDOWN_METHOD_NOT_VOID,
                    m.getDeclaringClass().getName(),
                    m.getName(),
                    method.getReturnType());
        }
        if (method.getParameterTypes().length != 0) {
            throw new ConfigurationException(MessageNames.SHUTDOWN_METHOD_HAS_PARAMETERS,
                    m.getDeclaringClass().getName(),
                    m.getName());
        }
        return true;
    }

    private boolean isAnnotatedAsInjected(Member m) {
        AnnotatedElement annEm = (AnnotatedElement) m;
        if (annEm.getAnnotation(Injected.class) != null) {
            if (m instanceof Field) {
                return true;
            }
            if (m instanceof Method && m.getName().startsWith(Strings.SET)) {
                return true;
            } else {
                throw new ConfigurationException(MessageNames.BAD_MEMBER_FOR_INJECTED_ANNOTATION, m.getDeclaringClass(), m.getName());
            }
        }
        return false;
    }

    private boolean isAnnotatedAsName(Member m) {
        AnnotatedElement annEm = (AnnotatedElement) m;
        if (annEm.getAnnotation(Name.class) != null) {
            if (m instanceof Field && ((Field) m).getType() == String.class) {
                return true;
            }
            if (m instanceof Method && m.getName().startsWith(Strings.SET)
                    && ((Method) m).getParameterTypes().length == 1
                    && ((Method) m).getParameterTypes()[0] == String.class) {
                return true;
            } else {
                throw new ConfigurationException(MessageNames.BAD_MEMBER_FOR_NAME_ANNOTATION, m.getDeclaringClass(), m.getName());
            }
        }
        return false;
    }

    private boolean isNull(Object o, Member m) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Object value = null;
        if (m instanceof Field) {
            value = ((Field) m).get(o);
        }
        if (m instanceof Method) {
            value = ((Method) m).invoke(o, new Object[0]);
        }
        return value == null;
    }

    /** Build a list of members that might be candidates for injection.
    
    @param cls
    @return
    @throws SecurityException
     */
    List<Member> buildMemberList(Class cls) throws SecurityException {
        List<Member> members = new ArrayList<Member>();
        try {
        members.addAll(Arrays.asList(cls.getDeclaredMethods()));
        members.addAll(Arrays.asList(cls.getDeclaredFields()));
        } catch(Error err) {
            Utils.logClassLoaderHierarchy(log, cls);
            throw err;
        }
        return members;
    }

    private void removeDependencyFromUnresolvedList(DeployedObject deployed, Member m) {
        deployed.getUnresolvedDependencies().remove(m);
    }

    private void resolve() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        boolean changed;
        do {
            changed = false;
            /* For each object in the unsatisfied group, */
            /* Group of uninitialized objects may change while we're going
            through them, so use a copy of the array.
             */
            for (DeployedObject deployed : new ArrayList<DeployedObject>(uninitializedObjects)) {
                changed = resolveDeployedObject(deployed);
            }
        } while (changed);

    }

    private boolean resolveDeployedObject(DeployedObject deployed) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        /* Attempt to resolve unsatisfied dependencies. */
        for (Member m : new ArrayList<Member>(deployed.getUnresolvedDependencies())) {
            Object val = null;
            InjectionStyle style = injectionStyle(m);

            if (style == InjectionStyle.BY_NAME) {
                String name = nameOfRequiredObject(m);
                DeployedObject deployedCandidate=initializedObjects.get(name);
                if (deployedCandidate != null) {
                    val=deployedCandidate.getDeployedObject();
                }
            } else if (style==InjectionStyle.BY_TYPE) {
                /* Find a type-compatible object if name lookup fails. */
                val = findAssignable(m);
            }
            if (val != null) {
                inject(deployed, m, val);
                removeDependencyFromUnresolvedList(deployed, m);
            }
        }
        /* If satisfied, remove from unsatisfied group and
        put into candidate group, then initialize. */
        boolean changed = initializeIfFullyResolved(deployed);
        return changed;
    }

    private InjectionStyle injectionStyle(Member m) {
        AnnotatedElement annEm = (AnnotatedElement) m;
        Injected inj = (Injected) annEm.getAnnotation(Injected.class);
        return inj.style() != InjectionStyle.DEFAULT ? inj.style() : InjectionStyle.BY_NAME;
    }

    private String nameOfRequiredObject(Member m) {
        AnnotatedElement annEm = (AnnotatedElement) m;
        Injected inj = (Injected) annEm.getAnnotation(Injected.class);
        if (inj.value() != null && !Strings.EMPTY.equals(inj.value())) {
            return inj.value();
        }
        if (m instanceof Field) {
            return ((Field) m).getName();
        }
        if (m instanceof Method) {
            String methodName = m.getName();
            if (methodName.startsWith(Strings.SET)) {
                /* Strip off 'set' and decapitalize the remainder according
                to JavaBeans spec. */
                return Introspector.decapitalize(methodName.substring(3));
            }
        }
        return null;
    }

    /**
    Find an injection candidate object that is assignable to the given member.
    This candidate must be completely initialized.  As a special case, if the
    type is assignable from the context object, then the context object will
    be returned.
    @param m
    @return
     */
    private Object findAssignable(Member m) {
        Class requiredType = null;
        if (m instanceof Method) {
            requiredType = ((Method) m).getParameterTypes()[0];
        } else {
            requiredType = ((Field) m).getType();
        }

        /* Don't do injection by type on value types or String. */
        if (isSimpleType(requiredType)) {
            return null;
        }
        if (requiredType.isAssignableFrom(Context.class)) {
            return context;
        }
        for (DeployedObject candidate : initializedObjects.values()) {
            if (requiredType.isAssignableFrom(candidate.getDeployedObject().getClass())) {
                return candidate.getDeployedObject();
            }
        }
        return null;
    }
    private Class[] simpleTypes = {
        boolean.class,
        int.class,
        long.class,
        float.class,
        double.class,
        Boolean.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        java.util.Date.class,
        String.class
    };

    private boolean isSimpleType(Class type) {
        for (Class simpleType : simpleTypes) {
            if (type == simpleType) {
                return true;
            }
        }
        return false;
    }

    /** Inject a resolved value into the deployed object.
    After the injection is complete, remove the member from our list of
    unresolved dependencies for this object.  If there are no further
    unresolved dependencies, call the object's init method and move it into
    the initialized objects group.
    
    @param deployed The holder for the deployment unit.
    @param m The member (either Field or Method that is used to set the value.
    @param val The value to set.
     */
    private void inject(DeployedObject deployed, Member m, Object val) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        log.log(Level.FINER,
                MessageNames.INJECT,
                new Object[]{deployed.getName(), m.getName(), val});
        memberSet(deployed.getDeployedObject(), m, val);
    }

    private void memberSet(Object target, Member member, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (member instanceof Field) {
            Field f = (Field) member;
            boolean originalAccess = f.isAccessible();
            f.setAccessible(true);
            f.set(target, convertProperty(value, f.getType()));
            f.setAccessible(originalAccess);
        } else {
            Method m = (Method) member;
            boolean originalAccess = m.isAccessible();
            m.setAccessible(true);
            m.invoke(target, convertProperty(value, m.getParameterTypes()[0]));
            m.setAccessible(originalAccess);
        }
    }

    private Object convertProperty(Object value, Class requiredType) {
        /* TODO: Make this a little more flexible and configurable! */
        if (requiredType.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (requiredType.equals(Integer.class)
                || requiredType.equals(int.class)) {
            return Integer.parseInt(value.toString());
        }
        throw new LocalizedRuntimeException(MessageNames.BUNDLE_NAME, MessageNames.CANT_CONVERT_EXCEPTION,
                new Object[]{value.toString(), requiredType.toString()});
    }

    private class ShutdownRunner implements DeploymentListener {

        private DeployedObject deployedObject = null;

        ShutdownRunner(DeployedObject o) {
            deployedObject = o;
        }

        @Override
        public void postInit(String name, Object object) {
            // No action.  Handled in upper level.
        }

        @Override
        public void shutDown() {
            for (Method m : deployedObject.getShutdownMethods()) {
                try {
                    m.invoke(deployedObject.getDeployedObject(), new Object[0]);
                } catch (Throwable t) {
                    // TODO: Figure out what to do here!
                    t.printStackTrace();
                }
            }
        }
    }
}
