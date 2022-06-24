package dev.jeka.ide.intellij.extension.runconfiguration;

import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.JvmMainMethodRunConfigurationOptions;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import dev.jeka.core.tool.Main;
import dev.jeka.ide.intellij.engine.ConfigurationRunner;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class JekaRunConfiguration extends ApplicationConfiguration {

    protected JekaRunConfiguration(String name, @NotNull Project project, @NotNull ConfigurationFactory factory) {
        super(name, project, factory);
        ConfigurationRunner.initConfiguration(this);
        if (getModules().length == 0  && getDefaultModule() == null && !getAllModules().isEmpty()) {
            this.setModule(this.getAllModules().iterator().next());
        }
        this.setName("Unnamed");
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

}
