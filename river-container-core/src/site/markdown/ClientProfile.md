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

Client Profile - Running clients inside the River Container
===========================================================

Client programs (i.e. consumers of Jini services) also need an environment that
is somewhat customized.  They need to run with appropriate security permissions,
plus they often need to attach codebase annotations to classes and make the 
appropriate files available as downloads.  In addition, they need to access
configurations, and may want to use the same configuration files as a server
container.

Client programs will be the only program running in the container, so they can 
go ahead and call System.exit() when they're done.

When client programs are run, they might need parameters from the command line.
So, we should call main() with any parameters that are supplied, less any that
are used for the configuration of the container.

To provide this functionality, we can have a "client" profile.  This profile will
be configured with a different startup system that reads the command line to
find out which client program to run.  We will likely have a container with
multiple client programs, so we'll allow more than one client program in the 
profile.  The invocation will be

    run.sh -client AppName <params>*