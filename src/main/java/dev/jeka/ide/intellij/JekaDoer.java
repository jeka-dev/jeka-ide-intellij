package dev.jeka.ide.intellij;

import java.nio.file.Path;

public interface JekaDoer {

    static JekaDoer getInstance() {
        return new PluginJakeDoer();
    }

    void generateIml(Path moduleRoor, String className);

    void scaffoldModule(Path moduleDir);
}
