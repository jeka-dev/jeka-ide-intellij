package dev.jeka.ide.intellij.runconfigations;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class JekaRunConfigurationProducer extends RunConfigurationProducer<ApplicationConfiguration> {


    public JekaRunConfigurationProducer(boolean internalUsageOnly) {
        super(internalUsageOnly);
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull ApplicationConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        if (sourceElement.isNull()) {
            return false;
        }
        configuration.setWorkingDirectory("$MODULE_WORKING_DIR$");
        configuration.setModule(context.getModule());
        configuration.setMainClassName("dev.jeka.core.tool.Main");
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull ApplicationConfiguration configuration,
                                              @NotNull ConfigurationContext context) {
        return false;
    }
}
