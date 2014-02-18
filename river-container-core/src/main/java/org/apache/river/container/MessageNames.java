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

/*
 TODO: Should the messages be separated into different domains based on the
 audience?  i.e. Should we have ExceptionMessages that might be seen by users
 and LogMessages that are primarily meant to be seen by developers? UIMessages
 that are assumed to be used in the UI?

 Advantages:
 - Possibly better organization
 - Internationalization/Translation efforts could be more focused.

 Also, should we assign a code to each message (msg number, etc)?  That way,
 support tools could search an email message for the exception code and
 then generate a list of candidate support messages.
 */

/**
 * Constants that hold message names used in the message resource bundle.
 * @author trasukg
 */
public class MessageNames {
    public static final String BUNDLE_NAME="org.apache.river.container.Messages";

    public static final String
            ADDED_PLATFORM_CODEBASE_JAR="addedPlatformCodebaseJar",
            ADDING_CLASSPATH_ENTRY="addingClasspathEntry",
            ANNOTATED_OBJECT_DEPLOYER_HAS_UNRESOLVED_DEPENDENCIES="annotatedObjectDeployerHasUnresolvedDependencies",
            AUTO_DEPLOYER_FAILED_INIT="autoDeployerFailedInit",
            AUTO_DEPLOYER_STARTING="autoDeployerStarting",
            BAD_CLASSPATH_EXPR="badClasspathExpression",
            BAD_MEMBER_FOR_INJECTED_ANNOTATION="badMemberForInjectedAnnotation",
            BAD_MEMBER_FOR_NAME_ANNOTATION="badMemberForNameAnnotation",
            BASIC_WORK_MANAGER_INITIALIZED="basicWorkManagerInitialized",
            CALLING_MAIN="callingMain",
            CANT_CONVERT_EXCEPTION="cantConvertException",
            CANT_READ_START_PROPERTIES="cantReadStartProperties",
            CIRCULAR_CLASSPATH="circularClasspath",
            CLASSLOADER_IS="classLoaderIs",
            CLASSPATH_UNDEFINED="classpathUndefined",
            CLASS_SERVER_BAD_REQUEST="classServerBadRequest",
            CLASS_SERVER_ERROR_ACCEPTING_CONNECTIONS="classServerErrorAcceptingConnections",
            CLASS_SERVER_ESTABLISHED="classServerEstablished",
            CLASS_SERVER_EXCEPTION_DURING_SHUTDOWN="classServerExceptionDuringShutdown",
            CLASS_SERVER_EXCEPTION_GETTING_BYTES="classServerExceptionGettingBytes",
            CLASS_SERVER_EXCEPTION_WRITING_RESPONSE="classServerExceptionWritingResponse",
            CLASS_SERVER_INIT_FAILED="classServerInitFailed",
            CLASS_SERVER_NO_CONTENT_FOUND="classServerNoContentFound",
            CLASS_SERVER_RECEIVED_REQUEST="classServerReceivedRequest",
            CLASS_SERVER_RECEIVED_PROBE="classServerReceivedProbe",    
            CLASS_SERVER_REJECTED_PATH="classServerRejectedPath",
            CLASS_SERVER_TERMINATED="classServerTerminated",
            CLIENT_APP_USAGE="clientAppUsage",
            CODESOURCE_IS="codeSourceIs",
            COMPLETED_SERVICE_DEPLOYMENT="completedServiceDeployment",
            CONFIG_FILE="configFile",
            CONFIGURED_CLASSPATH = "configuredClasspath",
            CONTEXT_ITEM = "contextItem",
            CREATED_THREAD="createdThread",
            DUPLICATE_CLASSPATH="duplicateClasspath",
            EXCEPTION_THROWN="exceptionThrown",
            EXCEPTION_WHILE_STOPPING="exceptionWhileStopping",
            EXECUTOR_NAME_IS="executorNameIs",
            FAILED_CLEAN_SHUTDOWN="failedCleanShutdown",
            FAILED_DEPLOY_SERVICE="failedDeployService",
            FAILED_READ_PROPERTIES="failedReadProperties",
            FAILED_TO_REMOVE_MBEAN="failedToRemoveMBean",
            FAILED_TO_SET_PROPERTY="failedToSetProperty",
            FOUND_NO_SERVICE_ARCHIVES="foundNoServiceArchives",
            FOUND_SERVICE_ARCHIVES="foundServiceArchives",
            GRANTS_TO_CLASS_ARE="grantsToClassAre",
            ILLEGAL_ARGUMENT_EXCEPTION="illegalArgumentException",
            ILLEGAL_ACCESS_EXCEPTION="illegalAccessException",
            INITIALIZATION_EXCEPTION="initializationException",
            INTIALIZING_EVENT_TABLE="initializingEventTable",
            INVALID_CLASSPATH_ENTRY="invalidClasspathEntry",
            INVOCATION_TARGET_EXCEPTION="invocationTargetException",
            INIT_METHOD_HAS_PARAMETERS="initMethodHasParameters",
            INIT_METHOD_NOT_VOID="initMethodIsntVoid",
            INJECT="inject",
            MISSING_PROPERTY_ENTRY="missingPropertyEntry",
            MISSING_SPECIAL_VALUE="missingSpecialValue",
            N_THREADS_LEFT="nThreadsLeft",
            NO_DEPLOYMENT_DIRECTORY="noDeploymentDirectory",
            NO_SUCH_CLIENT_APP="noSuchClientApp",
            PARENT_CLASS_LOADER_IS="parentClassLoaderIs",
            POLICY_DECLINED="policyDeclined",
            PORT_IN_USE="portInUse",
            PROFILE_CONFIG_EXCEPTION="profileConfigurationException",
            PROFILE_CONFIG_LOADING="profileConfigLoading",
            READ_PROPERTIES="readProperties",
            READ_PROPERTIES_FILE="readPropertiesFile",
            READING_OBJECT="readingObject",
            READING_OBJECT_MEMBER_COUNT="readingObject.memberCount",
            READING_OBJECT_ANNOTATED_MEMBER_FOUND="readingObject.annotatedMemberFound",
            READING_OBJECT_NON_ANNOTATED_MEMBER_FOUND="readingObject.nonAnnotatedMemberFound",
            RECEIVED_START="receivedStart",
            RECEIVED_START_WITH_ARGS="receivedStartWithArgs",
            SCANNING_DEPLOYMENT_DIRECTORY="scanningDeploymentDirectory",
            SECURITY_INIT_FAILED="securityInitializationFailed",
            SECURITY_INIT_SUCCEEDED="securityInitializationSucceeded",
            SECURITY_INIT_WRONG_POLICY="securityInitializationWrongPolicy",
            SERVICE_PARENT_CLASSLOADER_IS="serviceParentClassloaderIs",
            SET_PROPERTY_ON_COMPONENT="setPropertyOnComponent",
            SHOW_COMMAND_LINE_ARGUMENTS="showCommandLineArguments",
            SHUTDOWN_FAILED="shutdownFailed",
            SHUTDOWN_METHOD_HAS_PARAMETERS="shutdownMethodHasParameters",
            SHUTDOWN_METHOD_NOT_VOID="shutdownMethodIsntVoid",
            STARTER_SERVICE_DEPLOYER_FAILED_INIT="starterServiceDeployerFailedInit",
            STARTER_SERVICE_DEPLOYER_INITIALIZED="starterServiceDeployerInitialized",
            STARTER_SERVICE_DEPLOYER_STARTING="starterServiceDeployerStarting",
            STARTING_SERVICE="startingService",
            STARTUP_DEPLOYER_FAILED_INIT="startupDeployerFailedInit",
            STARTUP_DEPLOYER_INITIALIZED="startupDeployerInitialized",
            STARTUP_DEPLOYER_STARTING="startupDeployerStarting",
            STOPPING_SERVICE="stoppingService",
            SYSTEM_CLASSLOADER_IS="systemClassLoaderIs",
            UNRESOLVED_DEPENDENCY="unresolvedDependency",
            UNSUPPORTED_ELEMENT="unsupportedElement",
            UPDATING_SERVICE="updatingService";
}
