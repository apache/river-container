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

Surrogate Deployment
====================

- So far as the container is concerned, a Surrogate is just another kind of
application that is handled by a deployer.
- The surrogate deployer sets up on an interface of some kind looking for 
surrogates to deploy.  As per the surrogate over IP spec, this will involve
setting up a multicast listener that responds to requests on the surrogate
interface.
- One thing to note - the surrogate deployer might be listening to a  different
interface than the Jini workgroup is running on.  In the case of a non-TCP/IP
surrogate, this is guaranteed to be the case.
- For instance, let's say we have a PBX or asterisk implementation that wants
to publish a Jini service by means of a surrogate.  The scenario is that the 
surrogate talks to the PBX by means of UDP or simple TCP sockets.  If the PBX
was unable to support a secured protocol, then a node might 
have a connection on a private network to the PBX, but then might have a normal
network connection for other clients of the PBX surrogate.  Since the surrogate 
runs in the Java/Jini environment, it can use the full JERI security stack.