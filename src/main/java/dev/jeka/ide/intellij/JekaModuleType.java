package dev.jeka.ide.intellij;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JekaModuleType extends ModuleType {

    private static final String ID = "JEKA_MODULE_TYPE";

    static final JekaModuleType INSTANCE = new JekaModuleType();

    protected JekaModuleType() {
        super(ID);
    }

    @NotNull
    @Override
    public ModuleBuilder createModuleBuilder() {
        return new JekaModuleBuilder();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @NotNull
    @Override
    public String getName() {
        return "Jeka";
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getDescription() {
        return "A module built with Jeka";
    }

    @NotNull
    @Override
    public Icon getNodeIcon(boolean isOpened) {
        return JkIcons.JEKA_GROUP_ACTION;
    }
}
