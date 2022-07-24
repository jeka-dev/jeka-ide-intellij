package dev.jeka.ide.intellij.panel;

import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import dev.jeka.core.api.function.JkConsumers;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

class TemplateDetailPanel {

    private final JBTextField cmdTextField = new ExpandableTextField();

    private final JBTextArea descTextarea = new JBTextArea();

    @Getter
    private JekaTemplate editedTemplate;

    @Getter
    private JkConsumers<JekaTemplate, Void> nameChangeListener = JkConsumers.of();

    @Getter
    private JPanel panel;

    TemplateDetailPanel() {
        descTextarea.setFont(cmdTextField.getFont());
        descTextarea.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        descTextarea.setLineWrap(true);
        descTextarea.setWrapStyleWord(true);
        descTextarea.setEditable(false);
        panel = panel();
    }

    private JPanel panel() {
        JPanel cmdPanel = UI.PanelFactory.panel(cmdTextField)
                .withLabel("Cmd Arguments:")
                .moveLabelOnTop()
                .createPanel();
        return FormBuilder.createFormBuilder()
                .addComponent(cmdPanel)
                .addLabeledComponentFillVertically("Description:", descTextarea)
                .getPanel();
    }

    public void fill(JekaTemplate template) {
        editedTemplate = template;
        cmdTextField.setText(template.getCommandArgs());
        descTextarea.setText(template.getDescription());
    }

    String getCmd() {
        return cmdTextField.getText();
    }

}
