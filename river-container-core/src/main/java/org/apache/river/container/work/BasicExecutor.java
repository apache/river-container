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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
public class BasicExecutor implements ScheduledExecutorService {

    private static final Logger log = Logger.getLogger(BasicExecutor.class.getName(), MessageNames.BUNDLE_NAME);
    ExecutorService executor = null;
    ScheduledExecutorService scheduledExecutor=null;
    private MyThreadFactory threadFactory = null;
    private String name = Strings.UNNAMED;
    private ClassLoader contextLoader;
    
    public BasicExecutor(ClassLoader contextLoader) {
        this(contextLoader, Strings.UNNAMED);
    }

    public BasicExecutor(ClassLoader contextLoader, String name) {
        this.contextLoader=contextLoader;
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
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(new TaskRunnable(command, classLoaderToUse()), delay, unit);
    }

    private class TaskRunnable implements Runnable {

        Runnable task = null;
        ClassLoader contextClassLoader = null;
        ClassLoader originalClassLoader = null;

        TaskRunnable(Runnable task, ClassLoader contextClassLoader) {
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

    private class TaskCallable<T> implements Callable<T> {

        Callable<T> task = null;
        ClassLoader contextClassLoader = null;
        ClassLoader originalClassLoader = null;

        TaskCallable(Callable<T> task, ClassLoader contextClassLoader) {
            this.task = task;
            this.contextClassLoader = contextClassLoader;
        }

        @Override
        public T call() throws Exception {
            originalClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            try {
                return task.call();
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

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(new TaskCallable(callable, classLoaderToUse()), delay, unit);
    }

    private ClassLoader classLoaderToUse() {
        ClassLoader classLoaderToUse =
                contextLoader != null ? contextLoader : Thread.currentThread().getContextClassLoader();
        return classLoaderToUse;
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(new TaskRunnable(command, classLoaderToUse()), initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduledExecutor.scheduleWithFixedDelay(new TaskRunnable(command, classLoaderToUse()), initialDelay, delay, unit);
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> neverCommenced=new ArrayList<Runnable>();
        neverCommenced.addAll(scheduledExecutor.shutdownNow());
        neverCommenced.addAll(executor.shutdownNow());
        return neverCommenced;
    }

    public boolean isShutdown() {
        return scheduledExecutor.isShutdown() & executor.isShutdown();
    }

    public boolean isTerminated() {
        return scheduledExecutor.isTerminated() & executor.isTerminated() & getActiveCount()==0;
    }

    /**
     * Await termination.  Note that this implementation doesn't make any guarantees
     * about accuracy of the termination wait time, but it will be bounded at 2*timeout.
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException 
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit) & scheduledExecutor.awaitTermination(timeout, unit);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(new TaskCallable(task, classLoaderToUse()));
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(new TaskRunnable(task, classLoaderToUse()), result);
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(new TaskRunnable(task, classLoaderToUse()));
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Callable<T>> wrappedTasks = constructListOfWrappedTasks(tasks);
        return executor.invokeAll(wrappedTasks);
    }

    private <T> List<Callable<T>> constructListOfWrappedTasks(Collection<? extends Callable<T>> tasks) {
        /* Construct a list of wrapped tasks. */
        List<Callable<T>> wrappedTasks=new ArrayList<Callable<T>>(tasks.size());
        for (Callable<T> task: tasks) {
            wrappedTasks.add(new TaskCallable(task, classLoaderToUse()));
        }
        return wrappedTasks;
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        List<Callable<T>> wrappedTasks = constructListOfWrappedTasks(tasks);
        return executor.invokeAll(wrappedTasks, timeout, unit);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        List<Callable<T>> wrappedTasks = constructListOfWrappedTasks(tasks);
        return executor.invokeAny(wrappedTasks);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        List<Callable<T>> wrappedTasks = constructListOfWrappedTasks(tasks);
        return executor.invokeAny(wrappedTasks, timeout, unit);
    }

    public void execute(Runnable command) {
        executor.execute(new TaskRunnable(command, classLoaderToUse()));
    }
    
    
}
