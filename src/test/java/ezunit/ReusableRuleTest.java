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
package ezunit;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2010/10/07 15:50:26
 */
public class ReusableRuleTest {

    @Rule
    public static final TopRule rule = new TopRule();

    /** The counter of method invocation. */
    private static int counter = 0;

    @Rule
    public static final LifecycleMethodValidator checker = new LifecycleMethodValidator();

    /** The container for lifecycle methods. */
    private static List<String> invocations = new ArrayList();

    @BeforeClass
    public static void beforeClass() {
        assertEquals(0, invocations.size());
    }

    @AfterClass
    public static void afterClass() {
        assertEquals(2, invocations.size());
    }

    @Before
    public void before() {
        counter++;
    }

    @Test
    public void invokeSubRules1() throws Exception {
        assertTrue(rule.beforeClassInvoked);
        assertEquals(1, TopRule.sub1.beforeClassInvoked);
        assertEquals(1, TopRule.sub2.beforeClassInvoked);
        assertEquals(counter, TopRule.sub1.counter);
        assertEquals(counter, TopRule.sub2.counter);
    }

    @Test
    public void invokeSubRules2() throws Exception {
        assertTrue(rule.beforeClassInvoked);
        assertEquals(1, TopRule.sub1.beforeClassInvoked);
        assertEquals(1, TopRule.sub2.beforeClassInvoked);
        assertEquals(counter, TopRule.sub1.counter);
        assertEquals(counter, TopRule.sub2.counter);
    }

    /**
     * @version 2010/10/07 15:55:57
     */
    private static class TopRule extends ReusableRule {

        @Rule
        public static final SubRule sub1 = new SubRule();

        @Rule
        public static final SubRule sub2 = new SubRule();

        private boolean beforeClassInvoked = false;

        /**
         * @see ezunit.ReusableRule#beforeClass()
         */
        @Override
        protected void beforeClass() throws Exception {
            beforeClassInvoked = true;
        }
    }

    /**
     * @version 2010/10/07 15:55:57
     */
    private static class SubRule extends ReusableRule {

        private int beforeClassInvoked = 0;

        private int counter = 0;

        /**
         * @see ezunit.ReusableRule#beforeClass()
         */
        @Override
        protected void beforeClass() throws Exception {
            beforeClassInvoked++;
            assertEquals(ReusableRuleTest.class, testcase);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void before(Method method) throws Exception {
            counter++;
            assertTrue(method.getName().startsWith("invokeSubRules"));
        }
    }

    /**
     * @version 2010/10/11 0:59:19
     */
    private static class LifecycleMethodValidator extends ReusableRule {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void beforeClass() throws Exception {
            invocations.add("beforeClass from Sub");
            assertEquals(1, invocations.size());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void afterClass() {
            assertEquals(1, invocations.size());
            invocations.add("afterClass from Sub");
        }

    }
}
