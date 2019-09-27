package dev.jeka.ide.intellij.platform;

import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class JekaConfigurationFactory extends ConfigurationFactory {


    protected JekaConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration("toto", project, this.getType());
        applicationConfiguration.setWorkingDirectory("$MODULE_WORKING_DIR$");
        applicationConfiguration.setGeneratedName();
        applicationConfiguration.set
        return applicationConfiguration;
    }
}
