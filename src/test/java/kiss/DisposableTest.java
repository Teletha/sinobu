/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import org.junit.jupiter.api.Test;

/**
 * @version 2014/07/19 0:51:19
 */
public class DisposableTest {

    @Test
    public void and() throws Exception {
        Task task1 = new Task();
        Task task2 = new Task();
        Disposable composed = task1.add(task2);

        assert task1.executed == false;
        assert task2.executed == false;

        composed.dispose();
        assert task1.executed == true;
        assert task2.executed == true;
    }

    /**
     * @version 2014/01/31 16:41:02
     */
    private static class Task implements Disposable {

        private boolean executed;

        /**
         * {@inheritDoc}
         */
        @Override
        public void vandalize() {
            executed = true;
        }
    }
}
