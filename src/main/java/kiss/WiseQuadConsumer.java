/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

public interface WiseQuadConsumer<Param1, Param2, Param3, Param4>
        extends Narrow<WiseTriConsumer<Param2, Param3, Param4>, Param1, WiseTriConsumer<Param1, Param2, Param3>, Param4> {

    /**
     * Internal API.
     *
     * @param param1 The input argument
     * @param param2 The input argument
     * @param param3 The input argument
     * @param param4 The input argument
     */
    void ACCEPT(Param1 param1, Param2 param2, Param3 param3, Param4 param4) throws Throwable;

    /**
     * Performs this operation on the given argument.
     *
     * @param param1 The input argument
     * @param param2 The input argument
     * @param param3 The input argument
     * @param param4 The input argument
     */
    default void accept(Param1 param1, Param2 param2, Param3 param3, Param4 param4) {
        try {
            ACCEPT(param1, param2, param3, param4);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Param1 invoke(Object... params) {
        accept((Param1) params[0], (Param2) params[1], (Param3) params[2], (Param4) params[3]);
        return (Param1) params[0];
    }
}