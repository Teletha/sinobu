/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * @version 2018/09/13 9:06:27
 */
public interface WiseSupplier<Return> extends Supplier<Return>, Serializable {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @return A proxy result.
     * @throws Throwable A sneaky exception for lambda.
     */
    Return GET() throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default Return get() {
        try {
            return GET();
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Convert to {@link WiseFunction}.
     * </p>
     * 
     * @return A converted {@link WiseFunction}.
     */
    default WiseFunction<Object, Return> asFunction() {
        return p -> GET();
    }
}
