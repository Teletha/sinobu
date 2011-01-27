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
package ezbean.model;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.junit.Test;

import ezbean.I;
import ezbean.sample.bean.GenericBoundedBean;
import ezbean.sample.bean.Student;

/**
 * @version 2010/02/15 15:09:38
 */
public class ClassUtilTest {

    /**
     * Test method for {@link ezbean.model.ClassUtil#getTypes(java.lang.Class)}.
     */
    @Test
    public void testGetAllTypes01() {
        Set<Class> classes = ClassUtil.getTypes(ExtendClass.class);
        assertEquals(11, classes.size());
        assertTrue(classes.contains(ExtendClass.class));
    }

    /**
     * Test method for {@link ezbean.model.ClassUtil#getTypes(java.lang.Class)}.
     */
    @Test
    public void testGetAllTypes02() {
        Set<Class> classes = ClassUtil.getTypes(null);
        assertEquals(0, classes.size());
    }

    /**
     * Test none constructor class.
     */
    @Test
    public void testGetMiniConstructor01() {
        Constructor constructor = ClassUtil.getMiniConstructor(NoneConstructor.class);
        assertNotNull(constructor);
    }

    /**
     * Test one constructor class.
     */
    @Test
    public void testGetMiniConstructor02() {
        Constructor constructor = ClassUtil.getMiniConstructor(OneConstructor.class);
        assertNotNull(constructor);
        assertEquals(1, constructor.getParameterTypes().length);
    }

    /**
     * Test two constructor class.
     */
    @Test
    public void testGetMiniConstructor03() {
        Constructor constructor = ClassUtil.getMiniConstructor(TwoConstructor.class);
        assertNotNull(constructor);
        assertEquals(1, constructor.getParameterTypes().length);
    }

    /**
     * Test two constructor class.
     */
    @Test
    public void testGetMiniConstructor04() {
        Constructor constructor = ClassUtil.getMiniConstructor(HashMap.class);
        assertNotNull(constructor);
        assertEquals(0, constructor.getParameterTypes().length);
    }

