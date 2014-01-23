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
package org.apache.river.container.security;

import java.security.Policy;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jini.security.Security;
import net.jini.security.policy.DynamicPolicyProvider;
import org.apache.river.container.ConfigurationException;
import org.apache.river.container.Context;
import org.apache.river.container.Init;
import org.apache.river.container.Injected;
import org.apache.river.container.InjectionStyle;
import org.apache.river.container.MessageNames;
import org.apache.river.container.Utils;

/**
 * This class is the container component that sets up the security manager and
 * dynamic policy provider.
 *
 * @author trasukg
 */
public class SecurityInitializer {

    private static Logger log
            = Logger.getLogger(SecurityInitializer.class.getName(),
                    MessageNames.BUNDLE_NAME);
    @Injected(style = InjectionStyle.BY_TYPE)
    private Context context;

    @Injected
    private ClassLoader containerClassLoader;

    @Injected 
    private ClassLoader bootstrapClassLoader;
    
    @Init
    public void initialize() {
        Security.doPrivileged(new PrivilegedAction() {

            public Object run() {
                log.info("Container classloader is...");
                Utils.logClassLoaderHierarchy(log, Level.INFO, containerClassLoader);
                Policy basePolicy = new ContainerCodePolicy(containerClassLoader, bootstrapClassLoader);
                DynamicPolicyProvider policy = new DynamicPolicyProvider(basePolicy);
                Policy.setPolicy(policy);

                context.put(org.apache.river.container.Strings.SECURITY_POLICY, policy);

                System.setSecurityManager(new SecurityManager());

                Policy installedPolicy = Policy.getPolicy();
                if (installedPolicy != policy) {
                    throw new ConfigurationException(MessageNames.SECURITY_INIT_WRONG_POLICY,
                            installedPolicy);
                }
                return null;
            }
        });

        log.log(Level.INFO, MessageNames.SECURITY_INIT_SUCCEEDED);

    }
}
