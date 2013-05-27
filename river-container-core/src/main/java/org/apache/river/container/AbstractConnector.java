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

import java.io.File;
import java.net.URL;

/**
 * Defines an abstract connector for the surrogate container.
 * The connector looks after discovering the device that needs the surrogate,
 * then loads up the surrogate's jar file and installs the surrogate into the
 * container.
 * @author trasukg
 */
public class AbstractConnector {

    Host host=null;

    /**
     * Process the surrogate device's request to host a surrogate, starting
     * from a URL to the surrogate package.
     */
    public void processSurrogateHostingRequest(URL surrogatePackage) {
        // Allocate a working directory
        File workingDir=host.newWorkDirectory();
        
        SurrogateInstaller installer= (SurrogateInstaller)
                host.getAttribute(Names.SURROGATE_INSTALLER);

        installer.installSurrogate(host, workingDir);
    }
}
