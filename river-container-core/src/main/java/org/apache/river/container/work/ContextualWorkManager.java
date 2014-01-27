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
import java.util.concurrent.ScheduledExecutorService;
import org.apache.river.container.Strings;

/**

 @author trasukg
 */
public class ContextualWorkManager {

    List<Context> contexts=new ArrayList<Context>();
    
    public WorkingContext createContext(String name, ClassLoader contextLoader) {
        Context context=new Context(name, contextLoader);
        contexts.add(context);
        return context;
    }
    
    private class Context implements WorkingContext {
        String name=Strings.UNNAMED;
        ClassLoader contextLoader;
        
        public String getName() {
            return name;
        }

        public Context(String name, ClassLoader contextLoader) {
            this.name=name;
            this.contextLoader=contextLoader;
            executor=new BasicExecutor(contextLoader, name);
        }
        
        BasicExecutor executor=null;
        
        @Override
        public ScheduledExecutorService getScheduledExecutorService() {
            return executor;
        }

        @Override
        public int getActiveThreadCount() {
            return executor.getActiveCount();
        }

        @Override
        public void shutdown() {
            executor.shutdownNow();
        }

        @Override
        public void interrupt() {
            executor.shutdownNow();
        }
        
    }
}
