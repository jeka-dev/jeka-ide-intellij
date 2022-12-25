package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBTextArea;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import dev.jeka.ide.intellij.extension.TemplatePersistentStateComponent;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TemplatesPanel {

    private final Project project;

    private final TemplatePersistentStateComponent persistedTemplatesComponent = TemplatePersistentStateComponent.getInstance();

    private final ComboBox<JekaTemplate> templateComboBox = new ComboBox<>();

    private final JBTextArea descTextarea = new JBTextArea();

    private volatile boolean skipChange;

    @Getter
    private JComponent component;

    public TemplatesPanel(Project project) {

        this.component = component();
        this.project = project;
        this.descTextarea.setOpaque(false);
        this.descTextarea.setLineWrap(true);
        this.descTextarea.setWrapStyleWord(true);
    }

    public void setEnabled(boolean enabled) {
        templateComboBox.setEnabled(enabled);
    }

    public JekaTemplate getSelectedTemplate() {
        return templateComboBox.getItem();
    }

    public String getTemplateCmd() {
        return templateComboBox.getItem().getCommandArgs();
    }

    private JComponent component() {
        List<JekaTemplate> templates = this.persistedTemplatesComponent.getTemplates();
        templates.forEach(template -> this.templateComboBox.addItem(template));
        templateComboBox.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        templateComboBox.addItemListener(event -> {
            if (skipChange) {
                return;
            }
            if (templateComboBox.getItem() != null) {
                this.descTextarea.setText(templateComboBox.getItem().getDescription());
            }
        });
        templateComboBox.setSelectedIndex(0);
        this.descTextarea.setText(templateComboBox.getItem().getDescription());
        JPanel panel = new JPanel();
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(15);
        panel.setLayout(borderLayout);
        panel.add(templateComboBox, BorderLayout.NORTH);
        panel.add(descTextarea, BorderLayout.CENTER);
        return panel;
    }

    void update(List<JekaTemplate> templates) {
        skipChange = true;
        this.templateComboBox.removeAllItems();
        templates.forEach(template -> this.templateComboBox.addItem(template));
        skipChange = false;
        if (templates.size() > 0) {
            templateComboBox.setSelectedIndex(0);
        }
    }



}
