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

/* TODO: - Complete deployment of items that are in the deployment
 directory.
 */
package org.apache.river.container.deployer;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jini.security.GrantPermission;
import net.jini.security.policy.DynamicPolicyProvider;
import net.jini.security.policy.UmbrellaGrantPermission;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.river.container.ConfigurationException;
import org.apache.river.container.Context;
import org.apache.river.container.FileUtility;
import org.apache.river.container.Init;
import org.apache.river.container.Injected;
import org.apache.river.container.InjectionStyle;
import org.apache.river.container.LocalizedRuntimeException;
import org.apache.river.container.MessageNames;
import org.apache.river.container.Name;
import org.apache.river.container.PropertiesFileReader;
import org.apache.river.container.Strings;
import org.apache.river.container.Utils;
import org.apache.river.container.classloading.ClasspathFilter;
import org.apache.river.container.classloading.VirtualFileSystemClassLoader;
import org.apache.river.container.codebase.CodebaseContext;
import org.apache.river.container.codebase.CodebaseHandler;
import org.apache.river.container.el.ArgsParser;
import org.apache.river.container.liaison.VirtualFileSystemConfiguration;
import org.apache.river.container.work.ContextualWorkManager;
import org.apache.river.container.work.WorkManager;

/**
 * Deployer that instantiates applications or services based on the
 * com.sun.jini.starter API
 */
public class StarterServiceDeployer  {

    private static final Logger log
            = Logger.getLogger(StarterServiceDeployer.class.getName(), MessageNames.BUNDLE_NAME);
    @Injected(style = InjectionStyle.BY_TYPE)
    private FileUtility fileUtility = null;
    @Injected(style = InjectionStyle.BY_TYPE)
    private Context context;
    @Name
    private String myName = null;
    @Injected(style = InjectionStyle.BY_TYPE)
    private CodebaseHandler codebaseHandler = null;
    private String config = Strings.STARTER_SERVICE_DEPLOYER_CONFIG;
    private ASTconfig configNode = null;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
    @Injected(style = InjectionStyle.BY_TYPE)
    private PropertiesFileReader propertiesFileReader = null;
    @Injected(style = InjectionStyle.BY_TYPE)
    private ArgsParser argsParser = null;
    @Injected(style = InjectionStyle.BY_TYPE)
    WorkManager workManager = null;
    @Injected(style = InjectionStyle.BY_TYPE)
    ContextualWorkManager contextualWorkManager = null;
    @Injected(style = InjectionStyle.BY_TYPE)
    private DynamicPolicyProvider securityPolicy = null;

    public void addPlatformCodebaseJars(CodebaseContext codebaseContext) throws IOException {
        ASTcodebase codebaseNode = (ASTcodebase) configNode.search(new Class[]{
            ASTconfig.class, ASTclassloader.class, ASTcodebase.class
        }).get(0);
        /*
         Register the platform codebase jars with the codebase service.
         */
        for (int i = 0; i < codebaseNode.jjtGetNumChildren(); i++) {
            String jarFile = codebaseNode.jjtGetChild(i).toString();
            FileObject fo = fileUtility.getLibDirectory().resolveFile(jarFile);
            codebaseContext.addFile(fo);
            log.log(Level.FINE, MessageNames.ADDED_PLATFORM_CODEBASE_JAR,
                    jarFile);
        }
    }

    public String[] constructArgs(String argLine, String[] serviceArgs) {
        String[] args = null;
        if (argLine == null) {
            args = new String[0];
        } else {
            args = argsParser.toArgs(argLine, serviceArgs);
        }
        return args;
    }

