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

import java.security.GuardedObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author trasukg
 */
public class AttributeStoreImpl implements AttributeStore {

    private Map<String, Object> attributes=new HashMap<String, Object>();

    /**
     Retrieve an attribute, checking security permissions if it happens
     to be a guarded object.
     @param name
     @return
     */
    public Object getAttribute(String name) {
        Object attr=attributes.get(name);
        if (attr instanceof GuardedObject) {
            return ((GuardedObject) attr).getObject();
        }
        return attr;
    }

    public void setAttribute(String name, Object attribute) {
        attributes.put(name, attribute);
    }

}
