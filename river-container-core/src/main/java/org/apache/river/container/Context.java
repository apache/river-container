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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author trasukg
 */
public class Context {
    Map<String, Object> contents=new HashMap<String, Object>();
    List<ContextListener> listeners=new ArrayList<ContextListener>();

    /**
     Put an object into the context.  Object will be indexed under its
     fully qualified class name.
     @param o
     */
    public void put(Object o) {
        put(o.getClass().getName(), o);
    }

    public void put(String name, Object o) {
        contents.put(name, o);
        if (o instanceof ContextListener) {
            ContextListener l=(ContextListener) o;
            l.setContext(this);
            listeners.add(l);
        }
        /*
         If the added object happens to implement ContextListener, it
         will be notified that it was added to the context.
         */
        for (ContextListener l: new ArrayList<ContextListener>(listeners)) {
            l.put(name, o);
        }
    }

    /** 
    Retrieve an object from the context.
    @param name Name of the object.
    @return 
    */
    public Object get(String name) {
        return contents.get(name);
    }
    
    public <T extends Object> Collection<T> getAll(Class<T> type) {
        List<T> list=new ArrayList<T>();
        for (Object item:contents.values()) {
            if (type.isAssignableFrom(item.getClass())) {
                list.add((T) item);
            }
        }
        return list;
    }
    

    /**
     Called by the bootstrapper to tell us that processing of the initialization
     file is now complete.
     */
    public void initComplete() {
        for (ContextListener l: listeners) {
            l.initComplete();
        }

    }
    
    public void shutDown() {
        for(ContextListener l:listeners) {
            l.shutDown();
        }
    }
}
