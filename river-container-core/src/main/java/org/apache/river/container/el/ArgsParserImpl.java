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
package org.apache.river.container.el;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.river.container.Strings;

/**
 *
 * @author trasukg
 */
public class ArgsParserImpl implements ArgsParser {
    /**
    Take a command line as one string and break it into a set
    of arguments as expected by main(String[] args).
    
    @param input
    @return 
    */
    @Override
    public String[] toArgs(String input, String[] inputArgs) {
        List<String> args=new ArrayList<String>();
        StringTokenizer tok=new StringTokenizer(input," ");
        while(tok.hasMoreTokens()) {
            String token=tok.nextToken();
            
            if (Strings.DOLLAR_STAR.equals(token)) {
                for(String arg: inputArgs) {
                    args.add(arg);
                }
            } else {
                args.add(token);
            }
        }
        return args.toArray(new String[0]);
    }
}
