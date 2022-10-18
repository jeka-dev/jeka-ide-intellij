package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class TemplateEditDialogWrapper extends DialogWrapper {

    private TemplatesEditPanel templatesEditPanel;

    private Consumer<java.util.List<JekaTemplate>> onOk;

    public TemplateEditDialogWrapper(Project project, TemplatesEditPanel templatesEditPanel,
                                     Consumer<List<JekaTemplate>> onOk) {
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
        if (templatesEditPanel.templatesChanged()) {
            onOk.accept(templatesEditPanel.getTemplates());
        }
        close(OK_EXIT_CODE);
    }
}
