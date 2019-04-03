/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.function;

import kiss.WiseRunnable;

class WiseRunnableTest {

    private String value = null;

    WiseRunnable constant = () -> value = "constant";
}
