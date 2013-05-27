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

/**
 *
 * @author trasukg
 */
public interface ContextListener {

    /**
     Indicates that an object has been added to the context.
     @param name
     @param o
     */
    public void put(String name, Object o);

    /**
     Indicates that an object has been removed from the context.
     @param o
     */
    public void remove(Object o);

    /**
     Informs the listener of the context object.
     @param ctx
     */
    public void setContext(Context ctx);

    /**
     Indicates that processing of any initialization file is complete.
     */
    public void initComplete();
    
    /**
    Indicates that the container is shutting down, so all resources should
    be closed and/or released.
    */
    public void shutDown();
}
