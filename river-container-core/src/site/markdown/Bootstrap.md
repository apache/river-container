<!--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

-->

The bootstrap process looks like this:

- The bootstrapper creates the overall container context object
- Core configuration is read from the classpath at "o.a.r.c.core-config.xml".
- Each component called out in the core configuration is instantiated and put
into the context.  
- The core config includes component callouts for the following:
    - AnnotatedClassDeployer, which sets up dependency injection for the container
    components.
    - CommandLineArgumentParser, which reads the command line and sets appropriate
    values into the context (mainly the profile directory).
    - MBeanRegistrar, which takes note of any manageable components placed in 
    the context, and registers them with the MBeanContainer.
    - ShutdownListener, which acts as an MBean to allow shutdown of the 
    container from a JMX console.
    - FileUtilityImpl, which provides file services to other components.
    - PropertiesFileReader, which reads all the '.properties' files in the 
    profile directory, and puts them into the context under their file names.
        - This facility allows components to simply declare a Properties object
        and use the @Injected annotation to get their config files loaded.
    - ProfileConfigReader, which reads the profile configuration and
    sets up all the components in that config.
        - The profile configuration is subject to more editing and customization
        than the core config.
    
