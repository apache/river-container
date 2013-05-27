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

import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import java.lang.reflect.Field;
import java.util.logging.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author trasukg
 */
public class PlainMachineExecutorTest {

    private static final Logger log =
            Logger.getLogger(PlainMachineExecutorTest.class.getName());

    public PlainMachineExecutorTest() throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger.getLogger("org.apache.river.container.hsm").setLevel(Level.FINER);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    TestSMInterface UUT = (TestSMInterface) PlainStateMachineExecutor.createProxy(TestSM.class);

    @Test
    /**
     * Verify that the list of active states is correct at beginning.
     *
     */
    public void testActiveStates() {

        assertEquals(3, UUT.getActiveStates().size());
        assertTrue("activeStates should contain root state.",
                UUT.getActiveStates().contains(TestSM.class));
        assertTrue("activeStates should contain A.",
                UUT.getActiveStates().contains(TestSM.A.class));
        assertTrue("activeStates should contain A1.",
                UUT.getActiveStates().contains(TestSM.A.A1.class));
    }

    @Test
    /**
     * Test that the top-level (state-invariant) sayHelloConstant() method
     * returns the correct value. Verifies that the top-level proxy is
     * functioning correctly.
     */
    public void testStateMachine() throws InstantiationException, IllegalAccessException {
        assertEquals("Hello", UUT.sayConstantHello());
    }

    @Test
    /**
     * Test that the top-level (state-invariant) sayHelloConstant() method
     * returns the correct value. Verifies that the top-level proxy is
     * functioning correctly.
     */
    public void testNullReturn() throws InstantiationException, IllegalAccessException {
        assertEquals(null, UUT.returnNull());
    }

    @Test
    /**
     * <p> Verify that transitions from state A to B work, and that the
     * behaviour varies with state. </p>
     *
     * <p> First call to sayHello() should return "Hello", second call should
     * return "There". </p>
     *
     */
    public void testSimpleTransition() throws InstantiationException, IllegalAccessException {
        assertTrue("activeStates should contain A.",
                UUT.getActiveStates().contains(TestSM.A.class));
        log.info("\n\nCalling hello()\n\n");
        assertEquals("Hello", UUT.sayHello());
        log.info("\n\n...done\n\n");
        List<Class> activeStates = UUT.getActiveStates();
        assertTrue("activeStates should contain B after transition.",
                activeStates.contains(TestSM.B.class));
        log.info("TestSM.B appears to have been active.");
        assertFalse("activeStates should not contain A after transition.",
                activeStates.contains(TestSM.A.class));
        assertEquals("There", UUT.sayHello());
    }

    @Test
    /**
     * When we enter a state, the entry method should be called.
     */
    public void testEntryMethodExecution() {
        assertEquals(1, UUT.getAEntryCount());
        UUT.sayHello();
        assertEquals(1, UUT.getAExitCount());
    }

    @Test
    /**
     * When we transition to a state but we are already in that state, the
     * onEntry method should not be run.
     */
    public void testNullTransition() {
        UUT.nullTransition();
        UUT.nullTransition();
        UUT.nullTransition();
        assertEquals(1, UUT.getNullTransitionEntryCount());
    }

    @Test
    /**
     * Make sure that the gotoA() and gotoB() methods cause the appropriate
     * transitions.
     */
    public void testABTransitions() {
        UUT.gotoA();
        assertTrue("activeStates should contain A.",
                UUT.getActiveStates().contains(TestSM.A.class));
        assertFalse("activeStates should not contain B.",
                UUT.getActiveStates().contains(TestSM.B.class));
        UUT.gotoB();

        assertFalse("activeStates should not contain A.",
                UUT.getActiveStates().contains(TestSM.A.class));
        assertTrue("activeStates should contain B.",
                UUT.getActiveStates().contains(TestSM.B.class));

        UUT.gotoA();
        assertTrue("activeStates should contain A.",
                UUT.getActiveStates().contains(TestSM.A.class));
        assertFalse("activeStates should not contain B.",
                UUT.getActiveStates().contains(TestSM.B.class));
    }

    @Test
    /**
     * @Retained annotations should be respected, and states should be
     * initialized on entry if the
     * @Retained is not there.
     */
    public void testStateInitialization() {
        UUT.gotoA();
        assertTrue("activeStates should contain A.",
                UUT.getActiveStates().contains(TestSM.A.class));
        assertFalse("activeStates should not contain B.",
                UUT.getActiveStates().contains(TestSM.B.class));
        UUT.gotoB();

        assertFalse("activeStates should not contain A.",
                UUT.getActiveStates().contains(TestSM.A.class));
        assertTrue("activeStates should contain B.",
                UUT.getActiveStates().contains(TestSM.B.class));
        assertTrue("activeStates should contain B1.",
                UUT.getActiveStates().contains(TestSM.B1.class));

        UUT.moveSubstateOfB();
        assertTrue("activeStates should contain B2.",
                UUT.getActiveStates().contains(TestSM.B2.class));
        UUT.gotoA();
        /* the substate isn't marked @Retained, so should reset to initial
         on gotoB().
         */
        UUT.gotoB();
        assertTrue("activeStates should contain B1.",
                UUT.getActiveStates().contains(TestSM.B1.class));


    }

    @Test
    /**
     * <p> Verify that the second interface is on the proxy and effective. </p>
     *
     * <p> After call to doSecondInterfaceAction(), call to sayHello() should
     * return "HelloFromC". </p>
     *
     */
    public void testSecondInterface() throws InstantiationException, IllegalAccessException {
        log.info("\n\nCalling doSecondInterfaceAction()\n\n");
        TestSMSecondInterface UUT2=(TestSMSecondInterface) UUT;
        UUT2.doSecondInterfaceAction();
        log.info("\n\n...done\n\n");
        List<Class> activeStates = UUT.getActiveStates();
        assertTrue("activeStates should contain C after transition.",
                activeStates.contains(TestSM.C.class));
        log.info("TestSM.C appears to have been active.");
        assertFalse("activeStates should not contain A after transition.",
                activeStates.contains(TestSM.A.class));
        assertEquals("HelloFromC", UUT.sayHello());
    }
    
    
    /**
     * Calling an event method that isn't implemented in the current state
     * should throw an IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void testUnimplementedMethod() {
        UUT.unimplementedMethod();
    }
}