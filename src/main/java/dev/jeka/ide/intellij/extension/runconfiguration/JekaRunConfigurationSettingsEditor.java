package dev.jeka.ide.intellij.extension.runconfiguration;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.JavaSettingsEditorBase;
import com.intellij.execution.ui.*;
import com.intellij.ui.EditorTextField;
import com.intellij.util.ui.UIUtil;
import dev.jeka.ide.intellij.panel.RunFormPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class JekaRunConfigurationSettingsEditor extends JavaSettingsEditorBase<ApplicationConfiguration> {


    public JekaRunConfigurationSettingsEditor(ApplicationConfiguration configuration) {
        super(configuration);
    }

    @Override
    public boolean isInplaceValidationSupported() {
        return true;
    }

    @Override
    protected void customizeFragments(List<SettingsEditorFragment<ApplicationConfiguration, ?>> fragments,
                                      SettingsEditorFragment<ApplicationConfiguration, ModuleClasspathCombo> moduleClasspath,
                                      CommonParameterFragments<ApplicationConfiguration> commonParameterFragments) {

        fragments.add(createJekaFormFragment());
        SettingsEditorFragment<ApplicationConfiguration, EditorTextField> mainClassFragment = createMainClass(moduleClasspath.component());
        fragments.add(mainClassFragment);
        DefaultJreSelector jreSelector = DefaultJreSelector.fromSourceRootsDependencies(moduleClasspath.component(), mainClassFragment.component());
        SettingsEditorFragment<ApplicationConfiguration, JrePathEditor> jrePath = CommonJavaFragments.createJrePath(jreSelector);
        fragments.add(jrePath);

        // Try to hide WORKING_DIRECTORY. Cannot find a solution to set it hidden.
        // The only thing working here, is compoent.setEnaabled
        commonParameterFragments.getFragments().get(0).setCanBeHidden(true);
        // commonParameterFragments.getFragments().get(0).getComponent().setVisible(false);  no effect
        commonParameterFragments.getFragments().get(0).setRemovable(true);
        commonParameterFragments.getFragments().get(0).getComponent().setEnabled(false);
    }

    @NotNull
    private SettingsEditorFragment<ApplicationConfiguration, EditorTextField> createMainClass(ModuleClasspathCombo classpathCombo) {
        EditorTextField textField = new EditorTextField();
        textField.setEnabled(false);
        textField.setBackground(UIUtil.getTextFieldBackground());
        textField.setText(dev.jeka.core.tool.Main.class.getName());
        CommonParameterFragments.setMonospaced(textField);
        SettingsEditorFragment<ApplicationConfiguration, EditorTextField> mainClassFragment =
                new SettingsEditorFragment<>("mainClass", ExecutionBundle.message("application.configuration.main.class"), null, textField, 20,
                        (configuration, editorTextField) -> {},
                        (configuration, editorTextField) -> {},
                        configuration -> true);
        return mainClassFragment;
    }

    private static SettingsEditorFragment createJekaFormFragment() {
        RunFormPanel runFormPanel = new RunFormPanel(null, "");
        SettingsEditorFragment<ApplicationConfiguration, JPanel> settingsEditorFragment =
                new SettingsEditorFragment<>("", "Jeka", null, runFormPanel.getPanel(),
                        SettingsEditorFragmentType.COMMAND_LINE,
                        (configuration, jPanel) -> {
                            runFormPanel.setCmd(configuration.getProgramParameters());
                            runFormPanel.setModule(configuration.getConfigurationModule().getModule());
                            runFormPanel.syncOptionsWithCmdLine();
                        },
                        (configuration, jPanel) -> {
                            configuration.setProgramParameters(runFormPanel.getCmd());
                        },
                        configuration -> true);
        settingsEditorFragment.setRemovable(false);
        return settingsEditorFragment;
    }

    @Override
    protected void initFragments(Collection<? extends SettingsEditorFragment<ApplicationConfiguration, ?>> settingsEditorFragments) {
        super.initFragments(settingsEditorFragments);
    }
}
