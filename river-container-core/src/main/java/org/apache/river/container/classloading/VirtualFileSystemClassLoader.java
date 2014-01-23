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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jini.security.Security;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileUtil;
import org.apache.river.container.LocalizedRuntimeException;
import org.apache.river.container.MessageNames;

/**
 *
 * @author trasukg
 */
public class VirtualFileSystemClassLoader extends URLClassLoader {

    private FileObject fileSystemRoot = null;
    private List<ClasspathEntry> classpathEntries = new ArrayList<ClasspathEntry>();
    private CodeSource codeSource = null;
    private boolean isAppPriority = false;

    public VirtualFileSystemClassLoader(FileObject fileSystemRoot, ClassLoader parent, CodeSource codeSource) {
        this(fileSystemRoot, parent, codeSource, false);
    }

    public VirtualFileSystemClassLoader(FileObject fileSystemRoot, ClassLoader parent, CodeSource codeSource, boolean isAppPriority) {
        super(new URL[0], parent);
        this.isAppPriority = isAppPriority;
        this.fileSystemRoot = fileSystemRoot;
        this.codeSource = codeSource;
    }

    public static String classToResourceName(String name) {
        String resourceName = name.replace(Strings.DOT, Strings.SLASH).concat(Strings.DOT_CLASS);
        return resourceName;
    }

    /**
     * Add the given classpath to this classloader, based on the default root
     * supplied at construction time.
     *
     * @param classPath
     */
    public void addClassPathEntry(String classPath) {

        addClassPathEntry(fileSystemRoot, classPath);
    }

    /**
     * Add the given classpath to this classloader, based on the given fileRoot.
     * The classpath can contain multiple entries, separated by colons, e.g.
     * 'jsk-platform.jar:jsk-lib.jar'.<br> Each entry can either be a jar file
     * or a jar file with a list of classes that the jar file can be used to
     * provide. For instance, 'surrogate.jar(org.apache.ABC, org.apache.DEF)'.
     *
     * @param fileRoot
     * @param classPath
     */
    public void addClassPathEntry(FileObject fileRoot, String classPath) {

        try {
            /*
             Classpath entry is a jar file with filter expressions that can be
             understood by ClasspathFilterBuilder.
             */
            /*
             Create a nested file system from it and add it to the file objects.
             */
            List<ClasspathFilter> filters = new ClasspathFilterBuilder().parseToFilters(classPath);
            addClasspathFilters(filters, fileRoot);
        } catch (FileSystemException ex) {
            throw new LocalizedRuntimeException(ex, MessageNames.BUNDLE_NAME, MessageNames.INVALID_CLASSPATH_ENTRY, classPath);
        }
    }

    public void addClasspathFilters(List<ClasspathFilter> filters, FileObject fileRoot) throws FileSystemException {
        for (ClasspathFilter filter : filters) {
            FileObject entryObject = fileRoot.resolveFile(filter.getJarName());

            FileObject entryFileSystem
                    = fileRoot.getFileSystem().getFileSystemManager().createFileSystem(entryObject);
            classpathEntries.add(new ClasspathEntry(filter, entryFileSystem));
        }
    }

