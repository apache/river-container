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

import org.apache.river.container.deployer.ApplicationEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is from the early work on the surrogate host and was meant to be
 the overall container.  At this point, it probably should be refactored or
 removed and replaced with something that supports the Context-based deployers.

 * @author trasukg
 */
public class Host {

    List<ApplicationEnvironment> applications = new ArrayList();

    AttributeStore attributeStore=new AttributeStoreImpl();

    public AttributeStore getAttributeStore() {
        return attributeStore;
    }

    public List<ApplicationEnvironment> getApplications() {
        return applications;
    }

    File newWorkDirectory() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     Create a new application environment and add it to the applications
     list in the Host.
     @return The newly-created ApplicationEnvironment.
     */
    public ApplicationEnvironment newApplicationEnvironment() {
        ApplicationEnvironment appEnv=new ApplicationEnvironment();
        applications.add(appEnv);
        return appEnv;
    }

    /**
     Looks up an attribute in the attribute set.
     @param name
     @return
     */
    public Object getAttribute(String name) {
        return getAttributeStore().getAttribute(name);
    }

}
