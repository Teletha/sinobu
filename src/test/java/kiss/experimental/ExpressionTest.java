/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.experimental;

import java.util.HashMap;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import kiss.sample.bean.Person;
import kiss.sample.bean.StringList;
import kiss.sample.bean.StringMap;

/**
 * 
 */
class ExpressionTest {

    @Test
    void variable() {
        Object context = define("item", "minimum language");

        assert Expression.express("I want {item}.", context).equals("I want minimum language.");
    }

    @Test
    void variableNotFound() {
        Object context = define();

        assert Expression.express("unknown variable is {ignored}", context).equals("unknown variable is ");
    }

    @Test
    void variables() {
        Object context = define("multiple", "all").define("variable", "values");

        assert Expression.express("{multiple} {variable} are accepted", context).equals("all values are accepted");
    }

    @Test
    void variableLocation() {
        assert Expression.express("{variableOnly}", define("variableOnly", "ok")).equals("ok");
        assert Expression.express("{head} is accepted", define("head", "this")).equals("this is accepted");
        assert Expression.express("tail is {accepted}", define("accepted", "ok")).equals("tail is ok");
        assert Expression.express("middle {is} fine too", define("is", "is")).equals("middle is fine too");
    }

    @Test
    void variableDescription() {
        assert Expression.express("{ spaceAtHead}", define("spaceAtHead", "ok")).equals("ok");
        assert Expression.express("{spaceAtTail }", define("spaceAtTail", "ok")).equals("ok");
        assert Expression.express("{ space }", define("space", "ok")).equals("ok");
        assert Expression.express("{\t spaceLike　}", define("spaceLike", "ok")).equals("ok");
        assert Expression.express("{separator . withSpace}", define("separator", define("withSpace", "ok"))).equals("ok");
    }

    @Test
    void contextNull() {
        assert Expression.express("null context is {ignored}", (Object) null).equals("null context is ");
        assert Expression.express("null context is {ignored}", (Object[]) null).equals("null context is ");
    }

    @Test
    void nestedVariable() {
        Object context = define("acceptable", $ -> {
            $.define("value", "ok");
        });

        assert Expression.express("nested variable is {acceptable.value}", context).equals("nested variable is ok");
    }

    @Test
    void nestedVariableNotFound() {
        Object context = define("is", $ -> {
        });

        assert Expression.express("nested unknown variable {is.ignored}", context).equals("nested unknown variable ");
    }

    @Test
    void object() {
        Person context = new Person();
        context.setAge(15);
        context.setLastName("Kahu");
        context.setFirstName("Tino");

        assert Expression.express("{firstName}({age}) : It's noisy.", context).equals("Tino(15) : It's noisy.");
    }

    @Test
    void list() {
        StringList context = new StringList();
        context.add("one");
        context.add("two");
        context.add("three");

        assert Expression.express("{0} {1} {2}", context).equals("one two three");
    }

    @Test
    void map() {
        StringMap context = new StringMap();
        context.put("1", "one");
        context.put("2", "two");
        context.put("3", "three");

        assert Expression.express("{1} {2} {3}", context).equals("one two three");
    }

    @Test
    void method() {
        StringList context = new StringList();
        context.add("one");
        context.add("two");
        context.add("three");

        assert Expression.express("list size is {size}", context).equals("list size is 3");
    }

    @Test
    void contexts() {
        Object c1 = define("first context", "will be ignored");
        Object c2 = define("value", "variable");

        assert Expression.express("{value} is not found in first context", c1, c2).equals("variable is not found in first context");
        assert Expression.express("{$} is not found in both contexts", c1, c2).equals(" is not found in both contexts");
    }

    @Test
    void contextsHaveSameProperty() {
        Object c1 = define("highPriority", "value");
        Object c2 = define("highPriority", "unused");

        assert Expression.express("first context has {highPriority}", c1, c2).equals("first context has value");
    }

    /**
     * Shorthand to create empty {@link Context}.
     * 
     * @return
     */
    private Context define() {
        return new Context();
    }

    /**
     * Shorthand to create {@link Context}.
     * 
     * @param key
     * @param value
     * @return
     */
    private Context define(String key, Object value) {
        return new Context().define(key, value);
    }

    /**
     * Shorthand to create {@link Context}.
     * 
     * @param key
     * @param value
     * @return
     */
    private Context define(String key, Consumer<Context> nested) {
        Context nest = new Context();
        nested.accept(nest);

        return new Context().define(key, nest);
    }

    /**
     * 
     */
    @SuppressWarnings("serial")
    private static class Context extends HashMap<String, Object> {

        Context define(String key, Object value) {
            put(key, value);
            return this;
        }
    }
}
