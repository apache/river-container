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
Some thinking on the Hierarchical State Machine
===============================================

The application deployment system includes a hierarchical state machine
implementation that is intended to be used to help control the deployment of 
applications.

Use cases:

For instance, an application exists in one of several states: Created, 
Resolved, Starting, Started, Stopping Gracefully, Stopped, Stopping Forcefully,
Failed to Stop.  If we have an application that started successfully, then
we attempt to stop it, it may shut all its threads down properly, which puts it
into the Stopped state.  Or it may fail to stop all its threads, in which case, 
we would attempt to interrupt the thread group (Stopping Forcefully).  After a 
while, if the thread group's active thread count didn't go to zero, it would
enter the "Failed to Stop" state.  In this state, we would interrupt the thread
group at regular intervals.  If the thread count happened to go to zero, it
would go to the Stopped state, otherwise it would just sit there in the "Failed 
to Stop" state.

The model for the HSM is as follows:

- events on the state machine are expressed as methods defined in the interface.
- User sends an event by calling a method on a proxy that implements the machine.
- An event is "run" on the current state hierarchy (top-level state and all
active substates).
- Each event can trigger a transition (actually one or more) by invoking a method
that has an @Transition annotation that lists the set of states that should be
activated after the event is run.
- The state can have sub-states, so when we transition to a state, a sub-state
may also become active, and will need to have its entry code run.  The sub-state
might be a fresh state on entry, or might have been retained from the last time
the state was active.  
- Each state may have an "on-entry" and "on-exit" function, which will be executed
by the transition code.  Entry and exit code 
typically sets up timers or toggles outputs.
- Each state can have zero or more "guard" methods, each specifying a transition.
A guard method is run after every event is executed, and determines whether the
transition is taken.  If the
guard method returns true, the corresponding transition is executed.  These 
could also be seen as "anonymous" transitions, as they are not attached to any
particular event.


