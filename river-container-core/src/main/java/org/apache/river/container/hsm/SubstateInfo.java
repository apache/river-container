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

/**
 *
 * Information on substates within a MetaState.
 */
class SubstateInfo {

    private Field field;
    private Object objectThatHoldsField;

    public Object getObjectThatHoldsField() {
        return objectThatHoldsField;
    }

    public void setObjectThatHoldsField(Object objectThatHoldsField) {
        this.objectThatHoldsField = objectThatHoldsField;
    }
    
    /** this field will disappear when the StateMachineCompiler is complete. */
    private Class[] possibleStates;
    
    private MetaState[] possibleMetaStates;

    public MetaState[] getPossibleMetaStates() {
        return possibleMetaStates;
    }

    public void setPossibleMetaStates(MetaState[] possibleMetaStates) {
        this.possibleMetaStates = possibleMetaStates;
    }

    public MetaState getInitialMetaState() {
        return initialMetaState;
    }

    public void setInitialMetaState(MetaState initialMetaState) {
        this.initialMetaState = initialMetaState;
    }
    
    /** this field will disappear when the StateMachineCompiler is complete. */
    private Class initialState;
    
    private MetaState initialMetaState;
    
    private boolean retained=false;
    private MetaState activeMetaState;

    public MetaState getActiveMetaState() {
        return activeMetaState;
    }

    public void setActiveMetaState(MetaState activeMetaState) {
        this.activeMetaState = activeMetaState;
    }
    
    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Class getInitialState() {
        return initialState;
    }

    public void setInitialState(Class initialState) {
        this.initialState = initialState;
    }

    public Class[] getPossibleStates() {
        return possibleStates;
    }

    public void setPossibleStates(Class[] possibleStates) {
        this.possibleStates = possibleStates;
    }

    public boolean isRetained() {
        return retained;
    }

    public void setRetained(boolean retained) {
        this.retained = retained;
    }

}