    public VirtualFileSystemClassLoader createServiceClassloader(FileObject serviceRoot, CodeSource codeSource) throws IOException, FileSystemException {

        String parentLoaderName = configNode.search(
                new Class[]{ASTconfig.class, ASTclassloader.class, ASTparent.class}).get(0).jjtGetChild(0).toString();
        log.log(Level.FINE, MessageNames.SERVICE_PARENT_CLASSLOADER_IS, parentLoaderName);
        boolean isAppPriority = false;
        if (!configNode.search(new Class[]{ASTconfig.class, ASTclassloader.class, ASTappPriority.class}).isEmpty()) {
            isAppPriority = true;
        }
        ClassLoader parentLoader = (ClassLoader) context.get(parentLoaderName);
        VirtualFileSystemClassLoader cl
                = createChildOfGivenClassloader(parentLoader, codeSource, isAppPriority);
        /*
         Include platform jars from the container's lib directory.
         */
        List classpathNodes = configNode.search(new Class[]{ASTconfig.class,
            ASTclassloader.class, ASTjars.class, ASTclasspath.class});
        if (classpathNodes.size() > 0) {
            ASTclasspath platformJarSpec = (ASTclasspath) classpathNodes.get(0);
            addPlatformJarsToClassloader(platformJarSpec, cl);
        }
        addLibDirectoryJarsToClasspath(serviceRoot, cl);
        return cl;

    }

    protected void addLibDirectoryJarsToClasspath(FileObject serviceRoot, VirtualFileSystemClassLoader cl) throws FileSystemException {
        /*
         Add the jar files from the service's 'lib' directory.
         */
        FileObject libDir = serviceRoot.resolveFile(Strings.LIB);
        List<FileObject> jarFiles = Utils.findChildrenWithSuffix(libDir,
                Strings.DOT_JAR);
        for (FileObject jarFile : jarFiles) {
            cl.addClassPathEntry(libDir, jarFile.getName().getBaseName());
        }
    }

    protected void addPlatformJarsToClassloader(ASTclasspath platformJarSpec, VirtualFileSystemClassLoader cl) throws IOException, LocalizedRuntimeException {
        log.log(Level.FINE, MessageNames.ADDING_CLASSPATH_ENTRY, new Object[]{platformJarSpec.toString()});
        List<ClasspathFilter> filters = ClasspathFilterBuilder.filtersFromClasspathExpression(platformJarSpec);

        cl.addClasspathFilters(filters, fileUtility.getLibDirectory());
    }

    protected VirtualFileSystemClassLoader createChildOfGivenClassloader(ClassLoader parent, CodeSource codeSource, boolean isAppPriority) {
        /*
         Create the service classloader.
         */
        VirtualFileSystemClassLoader cl
                = new VirtualFileSystemClassLoader(null, parent, codeSource, isAppPriority);
        return cl;
    }

    public void exportServiceCodebaseJars(FileObject serviceRoot, CodebaseContext codebaseContext) throws FileSystemException {
        /*
         Register the service's codebase jars with the codebase service.
         */
        FileObject libDlDir = serviceRoot.resolveFile(Strings.LIB_DL);
        /* Don't bother if there is no lib-dl (e.g. for simple clients) */
        if (libDlDir.exists()) {
            List<FileObject> dljarFiles = Utils.findChildrenWithSuffix(libDlDir,
                    Strings.DOT_JAR);
            for (FileObject jarFile : dljarFiles) {
                codebaseContext.addFile(jarFile);
            }
        }
    }

    @Init
    public void init() {
        try {
            tryInitialize();
        } catch (Throwable ex) {
            log.log(Level.SEVERE, MessageNames.STARTER_SERVICE_DEPLOYER_FAILED_INIT,
                    ex);
            throw new ConfigurationException(ex,
                    MessageNames.STARTER_SERVICE_DEPLOYER_FAILED_INIT);
        }
    }

