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

import org.junit.jupiter.api.Test;

import antibug.ExpectThrow;

/**
 * @version 2018/03/31 23:14:49
 */
public class SwitchMapTest extends SignalTester {

    @Test
    public void value() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    public void complete() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Complete).value(10, 11, 20, 21);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    public void error() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Error).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void errorInFunction() {
        monitor(() -> signal(1, 2).switchMap(errorFunction()));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    public void innerComplete() {
        monitor(Integer.class, signal -> signal.switchMap(v -> signal(v).take(1)));

        assert main.emit(10, 20).value(10, 20);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    public void innerError() {
        monitor(Integer.class, signal -> signal.switchMap(v -> errorSignal()));

        assert main.emit(10, 20).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @ExpectThrow(NullPointerException.class)
    public void rejectNull() {
        monitor(() -> signal(1, 2).switchMap(null));
    }

    @Test
    public void delayAndInterval() {
        monitor(Integer.class, signal -> signal.switchMap(time -> signal(time, time + 1).delay(time, ms).interval(50, ms)));

        main.emit(60, 40, 20);
        assert await().value(20, 21);
    }

    @Test
    public void detail() {
        monitor(String.class, signal -> signal.switchMap(x -> x.equals("start other") ? other.signal() : another.signal()));

        assert main.emit("start other").size(0);
        assert other.emit("other is connected").size(1);
        assert another.emit("another is not connected yet").size(0);

        assert main.emit("start another").size(0);
        assert another.emit("another is connected").size(1);
        assert other.emit("other is disconnected").size(0);
        assert other.isDisposed();
        assert another.isNotDisposed();

        assert main.emit("start other").size(0);
        assert other.emit("other is connected again").size(1);
        assert another.emit("another is disconnected").size(0);
        assert other.isNotDisposed();
        assert another.isDisposed();

        main.dispose();
        assert main.isDisposed();
        assert other.isDisposed();
        assert another.isDisposed();
    }
}
