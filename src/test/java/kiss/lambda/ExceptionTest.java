/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lambda;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.crypto.IllegalBlockSizeException;

import antibug.ExpectThrow;
import kiss.I;
import kiss.WiseBiConsumer;
import kiss.WiseBiFunction;
import kiss.WiseConsumer;
import kiss.WiseFunction;
import kiss.WiseRunnable;

/**
 * @version 2018/03/31 23:16:30
 */
public class ExceptionTest {

    @ExpectThrow(IllegalBlockSizeException.class)
    public void runnable() {
        Runnable lambda = I.quiet(this::runnableThrow);
        assert lambda != null;
        assert lambda instanceof WiseRunnable;
        lambda.run();
    }

    private void runnableThrow() throws Exception {
        throw new IllegalBlockSizeException();
    }

    @ExpectThrow(IllegalBlockSizeException.class)
    public void consumer() {
        Consumer<Object> lambda = I.quiet(this::consumerThrow);
        assert lambda != null;
        assert lambda instanceof WiseConsumer;
        lambda.accept(null);
    }

    private void consumerThrow(Object p) throws Exception {
        throw new IllegalBlockSizeException();
    }

    @ExpectThrow(IllegalBlockSizeException.class)
    public void biconsumer() {
        BiConsumer<Object, Object> lambda = I.quiet(this::biconsumerThrow);
        assert lambda != null;
        assert lambda instanceof WiseBiConsumer;
        lambda.accept(null, null);
    }

    private void biconsumerThrow(Object p, Object q) throws Exception {
        throw new IllegalBlockSizeException();
    }

    @ExpectThrow(IllegalBlockSizeException.class)
    public void function() {
        Function<Object, Object> lambda = I.quiet(this::functionThrow);
        assert lambda != null;
        assert lambda instanceof WiseFunction;
        lambda.apply(null);
    }

    private Object functionThrow(Object p) throws Exception {
        throw new IllegalBlockSizeException();
    }

    @ExpectThrow(IllegalBlockSizeException.class)
    public void bifunction() {
        BiFunction<Object, Object, Object> lambda = I.quiet(this::bifunctionThrow);
        assert lambda != null;
        assert lambda instanceof WiseBiFunction;
        lambda.apply(null, null);
    }

    private Object bifunctionThrow(Object p, Object q) throws Exception {
        throw new IllegalBlockSizeException();
    }
}
