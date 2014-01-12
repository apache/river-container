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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

/**

 @author trasukg
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

    public static String stackTrace(Throwable t) {
        StringWriter s=new StringWriter();
        PrintWriter pw=new PrintWriter(s);
        t.printStackTrace(pw);
        return s.toString();
    }
}
