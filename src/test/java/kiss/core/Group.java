/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

/**
 * @version 2015/04/18 0:30:42
 */
public class Group {

    public final Person leader;

    /**
     * @param leader
     */
    public Group(Person leader) {
        this.leader = leader;
    }

    public static final Lens<Group, Person> $leader$ = new Lens<Group, Person>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public Person get(Group model) {
            return model.leader;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Group set(Group model, Person property) {
            return new Group(property);
        }
    };
}
