/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.river.container.deployer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.river.container.MessageNames;
import org.apache.river.container.Utils;
import org.apache.river.container.hsm.Controller;
import org.apache.river.container.hsm.Guard;
import org.apache.river.container.hsm.Initial;
import org.apache.river.container.hsm.OnEntry;
import org.apache.river.container.hsm.OnExit;
import org.apache.river.container.hsm.PlainStateMachineExecutor;
import org.apache.river.container.hsm.RootState;
import org.apache.river.container.hsm.State;
import org.apache.river.container.hsm.StateMachineInfo;
import org.apache.river.container.hsm.Transition;

/**
 * Life cycle controller for "service-starter" services. Idle --> Starting -->
 * Running --> Stopping --> Idle --> Zombie
 */
@RootState({ServiceLifeCycle.class, StatusEvents.class})
public class StarterServiceLifeCycleSM {

    public static final int MAX_RETRY_COUNT=10;
    
    private static final Logger logger = Logger.getLogger(StarterServiceLifeCycleSM.class.getName(),
            MessageNames.BUNDLE_NAME);
    private ApplicationEnvironment appEnv = null;
    private StarterServiceDeployer deployer = null;
    private StatusEvents eventProxy = null;
    private ServiceLifeCycle lifeCycleProxy = null;
    private List<Throwable> exceptions = new ArrayList<Throwable>();

    public static ServiceLifeCycle newStarterServiceLifeCycle(ApplicationEnvironment appEnv, StarterServiceDeployer deployer) {
        StarterServiceLifeCycleSM machine = new StarterServiceLifeCycleSM();
        machine.appEnv = appEnv;
        machine.deployer = deployer;
        machine.eventProxy = (StatusEvents) PlainStateMachineExecutor.createProxy(machine);
        machine.lifeCycleProxy = (ServiceLifeCycle) machine.eventProxy;
        return machine.lifeCycleProxy;
    }

    @State({Idle.class, Preparing.class, Prepared.class, Starting.class, 
        Failed.class, Running.class, Stopping.class, DirtyShutdown.class,
        Idle.class})
    @Initial(Idle.class)
    private Object state;
    @Controller
    private StateMachineInfo controller;

    public String getStatus() {
        return state.getClass().getSimpleName();
    }

    public String getName() {
        return appEnv.getServiceName();
    }

    public void start() {
        logger.log(Level.FINE,MessageNames.RECEIVED_START,
                new String[]{ getStatus() });
    }
    
    public void startWithArgs(String[] args) {
        logger.log(Level.FINE,MessageNames.RECEIVED_START_WITH_ARGS,
                new String[]{ getStatus() });
    }
    
    public void exception(Throwable ex) {
        logger.log(Level.SEVERE, MessageNames.EXCEPTION_THROWN, ex);
    }
    
    public class Idle {
        /*
         * To start from idle means to prepare, and then start.
         */

        @Transition(Preparing.class)
        public void start() {
            exceptions.clear();
            Runnable command = new Runnable() {
                public void run() {
                    /* Prepare the application environment. */
                    try {
                        deployer.prepareService(appEnv);
                        eventProxy.prepareSucceeded();
                        lifeCycleProxy.start();
                    } catch (Exception ex) {
                        eventProxy.exception(ex);
                    }
                }
            };
            deployer.workManager.queueTask(null, command);
        }

        @Transition(Preparing.class)
        public void startWithArgs(final String[] args) {
            exceptions.clear();
            Runnable command = new Runnable() {
                public void run() {
                    /* Prepare the application environment. */
                    try {
                        deployer.prepareService(appEnv);
                        eventProxy.prepareSucceeded();
                        lifeCycleProxy.startWithArgs(args);
                    } catch (Exception ex) {
                        eventProxy.exception(ex);
                    }
                }
            };
            deployer.workManager.queueTask(null, command);
        }

        @Transition(Preparing.class)
        public void prepare() {
            exceptions.clear();
            Runnable command = new Runnable() {
                public void run() {
                    /* Prepare the application environment. */
                    try {
                        deployer.prepareService(appEnv);
                        eventProxy.prepareSucceeded();
                    } catch (Exception ex) {
                        eventProxy.exception(ex);
                    }
                }
            };
            deployer.workManager.queueTask(null, command);
        }

    }

    public class Preparing {

        @Transition(Prepared.class)
        public void prepareSucceeded() {
        }

        @Transition(Failed.class)
        public void exception(Exception ex) {
            exceptions.add(ex);
        }

    }

    public class Prepared {

