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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.vfs2.FileObject;

/**
 *
 * @author trasukg
 */
public class DummyCodebaseHandler implements CodebaseHandler {

    @Override
    public CodebaseContext createContext(String appId) {
        return new DummyContext(appId);
    }

    @Override
    public void destroyContext(CodebaseContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private class DummyContext implements CodebaseContext {
        private String appId;
        
        private DummyContext(String appId) {
            this.appId=appId;
        }
        
        List<URL> urls=new ArrayList<URL>();
        
        @Override
        public void addFile(FileObject file) {
            try {
            urls.add(new URL("http://unknown.com/" + file.getName().getBaseName()));
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        
        @Override
        public URL[] getCodebaseAnnotation() {
            return urls.toArray(new URL[0]);
        }

        @Override
        public String getAppId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
