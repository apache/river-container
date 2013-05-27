/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.river.container.hsm;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author trasukg
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({LoggingTest.class, ReturnTypeTest.class, InitializedMachineTest.class, StateMachineCompilerTest.class, PlainMachineExecutorTest.class})
public class HSMTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
