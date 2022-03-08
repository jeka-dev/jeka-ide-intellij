package dev.jeka.ide.intellij.extension.projectwizard;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.InvalidDataException;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class JekaModuleBuilder2 extends JavaModuleBuilder {

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
        return "Jeka2";
    }

    @Override
    public boolean canCreateModule() {
        return true;
    }

    @Override
    public String getParentGroup() {
        return JavaModuleType.JAVA_GROUP;
    }

    @Override
    public ModuleWizardStep modifyProjectTypeStep(@NotNull SettingsStep settingsStep) {
        JekaWizardPanel panel = new JekaWizardPanel(settingsStep.getContext());
        settingsStep.addSettingsComponent(panel.getPanel());
        return super.modifyProjectTypeStep(settingsStep);
    }

    @Override
    public @Nullable @NonNls String getBuilderId() {
        return this.getClass().getName();
    }

    @Override  // 1
    public @Nullable Module commitModule(@NotNull Project project, @Nullable ModifiableModuleModel model) {
        return super.commitModule(project, model);
    }

    @Override  // 1.1  here the iml file is deleted
    public @NotNull Module createModule(@NotNull ModifiableModuleModel moduleModel) throws InvalidDataException, IOException, ModuleWithNameAlreadyExists, JDOMException, ConfigurationException {
        return super.createModule(moduleModel);
    }

    @Override // 1.1.1  this is call after iml file has been deleted
    protected void setupModule(Module module) throws ConfigurationException {
        super.setupModule(module);
    }

    @Override  // 1.1.1.1
    public void setupRootModel(@NotNull ModifiableRootModel rootModel) throws ConfigurationException {
        super.setupRootModel(rootModel);
    }
}
