package dev.jeka.ide.intellij;

import dev.jeka.core.api.system.JkException;
import dev.jeka.core.api.system.JkLog;
import dev.jeka.core.tool.Main;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class PluginJakeDoer implements JekaDoer {

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
