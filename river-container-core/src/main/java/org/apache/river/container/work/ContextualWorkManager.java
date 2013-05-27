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
package org.apache.river.container.work;

import java.util.ArrayList;
import java.util.List;
import org.apache.river.container.Strings;

/**

 @author trasukg
 */
public class ContextualWorkManager {

    List<Context> contexts=new ArrayList<Context>();
    
    public WorkingContext createContext(String name) {
        Context context=new Context(name);
        contexts.add(context);
        return context;
    }
    
    private class Context implements WorkingContext {
        String name=Strings.UNNAMED;

        public String getName() {
            return name;
        }

        public Context(String name) {
            this.name=name;
            workManager=new BasicWorkManager(name);
        }
        
        BasicWorkManager workManager=null;
        
        @Override
        public WorkManager getWorkManager() {
            return workManager;
        }

        @Override
        public int getActiveThreadCount() {
            return workManager.getActiveCount();
        }

        @Override
        public void shutdown() {
            workManager.shutdown();
        }

        @Override
        public void interrupt() {
            workManager.interrupt();
        }
        
    }
}
