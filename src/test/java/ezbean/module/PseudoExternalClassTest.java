/*
 * Copyright (C) 2010 Nameless Production Committee.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.module;

import static junit.framework.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import ezbean.model.Model;

/**
 * @version 2009/12/22 19:19:13
 */
public class PseudoExternalClassTest {

    @Rule
    public static ModuleTestRule registry = new ModuleTestRule();

    @Test
    public void resolveClass() {
        registry.load(registry.dir);

        assertNotNull(Model.load("external.Class1"));
    }
}
