package dev.jeka.ide.intellij.extension.runconfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.ModuleBasedConfigurationOptions.ClasspathModification;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import dev.jeka.ide.intellij.common.RunConfigurationHelper;
import dev.jeka.ide.intellij.engine.ConfigurationRunner;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JekaRunConfiguration extends ApplicationConfiguration {

    protected JekaRunConfiguration(String name, @NotNull Project project, @NotNull ConfigurationFactory factory) {
        super(name, project, factory);
        ConfigurationRunner.initConfiguration(this);
        if (getModules().length == 0  && getDefaultModule() == null && !getAllModules().isEmpty()) {
            this.setModule(this.getAllModules().iterator().next());
        }
        this.setName("Unnamed");
    }

    public JekaRunConfiguration(String name, @NotNull Project project) {
        this(name, project, JekaRunConfigurationFactory.INSTANCE);
    }

    @Override
    public @Nullable @NonNls String getId() {
        return JekaRunConfigurationType.ID;
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new JekaRunConfigurationSettingsEditor(this);
    }

    @Override
    public boolean isBuildBeforeLaunchAddedByDefault() {
        return false;
    }


    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        adaptClasspathExclusions();
        JavaCommandLineState state = (JavaCommandLineState) super.getState(executor, env);
        return state;
    }

    private void adaptClasspathExclusions() {
        Module module = this.getConfigurationModule().getModule();
        List<ClasspathModification> currentModifs = this.getClasspathModifications();
        List<ClasspathModification> intellijExclusions =
                RunConfigurationHelper.computeIntellijCompiledClassExclusions(module);
        for (ClasspathModification intellijModif : intellijExclusions) {
            boolean present = currentModifs.stream().anyMatch(currentModif -> modifEquals(currentModif, intellijModif));
            if (!present) {
                currentModifs.add(intellijModif);
            }
        }
    }

    private boolean modifEquals(ClasspathModification modif1, ClasspathModification modif2) {
        return modif1.getExclude() == modif2.getExclude()
                && modif1.getPath().equals(modif2.getPath());
    }

    @Override
    public @Nullable String suggestedName() {
        return null;
    }
}
