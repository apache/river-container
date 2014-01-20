Service Invocation Manager
--------------------------
* Somewhat similar to ServiceDiscoveryManager.
* Purpose is to allow a client to invoke an operation on a service, where there
may be more than one possible service client.
    * For instance, if the service interface is to a business process that uses
corellating data, there might be more than one provider, and every one but the 
"right" one will throw a "process not found" exception.  
    We want to keep the "right" one until such time as we're done interacting 
with it.  
* Two modes of operation:
    * Cached mode - somewhat like SDMCache, it keeps a cache of discovered services, and
runs through the cached services when we try to make a call.
    * Normal mode - When a "discovery" operation is required, it queries all the 
lookup services it can find for candidate services.  This would be used for
a web server installation where SDM can't export a listener.

Business Process
----------------
* Inspiration is drawn from how BPEL manages its business processes with a 
"stateless" interface.  
    * In other words, there's no need to lookup a particular process instance and 
then interact with it - you just make operation calls against the stateless
interface, and the BPEL engine maps the stateless call to the correct instance  
    *  Mapping is done using a "Corelation set" that is defined in the interface and
the process variables.  Parameters in the call are used to select the correct 
instance.  
    * e.g. confirmOrder(12667) will map to the instance that holds order 12667.  
    * This gets a little ugly if you allow that there may be more than one service
that "might" hold a given instance.  
    * you need to try the invocation against all known services that hold that 
interface  
    * Most of these services will return some kind of "no such process" exception,
but one of them will return successfully.  
    * (note that in all likelihood this means one or two services, not tens or 
hundreds).


River Container Maven Plugin
----------------------------

We'd like to have a plugin that makes it easy to deploy and run an application
created in Maven.  Proposed goals would be something like:

* Goals  
    * `mvn river:deploy` - Installs the current application to the default river
    container by copying to the 'deploy' dir in the container's default profile.
    If the container is running, this will effectively load the new service 
    * `mvn river:deploy-client` - Installs the current application to the default
    river container by copying to the 'deploy' dir in the container's client
    profile.  Note that the plugin doesn't try to figure out whether your app _should_
    be installed as a client or not - it just takes your word for it.
    * `mvn river:run` - Contacts the currently running container to deploy the current
    application (in-place with no copying),
    undeploying any application with the same name (i.e. the previous version).  
    * `mvn river:run-client` - Runs the current application using the client profile
    in-place (i.e. no copying to the deploy directory).  
    * `mvn river:start-server` - Starts the server in default profile, or other profile
    if the `river.container.profile` property is set.  
    * `mvn river:stop-server` - Stops the server's default profile, or other profile if
    the `river.container.profile` property is set.  
    * `mvn river:list-apps` - Lists the applications currently running in the container.  
* Properties
    * `river.container.home` - The home directory for the container.  
    * `river.container.profile` - The profile to run as a "server".  
    * `river.container.clientProfile` - The profile to run as a "client".  



