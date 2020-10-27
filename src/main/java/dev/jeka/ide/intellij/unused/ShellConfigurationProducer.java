package dev.jeka.ide.intellij.unused;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class ShellConfigurationProducer extends RunConfigurationProducer<ExternalSystemRunConfiguration> {

    public ShellConfigurationProducer() {
        super(true);
    }


    @Override
    protected boolean setupConfigurationFromContext(@NotNull ExternalSystemRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
       ExternalSystemTaskExecutionSettings settings = configuration.getSettings();
       return true;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull ExternalSystemRunConfiguration configuration,
                                              @NotNull ConfigurationContext context) {
        return false;
    }


}
