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
package org.apache.river.container.admin.impl;

import com.sun.jini.config.Config;
import com.sun.jini.start.LifeCycle;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.discovery.DiscoveryManagement;
import net.jini.export.Exporter;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;
import net.jini.security.ProxyPreparer;
import org.apache.river.container.MessageNames;
import org.apache.river.container.Utils;
import org.apache.river.container.admin.api.Admin;
import org.apache.river.container.admin.api.ApplicationInfo;
import org.apache.river.container.admin.api.ApplicationStatus;
import org.apache.river.container.admin.dl.AdminRemote;

/**
 *
 * @author trasukg
 */
public class AdminImpl implements ServiceIDListener, AdminRemote {

    private static final Logger log
            = Logger.getLogger(AdminImpl.class.getName(), MessageNames.BUNDLE_NAME);

    private static final String COMPONENT_ID = "basic";

    Configuration config = null;
    Admin myProxy = null;
    Exporter exporter = null;
    ProxyPreparer registrarPreparer = null;
    JoinManager joinManager = null;
    DiscoveryManagement discoveryManager = null;
    Entry[] attributes = null;
    ScheduledExecutorService executor=null;
    
    public AdminImpl(String args[], final LifeCycle lc) throws ConfigurationException, ExportException, IOException {

        config = ConfigurationProvider.getInstance(args);
        // Get the exporter and create our proxy.
        exporter = (Exporter) Config.getNonNullEntry(config, COMPONENT_ID, "exporter", Exporter.class);
        Utils.logGrantsToClass(log, Level.FINE, this.getClass());
        try {
            myProxy = (Admin) exporter.export(this);
        } catch (SecurityException se) {
            log.log(Level.SEVERE, "Caught security exception:", se);
        }
        discoveryManager = (DiscoveryManagement) config.getEntry(COMPONENT_ID, "discoveryManager", DiscoveryManagement.class);
        attributes = (Entry[]) config.getEntry(COMPONENT_ID, "attributes", Entry[].class);

        // Publish it using the join manager.
        // We don't have to do anything with it - just creating it starts the join process.
        joinManager = new JoinManager(myProxy, attributes, this, discoveryManager, null, config);
        log.info("Started the admin service");
        
        /* For local clients, we don't want to be dependent on the Jini infrastructure being setup
        correctly.  For this reason, we stash a copy of the proxy's MarshalledObject in the local 
        file system.
        */
        synchronized(this) {
            executor=(ScheduledExecutorService) Config.getNonNullEntry(config, COMPONENT_ID, "$executor", ScheduledExecutorService.class);
        }
        
    }

    ServiceID sid = null;

    public void serviceIDNotify(ServiceID sid) {
        this.sid = sid;
    }

    public ApplicationInfo[] listApplications() throws IOException {
        return new ApplicationInfo[]{
            new ApplicationInfo("abc", ApplicationStatus.UNKNOWN)
        };
    }

}
