package dev.jeka.ide.intellij.unused.runconfigations;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.LabeledComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JekaToolSettingsEditor extends SettingsEditor<JekaToolRunConfiguration> {

    private JPanel myPanel;

    private LabeledComponent<JTextField> myMainClass;

    @Override
    protected void resetEditorFrom(@NotNull JekaToolRunConfiguration s) {

    }

    @Override
    protected void applyEditorTo(@NotNull JekaToolRunConfiguration s) throws ConfigurationException {

    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        createUIComponents();
        return myMainClass;
    }

    private void createUIComponents() {
        myMainClass = LabeledComponent.create(new JTextField(), "Root");
    }
}
