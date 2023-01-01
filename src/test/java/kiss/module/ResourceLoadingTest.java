/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.module;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.sample.bean.Person;

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