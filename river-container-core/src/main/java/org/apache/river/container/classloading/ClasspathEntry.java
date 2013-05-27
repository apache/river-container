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

package org.apache.river.container.classloading;

import java.util.logging.Logger;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.river.container.MessageNames;

/**
 * A ClassPathEntry is used by the VirtualFileSystemClassLoader, and is a
 combination of a ClasspathFilter and the fileObject that points to the entry's
 jar file.  It effectively represents an entry like 'container.jar(org.apache.ABC)',
 which would mean 'the class org.apache.ABC contained inside the jar file
 container.jar'.  The idea is to include selected packages from a jar file on the
 classpath,
 * @author trasukg
 */
public class ClasspathEntry {
    private ClasspathFilter classpathFilter=null;

    private FileObject fileObject=null;

    public ClasspathEntry(ClasspathFilter filter, FileObject fileObject) {
        this.fileObject=fileObject;
        this.classpathFilter=filter;
    }

    public FileObject resolveFile(String name) throws FileSystemException {
        if ((classpathFilter.acceptsResource(name))) {
            return fileObject.resolveFile(name);
        }
        return null;
    }
    @Override
    public String toString() {
        return fileObject.toString() + classpathFilter.toString();
    }
        
}
