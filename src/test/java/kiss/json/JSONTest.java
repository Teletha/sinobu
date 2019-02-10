/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.model.Model;
import kiss.model.Property;
import kiss.sample.bean.BuiltinBean;
import kiss.sample.bean.ChainBean;
import kiss.sample.bean.DefaultValue;
import kiss.sample.bean.NestingList;
import kiss.sample.bean.Person;
import kiss.sample.bean.Primitive;
import kiss.sample.bean.School;
import kiss.sample.bean.StringListProperty;
import kiss.sample.bean.StringMapProperty;
import kiss.sample.bean.Student;
import kiss.sample.bean.TransientBean;
import kiss.sample.bean.VariablePropertyAtField;

/**
 * @version 2018/10/04 21:48:35
 */
class JSONTest {

    @Test
    void empty() {
        BuiltinBean instance = new BuiltinBean();

        // @formatter:off
        validate(instance,
        "{",
        "  'bigDecimal': null,",
        "  'bigInteger': null,",
        "  'file': null,",
        "  'schoolEnum': null,",
        "  'someClass': null,",
        "  'stringBuffer': null,",
        "  'stringBuilder': null",
        "}");
        // @formatter:on
    }

    @Test
    void singleProperty() {
        Person person = new Person();
        person.setAge(20);

        // @formatter:off
        validate(person,
        "{",
        "  'age': 20,",
        "  'firstName': null,",
        "  'lastName': null",
        "}");
        // @formatter:on
    }

    @Test
    void multipleProperties() {
        Person person = new Person();
        person.setAge(20);
        person.setFirstName("Umi");
        person.setLastName("Sonoda");

        // @formatter:off
        validate(person,
        "{",
        "  'age': 20,",
        "  'firstName': 'Umi',",
        "  'lastName': 'Sonoda'",
        "}");
        // @formatter:on
    }

    @Test
    void transientProperty() {
        TransientBean bean = I.make(TransientBean.class);
        bean.field = "transient";
        bean.noneField = "serializable";

        // @formatter:off
        validate(bean,
        "{",
        "  'noneField': 'serializable'",
        "}");
        // @formatter:on
    }

    @Test
    void variableProperty() {
        VariablePropertyAtField bean = I.make(VariablePropertyAtField.class);
        bean.string.set("value");
        bean.integer.set(10);
        bean.list.get().add("first");
        bean.list.get().add("second");
        bean.map.get().put("one", 11L);
        bean.map.get().put("two", 222L);

        // @formatter:off
        validate(bean,
        "{",
        "  'integer': '10',",
        "  'string': 'value',",
        "  'list': [",
        "    'first',",
        "    'second'",
        "  ],",
        "  'map': {",
        "    'one': '11',",
        "    'two': '222'",
        "  }",
        "}");
        // @formatter:on
    }

    @Test
    void defaultValue() {
        DefaultValue instant = new DefaultValue();

        // @formatter:off
        validate(instant,
        "{",
        "  'value': 'default',",
        "  'items': [",
        "    'default'",
        "  ]",
        "}");
         // @formatter:on

        // clear value
        instant.value = null;
        instant.items = null;

        // @formatter:off
        validate(instant,
        "{",
        "  'value': null,",
        "  'items': []",
        "}");
        // @formatter:on
    }

    @Test
    void list() {
        List<String> list = new ArrayList();
        list.add("one");
        list.add("two");
        list.add("three");
        StringListProperty strings = I.make(StringListProperty.class);
        strings.setList(list);

        // @formatter:off
        validate(strings,
        "{",
        "  'list': [",
        "    'one',",
        "    'two',",
        "    'three'",
        "  ]",
        "}");
        // @formatter:on
    }

    @Test
    void listNull() {
        List<String> list = new ArrayList();
        list.add(null);
        list.add("null");
        list.add(null);
        StringListProperty strings = I.make(StringListProperty.class);
        strings.setList(list);

        // @formatter:off
        validate(strings,
        "{",
        "  'list': [",
        "    null,",
        "    'null',",
        "    null",
        "  ]",
        "}");
        // @formatter:on
    }

    @Test
    void listNested() {
        NestingList list = I.make(NestingList.class);
        list.setNesting(Arrays.asList(Collections.EMPTY_LIST, Collections.EMPTY_LIST));

        // @formatter:off
        validate(list,
        "{",
        "  'nesting': [",
        "    [],",
        "    []",
        "  ]",
        "}");
        // @formatter:on
    }

