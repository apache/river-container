
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
    
