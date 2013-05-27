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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test the State Machine Compiler
 *
 */
public class StateMachineCompilerTest {

    public StateMachineCompilerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    StateMachineCompiler compiler = new StateMachineCompiler();

    @Test
    public void testCompileReturnsMetaState() throws Exception {
        MetaState stateMachine = compiler.compile(TestSM.class);
        assertTrue(stateMachine + " isn't a MetaState", stateMachine instanceof MetaState);
    }

    @Test
    public void testSampleActiveStates() throws Exception {
        MetaState stateMachine = compiler.compile(TestSM.class);
        List<Class> activeStates = stateMachine.getActiveStates();
        checkContains(activeStates, TestSM.class);
        checkContains(activeStates, TestSM.A.class);
        checkContains(activeStates, TestSM.A.A1.class);
    }

    /**
     * MetaState for TestSM should have event methods for sayHello and
     * nullTransition, but nothing else.
     *
     * @throws Exception
     */
    @Test
    public void testEventMethods() throws Exception {
        MetaState stateMachine = compiler.compile(TestSM.class);
        Collection<Method> methods = stateMachine.eventMethods.keySet();
        Method sayHello = TestSMInterface.class.getMethod("sayHello");
        assertNotNull("Didn't find sayHello() method in interface", sayHello);
        checkContains(methods, sayHello);
    }

    /**
     * A method annotated with
     *
     * @Guard should be reflected by a guarded transition operation.
     * @throws Exception
     */
    @Test
    public void testGuardMethod() throws Exception {
        MetaState rootState = compiler.compile(TestSM.class);
        assertEquals("Number of guard methods on root metastate", 1, rootState.guardMethods.size());

    }

    private void checkContains(Collection<?> collection, Object requiredObject) {
        assertTrue(collection + " doesn't include " + requiredObject, collection.contains(requiredObject));
    }

    /**
     * A method annotated with
     *
     * @Entry should be reflected by an invoke operation in the metastate.
     * @throws Exception
     */
    @Test
    public void testEntryMethod() throws Exception {
        MetaState rootState = compiler.compile(TestSM.class);
        MetaState metaStateA = findMetaState(rootState, TestSM.A.class);
        assertEquals("Count of onEntry methods for A", 1, metaStateA.entryMethods.size());
    }

    /**
     * A method annotated with
     *
     * @Entry should be reflected by an invoke operation in the metastate.
     * @throws Exception
     */
    @Test
    public void testExitMethod() throws Exception {
        MetaState rootState = compiler.compile(TestSM.class);
        MetaState metaStateA = findMetaState(rootState, TestSM.A.class);
        assertEquals("Count of onExit methods for A", 1, metaStateA.exitMethods.size());
    }

    MetaState findMetaState(MetaState metaState, Class stateClass) {
        for (SubstateInfo ssi : metaState.substates) {
            for (MetaState ms : ssi.getPossibleMetaStates()) {
                if (ms.stateClass == stateClass) {
                    return ms;
                }
            }
        }
        return null;
    }

    @Test
    public void testStructure() throws Exception {
        MetaState rootState = compiler.compile(TestSM.class);
        String expectedStructure = "TestSM(state(A(state(A1 ) B(state(B1 B2 B3 ) ) ";
        String actualStructure = rootState.getStateStructure();
    }
}
