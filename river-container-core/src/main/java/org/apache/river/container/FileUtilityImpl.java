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
import java.io.IOException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

/**
 *
 * @author trasukg
 */
public class FileUtilityImpl implements FileUtility {

    @Injected
    private String profile=null;

    private FileSystemManager fsm=null;

    public FileObject getWorkingDirectory(String name) throws IOException {

        FileObject workDir=getProfileDirectory().resolveFile(Strings.WORK).resolveFile(name);
        if (!workDir.exists()) {
            workDir.createFolder();
        }
        return workDir;
    }

    public FileObject getProfileDirectory() throws IOException {
        FileObject profileDir = fsm.resolveFile(new File(Strings.PROFILE), profile);
        return profileDir;
    }

    @Init
    public void init() throws FileSystemException {
        fsm=VFS.getManager();
    }

    @Override
    public FileObject getLibDirectory() throws IOException {
        FileObject libDir = fsm.resolveFile(new File(Strings.LIB), ".");
        return libDir;
    }
}
