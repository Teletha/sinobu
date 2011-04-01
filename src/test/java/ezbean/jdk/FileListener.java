/*
 * Copyright (C) 2011 Nameless Production Committee.
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
package ezbean.jdk;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * @version 2011/04/01 18:17:12
 */
public interface FileListener {

    /**
     * <p>
     * Listen file system event.
     * </p>
     * 
     * @param event
     */
    void change(WatchEvent<Path> event);
}