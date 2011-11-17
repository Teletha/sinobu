/*
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
package ezbean.module;

import org.junit.Test;

import ezbean.I;
import ezbean.sample.bean.Person;

/**
 * @version 2011/03/22 17:06:14
 */
public class ResourceLoadingTest {

    @Test
    public void loadResourceByNormalClass() {
        Person person = new Person();
        assert person.getClass().getResource(Person.class.getSimpleName() + ".class") != null;
    }

    @Test
    public void loadResourceByEnhancedClass() {
        Person person = I.make(Person.class);
        assert person.getClass().getResource(Person.class.getSimpleName() + ".class") != null;
    }
}
