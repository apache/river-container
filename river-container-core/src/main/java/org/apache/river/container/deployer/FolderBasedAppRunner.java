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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.river.container.ApplicationManager;
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
import org.apache.river.container.admin.api.ApplicationInfo;
import org.apache.river.container.admin.api.ApplicationStatus;

/**
 *
 * A task that deploys and runs all the applications in a given directory when
 * the container is started up.
 */
public class FolderBasedAppRunner implements ApplicationManager  {

    private static final Logger log
            = Logger.getLogger(FolderBasedAppRunner.class.getName(), MessageNames.BUNDLE_NAME);

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

    private Map<String, DeploymentRecord> deployedServices = new HashMap<String, DeploymentRecord>();

    private class DeploymentRecord {

        String name;
        long updateTime;
        FileObject fileObject;
        ServiceLifeCycle serviceLifeCycle;
    }

    private boolean autoDeploy = false;

    public boolean isAutoDeploy() {
        return autoDeploy;
    }

    public void setAutoDeploy(boolean autoDeploy) {
        this.autoDeploy = autoDeploy;
    }

    public int getScanInterval() {
        return scanInterval;
    }

    public void setScanInterval(int scanInterval) {
        this.scanInterval = scanInterval;
    }

    private int scanInterval = 5;

    public String getDeployDirectory() {
        return deployDirectory;
    }

    public void setDeployDirectory(String deployDirectory) {
        this.deployDirectory = deployDirectory;
    }

    FileObject deploymentDirectoryFile = null;

    private String deployerName=null;

    public String getDeployerName() {
        return deployerName;
    }

    public void setDeployerName(String deployerName) {
        this.deployerName = deployerName;
    }
    
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
        If the deployerName is supplied, look it up and override the injected deployer.
        */
        if(deployerName != null) {
            deployer=(StarterServiceDeployer) context.get(deployerName);
        }
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
         Do the scan task once - this will launch all the services currently in 
         deploy dir.
         */
        new ScanTask().runOnce();

