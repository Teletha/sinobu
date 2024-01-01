/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean;

import java.util.Optional;

/**
 * @version 2016/04/04 14:30:27
 */
public class EnumProperty {

    public Value field;

    public Value fieldWithDefault = Value.One;

    public Optional<Value> optional;

    public Optional<Value> optionalWithDefault = Optional.of(Value.One);

    private Value method;

    /**
     * Get the method property of this {@link EnumProperty}.
     * 
     * @return The method property.
     */
    public Value getMethod() {
        return method;
    }

    /**
     * Set the method property of this {@link EnumProperty}.
     * 
     * @param method The method value to set.
     */
    public void setMethod(Value method) {
        this.method = method;
    }

    /**
     * @version 2016/04/04 14:31:05
     */
    public static enum Value {
        One, Two, Three;
    }
}