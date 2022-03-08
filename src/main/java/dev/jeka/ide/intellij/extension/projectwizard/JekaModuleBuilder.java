package dev.jeka.ide.intellij.extension.projectwizard;

import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsContexts;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.api.utils.JkUtilsPath;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.engine.CmdJekaDoer2;
import dev.jeka.ide.intellij.engine.ScaffoldNature;
import icons.JekaIcons;
import lombok.Data;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/// https://github.com/redline-smalltalk/intellij-redline-plugin/blob/master/src/st/redline/smalltalk/module/RsModuleBuilder.java
public class JekaModuleBuilder extends ModuleBuilder {

    final ModuleData moduleData = new ModuleData();

    @Override
    public @Nullable ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        JekaWizardStep jekaWizardStep = JekaWizardStep.of(context, this);
        Disposer.register(parentDisposable, jekaWizardStep);
        return jekaWizardStep;
    }

    @Override
    public @NotNull List<Class<? extends ModuleWizardStep>> getIgnoredSteps() {
       // return super.getIgnoredSteps();
       return JkUtilsIterable.listOf(ProjectSettingsStep.class);
    }

    @Override
    public ModuleType<?> getModuleType() {
        return JavaModuleType.getModuleType();
    }

    @Override
    public @NlsContexts.DetailedDescription String getDescription() {
        return "Module built with Jeka";
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
        return JavaModuleType.JAVA_GROUP;
    }

    @Override
    public Icon getNodeIcon() {
        return JekaIcons.JEKA_GROUP_ACTION;
    }

    @Override
    public String getName() {
        return "Jeka";
    }

    @Override
    public int getWeight() {
        return 2;
    }

    @Override
    public @Nullable @NonNls String getBuilderId() {
        return this.getClass().getName();
    }

    @Override
    public Module createModule(ModifiableModuleModel model) throws ConfigurationException, IOException {
        Path wrapperDelegateModulePath = moduleData.getWrapperDelegate() == null ? null
                : ModuleHelper.getModuleDir(moduleData.getWrapperDelegate()).toNioPath();
        Path moduleDir = Paths.get(moduleData.moduleDir);
        JkUtilsPath.createDirectories(moduleDir);
        CmdJekaDoer2.INSTANCE.scaffoldModule(
                model.getProject(),
                moduleDir,
                true,
                moduleData.getWrapperDelegate() != null || moduleData.getWrapperVersion() != null,
                wrapperDelegateModulePath,
                moduleData.getWrapperVersion(),
                null,
                moduleData.getScaffoldNature(),
                false);
        Path imlFile = JkExternalToolApi.getImlFile(moduleDir);
        Module module = model.loadModule(imlFile);
        setupModule(module);
        return module;
    }

    @Data
    static
    class ModuleData {

        private boolean valid = true;

        private String moduleDir;

        private ScaffoldNature scaffoldNature;

        private Module wrapperDelegate;

        private String wrapperVersion;
    }

}
