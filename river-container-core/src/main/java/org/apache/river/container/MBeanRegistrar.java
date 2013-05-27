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

import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 *
 * @author trasukg
 */
public class MBeanRegistrar implements DeploymentListener {

    private MBeanServer mbeanServer=null;

    public MBeanServer getMbeanServer() {
        return mbeanServer;
    }

    public void postInit(String name, Object object) {
        try {
            /*
             Just try to register it.  If it fails, that's OK.
             */
            Hashtable<String,String> props=new Hashtable<String, String>();
            props.put(Strings.NAME, name);
            ObjectName objectName=new ObjectName(Strings.CONTAINER_JMX_DOMAIN, props);
            mbeanServer.registerMBean(object, objectName);
        } catch(Exception ex) {
            // Don't really care.
        }
    }

    @Init
    public void init() {
        mbeanServer=ManagementFactory.getPlatformMBeanServer();
    }

    @Override
    public void shutDown() {
        
    }
}
