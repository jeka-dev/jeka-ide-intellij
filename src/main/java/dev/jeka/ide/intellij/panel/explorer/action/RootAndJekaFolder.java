package dev.jeka.ide.intellij.panel.explorer.action;

import com.intellij.openapi.actionSystem.DataKey;
import dev.jeka.ide.intellij.panel.explorer.model.JekaFolderNode;
import dev.jeka.ide.intellij.panel.explorer.model.JekaRootManager;
import lombok.Value;

@Value
public class RootAndJekaFolder {

    public static final DataKey<RootAndJekaFolder> DATA_KEY = DataKey.create("jekaFolder");

    JekaRootManager rootManager;

    JekaFolderNode jekaFolder;
}
