/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.Disposable;
import kiss.I;
import kiss.Signal;

class StartWithTest extends SignalTester {

    @Test
    void value() {
        monitor(() -> signal(1, 2).startWith(0));
        assert main.value(0, 1, 2);
        assert main.isCompleted();
        assert main.isDisposed();

        monitor(() -> signal(1, 2).startWith(3, 4));
        assert main.value(3, 4, 1, 2);
        assert main.isCompleted();
        assert main.isDisposed();

        monitor(() -> signal(1, 2).startWith(3).startWith(4, 5));
        assert main.value(4, 5, 3, 1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void valueNull() {
        monitor(() -> signal("1", "2").startWith((String) null));
        assert main.value(null, "1", "2");
        assert main.isCompleted();
        assert main.isDisposed();

        monitor(() -> signal("1", "2").startWith((String[]) null));
        assert main.value("1", "2");
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void iterable() {
        monitor(() -> signal(1, 2).startWith(list(-1, 0)));
        assert main.value(-1, 0, 1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void iterableError() {
        monitor(() -> signal(1, 2).startWith(errorIterable()));
        assert main.value();
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    void iterableNull() {
        monitor(() -> signal(1, 2).startWith((Iterable) null));
        assert main.value(1, 2);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void signal() {
        monitor(signal -> signal.startWith(other.signal()));

        assert main.emit("other is not completed, so this value will ignored").value();
        assert other.emit("other is ", Complete).isEmmitted();
        assert main.emit("main can signal").isEmmitted();
        assert other.isCompleted();
        assert other.isDisposed();
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void signalError() {
        monitor(signal -> signal.startWith(other.signal()));

        assert main.emit("other is not completed, so this value will ignored").value();
        assert other.emit("other is ", Error).isEmmitted();
        assert main.emit("main can't signal").value();
        assert other.isNotCompleted();
        assert other.isError();
        assert other.isDisposed();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void signalChildCompleteWillNotAffectMain() {
        Disposable disposable = Signal.never().startWith(I.signal()).to(I.NoOP);
        assert disposable.isDisposed() == false;
    }

    @Test
    void signalChildErrorWillAffectMain() {
        Assertions.assertThrows(Error.class, () -> Signal.never().startWith(I.signalError(new Error())).to(I.NoOP));
    }

    @Test
    void withNull() {
        monitor(() -> signal("1", "2").startWithNull());
        assert main.value(null, "1", "2");
        assert main.isCompleted();
        assert main.isDisposed();
    }
}