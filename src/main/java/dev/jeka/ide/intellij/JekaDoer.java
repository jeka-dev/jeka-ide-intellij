/*
 * Copyright 2018-2019 original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.jeka.ide.intellij;

import java.nio.file.Path;

/**
 * @author Jerome Angibaud
 */
public interface JekaDoer {

    static JekaDoer getInstance() {
        return CmdJekaDoer.INSTANCE;
    }

    void generateIml(Path moduleRoor, String className);

    void scaffoldModule(Path moduleDir);
}
