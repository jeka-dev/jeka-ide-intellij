package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JekaApplicationSettingsConfigurable implements Configurable {

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Jeka";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
