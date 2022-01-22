package dev.jeka.ide.intellij.unused.newmodule;

import com.intellij.openapi.module.Module;
import com.intellij.ui.DocumentAdapter;
import dev.jeka.ide.intellij.panel.ScaffoldFormPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class WizardMainPanel extends JPanel {

    final ScaffoldFormPanel scaffoldFormPanel;

    final NamePanel namePanel;

    WizardMainPanel(Module[] modules, String parentPath) {
        JPanel wrapper1 = new JPanel();
        LayoutManager layout = new BoxLayout(wrapper1, BoxLayout.PAGE_AXIS);
        wrapper1.setLayout(layout);
        List<String> moduleNames =new LinkedList<>();
        for (Module module : modules) {
            moduleNames.add(module.getName());
        }
        namePanel = new NamePanel(parentPath);
        wrapper1.add(namePanel);
        scaffoldFormPanel = new ScaffoldFormPanel(modules, true);
        wrapper1.add(scaffoldFormPanel);
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        this.add(wrapper1, c);
    }

    static class NamePanel extends JPanel {

        private final String parentPath;

        private final JTextField moduleTextField;

        private final JLabel modulePathLabel;

        public NamePanel(String parentPath) {
            this.parentPath = parentPath;
            GridLayout layout = new GridLayout(2, 2);
            this.setLayout(layout);
            this.add(new JLabel("Module Name"));
            moduleTextField = new JTextField();
            moduleTextField.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    update();
                }
            });
            this.add(moduleTextField);
            this.add(new JLabel("Module Path"));
            modulePathLabel = new JLabel();
            this.add(modulePathLabel);
            update();
        }

        private void update() {
            this.modulePathLabel.setText(parentPath + File.separator + getModuleName());
        }

        String getModuleName() {
            return this.moduleTextField.getText().trim();
        }
    }
}
