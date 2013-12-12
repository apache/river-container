/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
