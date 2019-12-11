package dev.jeka.ide.intellij;

import java.nio.file.Path;

public interface JekaDoer {

    static JekaDoer getInstance() {
        return CmdJekaDoer.INSTANCE;
    }

    void generateIml(Path moduleRoor, String className);

    void scaffoldModule(Path moduleDir);
}
