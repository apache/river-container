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

import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author trasukg
 */
public class BasicWorkManagerTest {
    
    public BasicWorkManagerTest() {
    }

    BasicWorkManager UUT=new BasicWorkManager();
    
    @Test
    public void testNullContextClassLoader() {
        Harness h=new Harness();
        UUT.queueTask(null, h);
        waitHarness(4000,h);
        assertEquals("Didn't use current context classloader.", 
                Thread.currentThread().getContextClassLoader(), h.cl);
    }
    
    @Test
    public void testScheduledExecution() {
        Harness h=new Harness();
        long startTime=System.currentTimeMillis();
        UUT.schedule(null, h, 2, TimeUnit.SECONDS);
        waitHarness(4000,h);
        assertEquals("Didn't use current context classloader.", 
                Thread.currentThread().getContextClassLoader(), h.cl);
        assertTrue("Delay was only " + (h.runTime - startTime),
                h.runTime >= startTime+2000);
    }
    
    private void waitHarness(long time, Harness h) {
        long start=System.currentTimeMillis();
        while ( !h.done && System.currentTimeMillis() - start < time) {
            Thread.yield();
        }
        if (System.currentTimeMillis() - start >= time) {
            fail("Harness task did not run.");
        }
    }
    
    private class Harness implements Runnable {
        ClassLoader cl=null;
        volatile boolean done=false;
        String threadName=null;
        volatile long runTime=0;
        
        @Override
        public void run() {
            cl=Thread.currentThread().getContextClassLoader();
            threadName=Thread.currentThread().getName();
            done=true;
            runTime=System.currentTimeMillis();
        }
    }
            
}
