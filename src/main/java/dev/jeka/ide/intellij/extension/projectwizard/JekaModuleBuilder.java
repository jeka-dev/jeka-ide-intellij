package dev.jeka.ide.intellij.extension.projectwizard;

import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.FileHelper;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;
import dev.jeka.ide.intellij.engine.ScaffoldNature;
import icons.JekaIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/// https://github.com/redline-smalltalk/intellij-redline-plugin/blob/master/src/st/redline/smalltalk/module/RsModuleBuilder.java
public class JekaModuleBuilder extends JavaModuleBuilder {

    private JekaModuleData moduleData = new JekaModuleData();

    @Override
    public @Nullable ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return JekaWizardStep.of(context, moduleData);
    }

    @Override
    public ModuleWizardStep modifyProjectTypeStep(@NotNull SettingsStep settingsStep) {
        ModuleWizardStep moduleWizardStep = super.modifyProjectTypeStep(settingsStep);
        return  moduleWizardStep;
    }

    @Override
    public @NotNull List<Class<? extends ModuleWizardStep>> getIgnoredSteps() {
        return JkUtilsIterable.listOf(ProjectSettingsStep.class);
    }

    @Override
    public ModuleType<?> getModuleType() {
        return StdModuleTypes.JAVA;
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
    public @Nullable Module commitModule(@NotNull Project project, @Nullable ModifiableModuleModel model) {
        Path wrapperDelegateModulePath = moduleData.getWrapperDelegate() == null ? null
                : ModuleHelper.getModuleDir(moduleData.getWrapperDelegate()).toNioPath();
        CmdJekaDoer.INSTANCE.scaffoldModule(
                project,
                Paths.get(moduleData.getPath()),
                true,
                moduleData.getWrapperDelegate() != null || moduleData.getWrapperVersion() != null,
                wrapperDelegateModulePath,
                moduleData.getWrapperVersion(),
                null,
                moduleData.getScaffoldNature(),
                false);
        try {
            Module module = model.loadModule(JkExternalToolApi.getImlFile(Paths.get(moduleData.getPath())));
            super.commitModule(project, model);
            return module;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

}
