package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;
import dev.jeka.ide.intellij.engine.ConfigurationRunner;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;

public class RunDialogWrapper extends DialogWrapper {

    private final Module module;

    private final boolean debug;

    private RunFormPanel runFormPanel;

    private String originalCommand;

    private final String confifurationName;

    public RunDialogWrapper(Module module, boolean debug, String originalCommand, @Nullable String configurationName) {
        super(module.getProject(), true);
        this.module = module;
        this.debug = debug;
        this.originalCommand = originalCommand;
        this.confifurationName = configurationName;
        setTitle("Execute Jeka command");
        this.init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        runFormPanel = new RunFormPanel(module, originalCommand);
        return runFormPanel.getPanel();
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeLater(() -> {
            ConfigurationRunner.run(module, confifurationName, runFormPanel.getCmd(), false);
            RunDialogWrapper.this.close(0);
        });


    }

}
