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

import java.util.Arrays;
import org.apache.river.container.Utils;

/**
 *
 * @author trasukg
 */
public class ResourceAcceptor implements Acceptor {

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceAcceptor other = (ResourceAcceptor) obj;
        if (!Arrays.deepEquals(this.pathSteps, other.pathSteps)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Arrays.deepHashCode(this.pathSteps);
        return hash;
    }

    String[] pathSteps=null;

    public ResourceAcceptor(String resourcePath) {
        pathSteps=resourcePath.split(Strings.SLASH);
    }

    public boolean acceptsResource(String resourcePath) {
        /* A better programmer would use regular expressions here.
         But then he would have two problems...
         */
        String[] inputPathSteps=resourcePath.split(Strings.SLASH);
        int inputIndex=0, pathStepIndex=0;
        for (;;) {
            /* Hit the end of both paths at the same time. */
            if (inputIndex==inputPathSteps.length && pathStepIndex==pathSteps.length) {
                return true;
            }
            /* End of one path but not the other. */
            if (inputIndex==inputPathSteps.length || pathStepIndex==pathSteps.length) {
                return false;
            }
            if (pathSteps[pathStepIndex].equals(inputPathSteps[inputIndex])) {
                pathStepIndex++;
                inputIndex++;
                continue;
            }
            if (Strings.STAR.equals(pathSteps[pathStepIndex])) {
                pathStepIndex++;
                inputIndex++;
                continue;
            }
            else {
                return false;
            }
        }
    }

    public String toString() {
        return "ResourceAcceptor(" + Utils.format(pathSteps) + ")";
    }
}
