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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
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
 * A runner task that looks at the command line to determine the name of an
 * application to run from a deployment folder.  Generally used to run "client"
 * apps where the name of the app is supplied on the command line, for instance
 * the browser app or the admin client app.
 */
public class CommandLineAppRunner {

    private static final Logger log
            = Logger.getLogger(CommandLineAppRunner.class.getName(), MessageNames.BUNDLE_NAME);

    @Injected
    ResourceBundle messages;

    @Injected
    public String[] commandLineArguments = null;

    @Injected(style = InjectionStyle.BY_TYPE)
    Context context = null;

    private String deployDirectory = org.apache.river.container.Strings.DEFAULT_DEPLOY_DIRECTORY;

    @Injected(style = InjectionStyle.BY_TYPE)
    private FileUtility fileUtility = null;

    @Injected(style = InjectionStyle.BY_TYPE)
    private StarterServiceDeployer deployer;

    @Injected(style = InjectionStyle.BY_TYPE)
    private MBeanRegistrar mbeanRegistrar;

    @Name
    private String myName = null;

    private List<ApplicationEnvironment> applicationEnvironments
            = new ArrayList<ApplicationEnvironment>();

    public String getDeployDirectory() {
        return deployDirectory;
    }

    public void setDeployDirectory(String deployDirectory) {
        this.deployDirectory = deployDirectory;
    }

    FileObject deploymentDirectoryFile = null;

    private String clientAppName = null;

    public String getClientAppName() {
        return clientAppName;
    }

    /**
     * Set the client app that should be loaded. If not provided, client app
     * name is taken from the first parameter.
     *
     * @param clientApp
     */
    public void setClientAppName(String clientApp) {
        this.clientAppName = clientApp;
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
         Establish the deployment directory.
         */
        deploymentDirectoryFile = fileUtility.getProfileDirectory().resolveFile(deployDirectory);
        if (deploymentDirectoryFile == null
                || deploymentDirectoryFile.getType() != FileType.FOLDER) {
            log.log(Level.WARNING, MessageNames.NO_DEPLOYMENT_DIRECTORY,
                    new Object[]{deployDirectory, fileUtility.getProfileDirectory()});
        }
        /*
         * Find the name of the client we need to deploy.  
         */
        /* First argument was the profile name.  Second argument is the name of 
         * the client app to run.  All the rest are parameters to the client
         * app.
         */
        if (clientAppName == null && commandLineArguments.length < 2) {
            System.out.println(messages.getString(MessageNames.CLIENT_APP_USAGE));
            System.exit(1);
        }
        String[] clientAppArgs;
        if (clientAppName == null) {
            clientAppName = commandLineArguments[1];
            clientAppArgs = new String[commandLineArguments.length - 2];
            System.arraycopy(commandLineArguments, 2, clientAppArgs, 0,
                    clientAppArgs.length);
        } else {
            clientAppArgs = new String[commandLineArguments.length - 1];
            System.arraycopy(commandLineArguments, 1, clientAppArgs, 0,
                    clientAppArgs.length);
        }
        // Locate the service archive that has the client's name.
        // First get all the jar files.
        List<FileObject> serviceArchives
                = Utils.findChildrenWithSuffix(deploymentDirectoryFile,
                        org.apache.river.container.Strings.JAR);
        //Then find the one that starts with the client name
        FileObject serviceArchive = null;
        for (FileObject fo : serviceArchives) {
            if (fo.getName().getBaseName().startsWith(clientAppName + org.apache.river.container.Strings.DASH)) {
                serviceArchive = fo;
                break;
            }

        }

        if (serviceArchive == null) {
            System.err.println(MessageFormat.format(messages.getString(MessageNames.NO_SUCH_CLIENT_APP), clientAppName));
            System.exit(1);
        }
        // Deploy the service
        deployServiceArchive(serviceArchive, clientAppArgs);
        // Run the main method with the remaining command line parameters.
    }

    private void deployServiceArchive(FileObject archiveFile, String[] commandLineArgs) {
        try {
            /* Try the archive in all the deployers to see if someone can 
             * handle it. For now there's only one.
             */

            /*
             * Create the ApplicationEnvironment for the archive.
             */
            ServiceLifeCycle deployedApp = deployer.deployServiceArchive(myName, archiveFile);

            deployedApp.startWithArgs(commandLineArgs);
        } catch (Throwable t) {
            log.log(Level.WARNING, MessageNames.FAILED_DEPLOY_SERVICE, archiveFile.toString());
            log.log(Level.WARNING, MessageNames.EXCEPTION_THROWN, Utils.stackTrace(t));
        }
    }
}
