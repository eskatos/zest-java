/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
@Documented
@Inherited
/**
 * Applies a virtual annotation to a method or a mixin.
 *
 * <pre><code>
 * @Apply( toMethods =
 *   {
 *       @ToMethod( methods = "set*,add*,remove*", annotation = Write.class),
 *       @ToMethod( target=HelloWorldSettingsImpl.class, methods = "set*", annotation = TransactionRequired.class )
 *   },
 *       toMixins =
 *   {
 *       @ToMixin( target= PropertiesMixin.class, annotation = TransactionRequired.class ),
 *       @ToMixin( target= HelloWorldSpeaker.class, annotation = Scripted.class )
 *   }
 *)
 *</code></pre>
 */
public @interface Apply
{
    ToMethod[] toMethods();

    ToMixin[] toMixins();
}