    @Test
    void map() {
        Map<String, String> map = new LinkedHashMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");
        StringMapProperty strings = I.make(StringMapProperty.class);
        strings.setMap(map);

        // @formatter:off
        validate(strings,
        "{",
        "  'map': {",
        "    'one': '1',",
        "    'two': '2',",
        "    'three': '3'",
        "  }",
        "}");
        // @formatter:on
    }

    @Test
    void mapNull() {
        Map<String, String> map = new LinkedHashMap();
        map.put(null, null);
        map.put("null", "NULL");
        StringMapProperty strings = I.make(StringMapProperty.class);
        strings.setMap(map);

        // @formatter:off
        validate(strings,
        "{",
        "  'map': {",
        "    'null': 'NULL'",
        "  }",
        "}");
        // @formatter:on
    }

    @Test
    void primitives() {
        Primitive primitive = I.make(Primitive.class);
        primitive.setBoolean(true);
        primitive.setChar('c');
        primitive.setInt(-5);
        primitive.setFloat(0.1f);

        // @formatter:off
        validate(primitive,
        "{",
        "  'boolean': true,",
        "  'byte': 0,",
        "  'char': 'c',",
        "  'double': 0.0,",
        "  'float': 0.1,",
        "  'int': -5,",
        "  'long': 0,",
        "  'short': 0",
        "}");
        // @formatter:on
    }

    @Test
    void escaped() {
        Person person = new Person();
        person.setAge(20);
        person.setFirstName("A\r\nA\t");
        person.setLastName("B\n\"\\B");

        // @formatter:off
        validate(person,
        "{",
        "  'age': 20,",
        "  'firstName': 'A\\r\\nA\\t',",
        "  'lastName': 'B\\n\\\"\\\\B'",
        "}");
        // @formatter:on
    }

    @Test
    void codecValue() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setSomeClass(String.class);
        bean.setBigInteger(new BigInteger("1234567890987654321"));

        // @formatter:off
        validate(bean,
        "{",
        "  'bigDecimal': null,",
        "  'bigInteger': '1234567890987654321',",
        "  'file': null,",
        "  'schoolEnum': null,",
        "  'someClass': 'java.lang.String',",
        "  'stringBuffer': null,",
        "  'stringBuilder': null",
        "}");
        // @formatter:on
    }

    @Test
    void nestedObject() {
        School school = I.make(School.class);
        school.setName("Sakura High School");

        Student student = I.make(Student.class);
        student.setAge(15);
        student.setFirstName("Mio");
        student.setLastName("Akiyama");
        student.setSchool(school);

        // @formatter:off
        validate(student,
        "{",
        "  'age': 15,",
        "  'firstName': 'Mio',",
        "  'lastName': 'Akiyama',",
        "  'school': {",
        "    'name': 'Sakura High School',",
        "    'students': [],",
        "    'teachers': {}",
        "  }",
        "}");
        // @formatter:on
    }

    @Test
    void readIncompatible() {
        Person instance = I.read("15", I.make(Person.class));
        assert instance != null;
        assert instance.getAge() == 0;
        assert instance.getFirstName() == null;
        assert instance.getLastName() == null;
    }

    @Test
    void cyclic() {
        assertThrows(ClassCircularityError.class, () -> {
            ChainBean chain = I.make(ChainBean.class);
            chain.setNext(chain);
            validate(chain);
        });
    }

    /**
     * <p>
     * Write JSON.
     * </p>
     * 
     * @param texts
     * @return
     */
    private static <M> void validate(M object, String... texts) {
        StringBuilder output = new StringBuilder();
        I.write(object, output);
        String serialized = output.toString();

        StringJoiner joiner = new StringJoiner("\r\n");
        for (String text : texts) {
            text = text.replaceAll("'", "\"");
            joiner.add(text.replaceAll("  ", "\t"));
        }

        // validate serialized text
        assert joiner.toString().equals(serialized);

        // validate model and properties
        Model model = Model.of(object.getClass());

        // write and read
        validate(model, object, I.read(serialized, I.make((Class<M>) model.type)));
    }

    /**
     * <p>
     * Validate object by model.
     * </p>
     * 
     * @param model
     * @param one
     * @param other
     */
    private static void validate(Model<Object> model, Object one, Object other) {
        for (Property property : model.properties()) {
            Object oneValue = model.get(one, property);
            Object otherValue = model.get(other, property);

            if (property.isTransient) {
                // ignore
            } else if (property.isAttribute()) {
                if (oneValue == null) {
                    assert otherValue == null;
                } else {
                    assert oneValue.equals(otherValue);
                }
            } else {
                validate(property.model, oneValue, otherValue);
            }
        }
    }
}
