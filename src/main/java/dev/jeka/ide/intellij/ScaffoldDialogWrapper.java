package dev.jeka.ide.intellij;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;

class ScaffoldDialogWrapper extends DialogWrapper {

    private VirtualFile moduleDir;

    private Project project;

    private Checkbox createWrapperCb = new Checkbox("Create wrapper");

    protected ScaffoldDialogWrapper(@Nullable Project project) {
        super(project, true);
        init();
        setTitle("Create Jeka files");
        this.project = project;
    }

    void setModuleDir(VirtualFile moduleDir) {
        this.moduleDir = moduleDir;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        createWrapperCb.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(createWrapperCb, BorderLayout.CENTER);
        return dialogPanel;
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            JekaDoer jekaDoer = JekaDoer.getInstance();
            jekaDoer.scaffoldModule(project, moduleDir, createWrapperCb.getState());
            ScaffoldDialogWrapper.this.close(0);
        });
    }




}
