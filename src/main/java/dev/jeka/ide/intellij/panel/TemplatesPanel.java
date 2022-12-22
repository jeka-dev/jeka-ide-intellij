package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.ActionLink;
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

    @Getter
    private JComponent component;

    public TemplatesPanel(Project project) {
        init();
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
        templateComboBox.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        /*
        templateComboBox.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JBLabel(value.getName());
            label.setBorder(BorderFactory.createEmptyBorder(2, 5, 5 , 0));
            if (cellHasFocus) {
                label.setForeground(Color.white);
            }
            return label;
        });
        */
        templateComboBox.addItemListener(event -> {
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
        this.persistedTemplatesComponent.setTemplates(templates);
        templateComboBox.removeAll();
        templates.forEach(template -> this.templateComboBox.addItem(template));
        if (templates.size() > 0) {
            templateComboBox.setSelectedIndex(0);
        }
    }

    private void init() {
        List<JekaTemplate> templates = this.persistedTemplatesComponent.getTemplates();
        templateComboBox.removeAll();
        templates.forEach(template -> this.templateComboBox.addItem(template));
    }

}
