package dev.jeka.ide.intellij.panel;

import com.intellij.ui.CollectionListModel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class TemplatesPanel {

    private final List<JekaTemplate> originalTemplates;

    private JBList<JekaTemplate> templateJBList;

    private TemplateDetailPanel templateDetailPanel;

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
                System.out.println(templateListModel.getItems());
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
        return splitter;
    }

    private boolean templatesChanged() {
        return !this.originalTemplates.equals(this.templateListModel.getItems());
    }
}
