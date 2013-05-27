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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * Interface for the container's workload manager. System objects use an
 * instance of this interface to request the container to perform work on their
 * behalf. Using a centralized workload manager allows the container to both
 * control the scheduling of the workload and provide instrumentation on the
 * workload that might be useful for debugging or performance management.
 *
 * TODO: Need to have some way of grouping tasks, then killing off a task and
 * all its subtasks (thread groups etc) for shutdown purposes.
 *
 * @author trasukg
 */
public interface WorkManager {

    /**
     * Queue a task for execution.
     *
     * @param taskClass Indicates what type of task this is. The implementation
     * may use this information to assign the task to one of several execution
     * queues.
     *
     * @param contextClassLoader The context classloader that should be used
     * when running the task.
     *
     * @param task The task to be run.
     */
    public void queueTask(ClassLoader contextClassLoader,
            Runnable task);

    /**
     * Schedule a task for future execution
     *
     * @param command
     * @param delay
     * @param unit
     * @return
     */
    ScheduledFuture<?> schedule(ClassLoader contextClassLoader,
            Runnable command,
            long delay,
            TimeUnit unit);
}
