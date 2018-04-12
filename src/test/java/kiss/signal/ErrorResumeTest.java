/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import kiss.Signal;

/**
 * @version 2018/03/11 2:53:01
 */
class ErrorResumeTest extends SignalTester {

    @Test
    void resumeSignal() {
        monitor(signal -> signal.errorResume(signal("resume", "error")));

        assert main.emit("Signal", "will", Error).value("Signal", "will", "resume", "error");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void resumeNullSignal() {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume((Signal) null));

        assert main.value();
        assert main.isError();
    }

    @Test
    void resumeFunction() {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume(e -> signal(10, 11)));

        assert main.value(10, 11);
        assert main.isCompleted();
        assert main.isNotError();
    }

    @Test
    void resumeNullFunction() {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume((Function) null));

        assert main.value();
        assert main.isError();
    }
}
