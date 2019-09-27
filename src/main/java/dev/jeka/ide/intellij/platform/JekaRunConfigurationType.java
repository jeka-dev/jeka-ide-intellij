package dev.jeka.ide.intellij.platform;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;

import static dev.jeka.ide.intellij.platform.JkIcons.JEKA_GROUP_ACTION;

class JekaRunConfigurationType extends ConfigurationTypeBase {

    JekaRunConfigurationType() {
        super(JekaRunConfigurationType.class.getName(), "Jeka", "Jeka Build", JEKA_GROUP_ACTION);
        addFactory(new JekaConfigurationFactory(this));
    }


}
