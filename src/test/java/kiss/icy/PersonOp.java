/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy;

import java.util.function.UnaryOperator;

import kiss.Binary;

/**
 * @version 2015/04/19 19:23:20
 */
public class PersonOp<M extends Person> implements ModelOperationSet<Person> {

    /** The lens for leader property. */
    public static final Lens<Person, String> NAME = Lens.of(model -> model.name, (model, value) -> new Person(value, model.age, model.gender));

    /** The lens for age property. */
    public static final Lens<Person, Integer> AGE = Lens.of(model -> model.age, (model, value) -> new Person(model.name, value, model.gender));

    /** Name property. */
    private String name;

    /** Age property. */
    private int age;

    /** Gender property. */
    private Gender gender;

    private final Lens lens;

    /**
     * 
     */
    public PersonOp() {
        this.lens = null;
    }

    /**
     * @param lens
     */
    public PersonOp(Lens<?, Person> lens) {
        this.lens = lens;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person build() {
        return new Person(name, age, gender);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void with(Person model) {
        this.name = model.name;
        this.age = model.age;
        this.gender = model.gender;
    }

    /**
     * <p>
     * Assign name property.
     * </p>
     * 
     * @param name
     * @return
     */
    public PersonOp name(String name) {
        this.name = name;

        // Chainable API
        return this;
    }

    public <M> UnaryOperator<M> name2(String name) {
        return model -> (M) lens.then(NAME).set(model, name);
    }

    /**
     * <p>
     * Assign age property.
     * </p>
     * 
     * @param age
     * @return
     */
    public PersonOp age(int age) {
        this.age = age;

        // Chainable API
        return this;
    }

    /**
     * <p>
     * Assign gender property.
     * </p>
     * 
     * @param age
     * @return
     */
    public PersonOp gender(Gender gender) {
        this.gender = gender;

        // Chainable API
        return this;
    }

    /**
     * <p>
     * Create operation.
     * </p>
     * 
     * @param name
     */
    public static Binary<Lens<Person, String>, String> nameIs(String name) {
        return new Binary(NAME, name);
    }

    /**
     * <p>
     * Create operation.
     * </p>
     * 
     * @param name
     */
    public static ModelOperation<PersonOp> ageIs(int age) {
        return op -> op.age(age);
    }

    /**
     * @param name
     * @param age
     * @param gender
     * @return
     */
    public static Person with(String name, int age, Gender gender) {
        return new PersonOp().name(name).age(age).gender(gender).build();
    }
}
