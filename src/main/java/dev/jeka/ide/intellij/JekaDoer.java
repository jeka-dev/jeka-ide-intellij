package dev.jeka.ide.intellij;

import dev.jeka.core.api.java.project.JkJavaProjectIde;
import dev.jeka.core.api.java.project.JkJavaProjectIdeSupplier;
import dev.jeka.core.api.system.JkProcess;
import dev.jeka.core.api.tooling.intellij.JkImlGenerator;
import dev.jeka.core.tool.JkCommands;
import dev.jeka.core.tool.JkPlugin;

import java.nio.file.Path;

public class JekaDoer {

    public void generateIml(Path moduleDir) {
        JkProcess iml = JkProcess.ofWinOrUx("jeka.bat", "jeka")
                .andParams("intellij#iml").withWorkingDir(moduleDir);
        int result = iml.runSync();
        if (result != 0) {
            iml.andParams("-CC=dev.jeka.core.tool.JkCommands").withFailOnError(true).runSync();
        }
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
