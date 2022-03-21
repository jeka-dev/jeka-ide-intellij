package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import dev.jeka.ide.intellij.extension.TemplatePersistentStateComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.ListIterator;

public class TemplateDialogWrapper extends DialogWrapper {

    private CollectionListModel<JekaTemplate> templates;

    private JekaTemplate original;

    private TemplateEditPanel templateEditPanel;

    private JBList<JekaTemplate> templateJbList;

    public TemplateDialogWrapper(JekaTemplate template, CollectionListModel<JekaTemplate> templates,
                                 JBList<JekaTemplate> templateJBList) {
        super(true);
        original = template;
        this.templates = templates;
        this.templateJbList = templateJBList;
        this.init();
        setTitle("Edit template");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        templateEditPanel = new TemplateEditPanel();
        templateEditPanel.fill(original);
        return templateEditPanel.panel();
    }

    @Override
    protected void doOKAction() {
        JekaTemplate template = templateEditPanel.getTemplate();
        JekaTemplate.addOrReplace(templates, template);
        this.close(0);
        templateJbList.setSelectedValue(template, true);
    }
}
