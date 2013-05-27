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

package org.apache.river.container.deployer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public ASTNode(DeployerConfigParser p, int i) {
        super(p, i);
    }

    public String getName() {
        return DeployerConfigParserTreeConstants.jjtNodeName[id];
    }

    public String toString() {
        if (id==DeployerConfigParserTreeConstants.JJTSYMBOL) {
            return getValue().toString();
        }
        if (id==DeployerConfigParserTreeConstants.JJTLITERAL) {
            Object o=getValue();
            if (o instanceof String) {
                return "\"" + o.toString() + "\"";
            }
            return o.toString();
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
                    sb.append(childStr);
                }
            }
        }
        return sb.toString();
    }
    
    public List<ASTNode> search(Class[] path) {
        List<ASTNode> matches=new ArrayList<ASTNode>();
        search(path, matches);
        return matches;
    }
    
    public void search(Class[] path, List<ASTNode> matches) {
        if (path.length==0) {
            return;
        }
        if (!this.getClass().equals(path[0])){
            return;
        }
        if (path.length==1) {
            matches.add(this);
            return;
        }
        path=Arrays.copyOfRange(path, 1, path.length);
        for (int i=0; i < this.jjtGetNumChildren(); i++) {
            ((ASTNode) jjtGetChild(i)).search(path, matches);
        }
    }
}
