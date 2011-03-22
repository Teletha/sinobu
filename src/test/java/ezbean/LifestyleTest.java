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
package ezbean;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import ezbean.sample.bean.Person;
import ezbean.sample.bean.Student;
import ezunit.PrivateModule;

/**
 * @version 2011/03/22 16:32:28
 */
public class LifestyleTest {

    @Rule
    public static final PrivateModule module = new PrivateModule();

    @Test
    public void override() {
        With object = I.make(With.class);
        assert object != I.make(With.class);

        // unload module
        module.unload();

        object = I.make(With.class);
        assert object == I.make(With.class);
    }

    @Test
    public void unload() {
        assert WithoutLifestyle.object == I.make(Without.class);
        assert WithoutLifestyle.object == I.make(Without.class);

        // unload module
        module.unload();

        Without object = I.make(Without.class);
        assert WithoutLifestyle.object != object;
        assert object != I.make(Without.class);
    }

    @Test
    public void unloadOverridden() {
        assert Locale.ROOT.equals(I.make(Locale.class));

        // unload lifestyle definition
        I.make(I.class).unload(LocalLifestyle.class);

        assert Locale.getDefault().equals(I.make(Locale.class));
    }

    @Test
    public void extendPrototype() {
        Person person = I.make(Person.class);
        assert "default".equals(person.getFirstName());
    }

    @Test
    public void extendPrototypeWithClassParameter() {
        Student person = I.make(Student.class);
        assert person instanceof Student;
        assert "default".equals(person.getFirstName());
    }

    @Test
    public void customLifestyle() {
        assert WithoutLifestyle.object == I.make(Without.class);
    }

    @Test
    public void customPrototype() {
        CustomClass instance = I.make(CustomClass.class);
        assert CustomLifestyle.set.contains(instance);
    }

    /**
     * <p>
     * External class without {@link Manageable} annotation.
     * </p>
     * 
     * @version 2010/01/15 18:55:45
     */
    private static class Without {
    }

    /**
     * Custom lifestyle.
     * 
     * @version 2010/01/15 18:54:13
     */
    private static class WithoutLifestyle implements Lifestyle<Without> {

        private static final Without object = new Without();

        /**
         * @see ezbean.Lifestyle#resolve()
         */
        public Without resolve() {
            return object;
        }
    }

    /**
     * <p>
     * External class with {@link Manageable} annotation.
     * </p>
     * 
     * @version 2010/01/15 18:55:45
     */
    @Manageable(lifestyle = Singleton.class)
    private static class With {
    }

    /**
     * Custom lifestyle.
     * 
     * @version 2010/01/15 18:54:13
     */
    @SuppressWarnings("unused")
    private static class WithLifestyle1 implements Lifestyle<With> {

        /**
         * @see ezbean.Lifestyle#resolve()
         */
        public With resolve() {
            return new With();
        }
    }

    /**
     * Custom lifestyle.
     * 
     * @version 2010/01/15 18:54:13
     */
    @SuppressWarnings("unused")
    private static class WithLifestyle2 implements Lifestyle<With> {

        /**
         * @see ezbean.Lifestyle#resolve()
         */
        public With resolve() {
            return new With();
        }
    }

    /**
     * @version 2010/01/16 13:10:29
     */
    @SuppressWarnings("unused")
    private static class PersonLifestyle extends Prototype<Person> {

        public PersonLifestyle() {
            super(Person.class);
        }

        /**
         * @see ezbean.Prototype#resolve()
         */
        @Override
        public Person resolve() {
            Person person = super.resolve();

            person.setFirstName("default");

            return person;
        }
    }

    /**
     * @version 2010/01/16 13:10:29
     */
    @SuppressWarnings("unused")
    private static class StudentLifestyle extends Prototype<Student> {

        public StudentLifestyle() {
            super(Student.class);
        }

        /**
         * @see ezbean.Prototype#resolve()
         */
        @Override
        public Student resolve() {
            Student person = super.resolve();

            person.setFirstName("default");

            return person;
        }
    }

    /**
     * @version 2010/01/18 18:25:34
     */
    private static class LocalLifestyle implements Lifestyle<Locale> {

        /**
         * @see ezbean.Lifestyle#resolve()
         */
        public Locale resolve() {
            return Locale.ROOT;
        }
    }

    /**
     * @version 2010/01/22 1:20:16
     */
    private static class CustomLifestyle<M> extends Prototype<M> {

        private static Set set = new HashSet();

        public CustomLifestyle(Class<M> modelClass) {
            super(modelClass);
            assert CustomClass.class.equals(modelClass);
        }

        /**
         * @see ezbean.Prototype#resolve()
         */
        @Override
        public M resolve() {
            M m = super.resolve();

            set.add(m);

            return m;
        }
    }

    @Manageable(lifestyle = CustomLifestyle.class)
    private static class CustomClass {

    }
}
