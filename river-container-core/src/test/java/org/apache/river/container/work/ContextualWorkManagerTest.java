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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.river.container.Strings;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author trasukg
 */
public class ContextualWorkManagerTest {

    ContextualWorkManager UUT = new ContextualWorkManager();
    WorkingContext context = UUT.createContext("Test-ctx", Thread.currentThread().getContextClassLoader());

    @Test
    public void testContextCreation() {
        assertNotNull("context", context);
        assertNotNull("context.scheduledExecutorService", context.getScheduledExecutorService());
    }

    @Test
    public void testRunAndExit() {
        WorkerRunnable wt = new WorkerRunnable();
        context.getScheduledExecutorService().submit(wt);
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 2000 & context.getActiveThreadCount() < 1) {
            Thread.yield();
        }
        assertEquals("thread count", 1, context.getActiveThreadCount());
        
    }

    @Test
    public void testChildThreadGroup() throws Exception {
        WorkerRunnable wt = new WorkerRunnable();
        context.getScheduledExecutorService().submit(wt);
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 2000 & context.getActiveThreadCount() < 1) {
            Thread.yield();
        }
        Thread.sleep(1000); // Ugly wait for thread to start.
        assertTrue("Thread group name '" + wt.getThreadGroupName() + "' doesn't start with ctx name",
                wt.getThreadGroupName().startsWith("Test-ctx"));
    }

    /**
     * Hold off on this -- not needed yet. *
     */
    @Test
    public void testThreadCountWithChildren() throws Exception {
        WorkerRunnable wt = new WorkerRunnable(2);
        context.getScheduledExecutorService().submit(wt);
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 2000 & context.getActiveThreadCount() < 1) {
            Thread.yield();
        }
        Thread.sleep(500);
        try {
            System.out.println("Checking thread count.");
            assertEquals("thread count", 3, context.getActiveThreadCount());
        } finally {
            wt.proceed = true;
            Thread.sleep(1000);
        }
    }

    private class WorkerRunnable extends Thread {

        String threadGroupName = Strings.UNKNOWN;
        List<WorkerRunnable> children = new ArrayList<WorkerRunnable>();
        String id = "--";
        volatile boolean proceed = false;
        int nChildren = 0;

        public WorkerRunnable() {
        }

        public String getThreadGroupName() {
            return threadGroupName;
        }

        /**
         * Hmm.. Is it possible that the thread group is assigned at thread
         * creation time? Of course it is! Looks like the thread group id is
         * assigned to the parent when the Thread instance is instantiated,
         * rather than when the thread (i.e. the actual background thread) is
         * launched.
         *
         * @param nChildren
         */
        public WorkerRunnable(int nChildren) {
            this.nChildren = nChildren;
        }

        public void run() {
            threadGroupName = Thread.currentThread().getThreadGroup().getName();

            System.out.println("Worker " + id + " beginning in thread group "
                    + Thread.currentThread().getThreadGroup().getName() + ".");
            if (nChildren != 0) {
                for (int x = 0; x < nChildren; x++) {
                    WorkerRunnable newWorker = new WorkerRunnable();
                    newWorker.id = "WorkerRunnable-" + (x + 1);
                    children.add(newWorker);
                }
            }
            try {
                for (WorkerRunnable worker : children) {
                    worker.start();
                }
                while (!proceed) {
                    Thread.sleep(1500);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ContextualWorkManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                for (WorkerRunnable worker : children) {
                    worker.proceed = true;
                }
                System.out.println("Worker " + id + " ended.");

            }
        }
    }
}
