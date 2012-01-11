/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.sample.bean;

import java.util.List;
import java.util.Map;

/**
 * @version 2009/07/17 14:52:13
 */
public class GenericGetterBean extends GenericBean<String> {

    /**
     * @see ezbean.sample.bean.GenericBean#getGeneric()
     */
    @Override
    public String getGeneric() {
        return super.getGeneric();
    }

    /**
     * @see ezbean.sample.bean.GenericBean#getGenericList()
     */
    @Override
    public List<String> getGenericList() {
        return super.getGenericList();
    }

    /**
     * @see ezbean.sample.bean.GenericBean#getGenericMap()
     */
    @Override
    public Map<String, String> getGenericMap() {
        return super.getGenericMap();
    }

}
