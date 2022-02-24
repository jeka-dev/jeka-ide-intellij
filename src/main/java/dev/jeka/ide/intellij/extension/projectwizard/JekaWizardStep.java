package dev.jeka.ide.intellij.extension.projectwizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

class JekaWizardStep extends ModuleWizardStep {

    private final JekaWizardPanel wizardPanel;

    private final JekaModuleData moduleData;

    private JekaWizardStep(JekaWizardPanel wizardMainPanel, JekaModuleData moduleData) {
        this.wizardPanel = wizardMainPanel;
        this.moduleData = moduleData;
    }

    static JekaWizardStep of(WizardContext wizardContext, JekaModuleData moduleData) {
        Project project = wizardContext.getProject();
        Path projectRoot = Paths.get(project.getBasePath());
        Path contextDir = Paths.get(wizardContext.getProjectFileDirectory());
        String path = projectRoot.getParent().relativize(contextDir).toString();
        JekaWizardPanel wizardMainPanel = new JekaWizardPanel(project);
        return new JekaWizardStep(wizardMainPanel, moduleData);
    }

    @Override
    public JComponent getComponent() {
        return wizardPanel.getPanel();
    }

    @Override
    public void updateDataModel() {
        moduleData.setPath(wizardPanel.getLocation());
        moduleData.setScaffoldNature(wizardPanel.getScaffoldPanel().getScaffoldNature());
        moduleData.setWrapperDelegate(wizardPanel.getScaffoldPanel().getSelectedDelegateWrapperModule());
        moduleData.setWrapperVersion(wizardPanel.getScaffoldPanel().getSelectedJekaVersion());
    }
}
