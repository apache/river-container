river-container
===============

Initial development on Apache River Container

# Building from Source

    mvn clean install

# Running Services in the Container

    cd product/target/product*
    sh bin/run.sh [profile] arg*

If you don't specify [profile] the 'default' profile will be used.  'arg*' isn't
really used much in the service container profiles (like 'default').

Services are packaged into a jar file and placed into the 'deploy' folder
of the profile that you want to run.  When you run the container, all services
in the profile are started up.  The default profile monitors the deploy folder
each 5s and starts up any new services you put into the deploy folder.

Have a look at the 'reggie-module' or 'mahalo-module' target folders to see
what the archive should look like.  Startup parameters are in 'start.properties'.
'start.properties' calls out the startup class and the parameters to its constructor
(typically the name of the configuration file and any overrides to the config).

# Running Client Applications

    cd product/target/product*
    sh bin/run.sh client AppName arg*

Starts up the container using the 'client' profile, which then starts the client
that is named by 'AppName' (and only that client, no matter if there are multiple
apps in the deploy folder).

The container will host client applications, making the downloads available via
a codebase server, and setting up all the security polices that are required.
Client apps are packaged much like the services mentioned above.

# Service Browser

    cd product/target/product*
    sh bin/run.sh client browser

Starts up the service browser.

# Sample Service

Reggie-module and mahalo-module might be interesting samples.  For a simpler
'hello-world' example with a Maven build, 
see https://github.com/trasukg/river-container-examples.

