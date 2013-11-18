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
package org.apache.river.container.el;

import org.apache.river.container.Utils;
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
public class ArgParserTest {

    public ArgParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    ArgsParserImpl UUT = new ArgsParserImpl();

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSimpleLine() {
        String input = "A B C";
        String[] expected = {"A", "B", "C"};

        String[] actual = UUT.toArgs(input, new String[0]);

        checkStringArray(expected, actual);
    }

    /**
     * The arg parser should be called with a string array of input arguments
     * as well as a "command line".  Where the command line includes "$*",
     * the input arguments should be added.
     */
    @Test
    public void testArgsSubstitution() {
        String input ="A B $*";
        String[] args = {"C", "D"};
        String[] expected={"A", "B", "C", "D"};
        
        String[] actual=UUT.toArgs(input, args);
        checkStringArray(expected, actual);
    }
    
    private void checkStringArray(String[] expected, String[] actual) {
        boolean fail = false;
        if (actual.length != expected.length) {
            fail = true;
        }

        for (int i = 0; fail == false && i < expected.length; i++) {
            if (expected[i] == null) {
                fail = actual[i] == null;
                continue;
            }
            fail = !expected[i].equals(actual[i]);
        }
        if (fail) {
            fail("Expected " + Utils.format(expected) + ", got " + Utils.format(actual));
            
        }
    }
}
