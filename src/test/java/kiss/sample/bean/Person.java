/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean;

public class Person {

    private int age;

    private String firstName;

    private String lastName;

    /**
     * Get the age property of this {@link Person}.
     * 
     * @return The age prperty.
     */
    public int getAge() {
        return age;
    }

    /**
     * Set the age property of this {@link Person}.
     * 
     * @param age The age value to set.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Get the firstName property of this {@link Person}.
     * 
     * @return The firstName prperty.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the firstName property of this {@link Person}.
     * 
     * @param firstName The firstName value to set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get the lastName property of this {@link Person}.
     * 
     * @return The lastName prperty.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the lastName property of this {@link Person}.
     * 
     * @param lastName The lastName value to set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}