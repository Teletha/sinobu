/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.json;

import java.util.List;
import java.util.StringJoiner;

import org.junit.Test;

import kiss.I;
import kiss.JSON;
import kiss.sample.bean.Person;

/**
 * @version 2017/03/26 9:51:00
 */
public class JsonReadWithPathTest {

    @Test
    public void integer() throws Exception {
        // @formatter:off
        JSON json = json(
        "{",
        "  'age': 20",
        "}");
        // @formatter:on

        List<Integer> values = json.find("age", int.class).toList();
        assert values.size() == 1;
        assert values.get(0) == 20;
    }

    @Test
    public void string() throws Exception {
        // @formatter:off
        JSON json = json(
        "{",
        "  'name': 'Jill'",
        "}");
        // @formatter:on

        List<String> values = json.find("name").toList();
        assert values.size() == 1;
        assert values.get(0).equals("Jill");
    }

    @Test
    public void null_() throws Exception {
        // @formatter:off
        JSON json = json(
        "{",
        "  'name': null",
        "}");
        // @formatter:on

        List<String> values = json.find("name").toList();
        assert values.size() == 1;
        assert values.get(0) == null;
    }

    @Test
    public void nest() throws Exception {
        // @formatter:off
        JSON json = json(
        "{",
        "  'city': {",
        "    'id': 'NY'",
        "  }",
        "}");
        // @formatter:on

        List<String> values = json.find("city.id").toList();
        assert values.size() == 1;
        assert values.get(0).equals("NY");
    }

    @Test
    public void nestObject() throws Exception {
        // @formatter:off
        JSON json = json(
        "{",
        "  'city': {",
        "    'id': 'NY'",
        "  }",
        "}");
        // @formatter:on

        List<String> values = json.find("city.id").toList();
        assert values.size() == 1;
        assert values.get(0).equals("NY");
    }

    @Test
    public void arrayAll() throws Exception {
        // @formatter:off
        JSON json = json(
        "{",
        "  'names': [",
        "    'Jill',",
        "    'Bell',",
        "    'Alice'",
        "  ]",
        "}");
        // @formatter:on

        List<String> values = json.find("names").toList();
        assert values.size() == 3;
        assert values.get(0).equals("Jill");
        assert values.get(1).equals("Bell");
        assert values.get(2).equals("Alice");
    }

    @Test
    public void arrayIndex() throws Exception {
        // @formatter:off
        JSON json = json(
        "{",
        "  'names': [",
        "    'Jill',",
        "    'Bell',",
        "    'Alice'",
        "  ]",
        "}");
        // @formatter:on

        List<String> values = json.find("names[1]").toList();
        assert values.size() == 1;
        assert values.get(0).equals("Bell");
    }

    @Test
    public void arrayObject() throws Exception {
        // @formatter:off
        JSON json = json(
        "{",
        "  'names': [",
        "    {'name': 'Jill'},",
        "    {'name': 'Bell'},",
        "    {'name': 'Alice'}",
        "  ]",
        "}");
        // @formatter:on

        List<String> values = json.find("names.name").toList();
        assert values.size() == 3;
        assert values.get(0).equals("Jill");
        assert values.get(1).equals("Bell");
        assert values.get(2).equals("Alice");
    }

    @Test
    public void arrayIndexObject() throws Exception {
        // @formatter:off
        JSON json = json(
        "{",
        "  'names': [",
        "    {'name': 'Jill'},",
        "    {'name': 'Bell'},",
        "    {'name': 'Alice'}",
        "  ]",
        "}");
        // @formatter:on

        List<String> values = json.find("names[1].name").toList();
        assert values.size() == 1;
        assert values.get(0).equals("Bell");
    }

    @Test
    public void arrayModel() throws Exception {
        // @formatter:off
        JSON json = json(
        "{",
        "  'names': [",
        "    {'firstName': 'Jill'},",
        "    {'firstName': 'Bell'},",
        "    {'firstName': 'Alice'}",
        "  ]",
        "}");
        // @formatter:on

        List<Person> values = json.find("names", Person.class).toList();
        assert values.size() == 3;
        assert values.get(0).getFirstName().equals("Jill");
        assert values.get(1).getFirstName().equals("Bell");
        assert values.get(2).getFirstName().equals("Alice");
    }

    /**
     * <p>
     * Write JSON.
     * </p>
     * 
     * @param texts
     * @return
     */
    private static JSON json(String... texts) {
        StringJoiner joiner = new StringJoiner("\r\n");
        for (String text : texts) {
            text = text.replaceAll("'", "\"");
            joiner.add(text.replaceAll("  ", "\t"));
        }
        return I.json(joiner.toString());
    }
}
