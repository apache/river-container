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
 * Holds a transition on a substate.
 */
class TransitionOnSubstate {
    
    // package access, so the executor can use it directly.
    SubstateInfo substate;

    /**
     * Create an instance specifying the target metastate for the given
     * substate.
     * 
     * @param substate
     * @param targetMetaState 
     */
    public TransitionOnSubstate(SubstateInfo substate, MetaState targetMetaState) {
        this.substate = substate;
        this.targetMetaState = targetMetaState;
    }

    public SubstateInfo getSubstate() {
        return substate;
    }


    public MetaState getTargetMetaState() {
        return targetMetaState;
    }
    
    // package access, so the executor can use it directly.
    MetaState targetMetaState;
    
    
}
