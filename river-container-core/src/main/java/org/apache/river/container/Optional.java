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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 <p>
 Annotation to indicate that the contents of the annotated field should
 be injected by the AnnotatedClassDeployer when the appropriate reference
 becomes available.
 </p>

 <p>
 By default, the injection is done by type; in other words the value is set
 to the first thing in the context that is assignable to the target field.
 If the annotation includes the 'name' attribute, then the injection is done
 by name; the value is set to whatever is stored under that name in the context.
 </p>

 <p>
 If the type of the target happens to be Context, then the context itself
 will be injected.
 </p>

 <p>
 An object in the context will not be injected into any other object until
 it has been fully resolved and its initialization method has been called.
 Nonetheless, the target object should not do anything with the injected
 resource as part of the 'setter' method; it should initialize itself inside
 a method flagged with the @Init annotation, which will be called when all the
 @Injected fields/methods have been satisfied.
 </p>
 * @author trasukg
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Optional {
    String value() default Strings.EMPTY;
    InjectionStyle style() default InjectionStyle.DEFAULT;
}
