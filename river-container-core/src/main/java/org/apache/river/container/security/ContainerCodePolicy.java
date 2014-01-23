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

import java.security.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.river.container.MessageNames;

/**
 * Implements the base policy for the container: Anything loaded by the same
 * classloader (or one of its ancestors) as this policy has AllPermission.
 * Anything loaded by a different classloader has no permissions (and will
 * assumedly be granted appropriate permissions dynamically).
 *
 * @author trasukg
 */
public class ContainerCodePolicy extends Policy {

    private static final Logger log
            = Logger.getLogger(ContainerCodePolicy.class.getName(),
                    MessageNames.BUNDLE_NAME);
    List<ClassLoader> privilegedClassLoaders = new ArrayList<ClassLoader>();

    public ContainerCodePolicy(ClassLoader... classLoaders) {

        for (ClassLoader cl : classLoaders) {
            while (cl != null) {
                privilegedClassLoaders.add(cl);
                cl = cl.getParent();
            }
        }
        allPermissions.add(new AllPermission());
        allPermissions.setReadOnly();
        noPermissions.setReadOnly();
    }
    private PermissionCollection allPermissions = new Permissions();
    private PermissionCollection noPermissions = new Permissions();

    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        if (privilegedClassLoaders.contains(domain.getClassLoader())) {
            return copyPermissions(allPermissions);
        } else {
            log.log(Level.FINE, MessageNames.POLICY_DECLINED,
                    new Object[]{domain.getClassLoader()});
            return copyPermissions(noPermissions);
        }
    }

    /**
     * This seems to be necessary to allow the com.sun.rmi.server.LoaderHandler
     * class to read the marshalled object. LoaderHandler will call this method
     * to get the permissions that are granted to all classes, which in the case
     * of the container, is none. But the permissions collection must be
     * writable.
     *
     * @param codesource
     * @return
     */
    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        return copyPermissions(noPermissions);
    }

    PermissionCollection copyPermissions(PermissionCollection orig) {
        PermissionCollection pc = new Permissions();
        Enumeration perms = orig.elements();
        while (perms.hasMoreElements()) {
            pc.add((Permission) perms.nextElement());
        }
        return pc;
    }
}
