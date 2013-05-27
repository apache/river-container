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
package org.apache.river.container.hsm;

import java.util.logging.Level;
import java.util.logging.Logger;
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
public class InitializedMachineTest {

    public InitializedMachineTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        Logger.getLogger("org.apache.river.container.hsm").setLevel(Level.FINEST);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        UUT=new InitializedTestSM();
        UUTI=(InitializedTestSMInterface) PlainStateMachineExecutor.createProxy(UUT);
    }

    @After
    public void tearDown() {
    }

    InitializedTestSMInterface UUTI=null;
    InitializedTestSM UUT=null;
    
    @Test(expected=IllegalStateException.class)
    public void testLockedException() throws Exception {
        UUTI.setValue(20);
    }
    
    /**
     * If we transition to unlocked, then that means the @Transition tag
     * was interpreted and executed correctly.
     */
    @Test
    public void testUnlocking() {
        UUTI.unlock();
        assertTrue("lockedState is not instance of Unlocked", UUT.lockedState instanceof InitializedTestSM.Unlocked);
        UUTI.setValue(20);
    }
    
    /**
     * The "Armed" state subclasses Locked, so the unlocking should continue to
     * work.
     */
    @Test
    public void testArming() {
        UUTI.arm();
        assertTrue("lockedState is not instance of Armed", UUT.lockedState instanceof InitializedTestSM.Armed);
        
        UUTI.unlock();
        assertTrue("lockedState is not instance of Unlocked", UUT.lockedState instanceof InitializedTestSM.Unlocked);
        UUTI.setValue(20);
        
    }
    
    /**
     * Test that the methods are executing against the same instance that we
     * created.
     */
    @Test
    public void testSameInstance() {
        UUTI.unlock();
        UUTI.setValue(20);
        assertEquals("Value through local instance", 20, UUT.getValue());
    }
}
