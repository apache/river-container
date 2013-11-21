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
 */package org.apache.river.container.classloading;

import org.apache.river.container.classloading.VirtualFileSystemClassLoader;
import java.net.URL;
import java.io.InputStream;
import java.io.File;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.river.container.Bootstrap;
import org.apache.river.container.LocalizedRuntimeException;
import org.apache.river.container.MessageNames;
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
public class VFSClassLoaderTest {

    private static String JSK_VERSION="2.2.2";
    
    FileSystemManager fileSystemManager = null;
    FileObject reggieModuleRoot = null;
    FileObject libRoot=null;
    ClassLoader extensionLoader = Bootstrap.class.getClassLoader().getParent();

    public VFSClassLoaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        fileSystemManager = VFS.getManager();
        FileObject currentDir = fileSystemManager.toFileObject(new File("."));
        FileObject reggieModuleJar =
                currentDir.resolveFile("target/reggie-module/reggie-module.jar");
        reggieModuleRoot = fileSystemManager.createFileSystem(Strings.JAR,
                reggieModuleJar);
        libRoot=reggieModuleRoot.resolveFile(Strings.LIB);
    }

    @After
    public void tearDown() {
    }

    /**
    Just to make sure that we have the base setup correct, ensure that we
    can read the 'start.properties' file inside the reggie-module jar.
    @throws Exception
     */
    @Test
    public void testCanReadStartDotProperties() throws Exception {
        FileObject startProperties = reggieModuleRoot.resolveFile("start.properties");
        assertNotNull(startProperties);
        assertTrue("Properties file unreadable:"
                + startProperties.toString() + " type=" + startProperties.getType(),
                startProperties.isReadable());
    }

    /**
    Create a VFSClassLoader and make sure it throws an exception if we try
    to add a non-existent jar file to it.
    Also, test out the addClassPathEntry(root, fileName) method.
    @throws Exception
     */
    @Test
    public void testNonExistentJarFile() throws Exception {
        VirtualFileSystemClassLoader UUT =
                new VirtualFileSystemClassLoader(null, extensionLoader, null);
        try {
            UUT.addClassPathEntry(libRoot, "nonexistent.jar");
            fail("Should have thrown an invalid classpath entry exception");
        } catch (LocalizedRuntimeException ex) {
            assertEquals(MessageNames.INVALID_CLASSPATH_ENTRY, ex.getMessageKey());
        }
    }

    /**
    Create a VFSClassLoader and see if we can read a resource file from it.
    As shown below, we're just adding a classpath entry with no filters or
    codebase.
    @throws Exception
     */
    @Test
    public void testClassLoaderResourceLoading() throws Exception {
        VirtualFileSystemClassLoader UUT =
                new VirtualFileSystemClassLoader(libRoot, extensionLoader, null);
        UUT.addClassPathEntry("reggie-" + JSK_VERSION + ".jar");
        InputStream is = UUT.getResourceAsStream("META-INF/PREFERRED.LIST");
        assertNotNull("Failed to get resource stream for META-INF/PREFERRED.LIST",
                is);
    }

    /* Note to self- test for exception cases - bad fsroot, directory not jar, etc.
     */

    /**
    The classloader should be able to load a class that's in the jar file,
    and when we get an instance of that class, it should have the UUT
    as its classloader.
    @throws Exception
     */
    @Test
    public void testClassLoading() throws Exception {
        VirtualFileSystemClassLoader UUT =
                new VirtualFileSystemClassLoader(libRoot, extensionLoader, null);
        UUT.addClassPathEntry("reggie-" + JSK_VERSION + ".jar");
        Class c = UUT.loadClass("com.sun.jini.reggie.ClassMapper");
        assertNotNull(c);
        assertTrue("Class had wrong classloader:" + c.getClassLoader(),
                c.getClassLoader()==UUT);
    }

    /**
    The classloader should be able to load a class that's in the jar file,
    and when we get an instance of that class, it should have the UUT
    as its classloader.
    @throws Exception
     */
    @Test
    public void testParentClassLoading() throws Exception {
        VirtualFileSystemClassLoader UUT =
                new VirtualFileSystemClassLoader(libRoot, extensionLoader, null);
        UUT.addClassPathEntry("reggie-" + JSK_VERSION + ".jar");
        Class c = UUT.loadClass("java.util.List");
        assertNotNull(c);
        assertTrue("Class had wrong classloader:" + c.getClassLoader(),
                c.getClassLoader()==null);
        assertTrue("java.util.List".equals(c.getName()));
    }

    @Test
    public void testCodebaseAnnotation() throws Exception {
        VirtualFileSystemClassLoader UUT =
                new VirtualFileSystemClassLoader(libRoot, extensionLoader, null);
        UUT.addClassPathEntry("reggie-" + JSK_VERSION + ".jar");
        /* At this point, there should be no urls on the reported codebase. */
        URL[] actual=UUT.getURLs();
        assertTrue("Should be no urls, but got " + Utils.format(actual),
                actual.length==0);
        URL[] a={ new URL("http://localhost:8080/a.jar")};
        UUT.setCodebase(a);
        actual=UUT.getURLs();
        assertEquals("Should be one urls, but got " + Utils.format(actual),
                1, actual.length);

    }

    /**
     We can setup filtered classloading, such that the classloader only
     supplies classes that match a particular pattern for a given jar.
     This facility prevents having to create a "subset" jar for cases where
     we want to have only a few classes loaded by a child class loader.
     In particular, this is to allow the container liaison classes to be
     resident in the application's (surrogate's) classloader even though the
     classes are included in the source tree of the main project (hence in
     RiverSurrogate.jar).
     @throws Exception
     */
    @Test
    public void testFilteredClassLoading() throws Exception {
        VirtualFileSystemClassLoader UUT =
                new VirtualFileSystemClassLoader(libRoot, extensionLoader, null);
        UUT.addClassPathEntry("reggie-" + JSK_VERSION + ".jar(com.sun.jini.reggie.ClassMapper)");
        /* We should now be able to load the ClassMapper class, but nothing
        else.
        */
        Class classMapperClass=UUT.loadClass("com.sun.jini.reggie.ClassMapper");
        assertNotNull("loaded class was null", classMapperClass);

        try {

            Class eventLeaseClass=UUT.loadClass("com.sun.jini.reggie.EventLease");
            assertNull("loaded class was null", eventLeaseClass);
            fail("Really shouldn't have gotten to here!");
        } catch(Exception ex) {
            
        }
    }
}
