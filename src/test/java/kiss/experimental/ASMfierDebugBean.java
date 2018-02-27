/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

/**
 * @version 2011/11/19 18:40:08
 */
public class ASMfierDebugBean extends ASMfierDebugBase {

    /**
     * @see kiss.experimental.ASMfierDebugBase#getAge()
     */
    @Override
    public int getAge() {
        return super.getAge();
    }

    /**
     * @see kiss.experimental.ASMfierDebugBase#getName()
     */
    @Override
    public String getName() {
        return super.getName();
    }

    /**
     * @see kiss.experimental.ASMfierDebugBase#setAge(int)
     */
    @Override
    public void setAge(int age) {
        super.setAge(age);
    }

    /**
     * @see kiss.experimental.ASMfierDebugBase#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {

    }

    /**
     * 
     */
    public Object ezCall(int id, Object... params) {
        switch (id) {
        default:
            throw new IllegalArgumentException();

        case 0:
            return super.getAge();

        case 1:
            super.setAge((Integer) params[0]);
            return null;

        case 2:
            return getName();

        case 3:
            super.setName((String) params[0]);
            return null;

        case 4:
            return super.isHungry();

        case 5:
            super.talk((String) params[0], (Integer) params[1]);
            return null;
        }
    }
}
