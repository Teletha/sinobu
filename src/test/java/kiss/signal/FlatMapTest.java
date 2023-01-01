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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signaling;
import kiss.WiseFunction;

class FlatMapTest extends SignalTester {

    @Test
    void value() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void complete() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Complete).value(10, 11, 20, 21);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void error() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1)));

        assert main.emit(10, 20, Error).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void errorInFunction() {
        monitor(() -> signal(1, 2).flatMap(errorFunction()));

        assert main.value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void innerComplete() {
        monitor(Integer.class, signal -> signal.flatMap(v -> signal(v, v + 1, v + 2).take(2)));

        assert main.emit(10, 20, 30).value(10, 11, 20, 21, 30, 31);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void innerError() {
        monitor(Integer.class, signal -> signal.flatMap(v -> errorSignal()));

        assert main.emit(10, 20).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void rejectNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(() -> signal(1, 2).flatMap((WiseFunction) null));
        });
    }

    @Test
    void withContext() {
        monitor(Integer.class, signal -> signal.flatMap(AtomicInteger::new, (c, v) -> signal(v, c.incrementAndGet() + v)));

        assert main.emit(10, 20).value(10, 11, 20, 22);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void withContextError() {
        monitor(Integer.class, signal -> signal.flatMap(AtomicInteger::new, (c, v) -> signal(v, c.incrementAndGet() + v)));

        assert main.emit(Error, 10, 20).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void withContextErrorInFunction() {
        monitor(Integer.class, signal -> signal.flatMap(AtomicInteger::new, errorBiFunction()));

        assert main.emit(10, 20).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void withContextComplete() {
        monitor(Integer.class, signal -> signal.flatMap(AtomicInteger::new, (c, v) -> signal(v, c.incrementAndGet() + v)));

        assert main.emit(Complete, 10, 20).value();
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }

    @Test
    void withNullContext() {
        monitor(Integer.class, signal -> signal.flatMap(null, (c, v) -> signal(v, v + 1)));

        assert main.emit(10, 20).value(10, 11, 20, 21);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void delayAndInterval() {
        monitor(Integer.class, signal -> signal
                .flatMap(time -> signal(time, time + 1).delay(time, ms, scheduler).interval(100, ms, scheduler)));

        assert main.emit(300, 200, 100).value();
        scheduler.await();
        assert main.valueInNoParticularOrder(100, 101, 200, 201, 300, 301);
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void detail() {
        monitor(String.class, signal -> signal.flatMap(x -> x.equals("start other") ? other.signal() : another.signal()));

        assert main.emit("start other").value();
        assert other.emit("other is connected").isEmmitted();
        assert another.emit("another is not connected yet").value();

        assert main.emit("start another").value();
        assert another.emit("another is connected").isEmmitted();
        assert other.emit("other is also connected").isEmmitted();

        assert main.isNotDisposed();
        assert other.isNotDisposed();
        assert another.isNotDisposed();

        main.dispose();
        assert main.isDisposed();
        assert other.isDisposed();
        assert another.isDisposed();
    }

    @Test
    void fromFinitToInfinit() {
        Signaling<String> signaling = new Signaling();

        monitor(() -> I.signal(signaling).flatMap(s -> s.expose));

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        signaling.accept("ok");
        assert main.value("ok");
    }

    @Test
    void fromFinitToInfinitWithComplete() {
        Signaling<String> signaling = new Signaling();

        monitor(() -> I.signal(signaling).flatMap(s -> s.expose));

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        signaling.complete();
        assert main.isNotError();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void fromFinitToInfinitWithError() {
        Signaling<String> signaling = new Signaling();

        monitor(() -> I.signal(signaling).flatMap(s -> s.expose));

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
        signaling.error(new IllegalAccessError());
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    class Host {
        Signaling<String> signal = new Signaling();
    }

    @Test
    void fromInfinitToInfinit() {
        monitor(Host.class, String.class, signal -> signal.flatMap(s -> s.signal.expose));

        Host host = new Host();
        assert main.emit(host).value();

        host.signal.accept("1");
        assert main.value("1");
        host.signal.accept("2");
        assert main.value("2");

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void fromInfinitToInfinitWithComplete() {
        monitor(Host.class, String.class, signal -> signal.flatMap(s -> s.signal.expose));

        Host host1 = new Host();
        assert main.emit(host1).value();

        host1.signal.complete();
        host1.signal.accept("This value will be ignored");
        assert main.value();

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        Host host2 = new Host();
        assert main.emit(host2).value();

        host2.signal.accept("This value will be accepted");
        assert main.value("This value will be accepted");
        host2.signal.complete();
        host2.signal.accept("This value will be ignored");
        assert main.value();

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void fromInfinitToInfinitWithSourceComplete() {
        monitor(Host.class, String.class, signal -> signal.flatMap(s -> s.signal.expose));

        Host host1 = new Host();
        Host host2 = new Host();
        assert main.emit(host1, host2, Complete).value();

        host1.signal.accept("This value will be accepted");
        assert main.value("This value will be accepted");

        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        // host2 is remaining
        host1.signal.complete();
        assert main.isNotError();
        assert main.isNotCompleted();
        assert main.isNotDisposed();

        host2.signal.accept("This value will be accepted");
        assert main.value("This value will be accepted");

        host2.signal.complete();
        assert main.isNotError();
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void fromInfinitToInfinitWithError() {
        monitor(Host.class, String.class, signal -> signal.flatMap(s -> s.signal.expose));

        Host host = new Host();
        assert main.emit(host).value();

        host.signal.error(new java.lang.Error());
        host.signal.accept("This value will be ignored");
        assert main.value();

        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }
}