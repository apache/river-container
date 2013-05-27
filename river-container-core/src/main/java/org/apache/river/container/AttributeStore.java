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
 Implements an attribute store with some features of an IOC container, plus
 some security visibility features.
 * @author trasukg
 */
public interface AttributeStore {
    /**
     Return the attribute listed under the desired name, assuming that
     the current code has permission to access that attribute.
     
     @param name
     @return The attribute.
     */
    public Object getAttribute(String name);

    /**
     Store an attribute in the attribute store.

     @param name
     @param attribute
     */
    public void setAttribute(String name, Object attribute);
}
