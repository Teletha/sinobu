/*
 * Copyright (C) 2010 Nameless Production Committee.
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

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import ezbean.sample.bean.Person;
import ezbean.sample.bean.School;
import ezbean.sample.bean.Student;
import ezbean.unit.ClassModule;

/**
 * @version 2009/12/30 15:38:41
 */
public class ExtensibleTest {

    @Rule
    public static ClassModule module = new ClassModule();

    @Test
    public void listByClass() {
        assertEquals(5, I.find(EPClass.class).size());
    }

    @Test
    public void listByInterface() {
        assertEquals(4, I.find(EPInterface.class).size());
    }

    @Test
    public void listByNonExtensionPoint() {
        assertEquals(0, I.find(EPBoth.class).size());
    }

    @Test(expected = NullPointerException.class)
    public void listByNull() {
        I.find(null);
    }

    @Test
    public void unlist() throws Exception {
        assertEquals(5, I.find(EPClass.class).size());
        I.unload(module.moduleFile);
        assertEquals(0, I.find(EPClass.class).size());
    }

    @Test
    public void key() throws Exception {
        KEPClass extension = I.find(KEPClass.class, Person.class);
        assertNotNull(extension);
        assertEquals(KEPClassExtension1.class, extension.getClass());

        extension = I.find(KEPClass.class, String.class);
        assertNotNull(extension);
        assertEquals(KEPClassExtension2.class, extension.getClass());
    }

    @Test
    public void keyBySubclass() throws Exception {
        assertNull(I.find(KEPClass.class, Student.class));
    }

    @Test
    public void keyByNonExistence() throws Exception {
        assertNull(I.find(KEPClass.class, School.class));
    }

    @Test
    public void keyByNull() throws Exception {
        assertNull(I.find(null, null));
    }

    @Test
    public void keyByNullKey() throws Exception {
        assertNull(I.find(KEPClass.class, null));
    }

    @Test
    public void keyByNullExtensionPoint() throws Exception {
        assertNull(I.find(null, Person.class));
    }

    @Test
    public void unkey() throws Exception {
        KEPClass extension = I.find(KEPClass.class, Person.class);
        assertNotNull(extension);
        assertEquals(KEPClassExtension1.class, extension.getClass());

        I.unload(module.moduleFile);
        assertNull(I.find(KEPClass.class, Person.class));
    }

    @Test
    public void unSameKey() throws Exception {
        KEPClass extension = I.find(KEPClass.class, Integer.class);
        assertNotNull(extension);
        assertEquals(SameKEP2.class, extension.getClass());

        I.make(I.class).unload(SameKEP2.class);
        extension = I.find(KEPClass.class, Integer.class);
        assertNotNull(extension);
        assertEquals(SameKEP1.class, extension.getClass());

        I.make(I.class).unload(SameKEP1.class);
        assertNull(I.find(KEPClass.class, Integer.class));
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static interface EPInterface extends Extensible {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class EPClass implements Extensible {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPClassExtension1 extends EPClass {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPClassExtension2 extends EPClass {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static abstract class EPClassExtension3 extends EPClass {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPClassExtension4 extends EPClassExtension3 {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPInterfaceExtension1 implements EPInterface {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPInterfaceExtension2 implements EPInterface {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static abstract class EPInterfaceExtension3 implements EPInterface {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPInterfaceExtension4 extends EPInterfaceExtension3 {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class EPBoth extends EPClass implements EPInterface {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static interface KEPInterface<K> extends Extensible {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class KEPClass<K> implements Extensible {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class KEPClassExtension1 extends KEPClass<Person> {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class KEPClassExtension2 extends KEPClass<String> {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class SameKEP1 extends KEPClass<Integer> {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class SameKEP2 extends KEPClass<Integer> {
    }
}
