/*
 * Copyright (C) 2011 Nameless Production Committee.
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

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezunit.PrivateModule;

/**
 * @version 2011/03/22 17:08:02
 */
public class ClassloaderUnloadTest {

    @Rule
    public static PrivateModule module = new PrivateModule(true, false);

    /** The memory monitor. */
    private static MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

    /** The class monitor. */
    private static ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();

    /** The system garbage collectors. */
    private static List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();

    @BeforeClass
    public static void before() {
        classLoading.setVerbose(true);
    }

    @AfterClass
    public static void b2efore() {
        classLoading.setVerbose(false);
    }

    @Test
    public void count() throws Exception {
        // use module class
        assert I.make(module.convert(Single.class)) != null;

        // reload module and execute gc if possible
        module.load();
        tryGC();

        // create snapshot
        int initialLoaded = classLoading.getLoadedClassCount();
        long initialUnloaded = classLoading.getUnloadedClassCount();

        // use module class
        assert I.make(module.convert(Single.class)) != null;

        // create snapshot
        int loaded = classLoading.getLoadedClassCount();
        long unloaded = classLoading.getUnloadedClassCount();

        assert 2 <= loaded - initialLoaded;
        assert 0 == unloaded - initialUnloaded;

        // reload module and execute gc if possible
        module.load();
        tryGC();

        // create snapshot
        int lastLoaded = classLoading.getLoadedClassCount();
        long lastUnloaded = classLoading.getUnloadedClassCount();
        assert lastLoaded - loaded <= -2;
        assert 2 <= lastUnloaded - unloaded;
        assert loaded - lastLoaded == lastUnloaded - unloaded;
    }

    /**
     * <p>
     * Helper method to count a number of garbage collection since JVM was started.
     * </p>
     * 
     * @return A count of garbage collection.
     */
    private long countGC() {
        long count = 0;

        for (GarbageCollectorMXBean gc : gcs) {
            count = +gc.getCollectionCount();
        }

        // API definition
        return count;
    }

    /**
     * <p>
     * Try to execute Garbage Collection if possible.
     * </p>
     */
    private void tryGC() {
        tryGC(500);
    }

    /**
     * Try to execute Garbage colletion if possible.
     * 
     * @param threshold
     */
    private void tryGC(long threshold) {
        long previous = countGC();
        long data = System.currentTimeMillis();

        // try GC if possioble
        memory.gc();

        while (countGC() == previous && System.currentTimeMillis() - data < threshold) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * @version 2010/02/04 1:00:18
     */
    protected static class Single {

        private String name;

        /**
         * Get the name property of this {@link ClassloaderUnloadTest.Single}.
         * 
         * @return The name property.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name property of this {@link ClassloaderUnloadTest.Single}.
         * 
         * @param name The name value to set.
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}
