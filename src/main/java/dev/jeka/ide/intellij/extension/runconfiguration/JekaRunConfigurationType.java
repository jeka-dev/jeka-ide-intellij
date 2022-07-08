package dev.jeka.ide.intellij.extension.runconfiguration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import icons.JekaIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JekaRunConfigurationType implements ConfigurationType {

    static final String ID = JekaRunConfigurationType.class.getName();

    public static JekaRunConfigurationType INSTANCE = new JekaRunConfigurationType();

    @NotNull
    @Override
    public String getDisplayName() {
        return "Jeka";
    }

    @Nls
    @Override
    public String getConfigurationTypeDescription() {
        return "Jeka Run Configuration";
    }

    @Override
    public Icon getIcon() {
        return JekaIcons.JEKA_GROUP_ACTION;
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{JekaRunConfigurationFactory.INSTANCE};
    }
}
