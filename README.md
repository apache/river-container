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
river-container
===============

This project is the initial development of the Apache River Container.
It is _not_ yet released as Apache Software.  Use at your own risk, and please
post comments to dev@river.apache.org.

The First Fifteen Minutes
=========================

Let's assume you've managed to download the source from git.  In fact, let's assume that
you're reasonably conversant with git, so you can pull the examples project below.

# Before You Start  
You'll need Maven 3.x installed, such that you can run 'mvn' from the command
line.  

# Build From Source 

<i>This step may not be required in the future, since you'll be able to 
download a convenience binary once the project is released</i>

Go to the root of the river-container download, and type  
    mvn clean install

# Run the default container profile  
    cd product/target/product*
    bin/run 

The steps above will startup a default container that has instances of the 
service registrar (Reggie) and the transaction manager (Mahalo).  All services
are registered in a workgroup called 'RiverContainerDefault'.

# Run the service browser  
Open a new command line window in the root of the river-container download, then
do the following  
    cd product/target/product*
    bin/run client browser

You'll see a service browser window open up.  It should show one registrar.
Select the registrar.  You should now see the infrastructure services, Reggie and 
Mahalo.  Leave the service browser running while we start up a "Hello-world" 
service, below.

# Compile a "Hello-World" Service

<i>Maybe the 'hello-world' example should be included in the container deliverable?
Please comment on 'dev@river.apache.org'.</i>

<i>Eventually, we should be able to create this example service using a Maven
archetype.</i>

Using git, pull the examples from https://github.com/trasukg/river-container-examples.

'cd' into your hello-example' directory, and then  
    mvn clean install

# Deploy and run the "Hello-World" Service

When Maven is done, you should be able to see the finished service archive,
'hello-module/target/hello-module-1.0-SNAPSHOT.jar'

Copy that 'jar' file into the
'profiles/default/deploy' folder inside our 'river-container/product/target/product*' folder.

<i>Eventually, we'll have a maven plugin that does this, so you can just do 
'mvn river:deploy' rather than copying it manually.</i>

If you left the container running, you should see some output indicating that the 
service is being deployed.  If you didn't leave the container running, start it up now.

You should also see the service in the service browser, with the interface
'org.apache.river.container.hello.example.api.Greeter'

# Deploy and Run the "Hello-World" Consumer

When Maven finished above, it also created a client archive, 
'hello-client-module/target/hello-client-module-1.0-SNAPSHOT.jar'

Copy that 'jar' file into the
'profiles/client/deploy' folder inside our 'river-container/product/target/product*' folder.

Open a new command line window in the root of the river-container download, then
do the following  
    cd product/target/product*
    bin/run client hello-client

The client starts up, and eventually prompts 'Please enter your name'.  Enter
your name and then press return.

The client sends the greeter service a message, then prints out the reply.

# Use the Network!

If you have another machine on the local area network, and if the network is
configured to allow multicast, you should be able to run the browser and the
hello-client on a different machine.

That concludes the 'First Fifteen Minutes' demo.  Below, there is a little more 
detailed information...

# Building from Source

    mvn clean install

# Running Services in the Container

    cd product/target/product*
    bin/run [profile] arg*

If you don't specify [profile] the 'default' profile will be used.  'arg*' isn't
really used much in the service container profiles (like 'default').  

Services are packaged into a jar file and placed into the 'deploy' folder
of the profile that you want to run.  When you run the container, all services
in the profile are started up.  The default profile monitors the deploy folder
each 5s and starts up any new services you put into the deploy folder. The 
'default' profile already includes a transient reggie instance and a transient 
mahalo instance.  You can add your own services to this folder as well.

Have a look at the 'reggie-module' or 'mahalo-module' target folders to see
what the archive should look like.  Startup parameters are in 'start.properties'.
'start.properties' calls out the startup class and the parameters to its constructor
(typically the name of the configuration file and any overrides to the config).

# Running Client Applications

    cd product/target/product*
    bin/run client AppName arg*

Starts up the container using the 'client' profile, which then starts the client
that is named by 'AppName' (and only that client, no matter if there are multiple
apps in the deploy folder).

The container will host client applications, making the downloads available via
a codebase server, and setting up all the security polices that are required.
Client apps are packaged much like the services mentioned above.

# Service Browser

    cd product/target/product*
    bin/run client browser

Starts up the service browser.

# Sample Service

Reggie-module and mahalo-module might be interesting samples.  For a simpler
'hello-world' example with a Maven build, 
see https://github.com/trasukg/river-container-examples.

