package dev.jeka.ide.intellij.unused.newmodule;

import com.intellij.ide.projectWizard.NewProjectWizard;
import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import dev.jeka.ide.intellij.JkIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/// https://github.com/redline-smalltalk/intellij-redline-plugin/blob/master/src/st/redline/smalltalk/module/RsModuleBuilder.java
public class JekaModuleBuilder extends EmptyModuleBuilder {

    NewModuleData data = new NewModuleData();

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        JkWizardStep step = JkWizardStep.createMainStep(wizardContext, data);
        return new ModuleWizardStep[] {step};
    }

    @Nullable
    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
        int stepIndex = settingsStep.getContext().getWizard().getCurrentStep();
        System.out.println("** ----------------------------------" + stepIndex);
        System.out.println("** ++++++++++" + settingsStep.getContext().getWizard().getStepCount());
        return super.modifySettingsStep(settingsStep);
    }

    @Nullable
    @Override
    public ModuleWizardStep modifyProjectTypeStep(@NotNull SettingsStep settingsStep) {
        NewProjectWizard wizard = (NewProjectWizard) settingsStep.getContext().getWizard();
        System.out.println("AA ----------------------------------" + wizard.getSequence());
        System.out.println("AA ++++++++++" + settingsStep.getContext().getWizard().getStepCount());
        return super.modifyProjectTypeStep(settingsStep);
    }

    @Override
    public boolean canCreateModule() {
        return true;
    }

    @Override
    public String getPresentableName() {
        return "Jeka";
    }

    @Override
    public String getParentGroup() {
        return JavaModuleType.BUILD_TOOLS_GROUP;
    }

    @Override
    public Icon getNodeIcon() {
        return JkIcons.JEKA_GROUP_ACTION;
    }

    @Override
    public String getName() {
        return "Jeka";
    }

    @Override
    public int getWeight() {
        return JavaModuleBuilder.BUILD_SYSTEM_WEIGHT - 1;
    }

}
