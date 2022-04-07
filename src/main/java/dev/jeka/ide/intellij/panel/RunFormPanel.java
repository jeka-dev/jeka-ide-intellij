package dev.jeka.ide.intellij.panel;

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import lombok.Getter;

import javax.swing.*;

public class RunFormPanel {

    @Getter
    private JPanel panel;

    private final JBTextField cmdTextField = new ExpandableTextField();

    RunFormPanel(String originalCommand) {
        this.panel = panel();
        cmdTextField.setText(originalCommand);
    }

    public String getCmd() {
        return cmdTextField.getText();
    }



    private JPanel panel() {
        JPanel cmdPanel = UI.PanelFactory.panel(cmdTextField)
                .withLabel("Cmd Arguments:")
                .moveLabelOnTop()
                .createPanel();
        return FormBuilder.createFormBuilder()
                //.addLabeledComponent("Name:", nameTextField, false)
                .addComponent(cmdPanel)
               // .addLabeledComponentFillVertically("Description:", descTextarea)
                .getPanel();

    }
}
