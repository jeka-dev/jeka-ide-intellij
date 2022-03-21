package dev.jeka.ide.intellij.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@RequiredArgsConstructor
public class TemplatesPanel {

    @Getter
    private final List<JekaTemplate> templates;

    public JComponent component() {
        JBList<JekaTemplate> templateJBList = new JBList<>(templates);
        templateJBList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> new JBLabel(value.getName()));
        TemplateEditPanel templateEditPanel = new TemplateEditPanel();
        templateJBList.addListSelectionListener(event -> {
            if (templateJBList.getSelectedValue() != null) {
                templateEditPanel.fill(templateJBList.getSelectedValue());
            }
        });
        templateJBList.setSelectedValue(JekaTemplate.BLANK, true);
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(templateJBList)
                .setMinimumSize(new Dimension(200, 150))
                .addExtraAction(new AnActionButton("Duplicate", "Duplicate", AllIcons.Actions.Copy) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        JekaTemplate newTemplate = JekaTemplate.duplicate(templates, templateJBList.getSelectedValue());
                        templateJBList.setSelectedValue(newTemplate, false);
                    }
                })
                .addExtraAction(new AnActionButton("Reload standard template definitions", "Reload standard template definitions",
                        AllIcons.Actions.Refresh) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        JekaTemplate.resetBuiltin(templates);
                    }

                })
                ;

        JPanel decoratorPanel = toolbarDecorator.createPanel();
        JBSplitter splitter = new JBSplitter();
        splitter.setFirstComponent(decoratorPanel);
        splitter.setSecondComponent(templateEditPanel.panel());
        splitter.setProportion(0.2f);
        return splitter;
    }
}
