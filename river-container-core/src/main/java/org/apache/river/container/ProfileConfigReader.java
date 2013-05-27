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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.vfs2.FileObject;
import org.apache.river.container.config.ContainerConfig;
import org.xml.sax.SAXException;

/**
 *
 * @author trasukg
 */
public class ProfileConfigReader {

    private static final Logger log =
            Logger.getLogger(ProfileConfigReader.class.getName(), MessageNames.BUNDLE_NAME);

    @Injected
    private FileUtility fileUtility = null;
    @Injected(style= InjectionStyle.BY_TYPE)
    private Context context = null;

    @Injected private String profile=null;

    private ContainerConfig readProfileConfig() throws SAXException, JAXBException, FileNotFoundException, IOException {
        Unmarshaller um = Bootstrap.createConfigUnmarshaller();
        FileObject profileDir = fileUtility.getProfileDirectory();
        FileObject configFile = profileDir.resolveFile(Strings.CONFIG_XML);
        log.log(Level.FINE, MessageNames.CONFIG_FILE, configFile.toString());
        InputStream is = configFile.getContent().getInputStream();
        ContainerConfig containerConfig = (ContainerConfig) um.unmarshal(is);
        return containerConfig;
    }

    @Injected
    private ClassLoader containerClassLoader;
    
    @Init
    public void init() {
        try {
            ContainerConfig profileConfig = readProfileConfig();
            /* We use Object not ClassLoader because it might have been loaded
             by a different classloader.
             */
            
            log.log(Level.FINE, MessageNames.PROFILE_CONFIG_LOADING, 
                    new Object[] { containerClassLoader });
            Bootstrap.processConfiguration(profileConfig, containerClassLoader, context);
        } catch (Exception ex) {
            throw new ConfigurationException(ex, MessageNames.PROFILE_CONFIG_EXCEPTION, profile);
        }
    }
}
