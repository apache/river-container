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
package org.apache.river.container.deployer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.river.container.ConfigurationException;
import org.apache.river.container.Context;
import org.apache.river.container.FileUtility;
import org.apache.river.container.Init;
import org.apache.river.container.Injected;
import org.apache.river.container.InjectionStyle;
import org.apache.river.container.MBeanRegistrar;
import org.apache.river.container.MessageNames;
import org.apache.river.container.Name;
import org.apache.river.container.Utils;

/**
 *
 * A Deployer task that deploys all the applications in a given directory when
 * the container is started up.
 */
public class StartupDeployer {

    private static final Logger log =
            Logger.getLogger(StartupDeployer.class.getName(), MessageNames.BUNDLE_NAME);
    
    private String deployDirectory = org.apache.river.container.Strings.DEFAULT_DEPLOY_DIRECTORY;
    
    @Injected(style = InjectionStyle.BY_TYPE)
    private FileUtility fileUtility = null;
    
    @Injected(style = InjectionStyle.BY_TYPE)
    private Context context;
    
    @Injected(style = InjectionStyle.BY_TYPE)
    private StarterServiceDeployer deployer;
    
    @Injected(style = InjectionStyle.BY_TYPE)
    private MBeanRegistrar mbeanRegistrar;
    
    @Name
    private String myName = null;

    private List<ApplicationEnvironment> applicationEnvironments =
            new ArrayList<ApplicationEnvironment>();

    public String getDeployDirectory() {
        return deployDirectory;
    }

    public void setDeployDirectory(String deployDirectory) {
        this.deployDirectory = deployDirectory;
    }

    FileObject deploymentDirectoryFile=null;
    
    @Init
    public void init() {
        try {
            tryInitialize();
        } catch (Throwable ex) {
            log.log(Level.SEVERE, MessageNames.STARTUP_DEPLOYER_FAILED_INIT,
                    ex);
            throw new ConfigurationException(ex,
                    MessageNames.STARTUP_DEPLOYER_FAILED_INIT);
        }
    }

    private void tryInitialize() throws IOException, ParseException {
        log.log(Level.FINE, MessageNames.STARTER_SERVICE_DEPLOYER_STARTING, myName);
        /*
         Establish the deployment directory.
         */
        deploymentDirectoryFile = fileUtility.getProfileDirectory().resolveFile(deployDirectory);
        if (deploymentDirectoryFile == null
                || deploymentDirectoryFile.getType() != FileType.FOLDER) {
            log.log(Level.WARNING, MessageNames.NO_DEPLOYMENT_DIRECTORY,
                    new Object[]{deployDirectory, fileUtility.getProfileDirectory()});
        }
        /*
         Go through the deployment directory looking for services to deploy.
         */
        List<FileObject> serviceArchives =
                Utils.findChildrenWithSuffix(deploymentDirectoryFile,
                org.apache.river.container.Strings.SSAR);
        if (serviceArchives != null) {
            log.log(Level.FINE, MessageNames.FOUND_SERVICE_ARCHIVES,
                    new Object[]{serviceArchives.size(), deployDirectory});
            deployServiceArchives(serviceArchives);
        } else {
            log.log(Level.WARNING, MessageNames.FOUND_NO_SERVICE_ARCHIVES,
                    new Object[]{deployDirectory});

        }

    }

    private void deployServiceArchives(List<FileObject> serviceArchives) {
        /*
         Deploy those services.
         */
        for (FileObject archiveFile : serviceArchives) {
            try {
                /* Try the archive in all the deployers to see if someone can 
                 * handle it. For now there's only one.
                 */
                
                /*
                 * Create the ApplicationEnvironment for the archive.
                 */
                ServiceLifeCycle deployedApp=deployer.deployServiceArchive(archiveFile);
                // Register it as an MBean
                registerApplication(deployedApp);
                deployedApp.start();
            } catch (Throwable t) {
                log.log(Level.WARNING, MessageNames.FAILED_DEPLOY_SERVICE, archiveFile.toString());
                log.log(Level.WARNING, MessageNames.EXCEPTION_THROWN, Utils.stackTrace(t));
            }
        }
    }
    
    private void registerApplication(ServiceLifeCycle deployedApp) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        Hashtable<String, String> props=new Hashtable<String,String>();
        props.put(org.apache.river.container.Strings.NAME, deployedApp.getName());
        ObjectName oName=new ObjectName(org.apache.river.container.Strings.CONTAINER_JMX_DOMAIN, props);
        mbeanRegistrar.getMbeanServer().registerMBean(
                deployedApp, oName);
    }
}
