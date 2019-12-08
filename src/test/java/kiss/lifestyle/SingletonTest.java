/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lifestyle;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;

import org.junit.jupiter.api.Test;

/**
 * @version 2011/03/22 16:27:26
 */
public class SingletonTest {

    @Test
    public void resolve() {
        SingletonClass instance1 = I.make(SingletonClass.class);
        assert instance1 != null;

        SingletonClass instance2 = I.make(SingletonClass.class);
        assert instance2 != null;
        assert instance1 == instance2;
    }

    /**
     * @version 2011/03/22 16:29:43
     */
    @Managed(value = Singleton.class)
    private static class SingletonClass {
    }
}
