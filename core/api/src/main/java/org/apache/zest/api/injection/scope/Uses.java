/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.api.injection.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.zest.api.injection.InjectionScope;

/**
 * Annotation to denote the injection of a dependency to be used by a Mixin. The injected
 * object is provided either by the TransientBuilder.uses() declarations, or if an instance of the appropriate types is not
 * found, then a new Transient or Object is instantiated.
 * Call {@link org.apache.zest.api.composite.TransientBuilder#use} to provide the instance
 * to be injected.
 *
 * Example:
 * <pre>@Uses SomeType someInstance</pre>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.PARAMETER, ElementType.FIELD } )
@Documented
@InjectionScope
public @interface Uses
{
}