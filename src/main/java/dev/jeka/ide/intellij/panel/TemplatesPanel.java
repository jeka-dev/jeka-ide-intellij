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
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class TemplatesPanel {

    private final List<JekaTemplate> originalTemplates;

    private JBList<JekaTemplate> templateJBList;

    private TemplateEditPanel templateEditPanel;

    private CollectionListModel<JekaTemplate> templateListModel;

    private Consumer<List<JekaTemplate>> saveAction = null;

    @Getter
    private JComponent component;

    public TemplatesPanel(List<JekaTemplate> templates, Consumer<List<JekaTemplate>> saveAction) {
        this.templateListModel = new CollectionListModel<>(templates);
        this.originalTemplates = Collections.unmodifiableList(new LinkedList<>(templates));
        this.saveAction = saveAction;
        this.component = component();
    }

    public void setEnabled(boolean enabled) {
        templateJBList.setEnabled(enabled);
        templateEditPanel.setEnabled(enabled);
    }

    public JekaTemplate getSelectedTemplate() {
        return templateJBList.getSelectedValue();
    }

    private JComponent component() {
        templateJBList = new JBList<>(templateListModel);
        templateJBList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JBLabel(value.getName());
            if (cellHasFocus) {
                label.setForeground(Color.white);
            }
            return label;
        });
        templateEditPanel = new TemplateEditPanel();
        templateJBList.addListSelectionListener(event -> {
            if (templateJBList.getSelectedValue() != null) {
                templateEditPanel.fill(templateJBList.getSelectedValue());
                System.out.println(templateListModel.getItems());
            }
        });

        // feed back when template neme changed by user
        templateEditPanel.getNameChangeListener().append(template -> templateListModel.contentsChanged(template));

        templateJBList.setSelectedValue(JekaTemplate.blank(), true);
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

                })
                .addExtraAction(new AnActionButton("Save", "Save", AllIcons.Actions.MenuSaveall) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        saveAction.accept(templateListModel.getItems());
                    }

                    @Override
                    public boolean isVisible() {
                        return saveAction != null;
                    }
                })
                ;

        JPanel decoratorPanel = toolbarDecorator.createPanel();
        decoratorPanel.setMinimumSize(new Dimension(200, 0));
        JBSplitter splitter = new JBSplitter();
        splitter.setFirstComponent(decoratorPanel);
        splitter.setSecondComponent(templateEditPanel.getPanel());
        splitter.setProportion(0.2f);
        splitter.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        return splitter;
    }

    private boolean templatesChanged() {
        return !this.originalTemplates.equals(this.templateListModel.getItems());
    }
}
