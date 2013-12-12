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

Security in the Container
=========================

- We need to provide the following:
    - All permissions for container implementation code.
    - Limited permissions as defined for surrogates in each surrogate
    operating environment.  It's possible that different surrogates might have
    different permissions sets according to some criteria (don't know what that
    might be, perhaps different permissions for different connectors)
    - Limited permissions for other applications loaded into the container
    - Support for dynamic assignment of permissions to proxies by the proxy
    verifiers, such as would be allowed by DynamicPolicyProvider.
    - Should probably still support UmbrellaGrant, same as FilePolicyProvider.

- Some observations:
    - we have a more complicated class loader structure than contemplated by
    the PolicyFile provider.
    - in particular, in a Jini environment, grants to a codebase are pretty
    meaningless.  Grants to code signed by a particular entity are OK, but the
    keystore setup is more appropriate at the container-admin level.  Hence the
    'keystore' declaration in a policy file is not needed.
    - In all likelihood, we would have a policy file per application deployed
    to the container.
    - Would probably like to support a default application policy and then
    allow the container admin to replace that with a customized policy file for
    a particular application.
    - Since we are creating the classloader as an instance of our own
    classloader, we can provide a customized protection domain as well.
    - Applications and surrogates may come and go.  In order to prevent
    memory leaks, the policy provider will need to be informed of the
    ProtectionDomain shutdown (assuming that it needs to keep track of the
    protection domain) so it can clear out its references and allow the
    protection domain or classloader to be garbage-collected.
        - Perhaps we could store data in the protection domain, but what about
        the RMIClassLoader etc?  Can we make it use our custom domain?

- Tentative plan:
    - Create a parser that reads a limited version of the permissions file
    (like no codebase, no keystore lines) and creates permissions set from it.
    - Each application gets a protection domain; protection domain is taken
    from a per-app security config.
    - Default config provided, but is overridden by per-app config.
    - Maybe: app can have its own config file in META-INF.  When app is started,
    that file will be used unless it over-reaches the permissions allowed by the
    default or per-app config.
    - Implement a Policy that includes functions of DynamicPolicyProvider, but
    otherwise goes to the protection domain for the permissions.