        if (autoDeploy) {
            /* Now schedule a scan in the required scan time. */
            deployer.workManager.schedule(null, new ScanTask(), getScanInterval(), TimeUnit.SECONDS);
        }
    }

    private void registerApplication(ServiceLifeCycle deployedApp) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(org.apache.river.container.Strings.NAME, deployedApp.getName());
        ObjectName oName = new ObjectName(org.apache.river.container.Strings.CONTAINER_JMX_DOMAIN, props);
        mbeanRegistrar.getMbeanServer().registerMBean(
                deployedApp, oName);
    }

    private void unregisterApplication(ServiceLifeCycle deployedApp) {
        try {
            Hashtable<String, String> props = new Hashtable<String, String>();
            props.put(org.apache.river.container.Strings.NAME, deployedApp.getName());
            ObjectName oName = new ObjectName(org.apache.river.container.Strings.CONTAINER_JMX_DOMAIN, props);
            mbeanRegistrar.getMbeanServer().unregisterMBean(oName);
        } catch (Exception e) {
            log.log(Level.SEVERE, MessageNames.FAILED_TO_REMOVE_MBEAN,
                    new Object[]{deployedApp.getName()});
        }
    }

    private Map<String, DeploymentRecord> scanDeploymentArchives() throws FileSystemException {
        /*
         Go through the deployment directory looking for services to deploy.
         */
        Map<String, DeploymentRecord> deployDirListing = new HashMap<String, DeploymentRecord>();
        deploymentDirectoryFile.refresh();
        List<FileObject> serviceArchives
                = Utils.findChildrenWithSuffix(deploymentDirectoryFile,
                        org.apache.river.container.Strings.JAR);
        if (serviceArchives != null) {
            log.log(Level.FINER, MessageNames.FOUND_SERVICE_ARCHIVES,
                    new Object[]{serviceArchives.size(), deployDirectory});
            for (FileObject serviceArchive : serviceArchives) {
                DeploymentRecord rec = new DeploymentRecord();
                rec.fileObject = serviceArchive;
                rec.name = serviceArchive.getName().getBaseName();
                rec.updateTime = serviceArchive.getContent().getLastModifiedTime();
                deployDirListing.put(rec.name, rec);
            }
        }
        return deployDirListing;
    }

    private class ScanTask implements Runnable {

        public void runOnce() {
            try {
                log.log(Level.FINER, MessageNames.SCANNING_DEPLOYMENT_DIRECTORY,
                        new Object[]{deployDirectory});
                Map<String, DeploymentRecord> deployDirListing = scanDeploymentArchives();
                // DeployDirListing will become the deployedServices collection
                synchDeployedServices(deployedServices, deployDirListing);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        public void run() {
            runOnce();
            deployer.workManager.schedule(null, this, scanInterval, TimeUnit.SECONDS);
        }
    }

    private synchronized void synchDeployedServices(Map<String, DeploymentRecord> currentList,
            Map<String, DeploymentRecord> newList) {
        // For each entry
        for (DeploymentRecord rec : newList.values()) {
            // If it isn't already in deployedServices, start it
            DeploymentRecord current = currentList.get(rec.name);
            if (current == null) {
                log.log(Level.FINE, MessageNames.STARTING_SERVICE,
                        new Object[]{rec.name});
                currentList.put(rec.name, rec);
                deployAndStart(rec);
            } else if (current.updateTime != rec.updateTime) {
                // If it's in deployedServices but now newer, stop and restart
                log.log(Level.FINE, MessageNames.UPDATING_SERVICE,
                        new Object[]{rec.name});
                currentList.remove(current.name);
                stopAndRemove(current);
                currentList.put(rec.name, rec);
                deployAndStart(rec);
            }
        }
        // If there are any services left in deployedServices, stop them
        List<DeploymentRecord> removals = new ArrayList<DeploymentRecord>();
        for (DeploymentRecord current : currentList.values()) {
            if (!newList.containsKey(current.name)) {
                removals.add(current);
            }
        }
        for (DeploymentRecord current : removals) {
            log.log(Level.FINE, MessageNames.STOPPING_SERVICE,
                    new Object[]{current.name});
            currentList.remove(current.name);
            stopAndRemove(current);
        }

    }

    private void deployAndStart(DeploymentRecord dr) {
        try {
            /* Try the archive in all the deployers to see if someone can 
             * handle it. For now there's only one.
             */

            /*
             * Create the ApplicationEnvironment for the archive.
             */
            dr.serviceLifeCycle = deployer.deployServiceArchive(myName, dr.fileObject);
            // Register it as an MBean
            registerApplication(dr.serviceLifeCycle);
            dr.serviceLifeCycle.start();
        } catch (Throwable t) {
            log.log(Level.WARNING, MessageNames.FAILED_DEPLOY_SERVICE, dr.name);
            log.log(Level.WARNING, MessageNames.EXCEPTION_THROWN, Utils.stackTrace(t));
        }

    }

    private void stopAndRemove(DeploymentRecord dr) {
        dr.serviceLifeCycle.stop();
        unregisterApplication(dr.serviceLifeCycle);
    }

    public synchronized List<ApplicationInfo> getApplicationInfo() {
        List<ApplicationInfo> info=new ArrayList<ApplicationInfo>(deployedServices.size());
        for (DeploymentRecord rec: deployedServices.values()) {
            ApplicationInfo item=new ApplicationInfo(myName, rec.name, toApplicationStatus(rec.serviceLifeCycle));
            info.add(item);
        }
        return info;
    }
    
    ApplicationStatus toApplicationStatus(ServiceLifeCycle lc) {
        String status=lc.getStatus();
        if (Strings.RUNNING.equals(status)) {
            return ApplicationStatus.RUNNING;
        }
        if (Strings.IDLE.equals(status)) {
            return ApplicationStatus.STOPPED;
        }
        if (Strings.FAILED.equals(status)) {
            return ApplicationStatus.FAILED;
        }
        return ApplicationStatus.UNKNOWN;
    }
}
