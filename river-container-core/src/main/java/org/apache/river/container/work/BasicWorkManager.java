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
package org.apache.river.container.work;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.river.container.Init;
import org.apache.river.container.MessageNames;
import org.apache.river.container.Shutdown;
import org.apache.river.container.Strings;

/**
 *
 * A Basic implementation of WorkManager that runs the work threads through a
 * ThreadPoolExecutor.
 *
 * @author trasukg
 */
public class BasicWorkManager implements WorkManager {

    private static final Logger log = Logger.getLogger(BasicWorkManager.class.getName(), MessageNames.BUNDLE_NAME);
    ExecutorService executor = null;
    ScheduledExecutorService scheduledExecutor=null;
    private MyThreadFactory threadFactory = null;
    private String name = Strings.UNNAMED;

    public BasicWorkManager() {
        threadFactory = new MyThreadFactory();
        executor = Executors.newCachedThreadPool(threadFactory);
        scheduledExecutor=
                Executors.newSingleThreadScheduledExecutor(threadFactory);
        
    }

    public BasicWorkManager(String name) {
        this.name = name;
        threadFactory = new MyThreadFactory();
        executor = Executors.newCachedThreadPool(threadFactory);
        scheduledExecutor=
                Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    synchronized int getActiveCount() {
        return threadFactory.threadGroup.activeCount();
    }

    @Override
    public void queueTask(ClassLoader contextClassLoader, Runnable task) {
        ClassLoader classLoaderToUse =
                contextClassLoader != null ? contextClassLoader : Thread.currentThread().getContextClassLoader();
        executor.execute(new TaskHolder(task, classLoaderToUse));
    }

    @Override
    public ScheduledFuture<?> schedule(ClassLoader contextClassLoader, Runnable command, long delay, TimeUnit unit) {
        ClassLoader classLoaderToUse =
                contextClassLoader != null ? contextClassLoader : Thread.currentThread().getContextClassLoader();
        return scheduledExecutor.schedule(new TaskHolder(command, classLoaderToUse), delay, unit);
    }

    private class TaskHolder implements Runnable {

        Runnable task = null;
        ClassLoader contextClassLoader = null;
        ClassLoader originalClassLoader = null;

        TaskHolder(Runnable task, ClassLoader contextClassLoader) {
            this.task = task;
            this.contextClassLoader = contextClassLoader;
        }

        @Override
        public void run() {
            originalClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            try {
                task.run();
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        }
    }

    @Init
    public void init() {
        log.info(MessageNames.BASIC_WORK_MANAGER_INITIALIZED);
    }

    @Shutdown
    public void shutdown() {
        executor.shutdownNow();
        scheduledExecutor.shutdownNow();
    }

    public void interrupt() {
        threadFactory.threadGroup.interrupt();
    }
    
    private class MyThreadFactory implements ThreadFactory {

        private ThreadGroup threadGroup = new ThreadGroup(name);
        private int index = 0;

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(threadGroup, r);
            t.setName(name + Strings.DASH + index++);
            log.log(Level.FINE, MessageNames.CREATED_THREAD,
                    new Object[]{t.getName(), t.getThreadGroup().getName()});
            return t;
        }
    }
}
