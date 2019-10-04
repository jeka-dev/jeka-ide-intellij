package dev.jeka.ide.intellij.platform;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class JekaRunConfigurationFactory extends ConfigurationFactory {

    protected JekaRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        JekaRunConfiguration jekaRunConfiguration = new JekaRunConfiguration("Unamed", project);
        jekaRunConfiguration.setWorkingDirectory("$MODULE_WORKING_DIR$");
        return jekaRunConfiguration;
    }

}
