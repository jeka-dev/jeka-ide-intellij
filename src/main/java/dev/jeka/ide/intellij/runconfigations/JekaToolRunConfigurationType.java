package dev.jeka.ide.intellij.runconfigations;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import dev.jeka.ide.intellij.platform.JkIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JekaToolRunConfigurationType implements ConfigurationType {
    @NotNull
    @Override
    public String getDisplayName() {
        return "Jeka";
    }

    @Nls
    @Override
    public String getConfigurationTypeDescription() {
        return "Jeka Tool Run Configuration";
    }

    @Override
    public Icon getIcon() {
        return JkIcons.JEKA_GROUP_ACTION;
    }

    @NotNull
    @Override
    public String getId() {
        return "JEKA_RUN_CONFIGURATION";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new JekaToolRunConfigurationFactory(this)};
    }
}
