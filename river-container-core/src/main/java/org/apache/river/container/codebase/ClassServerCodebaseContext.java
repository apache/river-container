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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.vfs2.FileObject;

/**
 *
 * @author trasukg
 */
public class ClassServerCodebaseContext implements CodebaseContext {
    ClassServer classServer=null;
    
    String appId = null;
    Map<String, FileObject> fileEntries=new HashMap<String, FileObject>();
    
    ClassServerCodebaseContext(ClassServer classServer, String appId) {
        this.appId = appId;
        this.classServer=classServer;
    }

    @Override
    public String getAppId() {
        return appId;
    }

    @Override
    public void addFile(FileObject file) {
        
        /* Add the mapping into our list of objects. */
        String path=file.getName().getBaseName();
        fileEntries.put(path, file);
        /* Force update of the codebase. */
        codebaseAnnotation=null;
    }

    private List<URL> codebaseAnnotation=null;
    
    @Override
    public URL[] getCodebaseAnnotation() {
        try {
            if (codebaseAnnotation==null) {
                /*
                codebase is derived from the list of file objects.
                */
                codebaseAnnotation = new ArrayList<URL>();
                for(String path:fileEntries.keySet()) {
                    codebaseAnnotation.add(new URL(Strings.HTTP_COLON 
                            + Strings.SLASH_SLASH 
                            + classServer.getHost() 
                            + Strings.COLON
                            + classServer.getPort() + 
                            Strings.SLASH
                            + appId + Strings.SLASH + path));
                }
            }
            return codebaseAnnotation.toArray(new URL[0]);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
