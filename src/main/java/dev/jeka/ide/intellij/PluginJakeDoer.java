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

import dev.jeka.core.api.system.JkException;
import dev.jeka.core.api.system.JkLog;
import dev.jeka.core.tool.Main;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jerome Angibaud
 */
public class PluginJakeDoer implements JekaDoer {

    static final PluginJakeDoer INSTANCE = new PluginJakeDoer();

    static {
        JkLog.registerHierarchicalConsoleHandler();
    }

    public void generateIml(Path moduleDir, String qualifiedClassName) {
        List<String> args = new LinkedList<>();
        if (qualifiedClassName != null) {
            args.add("-CC=" + qualifiedClassName);
        }
        args.add("intellij#iml");
        args.add("java#");
        try {
            Main.exec(moduleDir, args.toArray(new String[0]));
        } catch (JkException e) {
            args.remove("-CC=" + qualifiedClassName);
            args.add("-CC=dev.jeka.core.tool.JkCommands");
            Main.exec(moduleDir, args.toArray(new String[0]));
        }
    }

    public void scaffoldModule(Path moduleDir) {
        List<String> args = new LinkedList<>();
        args.add("scaffold#run");
        args.add("scaffold#wrap");
        args.add("java#");
        args.add("intellij#iml");
        Main.exec(moduleDir, args.toArray(new String[0]));
    }

}
