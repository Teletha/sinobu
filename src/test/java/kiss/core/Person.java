/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;


/**
 * @version 2015/04/17 23:34:25
 */
public class Person {

    public final String name;

    public final int age;

    /**
     * @param name
     */
    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Person [name=" + name + ", age=" + age + "]";
    }
}
