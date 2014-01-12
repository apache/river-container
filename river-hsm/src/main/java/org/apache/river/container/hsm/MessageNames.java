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

/**
 * Message names for the state machine implementation's logging.
 */
public class MessageNames {

    public static final String BUNDLE_NAME = "org.apache.river.container.hsm.Messages";
    public static final String APPLYING_EVENT_TO_STATE = "applyingEventToState",
            BEGINNING_COMPILE="beginningCompile",
            CANT_RESOLVE_TRANSITIONS_FOR_CLASS = "cantResolveTransitionsForClass",
            COMPILE_COMPLETED="compileCompleted",
            ENTRY_METHOD_ISNT_VOID = "entryMethodIsntVoid",
            ERROR_APPLYING_TRANSITION="errorApplyingTransition",
            ERROR_CREATING_PROXY="errorCreatingProxy",
            ERROR_INCOMPATIBLE_OUTPUT="errorIncompatibleOutput",
            ERROR_INSTANTIATING="errorInstantiating",
            ERROR_INVOKING_TARGET_METHOD = "errorInvokingTargetMethod",
            EXIT_METHOD_ISNT_VOID = "exitMethodIsntVoid",
            GUARD_METHOD_DOESNT_RETURN_BOOLEAN = "guardMethodDoesntReturnBoolean",
            METASTATE_WAS_INSTANTIATED="metaStateWasInstantiated",
            MULTIPLE_EXCEPTIONS_THROWN="multipleExceptionsThrown",
            NO_PARENT_INSTANCE="noParentInstance",
            QUEUED_TRANSITION="queuedTransition",
            RUNNING_GUARD_ON_STATE = "runningGuardOnState",
            RUNNING_GUARD_TRANSITIONS="runningGuardTransitions",
            SETTING_INITIAL_SUBSTATE="settingInitialSubstate",
            SETTING_FIELD_TO="settingFieldTo",
            STORING_EXCEPTION="storingException";
}
