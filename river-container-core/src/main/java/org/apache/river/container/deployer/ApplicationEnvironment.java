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

import java.io.File;
import org.apache.commons.vfs2.FileObject;
import org.apache.river.container.classloading.VirtualFileSystemClassLoader;
import org.apache.river.container.codebase.CodebaseContext;
import org.apache.river.container.work.WorkingContext;

/**
 * Everything the host needs to know about the surrogate.
 * @author trasukg
 */
public class ApplicationEnvironment {
    VirtualFileSystemClassLoader classLoader=null;

    String applicationManagerName=null;

    public String getApplicationManagerName() {
        return applicationManagerName;
    }

    public void setApplicationManagerName(String applicationManagerName) {
        this.applicationManagerName = applicationManagerName;
    }
    
    String serviceName=null;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    FileObject serviceArchive=null;
    FileObject serviceRoot=null;
    CodebaseContext codebaseContext=null;

    public VirtualFileSystemClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(VirtualFileSystemClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public FileObject getServiceArchive() {
        return serviceArchive;
    }

    public void setServiceArchive(FileObject serviceArchive) {
        this.serviceArchive = serviceArchive;
    }

    public FileObject getServiceRoot() {
        return serviceRoot;
    }

    public void setServiceRoot(FileObject serviceRoot) {
        this.serviceRoot = serviceRoot;
    }

    public CodebaseContext getCodebaseContext() {
        return codebaseContext;
    }

    public void setCodebaseContext(CodebaseContext codebaseContext) {
        this.codebaseContext = codebaseContext;
    }
    
    Object serviceInstance = null;

    public Object getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(Object serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
    
    WorkingContext workingContext=null;

    public WorkingContext getWorkingContext() {
        return workingContext;
    }

    public void setWorkingContext(WorkingContext workingContext) {
        this.workingContext = workingContext;
    }
    
    File workingDirectory=null;

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    
    
}
