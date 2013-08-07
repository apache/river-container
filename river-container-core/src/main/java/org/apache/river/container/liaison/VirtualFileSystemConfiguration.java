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
package org.apache.river.container.liaison;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationFile;
import net.jini.config.ConfigurationNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;

/**
 *
 * @author trasukg
 */
public class VirtualFileSystemConfiguration
        implements Configuration {

    private static final Logger log=Logger.getLogger(VirtualFileSystemConfiguration.class.getName());
    
    private static FileObject rootDirectory = null;
    private static Map<String, Object> specialEntries =
            new HashMap<String, Object>();
    private Configuration delegate = null;

    /** Inject the working directory for this application  (which might actually
    be a jar file).  This injection is
    done using reflection by the ServiceStarterDeployer when the application
    is setup.  This way, the Configuration can be loaded without any hard-coded
    directories, etc.
    @param workingDirectory
     */
    public static void setWorkingDirectory(File workingDirectory) {
        /* Before we do anything, setup the class loader for the vfs manager.
         */
        
        setManagerClassLoader();
        try {
            if (workingDirectory.isDirectory()) {
                FileObject root = VFS.getManager().toFileObject(workingDirectory);
                VirtualFileSystemConfiguration.rootDirectory = root;
            } else { /* Try to create a virtual file system based on the file. */
                FileObject rootFileObject = VFS.getManager().toFileObject(workingDirectory);
                FileObject root = VFS.getManager().createFileSystem(Strings.JAR, rootFileObject);
                VirtualFileSystemConfiguration.rootDirectory = root;
            }
        } catch (FileSystemException ex) {
            /* Problem here is that we can't just throw the exception,
            because we expect to be called reflectively from code in a 
            different classloader, that won't have the exception class.
            So, we have to instead throw an exception that is part of the 
            jre platform.
             */
            log.log(Level.SEVERE, "Problem setting working directory", ex);
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }

    }

    private static void setManagerClassLoader() {
        try {
            Object mgr=VFS.getManager();
            Method setter=mgr.getClass().getMethod("setClassLoader", new Class[] {ClassLoader.class});
            setter.invoke(mgr, new Object[] {mgr.getClass().getClassLoader()});
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
    }
    
    public static FileObject getRootDirectory() {
        return rootDirectory;
    }

    /**
    Set the value of a 'Special Entry' as defined in ConfigurationFile, that
    can be accessed within the configuration by using the '$entryName'
    construct.
    @param name The name of the special entry, which must start with '$'.
    @param o The object to store.
     */
    public static void putSpecialEntry(String name, Object o) {
        specialEntries.put(name, o);
    }

    public VirtualFileSystemConfiguration(String[] options, ClassLoader cl) throws ConfigurationException {

        /* no options; just delegate. */
        if (options == null || options.length == 0) {
            delegate = new MyConfigurationFile(options, cl);
            return;
        }

        /* No file called for; just delegate. */
        if (Strings.DASH.equals(options[0])) {
            delegate = new MyConfigurationFile(options, cl);
            return;
        }

        /* Else, find the configuration file inside the working directory and
        open it.
        TODO: Should probably check to make sure that the supplied file
        name does not include absolute path or '..' path, i.e. make sure
        that the resolved file is actually a descendant of the working
        directory.
         */
        Reader reader = null;
        try {
            FileObject configFile = rootDirectory.resolveFile(options[0]);
            reader = new InputStreamReader(configFile.getContent().getInputStream());
            delegate = new MyConfigurationFile(reader, options, cl);
        } catch (FileSystemException ex) {
            throw new ConfigurationNotFoundException(options[0], ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    throw new ConfigurationException(Strings.ERROR_CLOSING_FILE, ex);
                }
            }
        }

    }

    public Object getEntry(String component, String name, Class type) throws ConfigurationException {
        return delegate.getEntry(component, name, type);
    }

    public Object getEntry(String component, String name, Class type, Object defaultValue) throws ConfigurationException {
        return delegate.getEntry(component, name, type, defaultValue);
    }

    public Object getEntry(String component, String name, Class type, Object defaultValue, Object data) throws ConfigurationException {
        return delegate.getEntry(component, name, type, defaultValue, data);
    }

    private static class MyConfigurationFile extends ConfigurationFile {

        public MyConfigurationFile(Reader reader, String[] overrides, ClassLoader cl) throws ConfigurationException {
            super(reader, overrides, cl);
        }

        public MyConfigurationFile(String[] options, ClassLoader cl) throws ConfigurationException {
            super(options, cl);
        }

        @Override
        protected Object getSpecialEntry(String name) throws ConfigurationException {
            if (specialEntries.containsKey(name)) {
                return specialEntries.get(name);
            }
            return super.getSpecialEntry(name);
        }

        @Override
        protected Class getSpecialEntryType(String name) throws ConfigurationException {
            if (specialEntries.containsKey(name)) {
                return specialEntries.get(name).getClass();
            }
            return super.getSpecialEntryType(name);
        }
    }
}