    public void launchService(final ApplicationEnvironment env, Properties startProps, final String[] args) throws ClassNotFoundException {
        final String startClassName = startProps.getProperty(Strings.START_CLASS);
        /*
         Launch the service.
         */
        log.log(Level.FINE, MessageNames.CALLING_MAIN, new Object[]{
            startClassName, Utils.format(args)
        });
        Runnable task = null;
        if (hasServiceStarterConstructor(env.getClassLoader(), startClassName)) {
            task = new Runnable() {
                @Override
                public void run() {
                    try {
                        env.setServiceInstance(instantiateService(env.getClassLoader(), startClassName, args));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
        } else if (hasMain(env.getClassLoader(), startClassName)) {
            task = new Runnable() {
                @Override
                public void run() {
                    try {
                        callMain(env.getClassLoader(), startClassName, args);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };

        } else {
            throw new UnsupportedOperationException();
        }
        env.getWorkingContext().getScheduledExecutorService().submit(task);
    }

    public Properties readStartProperties(FileObject serviceRoot) throws FileSystemException, LocalizedRuntimeException, IOException {
        /*
         Read the start.properties file.
         */
        FileObject startProperties = serviceRoot.resolveFile(Strings.START_PROPERTIES);
        if (startProperties == null || !startProperties.getType().equals(FileType.FILE)
                || !startProperties.isReadable()) {
            throw new LocalizedRuntimeException(MessageNames.BUNDLE_NAME,
                    MessageNames.CANT_READ_START_PROPERTIES,
                    new Object[]{Strings.START_PROPERTIES,
                        serviceRoot.getName().getBaseName()});
        }
        Properties startProps = propertiesFileReader.getProperties(startProperties);
        return startProps;
    }

    public void setupLiaisonConfiguration(ApplicationEnvironment env) throws ConfigurationException {
        /*
         Setup the liaison configuration.
         */
        ClassLoader originalContextCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(env.getClassLoader());
            File workingDir = null;
            if (env.getServiceArchive() != null) {
                /* This variable, and the method we're calling on VirtualFileSystemConfiguration,
                is named in an unfortunate manner for 
                historical reasons.  What we're really doing here is telling the 
                VirtualFileSystemConfiguration where to read its configuration 
                file from.  Iternally to VFSConfig, it's called the "root" directory.
                It has nothing to do with the "working" directory where the service
                should be allowed to write its own data.
                */
                workingDir = new File(env.getServiceArchive().getURL().toURI());
            } else {
                workingDir = new File(env.getServiceArchive().getURL().toURI());
            
            }

            grantPermissions(env.getClassLoader(),
                    new Permission[]{new FilePermission(workingDir.getAbsolutePath(), Strings.READ)});
            Utils.logClassLoaderHierarchy(log, Level.FINE, this.getClass());
            String configName = VirtualFileSystemConfiguration.class.getName();
            invokeStatic(env.getClassLoader(), configName,
                    Strings.SET_WORKING_DIRECTORY,
                    workingDir);
            /*
             Setup the "special" variables in the configuration.
             */
            ASTconfiguration configurationNode = (ASTconfiguration) configNode.search(new Class[]{ASTconfig.class, ASTconfiguration.class}).get(0);
            for (int i = 0; i < configurationNode.jjtGetNumChildren(); i++) {
                ASTconfigEntry cfgEntryNode = (ASTconfigEntry) configurationNode.jjtGetChild(i);
                String varName = cfgEntryNode.jjtGetChild(0).toString();
                String contextVarName = cfgEntryNode.jjtGetChild(1).toString();
                Object contextValue = context.get(contextVarName);
                if (contextValue != null) {
                    invokeStatic(env.getClassLoader(), configName,
                            Strings.PUT_SPECIAL_ENTRY,
                            new Class[]{String.class, Object.class},
                            Strings.DOLLAR + varName, contextValue);
                } else {
                    log.log(Level.WARNING, MessageNames.MISSING_SPECIAL_VALUE,
                            new Object[]{getConfig(), varName, contextVarName});
                }
            }
            
            /* 
            One extra "special" variable is the File that gives the working directory.
            */
            invokeStatic(env.getClassLoader(), configName, Strings.PUT_SPECIAL_ENTRY,
                    new Class[]{String.class, Object.class},
                    Strings.DOLLAR + Strings.WORKING_DIRECTORY, env.getWorkingDirectory());
            /* Install the Executor. */
            log.log(Level.INFO, MessageNames.EXECUTOR_NAME_IS, 
                    new Object[]{Strings.DOLLAR + Strings.EXECUTOR_NAME});
            invokeStatic(env.getClassLoader(), configName,
                    Strings.PUT_SPECIAL_ENTRY,
                    new Class[]{String.class, Object.class
                    },
                    Strings.DOLLAR + Strings.EXECUTOR_NAME, env.getWorkingContext().getScheduledExecutorService()
            );

        } catch (Exception ex) {
            log.log(Level.WARNING, MessageNames.EXCEPTION_THROWN, Utils.stackTrace(ex));
            throw new ConfigurationException(ex,
                    MessageNames.STARTER_SERVICE_DEPLOYER_FAILED_INIT);
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextCl);
        }
    }

    private void tryInitialize() throws IOException, ParseException {
        log.log(Level.FINE, MessageNames.STARTER_SERVICE_DEPLOYER_STARTING, myName);
        /*
         Read and parse the configuration file.
         */

        FileObject configFile = fileUtility.getProfileDirectory().resolveFile(config);
        InputStream in = configFile.getContent().getInputStream();
        configNode = DeployerConfigParser.parseConfig(in);
        log.log(Level.FINE, MessageNames.STARTER_SERVICE_DEPLOYER_INITIALIZED,
                new Object[]{myName});
    }

    public ServiceLifeCycle deployServiceArchive(String managerName, FileObject serviceArchive) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        // Create an application environment
        ApplicationEnvironment env = new ApplicationEnvironment();
        env.setApplicationManagerName(managerName);
        env.setServiceArchive(serviceArchive);
        env.setServiceRoot(
                serviceArchive.getFileSystem().getFileSystemManager().createFileSystem(Strings.JAR, serviceArchive));
        String serviceName = findServiceName(env.getServiceArchive(), env.getServiceRoot());
        env.setServiceName(serviceName);
        ServiceLifeCycle slc = StarterServiceLifeCycleSM.newStarterServiceLifeCycle(env, this);
        return slc;
    }

    private String findServiceName(FileObject serviceArchive, FileObject serviceRoot) {
        if (serviceArchive != null) {
            return serviceArchive.getName().getBaseName();
        }
        return serviceRoot.getName().getBaseName();
    }

    private URL findServiceURL(FileObject serviceArchive, FileObject serviceRoot) throws FileSystemException {
        if (serviceArchive != null) {
            return serviceArchive.getURL();
        }
        return serviceRoot.getURL();
    }

    void prepareService(ApplicationEnvironment env) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {

        CodeSource serviceCodeSource
                = new CodeSource(findServiceURL(env.getServiceArchive(), env.getServiceRoot()),
                        new Certificate[0]);
        log.log(Level.INFO, MessageNames.CODESOURCE_IS,
                new Object[]{env.getServiceName(), serviceCodeSource});
        VirtualFileSystemClassLoader cl = createServiceClassloader(env.getServiceRoot(), serviceCodeSource);
        env.setClassLoader(cl);

        /*
         Create a codebase context.
         */
        CodebaseContext codebaseContext
                = codebaseHandler.createContext(env.getServiceName());
        env.setCodebaseContext(codebaseContext);
        addPlatformCodebaseJars(codebaseContext);
        exportServiceCodebaseJars(env.getServiceRoot(), codebaseContext);

        /*
         Setup the classloader's codebase annotation.
         */
        cl.setCodebase(codebaseContext.getCodebaseAnnotation());
        /*
         Grant the appropriate permissions to the service's classloader and
         protection domain.
         */
        Permission[] perms = createPermissionsInClassloader(cl);
        grantPermissions(cl, perms);
        
        /*
         Create the service's working directory and grant permissions to it.
        */
        createWorkDirectoryFor(env);
        grantPermissionsToWorkDirectoryFor(env);
        
        /*
         * Create a working context (work manager).
         */
        env.setWorkingContext(contextualWorkManager.createContext(env.getServiceName(), env.getClassLoader()));
 
        setupLiaisonConfiguration(env);
    }

    void launchService(ApplicationEnvironment env, String[] serviceArgs) throws FileSystemException, IOException, ClassNotFoundException {
        Properties startProps = readStartProperties(env.getServiceRoot());
        String argLine = startProps.getProperty(Strings.START_PARAMETERS);
        final String[] args = constructArgs(argLine, serviceArgs);

        launchService(env, startProps, args);
        log.log(Level.INFO, MessageNames.COMPLETED_SERVICE_DEPLOYMENT, env.getServiceName());
    }

    Permission[] createPermissionsInClassloader(ClassLoader cl) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        List<Permission> perms = new ArrayList<Permission>();
        // Get all the permission nodes from the config.
        Class[] path = new Class[]{ASTconfig.class, ASTgrant.class, ASTpermission.class};
        List<ASTNode> permNodes = configNode.search(path);
        // Create a permission for each
        for (ASTNode node : permNodes) {
            String className = (String) ((ASTsymbol) node.jjtGetChild(0)).getValue();
            Object permissionConstructorArgs[] = new String[node.jjtGetNumChildren() - 1];
            for (int i = 0; i < permissionConstructorArgs.length; i++) {
                permissionConstructorArgs[i] = (String) ((ASTliteral) node.jjtGetChild(i + 1)).getValue();
            }
            Permission perm = (Permission) invokeConstructor(cl, className, permissionConstructorArgs);
            perms.add(perm);
        }
        return perms.toArray(new Permission[0]);
    }

    private Object invokeStatic(ClassLoader cl, String className, String methodName,
            Object... parms) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class clazz = Class.forName(className, true, cl);
        Class[] parameterTypes = new Class[parms.length];
        for (int i = 0; i < parms.length; i++) {
            parameterTypes[i] = parms[i].getClass();
        }
        Method method = clazz.getMethod(methodName, parameterTypes);
        return method.invoke(null, parms);
    }

