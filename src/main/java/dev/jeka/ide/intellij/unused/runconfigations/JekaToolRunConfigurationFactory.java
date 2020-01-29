package dev.jeka.ide.intellij.unused.runconfigations;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class JekaToolRunConfigurationFactory extends ConfigurationFactory {

    protected JekaToolRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new JekaToolRunConfiguration(project, this, "JekaRunConfiguration");
    }

}
