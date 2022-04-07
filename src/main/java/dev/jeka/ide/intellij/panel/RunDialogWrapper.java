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

    private Module module;

    private final boolean debug;

    private RunFormPanel runFormPanel;

    private String originalCommand;

    public RunDialogWrapper(Module module, boolean debug, String originalCommand) {
        super(module.getProject(), true);
        this.module = module;
        this.debug = debug;
        this.originalCommand = originalCommand;
        this.init();
        setTitle("Execute Jeka command");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        runFormPanel = new RunFormPanel(originalCommand);
        return runFormPanel.getPanel();
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            ConfigurationRunner.run(module, "my name", runFormPanel.getCmd(), false);
            RunDialogWrapper.this.close(0);
        });


    }

}
