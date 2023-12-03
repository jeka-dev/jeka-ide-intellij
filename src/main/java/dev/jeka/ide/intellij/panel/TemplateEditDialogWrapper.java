package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import dev.jeka.ide.intellij.extension.TemplatePersistentStateComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TemplateEditDialogWrapper extends DialogWrapper {

    private TemplatesEditPanel templatesEditPanel;

    private BiConsumer<List<JekaTemplate>, JekaTemplate> onOk;

    public TemplateEditDialogWrapper(Project project, TemplatesEditPanel templatesEditPanel,
                                     BiConsumer<List<JekaTemplate>, JekaTemplate> onOk) {
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
        component.setPreferredSize(new Dimension(1000, 600));
        return component;
    }

    @Override
    protected void doOKAction() {
        onOk.accept(templatesEditPanel.getTemplates(), templatesEditPanel.getEditedTemplate());
        TemplatePersistentStateComponent persistedTemplatesComponent =
                TemplatePersistentStateComponent.getInstance();
        persistedTemplatesComponent.setCustomizedTemplates(templatesEditPanel.getTemplates());
        close(OK_EXIT_CODE);
    }

}
