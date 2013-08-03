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

package org.apache.river.surrogate;

import java.io.File;
import org.apache.river.container.deployer.ApplicationEnvironment;
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
public class SurrogateContextTest {

    ApplicationEnvironment UUT=null;

    public SurrogateContextTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        UUT=new ApplicationEnvironment();
    }

    @After
    public void tearDown() {
    }

    //Not ready for prime time.
    //@Test
    /**
     Make sure that the jar containing our test surrogate is actually there.
     It should be built by the "-post-compile-test" target in 'build.xml'.
     */
    public void testThatTestJarIsPresent() {
        System.out.println("Working dir is " + new File(".").getAbsolutePath());
        File testJar=new File("../../build/test/files/sample-surrogate.jar");
        assertTrue("No test jar present", testJar.exists());
    }
}