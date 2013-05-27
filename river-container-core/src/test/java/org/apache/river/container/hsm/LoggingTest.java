/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class LoggingTest {

    Logger log=Logger.getLogger(LoggingTest.class.getName());
    
    public LoggingTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger.getLogger("org.apache.river.container.hsm").setLevel(Level.FINER);
        System.setProperty("java.util.logging.ConsoleHandler.level", "FINER");
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
    
    @Test
    public void testLogging() {
        System.out.println("Should be seeing some logging...");
        
        
        log.log(Level.FINEST, "Finest");
        log.log(Level.FINER, "Finer");
        log.log(Level.FINE, "Fine");
        log.log(Level.INFO, "Info");
        log.log(Level.WARNING, "Warning");
        log.log(Level.SEVERE, "Severe");
    }
}
