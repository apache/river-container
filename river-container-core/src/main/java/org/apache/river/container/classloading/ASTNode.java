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

package org.apache.river.container.classloading;

/**
 *
 * @author trasukg
 */
public class ASTNode extends SimpleNode {

    private Object value=null;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    
    public ASTNode(int i) {
        super(i);
    }

    public ASTNode(ClasspathExpressionParser p, int i) {
        super(p, i);
    }

    public String getName() {
        return ClasspathExpressionParserTreeConstants.jjtNodeName[id];
    }

    public String toString() {
        if (id==ClasspathExpressionParserTreeConstants.JJTSYMBOL
                || id==ClasspathExpressionParserTreeConstants.JJTSTRINGLITERAL) {
            return getValue().toString();
        }
        String childList = childList();
        if (!Strings.EMPTY.equals(childList)) {
            return getName() + " " + childList;
        } else {
            return getName();
        }
    }

    public String childList() {
        StringBuffer sb = new StringBuffer();
        if (jjtGetNumChildren() != 0) {
            boolean first = true;
            for (int i = 0; i < jjtGetNumChildren(); i++) {
                if (!first) {
                    sb.append(" ");
                } else {
                    first = false;
                }
                String childStr = jjtGetChild(i).toString();
                if (childStr.indexOf(Strings.SPACE) != -1) {
                    sb.append(Strings.LPAREN);
                    sb.append(childStr);
                    sb.append(Strings.RPAREN);
                } else {
                    sb.append(jjtGetChild(i).toString());
                }
            }
        }
        return sb.toString();
    }
}
