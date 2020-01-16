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

import dev.jeka.core.api.java.project.JkJavaProjectIde;
import dev.jeka.core.api.java.project.JkJavaProjectIdeSupplier;
import dev.jeka.core.api.system.JkLog;
import dev.jeka.core.api.system.JkProcess;
import dev.jeka.core.api.utils.JkUtilsSystem;
import dev.jeka.core.tool.JkCommands;
import dev.jeka.core.tool.JkPlugin;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Jerome Angibaud
 */
public class CmdJekaDoer implements JekaDoer {

    static final CmdJekaDoer INSTANCE = new CmdJekaDoer();

    static {
        JkLog.registerHierarchicalConsoleHandler();
    }

    public void generateIml(Path moduleDir, String qualifiedClassName) {
        JkProcess iml = jeka(moduleDir)
                .andParams("intellij#iml").withWorkingDir(moduleDir).withLogCommand(true).withLogOutput(true);
        if (qualifiedClassName != null) {
            iml = iml.andParams("-CC=" + qualifiedClassName);
        }
        int result = iml.runSync();
        if (result != 0) {
            iml.andParams("-CC=dev.jeka.core.tool.JkCommands", "java#").withFailOnError(true).runSync();
        }
    }

    public void scaffoldModule(Path moduleDir) {
        JkProcess iml = jeka(moduleDir)
                .andParams("scaffold#run")
                .andParams("scaffold#wrap")
                .andParams("java#")
                .andParams("intellij#iml").withWorkingDir(moduleDir).withLogCommand(true).withLogOutput(true);
        iml.runSync();
    }

    private JkJavaProjectIde findProjectIde(JkCommands jkCommands) {
        if (jkCommands instanceof JkJavaProjectIdeSupplier) {
            JkJavaProjectIdeSupplier javaProjectIdeSupplier = (JkJavaProjectIdeSupplier) jkCommands;
            return javaProjectIdeSupplier.getJavaProjectIde();
        }
        for (JkPlugin plugin : jkCommands.getPlugins().getAll()) {
            if (plugin instanceof JkJavaProjectIdeSupplier) {
                JkJavaProjectIdeSupplier javaProjectIdeSupplier = (JkJavaProjectIdeSupplier) plugin;
                return javaProjectIdeSupplier.getJavaProjectIde();
            }
        }
        return null;
    }

    private JkProcess jeka(Path moduleDir) {
        if (JkUtilsSystem.IS_WINDOWS) {
            String command = Files.exists(moduleDir.resolve("jekaw.bat")) ?
                    moduleDir.toAbsolutePath().resolve("jekaw.bat").toString() : "jeka.bat";
            return JkProcess.of(command);
        }
        return JkProcess.of(Files.exists(moduleDir.resolve("jekaw")) ? "./jekaw" : "jeka");
    }

}
