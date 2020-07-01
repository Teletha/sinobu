/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/03/02 13:36:05
 */
class IsSignaledTest extends SignalTester {

    @Test
    void value() {
        monitor(Object.class, Boolean.class, signal -> signal.isSignaled());

        assert main.emit(1, 2, 3).value(true);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(Object.class, Boolean.class, signal -> signal.isSignaled());

        assert main.emit(Error.class).value(true);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void complete() {
        monitor(Object.class, Boolean.class, signal -> signal.isSignaled());

        assert main.emit(Complete).value(true);
        assert main.isCompleted();
        assert main.isDisposed();
    }
}