/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.powerassert;

import java.util.Arrays;

/**
 * @version 2012/01/19 12:01:26
 */
class Operand {

    /** The human redable expression. */
    String name;

    /** The actual value. */
    Object value;

    /**
     * 
     */
    Operand(Object value) {
        if (value instanceof String) {
            this.name = "\"" + value + "\"";
        } else if (value instanceof Class) {
            this.name = ((Class) value).getSimpleName() + ".class";
        } else {
            this.name = String.valueOf(value);
        }
        this.value = value;
    }

    /**
     * 
     */
    Operand(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        Class type = getClass();
        Class otherType = obj.getClass();

        if (type != otherType) {
            return false;
        }

        Operand other = (Operand) obj;

        // check name
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }

        // check value
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            type = value.getClass();

            if (!type.isArray()) {
                return false;
            } else {
                switch (type.getComponentType().getSimpleName()) {
                case "int":
                    return Arrays.equals((int[]) value, (int[]) other.value);

                case "long":
                    return Arrays.equals((long[]) value, (long[]) other.value);

                case "float":
                    return Arrays.equals((float[]) value, (float[]) other.value);

                case "double":
                    return Arrays.equals((double[]) value, (double[]) other.value);

                case "char":
                    return Arrays.equals((char[]) value, (char[]) other.value);

                case "boolean":
                    return Arrays.equals((boolean[]) value, (boolean[]) other.value);

                case "short":
                    return Arrays.equals((short[]) value, (short[]) other.value);

                case "byte":
                    return Arrays.equals((byte[]) value, (byte[]) other.value);

                default:
                    return Arrays.deepEquals((Object[]) value, (Object[]) other.value);
                }
            }
        }
        return true;
    }

    /**
     * <p>
     * Compute human-readable expression of value.
     * </p>
     * 
     * @return
     */
    String toValueExpression() {
        if (value == null) {
            return "null";
        }

        if (value instanceof CharSequence) {
            return "\"" + value + "\"";
        }

        if (value instanceof Enum) {
            Enum enumration = (Enum) value;
            return enumration.getDeclaringClass().getSimpleName() + '.' + enumration.name();
        }

        Class clazz = value.getClass();

        if (clazz == Class.class) {
            return ((Class) value).getSimpleName() + ".class";
        }

        if (clazz.isArray()) {
            switch (clazz.getComponentType().getSimpleName()) {
            case "int":
                return Arrays.toString((int[]) value);

            case "long":
                return Arrays.toString((long[]) value);

            case "float":
                return Arrays.toString((float[]) value);

            case "double":
                return Arrays.toString((double[]) value);

            case "char":
                return Arrays.toString((char[]) value);

            case "boolean":
                return Arrays.toString((boolean[]) value);

            case "short":
                return Arrays.toString((short[]) value);

            case "byte":
                return Arrays.toString((byte[]) value);

            default:
                return Arrays.toString((Object[]) value);
            }
        }
        return value.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}