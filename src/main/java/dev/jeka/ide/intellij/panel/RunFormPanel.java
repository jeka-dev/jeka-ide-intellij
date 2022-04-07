package dev.jeka.ide.intellij.panel;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import dev.jeka.core.api.utils.JkUtilsString;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
        JPanel optionPanel = new OptionPanel();
        return FormBuilder.createFormBuilder()
                //.addLabeledComponent("Name:", nameTextField, false)
                .addComponent(cmdPanel)
                .addComponent(optionPanel)
                //.addLabeledComponentFillVertically("Description:", descTextarea)
                .getPanel();

    }

    void adaptText(String keyword, Object value) {
        List<String> tokens = Arrays.stream(cmdTextField.getText().split(" "))
                .map(String::trim)
                .collect(Collectors.toList());
        final List<String> result = new LinkedList<>();
        boolean found = false;
        for (String token : tokens) {
            String extractedOption = extractStandardOption(token);
            if (extractedOption == null || !keyword.equals(extractedOption)) {
                result.add(token);
            } else {
                found = true;
                result.add("-" + keyword + "=" + value);
            }
        }
        if (!found) {
            result.add("-" + keyword + "=" + value);
        }
        String text = String.join(" ", result);
        cmdTextField.setText(text);
    }

    private String extractStandardOption(String token) {
        if (token.startsWith("-D")) {
            return null;
        }
        if (!token.startsWith("-")) {
            return null;
        }
        if (!token.contains("=")) {
            return token.substring(1);
        }
        return JkUtilsString.substringBeforeFirst(token.substring(1), "=");
    }

    private class OptionPanel extends JBPanel {

        private JBCheckBox lvCb = new JBCheckBox();

        private JBCheckBox lsuCb = new JBCheckBox();

        private JBCheckBox lstCb = new JBCheckBox();

        private JBCheckBox lriCb = new JBCheckBox();

        private JBCheckBox wcCb = new JBCheckBox();

        OptionPanel() {
            GridLayout layout = new GridLayout(0,3);
            this.setLayout(layout);

            // -lv
            listen(lvCb, "lv");
            this.add(panel(lvCb, "lv", "Log Verbose"));

            // -lsu
            listen(lsuCb, "lsu");
            this.add(panel(lsuCb, "lsu", "Log Setup : display information when Jeka is " +
                    "setting up build classes and instances."));

            // -lst
            listen(lstCb, "lst");
            this.add(panel(lstCb, "lst", "Log Stacktrace : log complete stacktrace on failure."));

            // -lri
            listen(lriCb, "lri");
            this.add(panel(lriCb, "lri", "Log Runtime Info : log Jeka runtime information."));

            // -wc
            listen(wcCb, "wc");
            this.add(panel(wcCb, "wc", "Work Clean : delete Jeka caches before running."));
        }

        private JPanel panel(JComponent cmp, String label, String tooltipText) {
            return UI.PanelFactory.panel(cmp).withLabel(label).withTooltip(tooltipText).createPanel();
        }

        private void listen(JBCheckBox cb, String keyword) {
            cb.addItemListener(item -> {
                Object value = cb.isSelected() ? true : false;
                adaptText(keyword, value);
            });
        }

    }
}
