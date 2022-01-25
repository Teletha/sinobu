/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean.modifiers;

public class FinalAccessor {

    private String both;

    private String getter;

    private String setter;

    /**
     * Get the both property of this {@link FinalAccessor}.
     * 
     * @return The both prperty.
     */
    public final String getBoth() {
        return both;
    }

    /**
     * Set the both property of this {@link FinalAccessor}.
     * 
     * @param both The both value to set.
     */
    public final void setBoth(String both) {
        this.both = both;
    }

    /**
     * Get the getter property of this {@link FinalAccessor}.
     * 
     * @return The getter prperty.
     */
    public final String getGetter() {
        return getter;
    }

    /**
     * Set the getter property of this {@link FinalAccessor}.
     * 
     * @param getter The getter value to set.
     */
    public void setGetter(String getter) {
        this.getter = getter;
    }

    /**
     * Get the setter property of this {@link FinalAccessor}.
     * 
     * @return The setter prperty.
     */
    public String getSetter() {
        return setter;
    }

    /**
     * Set the setter property of this {@link FinalAccessor}.
     * 
     * @param setter The setter value to set.
     */
    public final void setSetter(String setter) {
        this.setter = setter;
    }
}