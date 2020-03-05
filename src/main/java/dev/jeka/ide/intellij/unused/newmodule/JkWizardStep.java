package dev.jeka.ide.intellij.unused.newmodule;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JkWizardStep extends ModuleWizardStep {

    private final Project project;

    private final WizardMainPanel wizardMainPanel;

    private final NewModuleData data;

    public JkWizardStep(Project project, WizardMainPanel wizardMainPanel, NewModuleData data) {
        this.project = project;
        this.wizardMainPanel = wizardMainPanel;
        this.data = data;
    }

    static JkWizardStep createMainStep(WizardContext wizardContext, NewModuleData data) {
        Project project = wizardContext.getProject();
        Module[] modules = ModuleManager.getInstance(wizardContext.getProject()).getModules();
        Path projectRoot = Paths.get(project.getBasePath());
        Path contextDir = Paths.get(wizardContext.getProjectFileDirectory());
        String path = projectRoot.getParent().relativize(contextDir).toString();
        WizardMainPanel wizardMainPanel = new WizardMainPanel(modules, path);
        return new JkWizardStep(project, wizardMainPanel, data);
    }

    @Override
    public JComponent getComponent() {
        return wizardMainPanel;
    }

    @Override
    public void updateDataModel() {
        data.path = wizardMainPanel.namePanel.getModuleName();
        data.scaffoldFormPanel = wizardMainPanel.scaffoldFormPanel;
    }
}
