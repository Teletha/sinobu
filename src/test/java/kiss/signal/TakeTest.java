/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import org.junit.Test;

/**
 * @version 2017/04/07 1:57:35
 */
public class TakeTest extends SignalTestBase {

    @Test
    public void takeByValueCondition() {
        monitor(() -> signal(1, 2, 3, 4, 5, 6).take(value -> value % 3 == 0));

        assert result.value(3, 6);
        assert result.completed();
    }

    @Test
    public void takeByValueConditionWithPrevious() {
        monitor(() -> signal(10, 11, 20, 21).take(0, (prev, now) -> now - prev > 5));

        assert result.value(10, 20);
        assert result.completed();
    }

    @Test
    public void takeAt() throws Exception {
        monitor(() -> signal(1, 2, 3, 4, 5, 6).takeAt(index -> 3 < index));
        assert result.value(5, 6);

        monitor(() -> signal(1, 2, 3, 4, 5, 6).takeAt(index -> index % 2 == 0));
        assert result.value(1, 3, 5);
    }

    @Test
    public void takeWhile() {
        monitor(signal -> signal.takeWhile(condition.signal()));

        assert emit(1, 2).value();
        condition.emit(true);
        assert emit(1, 2).value(1, 2);
        condition.emit(false);
        assert emit(1, 2).value();

        assert condition.isNotDisposed();
        dispose();
        assert condition.isDisposed();
    }
}
