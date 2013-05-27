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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 Processor that sets up various context items according to the command line
 arguments supplied.
 <p>
 For one thing, this sets up the profile according to the first command-line
 parameter.  If the first parameter is there and does not start with a '-',
 it is used as the profile name.  Otherwise the profile name is set to
 'default'.
 
 * @author trasukg
 */
public class CommandLineArgumentParser {
    private static final Logger log=
            Logger.getLogger(CommandLineArgumentParser.class.getName(),
            MessageNames.BUNDLE_NAME);

    @Injected
    public String[] commandLineArguments=null;

    @Injected(style= InjectionStyle.BY_TYPE)
    Context context=null;

    @Init
    public void init() {
        /* If there is a first argument, and it doesn't begin with '-', then
        it's a profile name.
         */
        String cmdString=Utils.format(commandLineArguments);
        log.log(Level.FINE, MessageNames.SHOW_COMMAND_LINE_ARGUMENTS, cmdString);
        if (commandLineArguments.length > 0 && !commandLineArguments[0].startsWith(Strings.DASH)) {
            String profileName = commandLineArguments[0];
            context.put(Strings.PROFILE, profileName);
        } else {
            context.put(Strings.PROFILE, Strings.DEFAULT);
        }
    }
}
