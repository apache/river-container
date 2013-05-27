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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 Holds information about an object that is being managed by the
 AnnotatedClassDeployer.
 * @author trasukg
 */
public class DeployedObject {
    private Object deployedObject=null;
    private String name;
    private List<Member> unresolvedDependencies=new ArrayList<Member>();
    private List<Method> initMethods=new ArrayList<Method>();
    private List<Method> shutdownMethods=new ArrayList<Method>();

    public List<Method> getShutdownMethods() {
        return shutdownMethods;
    }

    private boolean initialized=false;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean isInitialized) {
        this.initialized = isInitialized;
    }
    
    public List<Method> getInitMethods() {
        return initMethods;
    }

    public Object getDeployedObject() {
        return deployedObject;
    }

    public void setDeployedObject(Object deployedObject) {
        this.deployedObject = deployedObject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Member> getUnresolvedDependencies() {
        return unresolvedDependencies;
    }

}
