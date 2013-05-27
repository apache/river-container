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
package org.apache.river.container;

import java.util.Properties;
import java.util.logging.Logger;
import java.lang.reflect.Member;
import java.util.List;
import java.util.logging.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test Case for AnnotatedClassDeployer.
 * @author trasukg
 */
public class AnnotatedClassDeployerTest {

    Logger log = Logger.getLogger(AnnotatedClassDeployerTest.class.getName());

    public AnnotatedClassDeployerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger.getLogger(AnnotatedClassDeployer.class.getName()).setLevel(Level.FINEST);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    Context context = new Context();
    AnnotatedClassDeployer UUT = new AnnotatedClassDeployer();

    @Before
    public void setUp() {
        UUT.setContext(context);
    }

    @After
    public void tearDown() {
    }

    /**
    Test whether a single injected value is set on a component.
     */
    @Test
    public void testReadsPrivateField() {
        InjectionHarness harness = new InjectionHarness();
        UUT.put(harness.getClass().getName(), harness);
        List<Member> members = UUT.buildMemberList(harness.getClass());
        assertTrue("No members found", members.size() > 0);
    }

    /**
    Test whether a single injected value is set on a component.
     */
    @Test
    public void testInjected() {
        InjectionHarness harness = new InjectionHarness();
        UUT.put(harness.getClass().getName(), harness);
        assertEquals("Context wasn't injected", context, harness.context);
    }

    /**
    Init method must be void return, and we should get an exception if it
    specifies a return value;
     */
    @Test(expected = ConfigurationException.class)
    public void testInitMethodWithNonVoidReturn() {
        Object harness = new InjectionHarnessWithNonVoidInit();
        UUT.put(harness.getClass().getName(), harness);
    }

    /**
    Init method must take no parameters and we should get an exception if it
    has parameters;
     */
    @Test(expected = ConfigurationException.class)
    public void testInitMethodWithParameters() {
        Object harness = new InjectionHarnessWithInitParameters();
        UUT.put(harness.getClass().getName(), harness);
    }

    /**
    Test whether a single injected value is set on a component.
     */
    @Test
    public void testInitCalled() {
        InjectionHarness harness = new InjectionHarness();
        UUT.put(harness.getClass().getName(), harness);
        assertTrue("Init method wasn't called", harness.initialized);
        assertTrue("Second init method wasn't called", harness.secondInitCalled);
        assertEquals("Init was called more than once:", 1, harness.initCount);
    }

    /**
    Test that if we have two items deployed, one will be injected into the
    other.
     */
    @Test
    public void testResourceInjected() {
        log.info("testResourceInjected()");
        InjectionHarness harness = new InjectionHarness();
        InjectionHarnessWithDependencies harness2 =
                new InjectionHarnessWithDependencies();
        UUT.put(harness.getClass().getName(), harness);
        UUT.put(harness2.getClass().getName(), harness2);
        assertEquals(harness, harness2.harness);
    }

    /**
    /**
    Test that if we have two items deployed, one will be injected into the
    other.
    
    This injection should occur even if an item is a subclass of the
    injected field's type.  In other words, if a field wants a java.util.List
    for instance, it should get a java.util.ArrayList injected if that's the
    only available object.
     */
    @Test
    public void testDerivedResourceInjected() {
        log.info("testResourceInjected()");
        InjectionHarness harness = new InjectionHarnessSubclass();
        InjectionHarnessWithDependencies harness2 =
                new InjectionHarnessWithDependencies();
        UUT.put(harness.getClass().getName(), harness);
        UUT.put(harness2.getClass().getName(), harness2);
        assertEquals(harness, harness2.harness);
    }

    /**
    Test that if we have two items deployed, one will be injected into the
    other.
     */
    @Test
    public void testResourceInjectedByName() {
        InjectionHarnessWithNamedDependencies harness2 =
                new InjectionHarnessWithNamedDependencies();
        UUT.put("this.is.a.name", "Bob");
        UUT.put(harness2.getClass().getName(), harness2);
        assertEquals("Bob", harness2.injectedVariable);
    }

    @Test
    /**
    If we don't specify an explicit name as the value of the @Injected
    annotation, then the injection should be tried based on the 'implied'
    name of the member, i.e. the field name or property name for a 'setXYZ'
    method.  Also we need to make sure that 'byType' injection isn't imposed
    in a silly way, for instance injecting a string to a field, even though
    there is a string available to the context under a different name.
     */
    public void testInjectByImpliedName() {
        InjectionHarnessWithDependencies h1 = new InjectionHarnessWithDependencies();
        UUT.put(h1.getClass().getName(), h1);
        UUT.put("name", "abc");
        assertEquals("abc", h1.name);
        /* Shouldn't inject a silly string! */
        assertEquals(null, h1.otherName);
    }

    @Test
    /**
    If we have a properties field that has a name on it, then it should not
    be subject to injection by type.
    */
    public void noDefaultInjectionByType() {
        PropertyInjectionHarness pih=new PropertyInjectionHarness();
        UUT.put(pih.getClass().getName(), pih);
        Properties p1=new Properties();
        UUT.put("wrong.properties", p1);
        assertTrue("Shouldn't have injected properties with the wrong name",
                pih.properties==null);
        Properties p2=new Properties();
        UUT.put("a.properties", p2);
        assertEquals(p2, pih.properties);
        
    }
    private class InjectionHarness {

        @Injected(style=InjectionStyle.BY_TYPE)
        Context context = null;
        boolean initialized = false;
        boolean secondInitCalled = false;
        int initCount = 0;

        @Init
        void init() {
            initialized = true;
            initCount++;
        }

        @Init
        void initAgain() {
            secondInitCalled = true;
        }
    }

    private class InjectionHarnessSubclass extends InjectionHarness {
    }

    private class InjectionHarnessWithNonVoidInit {

        @Init
        public int init() {
            return -1;
        }
    }

    private class InjectionHarnessWithInitParameters {

        @Init
        public void init(int j) {
        }
    }

    private class InjectionHarnessWithDependencies {

        @Injected(style=InjectionStyle.BY_TYPE)
        InjectionHarness harness = null;
        @Injected
        String name = null;
        @Injected
        String otherName = null;
    }

    private class InjectionHarnessWithNamedDependencies {

        @Injected("this.is.a.name")
        String injectedVariable = null;
    }
    
    private class PropertyInjectionHarness {
        @Injected("a.properties")
        Properties properties;
    }
}
