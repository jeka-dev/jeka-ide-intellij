package dev.jeka.ide.intellij;

import dev.jeka.core.api.java.project.JkJavaProjectIde;
import dev.jeka.core.api.java.project.JkJavaProjectIdeSupplier;
import dev.jeka.core.api.system.JkHierarchicalConsoleLogHandler;
import dev.jeka.core.api.system.JkLog;
import dev.jeka.core.api.system.JkProcess;
import dev.jeka.core.api.tooling.intellij.JkImlGenerator;
import dev.jeka.core.tool.JkCommands;
import dev.jeka.core.tool.JkPlugin;

import java.nio.file.Path;

public class JekaDoer {

    static {
        JkLog.registerHierarchicalConsoleHandler();
    }

    public void generateIml(Path moduleDir) {
        JkProcess iml = JkProcess.ofWinOrUx("jeka.bat", "jeka")
                .andParams("intellij#iml").withWorkingDir(moduleDir).withLogCommand(true).withLogOutput(true);
        int result = iml.runSync();
        if (result != 0) {
            iml.andParams("-CC=dev.jeka.core.tool.JkCommands", "java#").withFailOnError(true).runSync();
        }
    }

    public void scaffoldModule(Path moduleDir) {
        JkProcess iml = JkProcess.ofWinOrUx("jeka.bat", "jeka")
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

}
