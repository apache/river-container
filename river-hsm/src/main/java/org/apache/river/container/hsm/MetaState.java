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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An object that corresponds to a state instance.
 */
public class MetaState {
    
    Object stateInstance=null;
    Class stateClass=null;
    MetaState parent=null;
    
    public List<MetaState> getActiveMetaStates() {
        List<MetaState> activeStates=new ArrayList<MetaState>();
        getActiveMetaStates(activeStates);
        return activeStates;
    }
    
    public void getActiveMetaStates(List<MetaState> activeStates) {
        /* Add my stateInstance's class. */
        activeStates.add(this);
        /* Iterate down for each substate. */
        for(SubstateInfo substate: substates) {
            substate.getActiveMetaState().getActiveMetaStates(activeStates);
        }
    }
    
    public List<Class> getActiveStates() {
        List<MetaState> activeStates=getActiveMetaStates();
        List<Class> activeStateClasses=new ArrayList(activeStates.size());
        for(MetaState ms: activeStates) {
            activeStateClasses.add(ms.stateClass);
        }
        return activeStateClasses;
    }
    List<SubstateInfo> substates=new ArrayList<SubstateInfo>();
    
    Map<Method, Operation> eventMethods=new HashMap<Method, Operation>();
    
    public void visitAll(MetaStateVisitor visitor) {
        visitor.visit(this);
        for (SubstateInfo substateInfo: substates) {
            for (MetaState ms: substateInfo.getPossibleMetaStates()) {
                ms.visitAll(visitor);
            }
        }
    }
    
    List<Operation> guardMethods=new ArrayList<Operation>();

    List<Operation> entryMethods=new ArrayList<Operation>();
    
    List<Operation> exitMethods=new ArrayList<Operation>();
    
    List<TransitionOnSubstate> entryTransitions=new ArrayList<TransitionOnSubstate>();
    
    public String toString() {
        return stateClass==null?"Uninitialized metastate":"Meta-" + stateClass.getName();
    }
    
    /**
     * Return a string representation of the possible state structure.  Mainly for unit-testing.
     * @return 
     */
    public String getStateStructure() {
        StringBuilder sb=new StringBuilder();
        sb.append(stateClass.getSimpleName());
        if (substates.size() != 0) {
            sb.append("(");
            for (SubstateInfo si:substates) {
                sb.append(si.getField().getName());
                sb.append("(");
                for (MetaState ms: si.getPossibleMetaStates()) {
                    sb.append(ms.getStateStructure());
                    sb.append(" ");
                }
                sb.append(") ");
            }
            sb.append(") ");
        }
        return sb.toString();
    }
}
