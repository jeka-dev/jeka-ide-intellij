package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.xmlb.XmlSerializerUtil;
import dev.jeka.ide.intellij.panel.AppSettingsComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class JekaApplicationSettingsConfigurable implements Configurable {

    private AppSettingsComponent appSettingsComponent;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "JeKa";
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return appSettingsComponent.getPreferredFocusedComponent();
    }

    @Override
    public @Nullable JComponent createComponent() {
        appSettingsComponent = new AppSettingsComponent();
        return appSettingsComponent.getMainPanel();
    }

    @Override
    public boolean isModified() {
        State settings = State.getInstance();
        boolean modified = !Objects.equals(appSettingsComponent.getDistributionPathText().getText(),
                settings.distributionDirPath);
        return modified;
    }

    @Override
    public void apply() {
        State settings = State.getInstance();
        settings.distributionDirPath = appSettingsComponent.getDistributionPathText().getText();
    }

    @Override
    public void reset() {
        State settings = State.getInstance();
        appSettingsComponent.getDistributionPathText().setText(settings.distributionDirPath);
    }

    @Override
    public void disposeUIResources() {
        appSettingsComponent = null;
    }

    @com.intellij.openapi.components.State(
            name = "dev.jeka.ide.intellij.extension.PersistentStateComponent.State",
            storages = @Storage("SdkSettingsPlugin.xml")
    )
    public static class State implements PersistentStateComponent<State> {

        public String distributionDirPath;

        public static State getInstance() {
            return ApplicationManager.getApplication().getService(State.class);
        }

        @Override
        public @Nullable State getState() {
            return this;
        }

        @Override
        public void loadState(@NotNull State state) {
            XmlSerializerUtil.copyBean(state, this);
        }

    }

}
