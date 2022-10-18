package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.project.Project;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import dev.jeka.ide.intellij.extension.TemplatePersistentStateComponent;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TemplatesPanel {

    private final Project project;

    private final TemplatePersistentStateComponent persistedTemplatesComponent = TemplatePersistentStateComponent.getInstance();

    private JBList<JekaTemplate> templateJBList;

    private TemplateDetailPanel templateDetailPanel;

    private CollectionListModel<JekaTemplate> templateListModel;

    private ActionLink actionLink;

    @Getter
    private JComponent component;


    public TemplatesPanel(Project project) {
        init();
        this.component = component();
        this.project = project;
    }

    public void setEnabled(boolean enabled) {
        templateJBList.setEnabled(enabled);
        templateDetailPanel.setEnabled(enabled);
        actionLink.setEnabled(enabled);
    }

    public JekaTemplate getSelectedTemplate() {
        return templateJBList.getSelectedValue();
    }

    public String getTemplateCmd() {
        return templateDetailPanel.getCmd();
    }

    private JComponent component() {
        templateJBList = new JBList<>(templateListModel);
        templateJBList.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        templateJBList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JBLabel(value.getName());
            label.setBorder(BorderFactory.createEmptyBorder(2, 5, 5 , 0));
            if (cellHasFocus) {
                label.setForeground(Color.white);
            }
            return label;
        });
        templateDetailPanel = new TemplateDetailPanel();
        templateJBList.addListSelectionListener(event -> {
            if (templateJBList.getSelectedValue() != null) {
                templateDetailPanel.fill(templateJBList.getSelectedValue());
            }
        });

        // feed back when template neme changed by user
        templateDetailPanel.getNameChangeListener().append(template -> templateListModel.contentsChanged(template));

        templateJBList.setSelectedIndex(0);

        templateJBList.setMinimumSize(new Dimension(200, 0));
        JBSplitter splitter = new JBSplitter();
        splitter.setFirstComponent(templateJBList);
        splitter.setSecondComponent(templateDetailPanel.getPanel());
        splitter.setProportion(0.2f);
        splitter.setBorder(BorderFactory.createLineBorder(Color.lightGray));

        actionLink = new ActionLink("Manage templates ...", e -> {
            TemplatesEditPanel templatesEditPanel = new TemplatesEditPanel();
            TemplateEditDialogWrapper dialogWrapper = new TemplateEditDialogWrapper(project, templatesEditPanel, this::update);
            dialogWrapper.show();
        });
        actionLink.setText("Manage templates ...");
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(splitter, BorderLayout.CENTER);
        panel.add(actionLink, BorderLayout.SOUTH);
        return panel;
    }

    private void update(List<JekaTemplate> templates) {
        this.persistedTemplatesComponent.setTemplates(templates);
        templateListModel.removeAll();
        templateListModel.addAll(0, templates);
        if (templates.size() > 0) {
            templateJBList.setSelectedIndex(0);
        }
    }

    private void init() {
        List<JekaTemplate> templates = this.persistedTemplatesComponent.getTemplates();
        this.templateListModel = new CollectionListModel<>(templates);
    }

}
