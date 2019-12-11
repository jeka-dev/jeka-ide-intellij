package dev.jeka.ide.intellij;

import dev.jeka.core.api.java.project.JkJavaProjectIde;
import dev.jeka.core.api.java.project.JkJavaProjectIdeSupplier;
import dev.jeka.core.api.system.JkException;
import dev.jeka.core.api.system.JkHierarchicalConsoleLogHandler;
import dev.jeka.core.api.system.JkLog;
import dev.jeka.core.api.system.JkProcess;
import dev.jeka.core.api.tooling.intellij.JkImlGenerator;
import dev.jeka.core.api.utils.JkUtilsSystem;
import dev.jeka.core.tool.JkCommands;
import dev.jeka.core.tool.JkPlugin;
import dev.jeka.core.tool.Main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

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
            return JkProcess.of(Files.exists(moduleDir.resolve("jekaw.bat"))  ? "jekaw" : "jeka.bat");
        }
        return JkProcess.of(Files.exists(moduleDir.resolve("jekaw"))  ? "jekaw" : "jeka");
    }

}
