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

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.river.container.LocalizedRuntimeException;
import org.apache.river.container.MessageNames;

/**
 *
 * @author trasukg
 */
public class ClasspathFilterBuilder {

    private static final Logger log = Logger.getLogger(ClasspathFilterBuilder.class.getName());

    public List<ClasspathFilter> parseToFilters(String input) {
        try {
            ASTcpExpression expression = classpathExpressionFromString(input);
            List<ClasspathFilter> filters = filtersFromClasspathExpression(expression);
            return filters;
        } catch (ParseException ex) {
            throw new LocalizedRuntimeException(MessageNames.BUNDLE_NAME,
                    MessageNames.BAD_CLASSPATH_EXPR,
                    new Object[]{input, ex.getMessage()});
        }
    }

    private ASTcpExpression classpathExpressionFromString(String input) throws ParseException {
        Reader r = new StringReader(input);
        ClasspathExpressionParser parser = new ClasspathExpressionParser(r);
        parser.cpExpression();
        ASTcpExpression expression = (ASTcpExpression) parser.jjtree.popNode();
        return expression;
    }

    private List<ClasspathFilter> filtersFromClasspathExpression(ASTcpExpression expression) {
        List<ClasspathFilter> filters = new ArrayList<ClasspathFilter>();
        for (int i = 0; i < expression.jjtGetNumChildren(); i++) {
            ASTcpClause clause = (ASTcpClause) expression.jjtGetChild(i);
            ClasspathFilter cpf = makeFilter(clause);
            filters.add(cpf);
        }
        return filters;
    }

    public ClasspathFilter makeFilter(ASTcpClause expression) {
        /* First node is the jar name.  Subsequent nodes are the filter
        conditions.
         */
        ClasspathFilter cpf = new ClasspathFilter();
        cpf.setJarName(expression.jjtGetChild(0).toString());
        for (int i = 1; i < expression.jjtGetNumChildren(); i++) {
            Node node = expression.jjtGetChild(i);
            if (node instanceof ASTsymbol) {
                String resourceName = VirtualFileSystemClassLoader.classToResourceName(node.toString());
                log.fine("Building ResourceAcceptor with string '" + resourceName + "'");
                Acceptor acc = new ResourceAcceptor(resourceName);
                cpf.getAcceptors().add(acc);
            }
            if (node instanceof ASTstringLiteral) {
                log.fine("Building ResourceAcceptor with string '" + node.toString() + "'");
                Acceptor acc = new ResourceAcceptor(node.toString());
                cpf.getAcceptors().add(acc);
            }
        }
        /* If there were no filter clauses, hence no acceptors, allow all
        patterns.
         */
        if (cpf.getAcceptors().isEmpty()) {
            cpf.getAcceptors().add(new AllAcceptor());
        }
        return cpf;
    }
}
