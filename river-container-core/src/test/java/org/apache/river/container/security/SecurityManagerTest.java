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
package org.apache.river.container.security;

import java.io.IOException;
import java.io.InputStream;
import org.junit.*;

/**
 * 
 * In order to test the security infrastructure, we need to setup a secure
 * environment and then see if certain activities are denied.
 * 
 * How to do this?
 * 
 * One option would be to setup a plain application in a "secured" environment
 * and have that application try to check access for a permission it doesn't
 * have.
 * 
 * Another option is to run a JUnit test inside the "secured" environment, and
 * verify that the allowed operation passes and the disallowed operation fails.
 * 
 * So... we'll need some infrastructure to run JUnit test suites inside the
 * container.
 * 
 * @author trasukg
 */
public class SecurityManagerTest {
    
    public SecurityManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
    
    // Not ready for prime time yet.
    //@Test
    public void testSecuritySetup() throws IOException {
        /* Design by Magic... */
        /* Start the container. */
        Process p=Runtime.getRuntime().exec("Run the container");
        InputStream pOut=p.getInputStream();
        waitForContainerStartupMessage(pOut);
        /* Deploy the test app to the container.  Test app starts up and runs the
         * JUnit tests.
         */
        /* Confirm that the JUnit test was run.  If at all possible, just get the
         * Result of the JUnit tests.
         */
        /* Undeploy the test app. */
        /* Shutdown the container. */
        p.destroy();
    }

    private void waitForContainerStartupMessage(InputStream pOut) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
