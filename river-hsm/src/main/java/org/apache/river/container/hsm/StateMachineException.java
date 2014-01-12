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

package org.apache.river.container.hsm;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This is a runtime exception with localized error messages.
 */
public class StateMachineException extends RuntimeException {
    private final String messageBundleName;
    private final String messageKey;

    String getMessageBundleName() {
        return messageBundleName;
    }

    String getMessageKey() {
        return messageKey;
    }

    Object[] getMessageParameters() {
        return messageParameters;
    }
    private final Object[] messageParameters;

    /**
     Construct a runtime exception with a localized message.
     @param messageBundleName Name of the message bundle.
     @param messageKey Message key for this message.
     @param messageParameters Parameters if required, null or empty if not.
     */
    public StateMachineException(String messageBundleName,
            String messageKey,
            Object[] messageParameters) {
        this.messageBundleName=messageBundleName;
        this.messageKey=messageKey;
        this.messageParameters=new Object[messageParameters.length];
        System.arraycopy(messageParameters, 0, this.messageParameters, 0 , 
                messageParameters.length);
    }

    /**
     Construct a runtime exception with a localized message.
     @param messageBundleName Name of the message bundle.
     @param messageKey Message key for this message.
     @param messageParameters Parameters if required, null or empty if not.
     @param rootCause A root cause for this exception if available.
     */
    public StateMachineException(Throwable rootCause,
            String messageBundleName,
            String messageKey,
            Object ... messageParameters) {
        super(rootCause);
        this.messageBundleName=messageBundleName;
        this.messageKey=messageKey;
        this.messageParameters=messageParameters;
    }

    /**
     Localize and return the error message, according to the default Locale.
     Note that the resolution of the resource bundle and the localization is
     deferred until this method is called, allowing the existence of different
     resource bundles or locales in the thrower and receiver (e.g. in the case
     of a serialized exception passed between two JVMs.

     @return The localized message.
     */
    @Override
    public String getMessage() {
        ResourceBundle bundle=ResourceBundle.getBundle(messageBundleName);
        String message=(String) bundle.getObject(messageKey);
        return MessageFormat.format(message, messageParameters);
    }

    /**
     Return the message localized using the given locale.
     Note that the resolution of the resource bundle and the localization is
     deferred until this method is called, allowing the existence of different
     resource bundles or locales in the thrower and receiver (e.g. in the case
     of a serialized exception passed between two JVMs.

     @param locale The localized message
     @return
     */
    public String getMessage(Locale locale) {
        ResourceBundle bundle=ResourceBundle.getBundle(messageBundleName, locale);
        String message=(String) bundle.getObject(messageKey);
        return MessageFormat.format(message, messageParameters);
    }
}
