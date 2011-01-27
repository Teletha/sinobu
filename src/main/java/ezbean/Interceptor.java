/**
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import java.lang.annotation.Annotation;

import ezbean.model.Model;

/**
 * @version 2010/11/12 9:28:46
 */
public class Interceptor<P extends Annotation> implements Extensible {

    /** The actual object. */
    protected Accessible that;

    /** The associated annotation. */
    protected P annotation;

    /** The property identifier. */
    private int id;

    /** The property name. */
    private String name;

    /** The parent interceptor to chain. */
    private Interceptor parent;

    /**
     * <p>
     * Intercept property access.
     * </p>
     * 
     * @param param A new value.
     */
    protected void invoke(Object param) {
        if (parent != null) {
            parent.invoke(param);
        } else {
            // Retrieve old value.
            Object old = that.access(id, null);

            // Apply new value.
            that.access(id + 2, param);

            // Notify to all listeners.
            that.context().notify(that, name, old, param);
        }
    }

    /**
     * <p>
     * NOTE : This is internal method. A user of Ezbean <em>does not have to use</em> this method.
     * </p>
     * 
     * @param that A current processing object.
     * @param id A property id.
     * @param name A property name.
     * @param param A new value.
     */
    public static final void invoke(Accessible that, int id, String name, Object param) {
        Interceptor current = new Interceptor();
        current.id = id;
        current.name = name;
        current.that = that;

        Annotation[] annotations = Model.load(that.getClass()).getProperty(name).getAccessor(true).getAnnotations();

        for (int i = annotations.length - 1; 0 <= i; --i) {
            Interceptor interceptor = I.find(Interceptor.class, annotations[i].annotationType());

            if (interceptor != null) {
                interceptor.that = that;
                interceptor.parent = current;
                interceptor.annotation = annotations[i];

                current = interceptor;
            }
        }

        // Invoke chain of interceptors.
        current.invoke(param);
    }
}
