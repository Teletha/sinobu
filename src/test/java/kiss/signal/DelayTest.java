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

import java.time.Duration;

import org.junit.jupiter.api.Test;

import kiss.Variable;

/**
 * @version 2018/07/16 11:07:12
 */
class DelayTest extends SignalTester {

    @Test
    void delay() {
        monitor(signal -> signal.delay(30, ms));

        assert main.emit("delay").value();
        assert await().value("delay");

        assert main.emit("one", "more").value();
        assert await().value("one", "more");
    }

    @Test
    void delayZero() {
        monitor(signal -> signal.delay(0, ms));

        assert main.emit("no delay").value("no delay");
    }

    @Test
    void delayNegative() {
        monitor(signal -> signal.delay(-10, ms));

        assert main.emit("no delay").value("no delay");
    }

    @Test
    void delayNullUnit() {
        monitor(signal -> signal.delay(10, null));

        assert main.emit("no delay").value("no delay");
    }

    @Test
    void delayDuration() {
        monitor(signal -> signal.delay(Duration.ofMillis(30)));

        assert main.emit("delay").value();
        assert await().value("delay");

        assert main.emit("one", "more").value();
        assert await().value("one", "more");
    }

    @Test
    void delayNegativeDuration() {
        monitor(signal -> signal.delay(Duration.ofMillis(-30)));

        assert main.emit("no delay").value("no delay");
    }

    @Test
    void delayZeroDuration() {
        monitor(signal -> signal.delay(Duration.ofMillis(0)));

        assert main.emit("no delay").value("no delay");
    }

    @Test
    void delayNullDuration() {
        monitor(signal -> signal.delay((Duration) null));

        assert main.emit("no delay").value("no delay");
    }

    @Test
    void delayVariable() {
        monitor(signal -> signal.delay(Variable.of(Duration.ofMillis(30))));

        assert main.emit("delay").value();
        assert await().value("delay");

        assert main.emit("one", "more").value();
        assert await().value("one", "more");
    }

    @Test
    void delayNegativeVariable() {
        monitor(signal -> signal.delay(Variable.of(Duration.ofMillis(-30))));

        assert main.emit("no delay").value("no delay");
    }

    @Test
    void delayZeroVariable() {
        monitor(signal -> signal.delay(Variable.of(Duration.ofMillis(0))));

        assert main.emit("no delay").value("no delay");
    }

    @Test
    void delayEmptyVariable() {
        monitor(signal -> signal.delay(Variable.empty()));

        assert main.emit("no delay").value("no delay");
    }

    @Test
    void delayNullVariable() {
        monitor(signal -> signal.delay((Variable<Duration>) null));

        assert main.emit("no delay").value("no delay");
    }

    @Test
    void delayByCount1() {
        monitor(signal -> signal.delay(1));

        assert main.emit("1").value();
        assert main.emit("2").value("1");
        assert main.emit("3").value("2");
    }

    @Test
    void delayByCount2() {
        monitor(signal -> signal.delay(2));

        assert main.emit("1").value();
        assert main.emit("2").value();
        assert main.emit("3").value("1");
        assert main.emit("4").value("2");
    }
}
