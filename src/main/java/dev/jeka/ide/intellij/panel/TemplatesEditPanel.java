package dev.jeka.ide.intellij.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import dev.jeka.ide.intellij.extension.TemplatePersistentStateComponent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class TemplatesEditPanel {

    private final List<JekaTemplate> originalTemplates;

    private final TemplatePersistentStateComponent persistedTemplatesComponent =
            TemplatePersistentStateComponent.getInstance();

    private JBList<JekaTemplate> templateJBList;

    private TemplateDetailEditPanel templateDetailEditPanel;

    private CollectionListModel<JekaTemplate> templateListModel;

    private Consumer<List<JekaTemplate>> saveAction;

    @Getter
    private JComponent component;

    public TemplatesEditPanel() {
        List<JekaTemplate> templates = persistedTemplatesComponent.getTemplates();
        this.templateListModel = new CollectionListModel<>(templates);
        this.originalTemplates = Collections.unmodifiableList(new LinkedList<>(templates));
        this.saveAction = saveAction;
        this.component = component();
    }

    private JComponent component() {
        templateJBList = new JBList<>(templateListModel);
        templateJBList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JBLabel(value.getName());
            label.setBorder(BorderFactory.createEmptyBorder(2, 5, 5 , 0));
            if (cellHasFocus) {
                label.setForeground(Color.white);
            }
            return label;
        });
        templateDetailEditPanel = new TemplateDetailEditPanel();
        templateJBList.addListSelectionListener(event -> {
            if (templateJBList.getSelectedValue() != null) {
                JekaTemplate template = templateJBList.getSelectedValue();
                templateDetailEditPanel.fill(template);
                templateDetailEditPanel.setEnabled(!template.isBuiltin());
            }
        });

        // feed back when template name has been changed by user
        templateDetailEditPanel.getNameChangeListener().append(template -> templateListModel.contentsChanged(template));

        templateJBList.setSelectedIndex(0);
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(templateJBList)
                .setMinimumSize(new Dimension(200, 150))
                .addExtraAction(new AnActionButton("Duplicate", "Duplicate", AllIcons.Actions.Copy) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        List<JekaTemplate> newTemplates = templateListModel.toList();
                        if (templateJBList.getSelectedValue() == null) {
                            return;
                        }
                        JekaTemplate newTemplate = JekaTemplate.duplicate(newTemplates,
                                templateJBList.getSelectedValue());
                        templateListModel.replaceAll(newTemplates);
                        templateJBList.setSelectedValue(newTemplate, true);
                    }
                })
                .setAddAction(anActionButton -> {
                    List<JekaTemplate> newTemplates = templateListModel.toList();
                    String name = JekaTemplate.suggestNewName(newTemplates);
                    JekaTemplate newTemplate = JekaTemplate.builder()
                                    .name(name).commandArgs("").description("No description available.").build();
                    newTemplates.add(newTemplate);
                    templateListModel.replaceAll(newTemplates);
                    templateJBList.setSelectedValue(newTemplate, true);
                })
                .addExtraAction(new AnActionButton("Reload standard template definitions", "Reload standard template definitions",
                        AllIcons.Diff.Revert) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        List<JekaTemplate> newTemplates = templateListModel.toList();
                        JekaTemplate.resetBuiltin(newTemplates);
                        templateListModel.replaceAll(newTemplates);
                    }



                });

        JPanel decoratorPanel = toolbarDecorator.createPanel();
        decoratorPanel.setMinimumSize(new Dimension(200, 0));
        JBSplitter splitter = new JBSplitter();
        splitter.setFirstComponent(decoratorPanel);
        splitter.setSecondComponent(templateDetailEditPanel.getPanel());
        splitter.setProportion(0.2f);
        splitter.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        return splitter;
    }

    void save() {
        if (templatesChanged()) {
            this.persistedTemplatesComponent.setTemplates(templateListModel.getItems());
        }
    }

    List<JekaTemplate> getTemplates() {
        return templateListModel.getItems();
    }

    boolean templatesChanged() {
        return !this.originalTemplates.equals(this.templateListModel.getItems());
    }

}
