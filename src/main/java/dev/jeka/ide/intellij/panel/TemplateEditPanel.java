package dev.jeka.ide.intellij.panel;

import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.ide.intellij.common.model.JekaTemplate;

import javax.swing.*;
import java.awt.*;

public class TemplateEditPanel {

    JBTextField nameTextField = new JBTextField();

    JBTextField cmdTextField = new ExpandableTextField();

    JBTextArea descTextarea = new JBTextArea();

    TemplateEditPanel() {
        descTextarea.setFont(cmdTextField.getFont());
        descTextarea.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    }

    public JPanel panel() {
        JPanel cmdPanel = UI.PanelFactory.panel(cmdTextField)
                .withLabel("Cmd Arguments:")
                .moveLabelOnTop()
                .createPanel();
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("Name:", nameTextField, false)
                .addComponent(cmdPanel)
                .addLabeledComponentFillVertically("Description:", descTextarea)
                .getPanel();
    }

    public void fill(JekaTemplate template) {
        nameTextField.setText(template.getName());
        cmdTextField.setText(template.getCommandArgs());
        descTextarea.setText(template.getDescription());
    }

    public JekaTemplate getTemplate() {
        return JekaTemplate.builder()
                .name(nameTextField.getText().trim())
                .commandArgs(cmdTextField.getText().trim())
                .description(descTextarea.getText().trim())
                .build();
    }



}
