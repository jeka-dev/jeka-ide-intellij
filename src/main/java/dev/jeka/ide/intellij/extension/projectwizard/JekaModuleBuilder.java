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
import dev.jeka.core.api.file.JkPathFile;
import dev.jeka.core.api.utils.JkUtilsIO;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.api.utils.JkUtilsPath;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;
import icons.JekaIcons;
import lombok.Data;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/// https://github.com/redline-smalltalk/intellij-redline-plugin/blob/master/src/st/redline/smalltalk/module/RsModuleBuilder.java
public class JekaModuleBuilder extends ModuleBuilder {

    final ModuleData moduleData = new ModuleData();

    JekaModuleBuilder() {
        this.addListener(this::moduleCreated);
    }

    @Override
    public @Nullable ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        JekaWizardStep jekaWizardStep = JekaWizardStep.of(context, this);
        Disposer.register(parentDisposable, jekaWizardStep);
        return jekaWizardStep;
    }

    @Override
    public @NotNull List<Class<? extends ModuleWizardStep>> getIgnoredSteps() {
       return JkUtilsIterable.listOf(ProjectSettingsStep.class);
    }

    @Override
    public ModuleType<?> getModuleType() {
        return null;
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
        Path moduleDir = Paths.get(moduleData.moduleDir);
        Path imlFile = JkExternalToolApi.getImlFile(moduleDir);

        // Create a naked Jeka module. def dir is to make the module recognized as 'Jeka' module by ToolWindox
        JkUtilsPath.createDirectories(moduleDir.resolve(JkConstants.DEF_DIR));
        String imlContent = JkUtilsIO.read(JekaModuleBuilder.class.getResource("naked-java.iml"));
        JkPathFile.of(imlFile).createIfNotExist().write(imlContent.getBytes(StandardCharsets.UTF_8));

        Module module = model.loadModule(imlFile);
        setupModule(module);
        return module;
    }

    private void moduleCreated(@NotNull Module module) {
        Path wrapperDelegateModulePath = moduleData.getWrapperDelegate() == null ? null
                : ModuleHelper.getModuleDir(moduleData.getWrapperDelegate()).toNioPath();
        Path moduleDir = Paths.get(moduleData.moduleDir);
        JkUtilsPath.createDirectories(moduleDir);
        CmdJekaDoer.getInstance(module.getProject()).scaffoldModule(
                moduleDir,
                true,
                moduleData.getWrapperDelegate() != null || moduleData.getWrapperVersion() != null,
                wrapperDelegateModulePath,
                moduleData.getWrapperVersion(),
                null,
                moduleData.getTemplateCmd()
        );
    }

    @Data
    static
    class ModuleData {

        private boolean valid = true;

        private String moduleDir;

        private String templateCmd;

        private Module wrapperDelegate;

        private String wrapperVersion;
    }

}