    /**
     * Test {@link String} parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes01() {
        Type[] types = ClassUtil.getParameter(ParameterizedStringByInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test {@link Object} parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes02() {
        Type[] types = ClassUtil.getParameter(ParameterizedObjectByInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(Object.class, types[0]);
    }

    /**
     * Test wildcard parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes14() {
        Type[] types = ClassUtil.getParameter(ParameterizedWildcardByInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(Map.class, types[0]);
    }

    /**
     * Test none parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes03() {
        Type[] types = ClassUtil.getParameter(ParameterizedNoneByInterface.class, ParameterInterface.class);
        assertEquals(0, types.length);
    }

    /**
     * Test parent parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes04() {
        Type[] types = ClassUtil.getParameter(ExtendedFromInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    @Test
    public void parameterFromOverriddenInterface() {
        Type[] types = ClassUtil.getParameter(TypedExtendedFromInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    @Test
    public void parameterFromOverrideInterface() {
        Type[] types = ClassUtil.getParameter(TypedExtendedFromInterface.class, ExtensibleByInterface.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test {@link String} parameter with class.
     */
    @Test
    public void testGetParameterizedTypes06() {
        Type[] types = ClassUtil.getParameter(ParameterizedStringByClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test {@link Object} parameter with class.
     */
    @Test
    public void testGetParameterizedTypes07() {
        Type[] types = ClassUtil.getParameter(ParameterizedObjectByClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(Object.class, types[0]);
    }

    /**
     * Test wildcard parameter with class.
     */
    @Test
    public void testGetParameterizedTypes15() {
        Type[] types = ClassUtil.getParameter(ParameterizedWildcardByClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(Map.class, types[0]);
    }

    /**
     * Test none parameter with class.
     */
    @Test
    public void testGetParameterizedTypes08() {
        Type[] types = ClassUtil.getParameter(ParameterizedNoneByClass.class, ParameterClass.class);
        assertEquals(0, types.length);
    }

    /**
     * Test none parameter with class.
     */
    @Test
    public void testGetParameterizedTypes09() {
        Type[] types = ClassUtil.getParameter(ExtendedFromClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test parent variable parameter with class.
     */
    @Test
    public void testGetParameterizedTypes10() {
        Type[] types = ClassUtil.getParameter(TypedExtendedFromClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test parameter from multiple source.
     */
    @Test
    public void testGetParameterizedTypes11() {
        Type[] types = ClassUtil.getParameter(ParameterFromMultipleSource.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(Type.class, types[0]);

        types = ClassUtil.getParameter(ParameterFromMultipleSource.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(Class.class, types[0]);
    }

    /**
     * Test multiple parameter.
     */
    @Test
    public void testGetParameterizedTypes12() {
        Type[] types = ClassUtil.getParameter(MultipleParameterClass.class, MultipleParameter.class);
        assertEquals(2, types.length);
        assertEquals(Integer.class, types[0]);
        assertEquals(Long.class, types[1]);
    }

    @Test
    public void parameterIsArrayFromInterface() {
        Type[] types = ClassUtil.getParameter(ParameterizedStringArrayByInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(String[].class, types[0]);
    }

    @Test
    public void parameterIsArrayFromClass() {
        Type[] types = ClassUtil.getParameter(ParameterizedStringArrayByClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String[].class, types[0]);
    }

    @Test
    public void methodGetPrameterAcceptsNullType() {
        Type[] types = ClassUtil.getParameter(null, ParameterClass.class);
        assertEquals(0, types.length);
    }

    @Test
    public void methodGetPrameterAcceptsNullTarget() {
        Type[] types = ClassUtil.getParameter(ParameterizedStringByClass.class, null);
        assertEquals(0, types.length);
    }

    @Test
    public void subclassHasAnotherParameter() {
        Type[] types = ClassUtil.getParameter(TypedSubClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    @Test
    public void constructorHasParameterClass() {
        Constructor constructor = ClassUtil.getMiniConstructor(ParameterClassConstructor.class);
        Type[] types = ClassUtil.getParameter(constructor.getGenericParameterTypes()[0], ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    @Test
    public void constructorHasExtendableByClass() {
        Constructor constructor = ClassUtil.getMiniConstructor(ExtensibleByClassConstructor.class);
        Type[] types = ClassUtil.getParameter(constructor.getGenericParameterTypes()[0], ExtensibleByClass.class);
        assertEquals(1, types.length);
        assertEquals(Integer.class, types[0]);
    }

    @Test
    public void constructorHasArrayParameter() {
        Constructor constructor = ClassUtil.getMiniConstructor(ArrayParameterConstructor.class);
        Type[] types = ClassUtil.getParameter(constructor.getGenericParameterTypes()[0], ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String[].class, types[0]);
    }

    @Test
    public void constructorHasMultipleParameter() {
        Constructor constructor = ClassUtil.getMiniConstructor(MultipleParameterConstructor.class);
        Type[] types = ClassUtil.getParameter(constructor.getGenericParameterTypes()[0], MultipleParameter.class);
        assertEquals(2, types.length);
        assertEquals(Readable.class, types[0]);
        assertEquals(Appendable.class, types[1]);
    }

    @Test
    public void constructorHasOverlapParameter() {
        Constructor constructor = ClassUtil.getMiniConstructor(ImplicitParameterConstructor.class);
        Type[] types = ClassUtil.getParameter(constructor.getGenericParameterTypes()[0], ParameterOverlapClass.class);
        assertEquals(1, types.length);
        assertEquals(Map.class, types[0]);
    }

    @Test
    public void constructorHasOverlappedParameter() {
        Constructor constructor = ClassUtil.getMiniConstructor(ImplicitParameterConstructor.class);
        Type[] types = ClassUtil.getParameter(constructor.getGenericParameterTypes()[0], ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    @Test
    public void parameterVariableFromInterface() {
        Type[] types = ClassUtil.getParameter(ParameterVariableStringByInterface.class, ParameterVariableInterface.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    @Test
    public void parameterVariableFromClass() {
        Type[] types = ClassUtil.getParameter(ParameterVariableStringByClass.class, ParameterVariableClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    @Test
    public void bundedBean() {
        Type[] types = ClassUtil.getParameter(I.make(BoundedBean.class).getClass(), GenericBoundedBean.class);
        assertEquals(1, types.length);
        assertEquals(Student.class, types[0]);
    }

    /**
     * Wrap class.
     */
    @Test
    public void testWrap() {
        assertEquals(Integer.class, ClassUtil.wrap(int.class));
        assertEquals(Long.class, ClassUtil.wrap(long.class));
        assertEquals(Float.class, ClassUtil.wrap(float.class));
        assertEquals(Double.class, ClassUtil.wrap(double.class));
        assertEquals(Boolean.class, ClassUtil.wrap(boolean.class));
        assertEquals(Byte.class, ClassUtil.wrap(byte.class));
        assertEquals(Short.class, ClassUtil.wrap(short.class));
        assertEquals(Character.class, ClassUtil.wrap(char.class));
        assertEquals(String.class, ClassUtil.wrap(String.class));
        assertEquals(null, ClassUtil.wrap(null));
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:51:49
     */
    private static class ExtendClass extends ArrayList {

        private static final long serialVersionUID = -5962628342667538716L;
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:22:45
     */
    private static class NoneConstructor {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:23:02
     */
    private static class OneConstructor {

        /**
         * Create OneConstructor instance.
         */
        private OneConstructor(int i) {
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:24:29
     */
    private static class TwoConstructor {

        /**
         * Create TwoConstructor instance.
         */
        private TwoConstructor(int i, String name) {
        }

        /**
         * Create TwoConstructor instance.
         */
        private TwoConstructor(int i) {
        }
    }

    /**
     * @version 2010/02/19 22:37:01
     */
    private static interface ParameterInterface<T> {
    }

    /**
     * @version 2010/02/19 22:37:01
     */
    private static interface ParameterVariableInterface<T extends Serializable> {
    }

    /**
     * @version 2010/02/19 22:50:39
     */
    private static class ParameterClass<T> {
    }

    /**
     * @version 2010/02/19 22:50:39
     */
    private static class ParameterVariableClass<T extends Serializable> {
    }

    /**
     * @version 2010/02/20 0:10:12
     */
    private static class ParameterOverlapClass<S> extends ParameterClass<String> {
    }

    /**
     * @version 2010/02/19 22:46:18
     */
    private static class ParameterizedStringByInterface implements ParameterInterface<String> {
    }

    /**
     * @version 2010/02/19 22:46:18
     */
    private static class ParameterVariableStringByInterface implements ParameterVariableInterface<String> {
    }

    /**
     * @version 2010/02/15 15:34:39
     */
    private static class ParameterizedStringArrayByInterface implements ParameterInterface<String[]> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedObjectByInterface implements ParameterInterface<Object> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedWildcardByInterface<T extends Map> implements ParameterInterface<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedNoneByInterface implements ParameterInterface {
    }

    /**
     * @version 2010/02/19 22:54:31
     */
    private static class ParameterizedStringByClass extends ParameterClass<String> {
    }

    /**
     * @version 2010/02/19 22:54:31
     */
    private static class ParameterVariableStringByClass extends ParameterVariableClass<String> {
    }

    /**
     * @version 2010/02/15 15:34:39
     */
    private static class ParameterizedStringArrayByClass extends ParameterClass<String[]> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedObjectByClass extends ParameterClass<Object> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedWildcardByClass<T extends Map> extends ParameterClass<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedNoneByClass extends ParameterClass {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ExtendedFromInterface extends ParameterizedStringByInterface {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ExtendedFromClass extends ParameterizedStringByClass {
    }

    /**
     * @version 2010/02/15 15:04:45
     */
    private static class ExtensibleByInterface<T> implements ParameterInterface<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 14:44:09
     */
    private static class TypedExtendedFromInterface extends ExtensibleByInterface<String> {
    }

    /**
     * @version 2010/02/15 15:04:51
     */
    private static class ExtensibleByClass<T> extends ParameterClass<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 14:44:09
     */
    private static class TypedExtendedFromClass extends ExtensibleByClass<String> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:15:56
     */
    private static class ParameterFromMultipleSource extends ParameterClass<Class> implements ParameterInterface<Type> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:19:19
     */
    private static interface MultipleParameter<S, T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:20:00
     */
    private static class MultipleParameterClass implements MultipleParameter<Integer, Long> {
    }

    /**
     * @version 2009/07/19 18:55:16
     */
    @SuppressWarnings("hiding")
    private static class TypedSubClass<Boolean> extends ParameterClass<String> {
    }

    /**
     * @version 2010/02/15 12:55:43
     */
    private static class ParameterClassConstructor {

        private ParameterClassConstructor(ParameterClass<String> param) {
        }
    }

    /**
     * @version 2010/02/15 12:55:43
     */
    private static class ExtensibleByClassConstructor {

        private ExtensibleByClassConstructor(ExtensibleByClass<Integer> param) {
        }
    }

    /**
     * @version 2010/02/15 12:55:43
     */
    private static class ArrayParameterConstructor {

        private ArrayParameterConstructor(ParameterClass<String[]> param) {
        }
    }

    /**
     * @version 2010/02/15 15:06:14
     */
    private static class MultipleParameterConstructor {

        private MultipleParameterConstructor(MultipleParameter<Readable, Appendable> param) {
        }
    }

    /**
     * @version 2010/02/20 0:05:01
     */
    private static class ImplicitParameterConstructor {

        private ImplicitParameterConstructor(ParameterOverlapClass<Map> param) {
        }
    }

    /**
     * @version 2010/02/19 23:43:53
     */
    protected static class BoundedBean extends GenericBoundedBean<Student> {
    }

    @Test
    public void complexTypeHierarchy1() {
        Type[] types = ClassUtil.getParameter(ConsolesUI.class, StackContainer.class);
        assertEquals(2, types.length);
        assertEquals(Shell.class, types[0]);
        assertEquals(Console.class, types[1]);

    }

    @Test
    public void complexTypeHierarchy2() {
        Type[] types = ClassUtil.getParameter(ConsolesUI.class, SelectableUI.class);
        assertEquals(3, types.length);
        assertEquals(JPanel.class, types[0]);
        assertEquals(Shell.class, types[1]);
        assertEquals(Console.class, types[2]);
    }

    @Test
    public void complexTypeHierarchy3() {
        Type[] types = ClassUtil.getParameter(ConsolesUI.class, UI.class);
        assertEquals(2, types.length);
        assertEquals(JPanel.class, types[0]);
        assertEquals(Shell.class, types[1]);
    }

    /**
     * @version 2010/02/15 15:11:48
     */
    private static interface Model {
    }

    /**
     * @version 2010/02/15 15:11:46
     */
    private static class SingleSelectableMode<M extends Model> implements Model {
    }

    /**
     * @version 2010/02/15 15:11:44
     */
    private static class Console implements Model {
    }

    /**
     * @version 2010/02/15 15:11:42
     */
    private static class Shell extends SingleSelectableMode<Console> {
    }

    /**
     * @version 2010/02/15 15:11:40
     */
    private static class UI<W extends JComponent, M extends Model> {
    }

    /**
     * @version 2010/02/15 15:11:38
     */
    private static class SelectableUI<W extends JComponent, M extends SingleSelectableMode<R>, R extends Model>
            extends UI<W, M> {
    }

    /**
     * @version 2010/02/15 15:11:36
     */
    private static class StackContainer<M extends SingleSelectableMode<R>, R extends Model>
            extends SelectableUI<JPanel, M, R> {
    }

    /**
     * @version 2010/02/15 15:11:33
     */
    private static class ConsolesUI extends StackContainer<Shell, Console> {
    }
}
