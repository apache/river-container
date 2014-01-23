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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.Permission;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jini.security.policy.DynamicPolicyProvider;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

/**
 *
 * @author trasukg
 */
public class Utils {

    public static String format(Object array[]) {
        if (array == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int j = 0; j < array.length; j++) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append("'");
            sb.append(array[j].toString());
            sb.append("'");
        }
        sb.append("]");

        return sb.toString();
    }

    public static String format(Properties props) {
        if (props == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (Map.Entry entry : props.entrySet()) {
            boolean first = true;
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append(entry.getKey() + "=\"");
            sb.append(entry.getValue());
            sb.append("\"");
        }
        sb.append("]");

        return sb.toString();
    }

    public static String[] splitOnWhitespace(String input) {
        List<String> strings = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(Strings.WHITESPACE_SEPARATORS);
        while (tok.hasMoreTokens()) {
            strings.add(tok.nextToken());
        }
        return (String[]) strings.toArray(new String[0]);
    }

    public static List<FileObject> findChildrenWithSuffix(FileObject dir, String suffix) throws FileSystemException {

        List<FileObject> ret = new ArrayList<FileObject>();

        for (FileObject fo : dir.getChildren()) {
            if (fo.getType() == FileType.FILE && fo.getName().getBaseName().endsWith(suffix)) {
                ret.add(fo);
            }
        }
        return ret;
    }

    public static void logClassLoaderHierarchy(Logger log,
            Class cls) {
        logClassLoaderHierarchy(log, Level.FINE, cls);
    }

    public static void logClassLoaderHierarchy(Logger log, Level level,
            Class cls) {
        log.log(level, MessageNames.CLASSLOADER_IS,
                new Object[]{cls.getName(), cls.getClassLoader()});
        try {
            ClassLoader parent = cls.getClassLoader().getParent();
            while (parent != null) {
                log.log(level, MessageNames.PARENT_CLASS_LOADER_IS,
                        new Object[]{parent});
                parent = parent.getParent();
            }
        } catch (Throwable t) {
            log.log(level, Strings.NEWLINE);
        }

    }

    public static void logClassLoaderHierarchy(Logger log,
            Level level,
            ClassLoader loader) {
        log.log(level, MessageNames.CLASSLOADER_IS,
                new Object[]{null, loader});
        ClassLoader parent = loader.getParent();
        while (parent != null) {
            log.log(level, MessageNames.PARENT_CLASS_LOADER_IS,
                    new Object[]{parent});
            try {
                parent = parent.getParent();
            } catch (Throwable t) {
                parent = null;
            }
        }
    }

    public static String stackTrace(Throwable t) {
        StringWriter s = new StringWriter();
        PrintWriter pw = new PrintWriter(s);
        t.printStackTrace(pw);
        return s.toString();
    }

    public static void logGrantsToClass(final Logger log, final Level level, final Class c) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                ClassLoader cl = c.getClassLoader();
                DynamicPolicyProvider dpp = (DynamicPolicyProvider) Policy.getPolicy();
                Permission[] perms = dpp.getGrants(c, null);
                log.log(level, MessageNames.GRANTS_TO_CLASS_ARE,
                        new Object[]{c.getName(), Utils.format(perms)});
                return null;
            }
        });
    }
}
