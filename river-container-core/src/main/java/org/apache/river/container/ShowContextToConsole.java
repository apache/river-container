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

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author trasukg
 */
public class ShowContextToConsole implements ContextListener {

    private static final Logger log=Logger.getLogger(ShowContextToConsole.class.getName(), MessageNames.BUNDLE_NAME);

    private Context context;

    public void init() {

        for(String key: context.contents.keySet()) {
            log.log(Level.FINE,MessageNames.CONTEXT_ITEM ,
                    new Object[] {key, context.contents.get(key)});
        }
    }

    public void put(String name, Object o) {

    }

    public void remove(Object o) {

    }

    public void setContext(Context ctx) {
        context=ctx;
    }

    public void initComplete() {
        init();
    }

    @Override
    public void shutDown() {
        
    }
}
