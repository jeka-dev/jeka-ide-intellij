package dev.jeka.ide.intellij.unused.newmodule;

import dev.jeka.ide.intellij.panel.ScaffoldFormPanel;

import java.nio.file.Paths;

class NewModuleData {

    ScaffoldFormPanel scaffoldFormPanel;

    String path;

    String name() {
        return Paths.get(path).getFileName().toString();
    }
}
