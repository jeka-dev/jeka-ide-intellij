package dev.jeka.ide.intellij.extension.runconfiguration;

import com.intellij.execution.application.JvmMainMethodRunConfigurationOptions;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class JekaRunConfigurationFactory extends ConfigurationFactory {

    static final JekaRunConfigurationFactory INSTANCE = new JekaRunConfigurationFactory(JekaRunConfigurationType.INSTANCE);

    private JekaRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return JekaRunConfigurationType.ID;
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new JekaRunConfiguration("", project, this);
    }

    @Override
    public @Nullable Class<? extends BaseState> getOptionsClass() {
        return JvmMainMethodRunConfigurationOptions.class;
    }

}
