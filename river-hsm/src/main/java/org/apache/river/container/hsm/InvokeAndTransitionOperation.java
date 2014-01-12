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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * State machine operation that invokes a method on the target instance
 * and then queues a transition (or set of transitions).
 */
class InvokeAndTransitionOperation implements Operation {

    private static final Logger log=
            Logger.getLogger(InvokeAndTransitionOperation.class.getName(),MessageNames.BUNDLE_NAME);
    
    
    Method targetMethod;

    /**
     * Create an instance that executes an event method that returns a value, but
     * does not include any transitions.
     * @param targetMetaState
     * @param targetMethod 
     */
    public InvokeAndTransitionOperation(MetaState targetMetaState, Method targetMethod) {
        this(targetMetaState, targetMethod, new TransitionOnSubstate[0]);
    }
    
    public InvokeAndTransitionOperation(MetaState targetMetaState, Method targetMethod,  TransitionOnSubstate[] transitions) {
        this.targetMethod = targetMethod;
        this.targetMetaState = targetMetaState;
        this.transitions = transitions;
    }
    MetaState targetMetaState;
    TransitionOnSubstate[] transitions;
    
    @Override
    public void eval(StateMachineExecutor exec, Object[] args) {
        try {
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, MessageNames.APPLYING_EVENT_TO_STATE,
                        new Object[]{targetMethod.getName(),
                            targetMetaState.stateClass.getSimpleName(),
                            targetMetaState.stateInstance});
            }
            exec.output(targetMethod.invoke(targetMetaState.stateInstance, args));
        } catch(InvocationTargetException e) {
            exec.exception(targetMetaState, targetMethod, e.getCause());
        } catch (Exception ex) {
            log.log(Level.SEVERE, MessageNames.APPLYING_EVENT_TO_STATE,
                    new Object[]{targetMethod.getName(),
                            targetMetaState.stateClass.getSimpleName(),
                            targetMetaState.stateInstance});
            log.log(Level.SEVERE, MessageNames.ERROR_INVOKING_TARGET_METHOD, ex);
        }
        // Execute the transitions
        for (TransitionOnSubstate t:transitions) {
            exec.queueTransition(t);
        }
    }
    
}
