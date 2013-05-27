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

import java.net.URLClassLoader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
    This class simply sets the systemClassLoader property in the context.
 * @author trasukg
 */
public class SystemClassloaderInitializer {
    private static final Logger log=Logger.getLogger(SystemClassloaderInitializer.class.getName(), MessageNames.BUNDLE_NAME);
    
    @Injected(style= InjectionStyle.BY_TYPE) private Context context=null;
    
    @Init
    public void init() {
        Map<String, ClassLoader> classLoaders=
                (Map<String, ClassLoader>) context.get(Strings.CLASS_LOADERS);
        ClassLoader cl=classLoaders.get(Strings.SYSTEM_CLASS_LOADER);
        for (String id: classLoaders.keySet()) {
            context.put(id, classLoaders.get(id));
        }
        String classpath=Strings.UNKNOWN;
        if(cl instanceof URLClassLoader) {
            URLClassLoader ucl=(URLClassLoader) cl;
            classpath=Utils.format(ucl.getURLs());
        }
        log.log(Level.FINE, MessageNames.SYSTEM_CLASSLOADER_IS,
                new Object[] {
                    cl.toString(), classpath
                });
    }
}
