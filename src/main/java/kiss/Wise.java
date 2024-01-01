/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

/**
 * General purpose flexible and invokable function interface.
 */
public interface Wise {

    /**
     * Invoke method with the specified parameters and return some value (may be null). This method
     * doesn't check type correctness, so runtime may be going to throw {@link ClassCastException}.
     * 
     * @param params A list of parameters.
     * @return A some value (may be null).
     */
    Object invoke(Object... params);
}