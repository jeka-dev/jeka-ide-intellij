package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import dev.jeka.ide.intellij.engine.ConfigurationRunner;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RunDialogWrapper extends DialogWrapper {

    private final Module module;

    private final boolean debug;

    private RunFormPanel runFormPanel;

    private String originalCommand;

    private final String configurationName;

    public RunDialogWrapper(Module module, boolean debug, String originalCommand, @Nullable String configurationName) {
        super(module.getProject(), true);
        this.module = module;
        this.debug = debug;
        this.originalCommand = originalCommand;
        this.configurationName = configurationName;
        if (debug) {
            setTitle("Debug Jeka command");
        } else {
            setTitle("Execute Jeka command");
        }
        this.init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        runFormPanel = new RunFormPanel(module.getProject(), module, originalCommand);
        return runFormPanel.getPanel();
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeLater(() -> {
            ConfigurationRunner.run(module, configurationName, runFormPanel.getCmd(), false);
            RunDialogWrapper.this.close(0);
        });


    }

}