        @Transition(Starting.class)
        public void start() {
            Runnable command = new Runnable() {
                public void run() {
                    /* Prepare the application environment. */
                    try {
                        deployer.launchService(appEnv, new String[0]);
                        eventProxy.startSucceeded();
                    } catch (Exception ex) {
                        eventProxy.exception(ex);
                    }
                }
            };
            deployer.workManager.queueTask( null, command);

        }
        
        @Transition(Starting.class)
        public void startWithArgs(final String[] args) {
            Runnable command = new Runnable() {
                public void run() {
                    /* Prepare the application environment. */
                    try {
                        deployer.launchService(appEnv, args);
                        eventProxy.startSucceeded();
                    } catch (Exception ex) {
                        eventProxy.exception(ex);
                    }
                }
            };
            deployer.workManager.queueTask( null, command);

        }
        
        @Transition(Idle.class)
        public void stop() {}
        
    }

    public class Running {
        @Transition(Stopping.class) 
        public void stop() {
             Runnable command = new Runnable() {
                public void run() {
                    /* Prepare the application environment. */
                    try {
                        deployer.stopService(appEnv);
                        if(appEnv.getWorkingContext().getActiveThreadCount()==0) {
                            eventProxy.stopSucceeded();
                        } else {
                            eventProxy.stopFailed();
                        }
                    } catch (Exception ex) {
                        eventProxy.exception(ex);
                    }
                }
            };
            deployer.workManager.queueTask(null, command);
        }
    }

    /**
     * We want the state to show as "Failed" but in reality, you can do all
     * the same commands as if you were in "Idle".  So we just extend "Idle".
     */
    public class Failed extends Idle {
    }

    public class Stopping {
        /* TODO: Implement the state machine from here to check for proper
         * shutdown.
         */
        
        @Transition(Idle.class) 
        public void stopSucceeded() {}
        
        @Transition(DirtyShutdown.class)
        public void stopFailed() {}
        
        @Guard(Idle.class) 
        public boolean areThreadsGone() {
            return appEnv.getWorkingContext().getActiveThreadCount()==0;
        }
        
        public void exception(Exception ex) {
            logger.log(Level.WARNING, MessageNames.EXCEPTION_WHILE_STOPPING,
                    new Object[] { Utils.stackTrace(ex) });
        }
    }

    public class DirtyShutdown {
        int retryCount=0;
        
        @OnEntry
        public void enter() {
            try {
            logger.log(Level.INFO, MessageNames.FAILED_CLEAN_SHUTDOWN, 
                    new Object[] { appEnv.getServiceName() });
            retryCount=0;
            /* Interrupt threads,  then start interval timer to repeat. */
            appEnv.getWorkingContext().shutdown();
            setTimer();
            } catch(Throwable t) {
                System.out.println("Got exception while entering DirtyShutdown");
                t.printStackTrace();
            }
        }
        
        @Transition(Idle.class)
        public void stopped() {}
        
        @OnExit
        public void exit() {
            clearTimer();
        }
        
        public void timeout() {
            appEnv.getWorkingContext().shutdown();
            appEnv.getWorkingContext().interrupt();
            retryCount++;
        }
        
        @Guard(Failed.class)
        public boolean isRetryCountExceeded() {
            if (retryCount > MAX_RETRY_COUNT) {
                logger.log(Level.INFO, MessageNames.SHUTDOWN_FAILED,
                        new Object[] { appEnv.getServiceName()});
            }
            return retryCount > MAX_RETRY_COUNT;
        }
        
        @Guard(Idle.class) 
        public boolean areThreadsGone() {
            int nThreads=appEnv.getWorkingContext().getActiveThreadCount();
            logger.log(Level.FINE, MessageNames.N_THREADS_LEFT,
                    new Object[]{ appEnv.getServiceName(), nThreads });
            return nThreads==0;
        }
    }
    public class Starting {

        @Transition(Running.class)
        public void startSucceeded() {
        }

        @Transition(Failed.class)
        public void exception(Exception ex) {
            exceptions.add(ex);
        }
    }
    
    ScheduledFuture timer=null;
    
    public synchronized void setTimer() {
        Runnable command=new Runnable() {
            public void run() {
                eventProxy.timeout();
                setTimer();
            }
        };
        clearTimer();
        // We're shutting down the appEnv's working context, so we need the 
        // deployer's work manager.
        timer=deployer.workManager.schedule(null, command, 2, TimeUnit.SECONDS);
    }
    
    public synchronized void clearTimer() {
        if (timer != null) {
            timer.cancel(true);
            timer=null;
        }
    }
}
