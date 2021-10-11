/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class ScanTest extends SignalTester {

    @Test
    void scan() {
        monitor(int.class, signal -> signal.scan(v -> v * 10, (accumulated, value) -> accumulated + value));

        assert main.emit(1).value(10); // 1 * 10
        assert main.emit(2).value(12); // 10 + 2
        assert main.emit(3).value(15); // 12 + 3
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void scanErrorInFirst() {
        monitor(int.class, signal -> signal.scan(errorFunction(), (accumulated, value) -> accumulated + value));

        assert main.emit(1).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void scanErrorInOthers() {
        monitor(int.class, signal -> signal.scan(v -> v * 10, errorBiFunction()));

        assert main.emit(1).value(10); // 1 * 10
        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();

        assert main.emit(2).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void scanComplete() {
        monitor(int.class, signal -> signal.scan(v -> v * 10, (accumulated, value) -> accumulated + value));

        assert main.emit(1, Complete).value(10);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void collector() {
        monitor(String.class, signal -> signal.scan(Collectors.joining("-")));

        assert main.emit("A").value("A");
        assert main.emit("B").value("A-B");
        assert main.emit("C").value("A-B-C");
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void scanWith() {
        monitor(int.class, signal -> signal.scanWith(10, (accumulated, value) -> accumulated + value));

        assert main.emit(1).value(11); // 10 + 1
        assert main.emit(2).value(13); // 11 + 2
        assert main.emit(3).value(16); // 13 + 3
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void scanWithError() {
        monitor(signal -> signal.scanWith(10, errorBiFunction()));

        assert main.emit(1).value();
        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void scanWithComplete() {
        monitor(int.class, signal -> signal.scanWith(10, (accumulated, value) -> accumulated + value));

        assert main.emit(1, Complete).value(11);
        assert main.isCompleted();
        assert main.isDisposed();
    }

    @Test
    void scanInitialSupplier() {
        monitor(int.class, signal -> signal.scan(() -> 10, (accumulated, value) -> accumulated + value));

        assert main.emit(1).value(11); // 10 + 1
        assert main.emit(2).value(13); // 11 + 2
        assert main.emit(3).value(16); // 13 + 3
        assert main.isNotCompleted();
        assert main.isNotDisposed();
    }

    @Test
    void scanInitialSupplierError() {
        monitor(int.class, signal -> signal.scan(() -> 10, (accumulated, value) -> accumulated + value));

        assert main.emit(1, Error).value(11);
        assert main.isError();
        assert main.isNotCompleted();
        assert main.isDisposed();
    }

    @Test
    void scanInitialSupplierComplete() {
        monitor(int.class, signal -> signal.scan(() -> 10, (accumulated, value) -> accumulated + value));

        assert main.emit(1, Complete).value(11);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}