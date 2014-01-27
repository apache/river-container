Work Manager and Work Management in the Container
=================================================

- In general, containers should be able to control the thread usage and scheduling of 
work inside the container.  Otherwise it's possible for an application to hijack
execution or prevent a different application, or possibly the container itself,
from executing properly.  
- So, this desire means that applications should be discouraged or disallowed from 
creating their own threads.  In turn, that restriction means that the container must
offer some way to schedule work that should happen on another thread, and possibly at
some time in the future, or even repeatedly.

How it is on 20140125
---------------------

- The container includes an attempt at this, embodied in `org.apache.river.container.work`  
    - There is an interface 'WorkManager' that contains a `queue(...)` method that 
    drops the task into a task queue.  The queue method also allows the user to 
    specify a classloader for the task to run in.  The thread pool is expected to set
    this classloader as the context classloader before executing the task.
    - There is a 'BasicWorkManager' implementation that uses a ThreadPoolExecutor to
    implement 'WorkManager'  
    - There is a 'ContextualWorkManager' implementation that allows jobs to be grouped
    together and cancelled en-masse, for instance when an application needs to be
    shut down.

Problems
--------

- We need to provide a way for well-written applications to schedule multi-threaded
work.  We probably shouldn't introduce container-specific API, especially since there
is a perfectly good API for work management in `javax.concurrent`.  
- As written now, the API doesn't prevent a single thread pool, but that isn't implemented
yet. The working contexts each have their own thread pool. 
- There is no facility for an application to have any internal prioritization.

Design Goals
------------

- Provide an API to applications that allows them to fire off background work, 
scheduled executions, and repetitive tasks.  
    - Essentially, one or more ScheduledExecutorService objects should be provided
    for the application.
- Ideally, there should be one thread pool that is managed by the container  
- The executor objects provided to the applications should be isolated from each
other, cancellable en-masse (for application shutdown) and should preserve the 
context classloader.
- The number of threads in the thread pool should be configurable.  
- Ideally, the thread pool policy should be configurable (i.e. fixed threads,
max threads, min threads, etc).
- Current users of WorkManager interface should be migrated to the new API.  
- Number of threads in use, etc should be visible through a management interface.  
- Applications should be able to provide prioritization on the tasks  
    - Perhaps by also implementing Comparable in the task that implements Runnable.  
    - Runnables that come "first" are run first.  





