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

import java.util.List;
import org.junit.Ignore;
import java.io.File;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
Exploratory tests to understand and document the behavior of Commons-VFS
 * @author trasukg
 */
public class CommonsVFSTest {

    FileSystemManager fileSystemManager = null;

    public CommonsVFSTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws FileSystemException {
        fileSystemManager = VFS.getManager();

    }

    @After
    public void tearDown() {
    }

    /**
    Should be able to get the current directory, and it should end with
    'testfiles/testroot'.
    @throws Exception
     */
    @Test
    public void testBaseFile() throws Exception {
        FileObject fo = fileSystemManager.resolveFile(new File("."), ".");
        System.out.println("fo=" + fo);
        assertTrue(fo.toString().endsWith("river-container-core"));
    }

    /**
    Should be able to go to a directory and ask for files that end with ".jar".
    @throws Exception
     */
    @Test
    public void testSuffixSelector() throws Exception {
        FileObject fo = fileSystemManager.resolveFile(new File("."), "target/reggie-module");
        System.out.println("fo=" + fo);
        assertTrue(fo.toString().endsWith("target/reggie-module"));
        List<FileObject> jars=Utils.findChildrenWithSuffix(fo, Strings.DOT_JAR);
        assertTrue("Didn't get any jar files.", jars.size()>0);
    }

    /**
    Make sure we can use the jar:syntax to get to the 'start.properties' file
    inside the constructed reggie module jar.
     */
    @Test
    public void testFileInReggieModuleJar() throws Exception {
        FileObject reggieJar =
                fileSystemManager.resolveFile(new File("target/reggie-module"), "reggie-module.jar");
        assertTrue("Bad file:" + reggieJar.toString(), reggieJar.toString().endsWith("reggie-module.jar"));
        FileObject reggieJarFS = fileSystemManager.createFileSystem(Strings.JAR, reggieJar);

        FileObject startProperties = reggieJarFS.resolveFile("start.properties");
        assertNotNull(startProperties);
        assertTrue("Properties file unreadable:" + startProperties.toString() + " type=" + startProperties.getType(), startProperties.isReadable());
    }

    /**
    If we create a virtual file system based on a jar file, we should be
    able to add other jar files by adding junctions to the root, with the name
    of the file we're adding.
    
        
    Unfortunately, this theory doesn't pan out...
    org.apache.commons.vfs.FileSystemException: Attempting to create a nested junction at "null/otherStart.properties".  Nested junctions are not supported.
        at org.apache.commons.vfs.impl.VirtualFileSystem.addJunction(VirtualFileSystem.java:111)
    
     */
    @Test @Ignore /*Didin't work, see above */
    public void testFileSystemJunctions() throws Exception {
        FileObject reggieJar =
                fileSystemManager.resolveFile(new File("../../build/test/files"), "reggie-module.jar");
        assertTrue("Bad file:" + reggieJar.toString(), reggieJar.toString().endsWith("reggie-module.jar"));
        FileObject reggieJarFS = fileSystemManager.createFileSystem(reggieJar);

        FileObject virtRoot = fileSystemManager.createVirtualFileSystem((String) null);
        virtRoot.getFileSystem().addJunction("/", reggieJarFS);
        checkPresentAndReadable(virtRoot, "start.properties");
        FileObject startProperties = virtRoot.resolveFile("start.properties");
        assertNotNull(startProperties);
        assertTrue("Properties file unreadable:" + startProperties.toString() + " type=" + startProperties.getType(), startProperties.isReadable());
        
        /* Now try to add in a junction to a jar file */
        virtRoot.getFileSystem().addJunction("otherStart.properties", startProperties);
        checkPresentAndReadable(virtRoot, "otherStart.properties");
    }
    
    void checkPresentAndReadable(FileObject root, String name) throws FileSystemException {
        FileObject fo = root.resolveFile(name);
        assertNotNull(fo);
        assertTrue("File unreadable:" + fo.toString() + " type=" + fo.getType(), fo.isReadable());
    }
}