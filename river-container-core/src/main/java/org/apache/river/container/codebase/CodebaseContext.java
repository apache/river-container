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
package org.apache.river.container.codebase;

import java.net.URL;
import org.apache.commons.vfs2.FileObject;

/**
    Context that interfaces with the codebase handling system to make 
    codebase files available for remote download, and supply the codebase 
    annotation that should be used by the classloader.
 * @author trasukg
 */
public interface CodebaseContext {
    
    public String getAppId();
    
    /**
    Add the given file into the exported set.
    @param file 
    */
    public void addFile(FileObject file);
    
    /**
    Get a string that represents the codebase annotation that should be returned
    by the classloader to correspond to this set.
    @return 
    */
    public URL[] getCodebaseAnnotation();
    
}
