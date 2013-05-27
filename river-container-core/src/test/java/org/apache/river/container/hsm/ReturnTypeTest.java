/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.river.container.hsm;

import java.lang.reflect.Method;
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
public class ReturnTypeTest {
    
    public ReturnTypeTest() {
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
    
    /**
     * Expectation is that if a method is declared to return 'void', then the
     * 'Method.getReturnType()' value should be null;
     */
    @Test
    public void testVoidReturnType() throws Exception {
        Method m=this.getClass().getMethod("testVoidReturnType", new Class[0]);
        assertEquals("return type wasn't void", void.class, m.getReturnType());
        
    }
}
