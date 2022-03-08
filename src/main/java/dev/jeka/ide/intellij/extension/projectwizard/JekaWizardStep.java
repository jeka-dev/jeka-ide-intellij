package dev.jeka.ide.intellij.extension.projectwizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import dev.jeka.core.tool.JkExternalToolApi;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

class JekaWizardStep extends ModuleWizardStep implements Disposable {

    private JekaWizardPanel wizardPanel;

    private JekaModuleBuilder moduleBuilder;

    private JekaWizardStep(JekaWizardPanel wizardMainPanel, JekaModuleBuilder moduleBuilder) {
        this.wizardPanel = wizardMainPanel;
        this.moduleBuilder = moduleBuilder;
    }

    static JekaWizardStep of(WizardContext wizardContext, JekaModuleBuilder moduleBuilder) {
        JekaWizardPanel wizardMainPanel = new JekaWizardPanel(wizardContext);
        return new JekaWizardStep(wizardMainPanel, moduleBuilder);
    }

    @Override
    public JComponent getComponent() {
        return wizardPanel.getPanel();
    }

    @Override
    public void updateDataModel() {
        moduleBuilder.setName(this.wizardPanel.getName());
        moduleBuilder.setContentEntryPath(wizardPanel.getLocation());
        moduleBuilder.setModuleFilePath(JkExternalToolApi.getImlFile(Paths.get(wizardPanel.getLocation())).toString());
        JekaModuleBuilder.ModuleData moduleData = moduleBuilder.moduleData;
        moduleData.setModuleDir(this.wizardPanel.getLocation());
        moduleData.setScaffoldNature(wizardPanel.getScaffoldPanel().getScaffoldNature());
        moduleData.setWrapperDelegate(wizardPanel.getScaffoldPanel().getSelectedDelegateWrapperModule());
        moduleData.setWrapperVersion(wizardPanel.getScaffoldPanel().getSelectedJekaVersion());
        if (wizardPanel.getWizardContext().getProject() == null) {
            wizardPanel.getWizardContext().setProjectFileDirectory(Paths.get(wizardPanel.getLocation()), true);
        } else {
            wizardPanel.getWizardContext().setProjectFileDirectory(wizardPanel.getWizardContext().getProject().getBasePath());
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        String validation = wizardPanel.validate();
        if (validation != null) {
            throw new ConfigurationException(validation);
        }
        return true;
    }

    @Override
    public void dispose() {
        this.wizardPanel = null;
        this.moduleBuilder = null;
    }
}
