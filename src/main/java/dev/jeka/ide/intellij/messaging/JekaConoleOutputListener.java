package dev.jeka.ide.intellij.messaging;

import dev.jeka.core.api.system.JkLog;

public interface JekaConoleOutputListener {

    void newLine(boolean err, String line);

}
