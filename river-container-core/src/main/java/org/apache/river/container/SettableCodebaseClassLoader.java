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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends URLClassLoader to allow alteration of the codebase annotation that
 * will be extracted from the class loader.  Essentially, allows you to use
 * one source for the actual classpath, and then have a marshalled object
 * return an arbitrary codebase.
 * @author trasukg
 */
public class SettableCodebaseClassLoader extends URLClassLoader {

    /** Stores the codebase that will be returned as the codebase annotation.
     *
     */
    private URL codebaseURLs[] = new URL[0];

    /**
     * Construct using a list of urls and the parent classloader.
     * @param urlList A list of URLs (which may point to directories or
     * jar files) to form the classpath.
     * @param extensionClassloader Class loader to use as parent of this
     * class loader.
     */
    SettableCodebaseClassLoader(URL urlList[], ClassLoader extensionClassloader) {
        super(urlList, extensionClassloader);
    }

    /**
     * Get the list of URLs that are used for the codebase annotation.
     * Note that this list is not the actual classpath (that is in the
     * superclass).  The codebase URLs are imposed to match whatever the Jini
     * service wants to expose as its codebase annotation.
     * @return
     */
    @Override
    public URL[] getURLs() {
        return codebaseURLs;
    }

    /** Add a URL to this classpath.
     */
    @Override
    public void addURL(URL url) {
        URL[] currentURLS = super.getURLs();
        for (int i = 0; i < currentURLS.length; i++) {
            if (url.equals(currentURLS[i])) {
                return;
            }
        }
        super.addURL(url);
    }

    /**
     * Set the codebase URLs to an arbitrary list of URLs.  These URLs form the
     * codebase annotation for classes loaded through this classloader.
     * For the sake of general paranoia, sets the codebase to a copy of the
     * provided array.
     * @param codebase
     */
    public void setCodebase(URL[] codebase) {
        if (codebase == null || codebase.length==0) {
            codebaseURLs = new URL[]{};
            return;
        }

        codebaseURLs = new URL[codebase.length];
        System.arraycopy(codebase, 0, codebaseURLs, 0, codebase.length);

    }

    static SettableCodebaseClassLoader createLoader(ClassLoader parent,
            File commonDirectory)
            throws MalformedURLException, IOException {
        List urlList = new ArrayList();

        if (!commonDirectory.isDirectory()) {
            /* There's no common directory, so we'll just use an empty url list
            to create the classloader.
             */
        } else {
            /* Create based around the unpacked directory.
             * TODO: Maybe later;  seems to me this would make a decent idea,
             * but the Surrogate spec specifically disallows the use of the
             * codebase attribute, and seems to require that all classes be
             * in the root of the surrogate package jar file.


            urlList.add(commonDirectory.toURI().toURL());

            /* Add all jar files in the directory. * /
            FileFilter filter = new JarFilter();
            File[] jars = commonDirectory.listFiles(filter);
            for (int i = 0; i < jars.length; i++) {
                urlList.add(jars[i].toURI().toURL());
            }
            */

        }
        URL[] urlArray = new URL[urlList.size()];
        for (int i = 0; i < urlArray.length; i++) {
            urlArray[i] = (URL) urlList.get(i);
        }
        SettableCodebaseClassLoader loader = null;

        loader = new SettableCodebaseClassLoader(urlArray, parent);
        return loader;
    }

    @Override
    public String toString() {
        StringBuffer listString = new StringBuffer();
        listString.append(getClass().getName() + " [");
        URL[] urlArray = super.getURLs();
        for (int i = 0; i < urlArray.length; i++) {
            listString.append(" ");
            listString.append(urlArray[i]);
        }
        listString.append("], codebase [");
        urlArray = getURLs();
        for (int i = 0; i < urlArray.length; i++) {
            listString.append(" ");
            listString.append(urlArray[i]);
        }
        listString.append("]");
        return listString.toString();
    }

}
