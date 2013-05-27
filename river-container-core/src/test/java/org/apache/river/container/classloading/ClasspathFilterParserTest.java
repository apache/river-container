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
package org.apache.river.container.classloading;

import java.util.logging.Logger;
import java.util.List;
import java.util.logging.Level;
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
public class ClasspathFilterParserTest {

    public ClasspathFilterParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger.getLogger(ClasspathFilterBuilder.class.getName()).setLevel(Level.ALL);
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

    /**
    Does basic test on parsing of the jar specification.
    Syntax is name.jar(classToServe).
    @throws Exception
     */
    @Test
    public void testParser() throws Exception {
        ClasspathFilterBuilder UUT = new ClasspathFilterBuilder();
        List<ClasspathFilter> cpfs = UUT.parseToFilters("reggie.jar(org.apache.Abc)");
        ClasspathFilter cpf=cpfs.get(0);
        assertEquals("reggie.jar", cpf.getJarName());
        List<Acceptor> actual = cpf.getAcceptors();
        assertEquals("Wrong number of filter clauses.", 1, actual.size());
        assertEquals("Filter condition", new ResourceAcceptor("org/apache/Abc.class"), actual.get(0));
    }

    /**
    Does basic test on parsing of the jar specification.
    Syntax is name.jar(classToServe).
    @throws Exception
     */
    @Test
    public void testParserOnMultipleClasses() throws Exception {
        ClasspathFilterBuilder UUT = new ClasspathFilterBuilder();
        String jarSpec = "reggie.jar(org.apache.ABC, org.apache.DEF)";
        List<ClasspathFilter> cpfs = UUT.parseToFilters(jarSpec);
        ClasspathFilter cpf=cpfs.get(0);
        assertEquals("reggie.jar", cpf.getJarName());
        List<Acceptor> actual = cpf.getAcceptors();
        assertEquals("Wrong number of filter clauses.", 2, actual.size());
        assertEquals("Filter condition", new ResourceAcceptor("org/apache/ABC.class"), actual.get(0));
        assertEquals("Filter condition", new ResourceAcceptor("org/apache/DEF.class"), actual.get(1));
    }

    /**
    Does basic test on parsing of the jar specification.
    Syntax is name.jar(classToServe[,filter]*).
     If the filter clause is a double-quoted string, then it represents
     a resource filter rather than a class filter (i.e. it specifies an actual
     resource in the jar file rather than a class description.
    @throws Exception
     */
    @Test
    public void testFilterAcceptance() throws Exception {
        ClasspathFilterBuilder UUT = new ClasspathFilterBuilder();
        String jarSpec = "reggie.jar(org.apache.ABC, org.apache.DEF, \"META-INF/*\")";
        List<ClasspathFilter> cpfs = UUT.parseToFilters(jarSpec);
        ClasspathFilter cpf=cpfs.get(0);
        assertEquals("reggie.jar", cpf.getJarName());
        assertTrue(cpf.acceptsResource("org/apache/ABC.class"));
        assertFalse(cpf.acceptsResource("org/apache/XYZ.class"));
        assertTrue(cpf.acceptsResource("org/apache/DEF.class"));
        assertTrue(cpf.acceptsResource("META-INF/start.properties"));
    }

    /**
    If there's no class specifications, should accept all class possibilities.
    @throws Exception
     */
    @Test
    public void testFilterAcceptanceWithJarOnly() throws Exception {
        ClasspathFilterBuilder UUT = new ClasspathFilterBuilder();
        String jarSpec = "reggie.jar";
        List<ClasspathFilter> cpfs = UUT.parseToFilters(jarSpec);
        ClasspathFilter cpf=cpfs.get(0);
        assertEquals("reggie.jar", cpf.getJarName());
        assertTrue(cpf.acceptsResource("org/apache/ABC.class"));
        assertTrue(cpf.acceptsResource("org/apache/XYZ.class"));
        assertTrue(cpf.acceptsResource("org/apache/DEF.class"));
     }
}
