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

/**
 *
 * @author trasukg
 */
public class SurrogateInstaller {

    /**
    Create and install a surrogate based on a surrogate file which has
    been unpacked into a working directory.
    
    <p>This seems to be a lot like installing a generic application, apart
    from the specific ways of determining the surrogate's class, so
    one wonders whether we might eventually make this a plain application
    loader, and separate out the surrogate-specific items into the
    connector.
    </p>
    <p>More formally then, the surrogate connector could read a surrogate
    file, then create a plain application that represents the surrogate.
    </p>
    <p>In that case, the surrogate application is simply a plain application
    that has some extra limitations placed on it (e.g. no access to
    local resources, a more restrictive security policy, etc).
    </p>
    
    @param workingDir
     */
    void installSurrogate(Host host, File workingDir) {
        // Create a context for the surrogate.
        ApplicationEnvironment appEnv = host.newApplicationEnvironment();

        try {
            /* Configure the application environment. */
            configureApplicationEnvironment(appEnv, workingDir);

            /* Startup the application environment. */
            // Initialize the class loader with the surrogate's classes
            /* TODO: Set the parent classloader to what? 
            SettableCodebaseClassLoader classLoader =
            SettableCodebaseClassLoader.createLoader(null, workingDir);
            appEnv.setClassLoader(classLoader);
             */
            // Instantiate the surrogate.
            // Try the surrogate's getCodebase method to find the codebase
            /*
            If codebase method returns nothing,
            read the manifest to get the
            codebase entries.
             */
            /*
             * Setup the discovery manager for the surrogate.
             */
            /*
             * Publish the surrogate's codebase jars through hosts's codebase server.
             */
            /* Initialize the surrogate.
             */
            /*
             * In case of failure, unpublish the codebase and clean up.
             */
            /*
             * Initiate liveness callbacks.
             */
        } catch (Exception e) {
            /* TODO: Handle this properly! */
            e.printStackTrace();
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void configureApplicationEnvironment(ApplicationEnvironment appEnv, File workingDir) {
        /* Read the manifest to get the surrogate's class. */
    }
}
