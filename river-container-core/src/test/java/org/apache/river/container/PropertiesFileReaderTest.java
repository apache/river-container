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
package org.apache.river.container;

import java.util.Properties;
import java.io.File;
import org.apache.commons.vfs2.FileSystemManager;
import java.io.IOException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
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
public class PropertiesFileReaderTest {

    public PropertiesFileReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    PropertiesFileReader UUT = new PropertiesFileReader();
    Context context = new Context();
    FileUtility fileUtility = new MockFileUtility();

    @Before
    public void setUp() {
        context.put(new AnnotatedClassDeployer());
        context.put(Strings.FILE_UTILITY, fileUtility);
        context.put(UUT);
    }

    @After
    public void tearDown() {
    }

    /** 
    Check that the MockFileUtility returns the profile directory as the
    'src/test/files' dir.
    @throws Exception 
    */
    @Test
    public void testMockFileUtility() throws Exception {
        FileObject fo=fileUtility.getProfileDirectory();
        
        System.out.println("fo=" + fo);
            assertTrue(fo.toString().endsWith("files"));
    }
    
    /**
    When setup in the context, the config file reader should scan the profile
    directory for files ending in ".properties", and create a Properties object
    based on each file.  As such, we should be able to just read the properties
    from the context.
    */
    @Test 
    public void testReader() {
        Properties testProps=(Properties) 
                context.get("property-file-reader-test.properties");
        assertNotNull("property-file-reader-test.properties wasn't loaded.",
                testProps);
        
        assertEquals("Expected message=Hello World", "Hello World", 
                testProps.getProperty("message"));
    }
    
    @Test
    public void testPropertiesInjection() throws Exception {
        AnnotatedTestHarness harness=new AnnotatedTestHarness();
        context.put(harness);
        
        assertNotNull("property-file-reader-test.properties wasn't loaded.",
                harness.props);
        
        assertEquals("Expected message=Hello World", "Hello World", 
                harness.props.getProperty("message"));
        
    }
    
    private class AnnotatedTestHarness {
        
        @Injected("property-file-reader-test.properties")
        Properties props=null;
        
    }
    
    private class MockFileUtility implements FileUtility {

        @Override
        public FileObject getWorkingDirectory(String name) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FileObject getProfileDirectory() throws IOException {
            FileSystemManager fileSystemManager=VFS.getManager();
            FileObject fo = fileSystemManager.resolveFile(new File("src/test/files"), ".");
            
            return fo;
        }

        @Override
        public FileObject getLibDirectory() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
