package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class TemplateEditDialogWrapper extends DialogWrapper {

    private TemplatesEditPanel templatesEditPanel;

    private Runnable onOk;

    public TemplateEditDialogWrapper(Project project, TemplatesEditPanel templatesEditPanel, Runnable onOk) {
        super(project, true);
        this.templatesEditPanel = templatesEditPanel;
        this.init();
        setTitle("Manage Templates");
        this.onOk = onOk;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JComponent component = templatesEditPanel.getComponent();
        component.setPreferredSize(new Dimension(700, 400));
        return component;
    }

    @Override
    protected void doOKAction() {
        templatesEditPanel.save();
        onOk.run();
        close(OK_EXIT_CODE);
    }
}
