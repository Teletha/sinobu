/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @version 2018/12/07 16:09:15
 */
public interface WiseFunction<Param, Return> extends Function<Param, Return>, Serializable {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @param param A proxy parameter.
     * @return A proxy result.
     * @throws Throwable A sneaky exception for lambda.
     */
    Return APPLY(Param param) throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default Return apply(Param param) {
        try {
            return APPLY(param);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default WiseSupplier<Return> with(Param param) {
        return () -> APPLY(param);
    }

    /**
     * Widen parameter at last (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default <Append> WiseBiFunction<Param, Append, Return> append() {
        return (p, q) -> apply(p);
    }

    /**
     * Widen parameter at last (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default <Prepend> WiseBiFunction<Prepend, Param, Return> prepend() {
        return (p, q) -> apply(q);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <V> WiseFunction<V, Return> compose(Function<? super V, ? extends Param> before) {
        return I.wise((Function) Function.super.compose(before));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <V> WiseFunction<Param, V> andThen(Function<? super Return, ? extends V> after) {
        return I.wise((Function) Function.super.andThen(after));
    }
}
