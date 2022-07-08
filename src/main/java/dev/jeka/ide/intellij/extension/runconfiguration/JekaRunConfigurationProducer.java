package dev.jeka.ide.intellij.extension.runconfiguration;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class JekaRunConfigurationProducer extends LazyRunConfigurationProducer<JekaRunConfiguration> {

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return JekaRunConfigurationFactory.INSTANCE;
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull JekaRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull JekaRunConfiguration configuration, @NotNull ConfigurationContext context) {
        return false;
    }
}