    private Object invokeStatic(ClassLoader cl, String className, String methodName, Class[] argTypes,
            Object... parms) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class clazz = Class.forName(className, true, cl);
        Method method = clazz.getMethod(methodName, argTypes);
        return method.invoke(null, parms);
    }

    private Object invokeConstructor(ClassLoader cl, String className,
            Object... parms) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        Class clazz = Class.forName(className, true, cl);
        Class[] parameterTypes = new Class[parms.length];
        for (int i = 0; i < parms.length; i++) {
            parameterTypes[i] = parms[i].getClass();
        }
        Constructor method = clazz.getConstructor(parameterTypes);
        return method.newInstance(parms);
    }

    private void grantPermissions(ClassLoader cl, Permission[] perms) {
        try {
            perms = expandUmbrella(perms);
            Class clazz = Class.forName(VirtualFileSystemConfiguration.class.getName(), true, cl);
            securityPolicy.grant(clazz, new Principal[0], perms);

        } catch (Throwable t) {
            throw new ConfigurationException(MessageNames.FAILED_DEPLOY_SERVICE, t);
        }
    }

    private static Permission[] expandUmbrella(Permission[] perms) {
        PermissionCollection pc = new Permissions();

        for (Permission p : perms) {
            pc.add(p);
        }
        if (pc.implies(new UmbrellaGrantPermission())) {
            List l = Collections.list(pc.elements());
            pc.add(new GrantPermission(
                    (Permission[]) l.toArray(new Permission[l.size()])));
        }
        List<Permission> permList = new ArrayList<Permission>();

        for (Enumeration<Permission> en = pc.elements(); en.hasMoreElements();) {
            permList.add(en.nextElement());
        }
        return permList.toArray(new Permission[0]);
    }

    private boolean hasServiceStarterConstructor(ClassLoader cl, String className) throws ClassNotFoundException {
        Class clazz = Class.forName(className, true, cl);
        log.log(Level.FINE, MessageNames.CLASSLOADER_IS,
                new Object[]{clazz.getName(), clazz.getClassLoader().toString()});

        // Get this through dynamic lookup becuase it won't be in the parent
        // classloader!
        Class lifeCycleClass = Class.forName(Strings.LIFECYCLE_CLASS, true, cl);
        try {
            Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String[].class, lifeCycleClass});
            return true;
        } catch (NoSuchMethodException nsme) {
            return false;
        }
    }

    private boolean hasMain(ClassLoader cl, String className) throws ClassNotFoundException {
        Class clazz = Class.forName(className, true, cl);
        log.log(Level.FINE, MessageNames.CLASSLOADER_IS,
                new Object[]{clazz.getName(), clazz.getClassLoader().toString()});

        // Get this through dynamic lookup becuase it won't be in the parent
        // classloader!
        try {
            Method main = clazz.getMethod(Strings.MAIN, new Class[]{String[].class});
            return true;
        } catch (NoSuchMethodException nsme) {
            return false;
        }
    }

    private void callMain(ClassLoader cl, String className, String[] args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class clazz = Class.forName(className, true, cl);
        log.log(Level.FINE, MessageNames.CLASSLOADER_IS,
                new Object[]{clazz.getName(), clazz.getClassLoader().toString()});

        // Get this through dynamic lookup becuase it won't be in the parent
        // classloader!
        try {
            Method main = clazz.getMethod(Strings.MAIN, new Class[]{String[].class});
            main.invoke(null, new Object[]{args});
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException(nsme);
        }

    }

    private Object instantiateService(ClassLoader cl, String className, String[] parms)
            throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        Class clazz = Class.forName(className, true, cl);
        log.log(Level.FINE, MessageNames.CLASSLOADER_IS,
                new Object[]{clazz.getName(), clazz.getClassLoader().toString()});

        // Get this through dynamic lookup becuase it won't be in the parent
        // classloader!
        Class lifeCycleClass = Class.forName(Strings.LIFECYCLE_CLASS, true, cl);
        Constructor[] constructors = clazz.getDeclaredConstructors();
        System.out.println("Class is " + clazz);
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            System.out.println("Found constructor " + constructor + " on " + className);
        }
        Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String[].class, lifeCycleClass});
        constructor.setAccessible(true);
        return constructor.newInstance(parms, null);
    }

    /**
     * Attempt to stop the service in an orderly fashion. Go to the service, see
     * if it implements Administrable, then get the admin proxy and see if it
     * implements DestroyAdmin. If so, call it.
     *
     * @param env
     */
    public void stopService(ApplicationEnvironment env) {
        /* Option 1 - Service has a getAdmin() method - it probably implements
         * Administrable.
         */
        Object serviceInstance = env.getServiceInstance();
        Method getAdmin = null;
        try {
            getAdmin = serviceInstance.getClass().getMethod(Strings.GET_ADMIN, new Class[0]);
        } catch (Exception ex) {
            // Silent catch - leave it null;
        }
        if (getAdmin != null) {
        }

    }
    
    void createWorkDirectoryFor(ApplicationEnvironment env) throws IOException {
        FileObject managerDir=fileUtility.getWorkingDirectory(env.getApplicationManagerName());
        FileObject workingDir=managerDir.resolveFile(env.getServiceName());
        if (!workingDir.exists()) {
            workingDir.createFolder();
        }
        File workingDirFile=new File(workingDir.getName().getPath());
        env.setWorkingDirectory(workingDirFile);
    }
    
    void grantPermissionsToWorkDirectoryFor(ApplicationEnvironment env) {
        Permission[] perms=new Permission[] {
            new FilePermission(env.getWorkingDirectory().getAbsolutePath()+"/-","read,write,delete")
        };
        grantPermissions(env.getClassLoader(), perms);
    }
}
