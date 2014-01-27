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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.river.container.config.*;
import org.xml.sax.SAXException;

/**
 * Bootstrap loader for the container. Performs roughly the following: <ul>
 * <li>Based on the configuration parameter fed in at the command line,
 * determine the configuration directory and the config file.</li> <li>Read the
 * configuration file</li> <li>Based on the classpath declared in the config
 * file, create the container's classloader.</li> <li>Using that classloader,
 * create the context.</li> <li>Load any command-line parameters into the
 * context</li>
 * <li>Create all the elements (beans, discovery sets, etc) that are called out
 * in the config file and put them into the context. This will cause those beans
 * to setup and initialize themselves.</li> </li>
 *
 * @author trasukg
 */
public class Bootstrap {

    private static final Logger log
            = Logger.getLogger(Bootstrap.class.getName(), MessageNames.BUNDLE_NAME);

    public static void main(String args[]) {
        try {
            initializeContainer(args);
        } catch (InvocationTargetException ex) {
            log.log(Level.SEVERE, MessageNames.INITIALIZATION_EXCEPTION, ex.getCause());
            ex.printStackTrace();
            System.exit(-1);
        } catch (Exception ex) {
            log.log(Level.SEVERE, MessageNames.INITIALIZATION_EXCEPTION, ex);
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    static Unmarshaller createConfigUnmarshaller() throws SAXException, JAXBException {
        JAXBContext ctx = JAXBContext.newInstance("org.apache.river.container.config");
        Unmarshaller um = ctx.createUnmarshaller();
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source source = new StreamSource(Bootstrap.class.getResourceAsStream("/schemas/config.xsd"));
        Schema schema = sf.newSchema(source);
        um.setSchema(schema);
        return um;
    }

    static URL[] findClasspathURLS(String classpathStr) throws MalformedURLException {
        StringTokenizer tok = new StringTokenizer(classpathStr, Strings.WHITESPACE_SEPARATORS);
        List<URL> pathElements = new ArrayList<URL>();
        while (tok.hasMoreTokens()) {
            File f = new File(tok.nextToken());
            pathElements.add(f.toURI().toURL());
        }
        URL[] urls = (URL[]) pathElements.toArray(new URL[0]);
        return urls;
    }

    private static Map<String, ClassLoader> createClassLoaders(ContainerConfig config) throws MalformedURLException {

        Map<String, ClassLoader> classLoaders = new HashMap<String, ClassLoader>();
        classLoaders.put(Strings.BOOTSTRAP_CLASS_LOADER, Bootstrap.class.getClassLoader());
        /*
         Setup the classloaders according to the config file.
         */
        List<String> seen = new LinkedList<String>();
        Map<String, Classpath> classpaths = new HashMap<String, Classpath>();
        for (Classpath classpath : config.getClasspath()) {
            if (classpaths.containsKey(classpath.getId())) {
                throw new ConfigurationException(MessageNames.DUPLICATE_CLASSPATH, classpath.getId());
            }
            classpaths.put(classpath.getId(), classpath);
        }

        for (String id : classpaths.keySet()) {
            resolveClassLoader(classLoaders, seen, classpaths, id);
        }
        return classLoaders;
    }

    private static ClassLoader resolveClassLoader(Map<String, ClassLoader> classLoaders,
            List<String> seen,
            Map<String, Classpath> classpaths,
            String id) throws MalformedURLException {
        if (classLoaders.containsKey(id)) {
            return classLoaders.get(id);
        }
        if (seen.contains(id)) {
            throw new ConfigurationException(MessageNames.CIRCULAR_CLASSPATH, id);
        }
        // Add the id to the list of classloaders we have attempted to build.
        seen.add(id);
        Classpath classpath = classpaths.get(id);
        if (classpath == null) {
            throw new ConfigurationException(MessageNames.CLASSPATH_UNDEFINED, id);
        }
        String parentClasspathId = classpath.getParent();
        ClassLoader parentClassLoader = null;
        if (parentClasspathId != null && !Strings.EMPTY.equals(parentClasspathId)) {
            parentClassLoader = resolveClassLoader(classLoaders, seen, classpaths, parentClasspathId);
        } else {
            /* Should be the 'extension' classloader. */
            parentClassLoader = Bootstrap.class.getClassLoader().getParent();
        }
        URL[] classpathUrls;
        classpathUrls = findClasspathURLS(classpath.getValue());

        SettableCodebaseClassLoader classLoader = new SettableCodebaseClassLoader(classpathUrls,
                parentClassLoader);
        classLoaders.put(id, classLoader);
        log.log(Level.FINE, MessageNames.CONFIGURED_CLASSPATH, new Object[]{
            id,
            Utils.format(classpathUrls)});
        seen.remove(id);
        return classLoader;
    }

    static void initializeContainer(String args[]) throws SAXException, JAXBException, FileNotFoundException, MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ConfigurationException, Exception {
        //Logger.getLogger("org.apache.river.container.AnnotatedClassDeployer").setLevel(Level.ALL);
        Logger.getLogger(CommandLineArgumentParser.class.getName()).setLevel(Level.ALL);

        ContainerConfig coreConfig = readCoreConfig();
        Map<String, ClassLoader> classLoaders = createClassLoaders(coreConfig);

        ClassLoader containerClassLoader = classLoaders.get(Strings.CONTAINER_CLASS_LOADER);

        /*
         Create the context object.
         */
        Object context = Class.forName(Strings.CONTEXT_CLASS, true, containerClassLoader).newInstance();
        Method putByNameMethod = context.getClass().getMethod(
                Strings.PUT, new Class[]{String.class, Object.class});
        Method initCompleteMethod = context.getClass().getMethod(Strings.INIT_COMPLETE, new Class[0]);
        Thread.currentThread().setContextClassLoader(containerClassLoader);
        putByNameMethod.invoke(context, Strings.CLASS_LOADERS, (Object) classLoaders);
        
        /* Store a link to the context in the context. */
        putByNameMethod.invoke(context, Strings.CONTEXT, context);
        
        /*
         Process the core configuration
         */
        processConfiguration(coreConfig, containerClassLoader, context);
        /*
         We need to set the command line args after processing the core
         configuration so that the items in the core-config get initialized.
         */
        putByNameMethod.invoke(context, Strings.COMMAND_LINE_ARGS, (Object) args);
        /*
         The core configuration now loads the profile configuration...
         */
        // processConfiguration(containerConfig, classLoader, putMethod, context, putByNameMethod);
        initCompleteMethod.invoke(context, new Object[0]);
    }

    static void processConfiguration(ContainerConfig config, Object classLoader, Object context) throws InvocationTargetException, ConfigurationException, IllegalArgumentException, InstantiationException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, MalformedURLException, Exception {
        Method putMethod = context.getClass().getMethod(Strings.PUT, new Class[]{Object.class});
        Method putByNameMethod = context.getClass().getMethod(
                Strings.PUT, new Class[]{String.class, Object.class});

        /*
         Add the classpath urls found in the configuration into the classloader.
         Note that we have to do this by reflection because the classloader
         we're handed may not be the same classloader that loaded this instance
         of the Bootstrap class. In particular, this occurs when we are loading
         the profile configuration by calling out ProfileConfigReader in
         core-config.xml. In that case, the container classloader is created by
         the original Bootstrap class inside the original classloader, but
         ProfileConfigReader and hence another Bootstrap class gets loaded by
         the container class loader.
         */
        /*
         Not really sure about this.... would be required if we wanted the
         profile configs to add jar files to the classpath. Not sure if that is
         really "on", seeing as how we don't really want users attempting to
         extend the container. Having said that, it's possible that certain
         deployers might be in a different project, hence different jar files,
         so we might want to let the profiles add to the classpath. Needs more
         thought. for (URL url : findClasspathURLS(config)) { new
         Statement(classLoader, Strings.ADD_URL, new Object[]{url}).execute(); }
         */
        for (Object element : config.getElements()) {
            processElement(element, (ClassLoader) classLoader, putMethod, context, putByNameMethod);
        }
    }

    private static ContainerConfig readCoreConfig() throws SAXException, JAXBException, FileNotFoundException {
        Unmarshaller um = createConfigUnmarshaller();
        InputStream is = Bootstrap.class.getResourceAsStream(Strings.CORE_CONFIG_XML);
        ContainerConfig containerConfig = (ContainerConfig) um.unmarshal(is);
        return containerConfig;
    }

    private static void processElement(Object element, ClassLoader classLoader, Method putMethod, Object context, Method putByNameMethod) throws ClassNotFoundException, InstantiationException, InvocationTargetException, ConfigurationException, IllegalAccessException, IllegalArgumentException {
        if (element instanceof Component) {
            Component c = (Component) element;
            Class compClass = Class.forName(c.getClazz(), true, classLoader);
            String name = c.getName();
            Object instance = compClass.newInstance();
            for (Property p : c.getProperty()) {
                setPropertyOnComponent(instance, p.getName(), p.getValue());
                log.log(Level.FINER, MessageNames.SET_PROPERTY_ON_COMPONENT,
                        new Object[] { 
                            p.getName(),
                            c.getClazz(),
                            c.getName(),
                            p.getValue()
                        });
            }
            if (name == null || name.trim().length() == 0) {
                putMethod.invoke(context, instance);
            } else {
                putByNameMethod.invoke(context, name, instance);
            }
        } else if (element instanceof Property) {
            Property p = (Property) element;
            putByNameMethod.invoke(context, p.getName(), p.getValue());
        } else if (element instanceof DiscoveryContextType) {
            /*
             Just drop the element into the context under the appropriate name.
             */
            DiscoveryContextType dct = (DiscoveryContextType) element;
            if (dct.getName() == null) {
                putByNameMethod.invoke(context, Strings.DEFAULT_DISCOVERY_CONTEXT, dct);
            } else {
                putByNameMethod.invoke(context, dct.getName(), dct);
            }
        } else {
            throw new ConfigurationException(MessageNames.UNSUPPORTED_ELEMENT, element.getClass().getName());
        }
    }

    private static void setPropertyOnComponent(Object instance, String propertyName, String propertyValue) {
        try {
            BeanInfo info = Introspector.getBeanInfo(instance.getClass());
            PropertyDescriptor pd=findPropertyDescriptor(info, propertyName);
            Object convertedValue=convert(propertyValue, pd.getPropertyType());
            pd.getWriteMethod().invoke(instance, convertedValue);
        } catch (Throwable t) {
            throw new ConfigurationException(t, MessageNames.FAILED_TO_SET_PROPERTY, propertyName, instance.getClass(), propertyValue);
        }
    }
    
    private static PropertyDescriptor findPropertyDescriptor(BeanInfo info, String propertyName) throws IntrospectionException {
        for (PropertyDescriptor possible: info.getPropertyDescriptors()) {
            if (propertyName.equals(possible.getName())) {
                return possible;
            }
        }
        throw new IntrospectionException(propertyName);
    }
    
    private static Object convert(String value, Class targetType) {
        if (targetType.equals(Boolean.class)  || targetType.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (targetType.equals(String.class)) {
            return value;
        } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
            return Float.parseFloat(value);
        }
        throw new UnsupportedOperationException();
    }
    
    /*
     static URL[] findClasspathURLS(ContainerConfig containerConfig) throws
     MalformedURLException { String classpathStr =
     containerConfig.getClasspath(); URL[] urls =
     findClasspathURLS(classpathStr); return urls; }
     */
}
