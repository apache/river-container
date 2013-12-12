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
Deploying Services Designed for ServiceStarter API
==================================================

Partly for compatability, but mainly so we can host the "core" River services
like Reggie, the container needs to be able to deploy services written for the
service starter interface.

Under service starter, a service is started by creating an instance of it
using a constructor that includes configuration options.  The first
configuration option is the name of a configuration file that would be read
by the ConfigurationFile configuration provider.

Under the container, the service cannot directly read the configuration file
since it doesn't know the actual location of the file relative to the startup
directory.  Additionally, we might want to supplement the configuration with
global config from the container; for instance, if we're deploying several
services plus reggie, we would probably want to specify the discovery groups
in just one location, rather than in every config file.

River's ConfigurationProvider allows us to substitute a different configuration
instance class in place of ConfigurationFile (see ConfigurationProvider javadoc)
so we can create a different provider that reads the file through the container.
This replacement configuration provider can also access global configuration.

The class loader hierarchy for the service-starter deployment looks like:

System CL
  |
  v
Service CL (River API's, container-liaison classes, service jars)

Packaging the Service Implementation
------------------------------------
Deployment jar file contains:

- Service jar files, in the "lib" directory.
- jar files for codebase, in the "lib-dl" directory.
- Configuration file
    - Indicates the service class and the name of the permissions file
- deployment descriptor (e.g. 'transient-reggie.config').

The file should be named ending in a '.ssar' extension (service starter archive)
and placed in the appropriate container profile's deployment directory (e.g.
default/deploy/reggie.ssar).

Deployment
----------
When the starter service deployer finds the archive, it:
- Creates a classloader that includes the jar files called out in the
starter service deployer's configuration file (this will typically include
'jsk-platform.jar', 'jsk-lib.jar', and 'RiverContainerLiaison.jar', plus the
jar files contained in the deployed service's 'lib' directory.  This
classloader will be annotated with a codebase that allows all the jar files in
the deployer config file's codebase element, plus all the jar files contained
in the service's 'lib-dl' directory.
- Registers the appropriate codebase files with the codebase service.
- Instantiates the service and runs it (on a separate thread from the deployer).
