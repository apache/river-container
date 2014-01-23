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
package org.apache.river.container.deployer;

import java.util.List;
import java.util.logging.Logger;
import java.io.InputStream;
import java.util.logging.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**

 @author trasukg
 */
public class DeployerConfigParserTest {

    private static final Logger log = Logger.getLogger(DeployerConfigParserTest.class.getName());

    public DeployerConfigParserTest() {
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

    @Test
    /**
     Ensure that the parsing basically happens; we can create the stream and run
     it through the parser without errors.
     */
    public void testBasicParsing() throws ParseException {
        log.setLevel(Level.FINE);
        ASTconfig config = parseTestConfig();
        log.fine("grants string is:" + config.toString());
        String expected = "config (grant (permission java.io.FilePermission \"${serviceArchive}\" \"read\") "
                + "(permission java.net.SocketPermission \"*\" \"connect\")) "
                + "(classloader (parent systemClassLoader) "
                + "appPriority "
                + "(jars (classpath (cpEntry commons-vfs-1.0.jar) "
                + "(cpEntry commons-logging-1.1.1.jar) (cpEntry jsk-platform.jar) "
                + "(cpEntry jsk-lib.jar) (cpEntry jsk-resources.jar) "
                + "(cpEntry RiverSurrogate.jar "
                + "org.apache.river.container.liaison.Strings "
                + "org.apache.river.container.liaison.VirtualFileSystemConfiguration "
                + "org.apache.river.container.liaison.VirtualFileSystemConfiguration$MyConfigurationFile "
                + "\"META-INF/services/*\"))) (codebase jsk-dl.jar)) (configuration "
                + "(configEntry discoveryGroup defaultDiscoveryGroup))";
        assertEquals(expected, config.toString());
    }

    private ASTconfig parseTestConfig() throws ParseException {
        InputStream in =
                DeployerConfigParserTest.class.getResourceAsStream("sample.config");
        assertTrue("No sample.config file found!", in != null);
        ASTconfig config = DeployerConfigParser.parseConfig(in);
        return config;
    }

    /**
     Matching the ASTConfig should return the root node.

     @throws Exception
     */
    @Test
    public void testPathMatch() throws Exception {
        ASTNode config = parseTestConfig();
        List<ASTNode> matches = config.search(new Class[]{ASTconfig.class});
        assertEquals("Length of match list", 1, matches.size());
        assertEquals("matched node", config, matches.get(0));
    }

    /**
     Matching the ASTConfig should return the root node.

     @throws Exception
     */
    @Test
    public void testlongerPathMatch() throws Exception {
        ASTNode config = parseTestConfig();
        List<ASTNode> matches = config.search(
                new Class[]{ASTconfig.class, ASTclassloader.class,
                    ASTjars.class});
        assertEquals("Length of match list", 1, matches.size());
        assertEquals("matched node class", ASTjars.class, matches.get(0).getClass());
    }

    /**
     Checking format and contents of the permission grants.
     */
    @Test
    public void testPermissionContents() throws Exception {
        ASTNode config = parseTestConfig();
        List<ASTNode> permNodes = config.search(
                new Class[]{ASTconfig.class, ASTgrant.class, ASTpermission.class});
        assertEquals("Number of permission nodes", 2, permNodes.size());
        ASTpermission firstNode = (ASTpermission) permNodes.get(0);
        assertEquals("permission java.io.FilePermission \"${serviceArchive}\" \"read\"", firstNode.toString());
        assertEquals("children of permission node", 3, firstNode.jjtGetNumChildren());
        assertEquals("Permission type for first node", "java.io.FilePermission",
                ((ASTsymbol) (firstNode.jjtGetChild(0))).getValue());
    }

    @Test
    public void testParentLoaderName() throws Exception {

        ASTNode configNode = parseTestConfig();
        String parentLoaderName = configNode.search(
                new Class[]{ASTconfig.class, ASTclassloader.class, ASTparent.class}).get(0).jjtGetChild(0).toString();
        assertEquals("parentLoaderName", "systemClassLoader", parentLoaderName);


    }

    @Test
    public void testCodebaseNode() throws Exception {
        ASTNode configNode = parseTestConfig();
        ASTcodebase codebaseNode = (ASTcodebase) configNode.search(new Class[]{
                    ASTconfig.class, ASTclassloader.class, ASTcodebase.class
                }).get(0);
        assertEquals("codebase callout", "jsk-dl.jar", codebaseNode.jjtGetChild(0).toString());
    }
}