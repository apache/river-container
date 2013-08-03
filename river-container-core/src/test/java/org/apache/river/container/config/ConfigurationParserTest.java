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

package org.apache.river.container.config;

import java.io.File;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author trasukg
 */
public class ConfigurationParserTest {

    public ConfigurationParserTest() throws Exception {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws JAXBException, SAXException {
        ctx = JAXBContext.newInstance("org.apache.river.container.config");
        um = ctx.createUnmarshaller();
        SchemaFactory sf=SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source source=new StreamSource(new File("src/main/xsd/config.xsd"));
        Schema schema=sf.newSchema(source);
        um.setSchema(schema);
    }

    @After
    public void tearDown() {
    }

    JAXBContext ctx = null;
    Unmarshaller um = null;

    @Test
    public void testConfigFileRead() throws Exception {

        InputStream is = getClass().getResourceAsStream("config-test-doc.xml");
        ContainerConfig containerConfig = (ContainerConfig) um.unmarshal(is);

        assertEquals("lib/abc.jar", containerConfig.getClasspath().get(0).getValue());
        assertEquals("lib/def.jar", containerConfig.getClasspath().get(1).getValue());
        assertEquals("a", containerConfig.getClasspath().get(1).getParent());
        assertEquals(3, containerConfig.getElements().size());
    }

    @Test
    public void testInvalidConfigFileRead() {
        InputStream is = getClass().getResourceAsStream("config-test-bad-doc.xml");
        try {
            ContainerConfig containerConfig = (ContainerConfig) um.unmarshal(is);
            fail("Should have gotten a validation error.");
        } catch (JAXBException ex) {
            // Got the exception; all is good.
        }
   }
}
