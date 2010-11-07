/*
 * Copyright (C) 2010 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.module;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import ezbean.Modules;
import ezbean.sample.bean.Person;
import ezunit.PrivateModule;

/**
 * @version 2010/01/21 19:48:01
 */
public class ModulesAwareTest {

    @Rule
    public static final PrivateModule module = new PrivateModule(true, false);

    private final Map<Class, String> map = Modules.aware(new HashMap());

    @Test
    public void unloadCorrectly() throws Exception {
        map.put(Person.class, "This entry will not be unloaded.");
        map.put(module.convert(ThisClassWillBeUnloaded.class), "This entry will be unloaded.");

        assertEquals(2, map.size());
        module.unload();
        assertEquals(1, map.size());
    }

    /**
     * @version 2010/01/21 19:50:48
     */
    private static class ThisClassWillBeUnloaded extends Person {
    }
}
