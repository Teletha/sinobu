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

import java.time.Duration;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

class DelayTest extends SignalTester {

    @Test
    void delay() {
        monitor(signal -> signal.delay(30, ms, scheduler));

        assert main.emit("delay").value();
        scheduler.await();
        assert main.value("delay");

        assert main.emit("one", "more").value();
        scheduler.await();
        assert main.value("one", "more");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayZero() {
        monitor(signal -> signal.delay(0, ms));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayNegative() {
        monitor(signal -> signal.delay(-10, ms));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayNullUnit() {
        monitor(signal -> signal.delay(10, null));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayDuration() {
        monitor(signal -> signal.delay(Duration.ofMillis(30), scheduler));

        assert main.emit("delay").value();
        scheduler.await();
        assert main.value("delay");

        assert main.emit("one", "more").value();
        scheduler.await();
        assert main.value("one", "more");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayNegativeDuration() {
        monitor(signal -> signal.delay(Duration.ofMillis(-30)));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayZeroDuration() {
        monitor(signal -> signal.delay(Duration.ofMillis(0)));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayNullDuration() {
        monitor(signal -> signal.delay((Duration) null));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delaySupplier() {
        monitor(signal -> signal.delay(() -> Duration.ofMillis(30), scheduler));

        assert main.emit("delay").value();
        scheduler.await();
        assert main.value("delay");

        assert main.emit("one", "more").value();
        scheduler.await();
        assert main.value("one", "more");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayNegativeSupplier() {
        monitor(signal -> signal.delay(() -> Duration.ofMillis(-30)));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayZeroSupplier() {
        monitor(signal -> signal.delay(() -> Duration.ofMillis(0)));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayNullSupplier() {
        monitor(signal -> signal.delay((Supplier<Duration>) null));

        assert main.emit("no delay").value("no delay");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayByCount1() {
        monitor(signal -> signal.delay(1));

        assert main.emit("1").value();
        assert main.emit("2").value("1");
        assert main.emit("3").value("2");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayByCount2() {
        monitor(signal -> signal.delay(2));

        assert main.emit("1").value();
        assert main.emit("2").value();
        assert main.emit("3").value("1");
        assert main.emit("4").value("2");
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayComplete() {
        monitor(signal -> signal.delay(30, ms, scheduler));

        assert main.emit("1", "2").value();
        scheduler.await();
        assert main.value("1", "2");
        assert main.isNotCompleted();
        assert main.emit("3", Complete).value();
        scheduler.await();
        assert main.value("3");
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void delayCompleteWithoutValues() {
        monitor(signal -> signal.delay(30, ms, scheduler));

        assert main.emit(Complete).value();
        scheduler.await();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}