    /**
     * Find a resource by searching through all the classpath entries that have
     * been set up.
     *
     * @param name
     * @return
     */
    @Override
    public URL findResource(final String name) {
        try {
            return (URL) Security.doPrivileged(new PrivilegedExceptionAction<URL>() {

                @Override
                public URL run() throws Exception {
                    FileObject fo = findResourceFileObject(name);
                    return fo == null ? null : fo.getURL();
                }
            });

        } catch (Exception ex) {
            Logger.getLogger(VirtualFileSystemClassLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {

        Enumeration result = (Enumeration) Security.doPrivileged(new PrivilegedAction<Enumeration>() {

            public Enumeration run() {
                List<URL> urlList = new ArrayList<URL>();
                try {

                    List<FileObject> foList = findResourceFileObjects(name);
                    for (FileObject fo : foList) {
                        urlList.add(fo.getURL());
                    }
                } catch (FileSystemException ex) {
                    Logger.getLogger(VirtualFileSystemClassLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
                return Collections.enumeration(urlList);
            }
        });
        return result;
    }

    /**
     * Find the file object for a resource by searching through all the
     * classpath entries that have been set up.
     *
     * @param name
     * @return
     */
    public FileObject findResourceFileObject(String name) {
        for (ClasspathEntry cpEntry : classpathEntries) {
            try {
                FileObject fo = cpEntry.resolveFile(name);
                if (fo != null && fo.isReadable()) {
                    return fo;
                }
            } catch (FileSystemException ex) {
                Logger.getLogger(VirtualFileSystemClassLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    /**
     * Find the all the file objects for a resource by searching through all the
     * classpath entries that have been set up.
     *
     * @param name
     * @return
     */
    public List<FileObject> findResourceFileObjects(String name) {
        List<FileObject> foList = new ArrayList<FileObject>();
        for (ClasspathEntry cpEntry : classpathEntries) {
            try {
                FileObject fo = cpEntry.resolveFile(name);
                if (fo != null && fo.isReadable()) {
                    foList.add(fo);
                }
            } catch (FileSystemException ex) {
                Logger.getLogger(VirtualFileSystemClassLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return foList;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            return (Class) Security.doPrivileged(new PrivilegedExceptionAction<Class>() {

                public Class run() throws ClassNotFoundException {
                    String resourceName = classToResourceName(name);
                    FileObject resourceFileObject = findResourceFileObject(resourceName);
                    if (resourceFileObject == null) {
                        throw new ClassNotFoundException(name + "(" + resourceName + ")");
                    }
                    try {
                        byte[] bytes = FileUtil.getContent(resourceFileObject);
                        return defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException ioe) {
                        throw new ClassNotFoundException(name, ioe);
                    }

                }
            });
        } catch (PrivilegedActionException ex) {
            throw (ClassNotFoundException) ex.getException();
        }
    }

    /**
     * Set the codebase URLs to an arbitrary list of URLs. These URLs form the
     * codebase annotation for classes loaded through this classloader. For the
     * sake of general paranoia, sets the codebase to a copy of the provided
     * array.
     *
     * @param codebase
     */
    public void setCodebase(URL[] codebase) {
        if (codebase == null || codebase.length == 0) {
            codebaseURLs = new URL[]{};
            return;
        }

        codebaseURLs = new URL[codebase.length];
        System.arraycopy(codebase, 0, codebaseURLs, 0, codebase.length);

    }

    /**
     * Get the list of URLs that are used for the codebase annotation. Note that
     * this list is not the actual classpath (that is in the superclass). The
     * codebase URLs are imposed to match whatever the Jini service wants to
     * expose as its codebase annotation.
     *
     * @return
     */
    @Override
    public URL[] getURLs() {
        return codebaseURLs;
    }
    /**
     * Stores the codebase that will be returned as the codebase annotation.
     *
     */
    private URL codebaseURLs[] = new URL[0];

    @Override
    public String toString() {
        StringBuffer listString = new StringBuffer();
        listString.append(format(classpathEntries));

        listString.append(", codebase [");
        URL[] urlArray = getURLs();
        for (int i = 0; i < urlArray.length; i++) {
            listString.append(" ");
            listString.append(urlArray[i]);
        }
        listString.append("]");
        return listString.toString();
    }

    public static String format(List<ClasspathEntry> items) {
        if (items == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        boolean first = true;
        for (Object o : items) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append("'");
            sb.append(o.toString());
            sb.append("'");
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Loads a class with the specified name.
     *
     * <p>
     * <code>VirtualFileSystemClassLoader</code> implements this method as
     * follows:
     *
     * <li>If <code>isAppPriority</code> is <code>true</code>, then this method
     * invokes {@link #findClass
     * findClass} with <code>name</code>. If <code>findClass</code> throws an
     * exception, then this method throws that exception. Otherwise, this method
     * returns the <code>Class</code> returned by <code>findClass</code>, and if
     * <code>resolve</code> is <code>true</code>,
     * {@link #resolveClass resolveClass} is invoked with the <code>Class</code>
     * before returning.
     *
     * <li>If <code>isAppPriority</code> is <code>false</code>, then this method
     * invokes the superclass implementation of {@link ClassLoader#loadClass(String,boolean)
     * loadClass} with <code>name</code> and <code>resolve</code> and returns
     * the result. If the superclass's <code>loadClass</code> throws an
     * exception, then this method throws that exception.
     *
     * </ul>
     *
     * @param name the binary name of the class to load
     *
     * @param resolve if <code>true</code>, then {@link #resolveClass
     * resolveClass} will be invoked with the loaded class before returning
     *
     * @return the loaded class
     *
     * @throws ClassNotFoundException if the class could not be found
     *
     */
    protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        // First, check if the class has already been loaded
	Class c = findLoadedClass(name);
        if (c!=null) return c;
        
        if (isAppPriority) {
            try {
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                return super.loadClass(name, resolve);
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        } else {
            return super.loadClass(name, resolve);
        }
    }

    /**
     * Gets a resource with the specified name.
     *
     * <p>
     * <code>VirtualFileSystemClassLoader</code> implements this method as
     * follows:
     * <li>If <code>isAppPriority</code> is <code>true</code>, then this method
     * invokes {@link
     * #findResource findResource} with <code>name</code> and returns the
     * result.
     *
     * <li>If <code>isAppPriority</code> is <code>false</code>, then this method
     * invokes the superclass implementation of
     * {@link ClassLoader#getResource getResource} with <code>name</code> and
     * returns the result.
     *
     * </ul>
     *
     * @param name the name of the resource to get
     *
     * @return a <code>URL</code> for the resource, or <code>null</code> if the
     * resource could not be found
     *
     */
    public URL getResource(String name) {
        return isAppPriority
                ? findResource(name) : super.getResource(name);
    }

}
