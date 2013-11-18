Management of the container using JMX
=====================================

- Perhaps the managed objects (e.g. classloaders, app environments, etc) should
implement the MBean protocol/annotation.  JSR160 provides for registration of
proxies in the lookup service, and it looks like it isn't too hard to integrate
JMX.
