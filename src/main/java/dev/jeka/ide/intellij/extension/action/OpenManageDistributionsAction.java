package dev.jeka.ide.intellij.extension.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import dev.jeka.ide.intellij.extension.JekaApplicationSettingsConfigurable;
import org.jetbrains.annotations.NotNull;

public class OpenManageDistributionsAction extends AnAction {

    public OpenManageDistributionsAction() {
        super("Manage Distributions...");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), JekaApplicationSettingsConfigurable.class);
    }
}
