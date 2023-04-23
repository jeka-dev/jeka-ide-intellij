package dev.jeka.ide.intellij.extension.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.wm.ToolWindowManager;
import dev.jeka.ide.intellij.extension.JekaApplicationSettingsConfigurable;
import dev.jeka.ide.intellij.extension.JekaConsoleToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class OpenJekaConsoleAction extends AnAction {

    public OpenJekaConsoleAction() {
        super("Open Jeka Console");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ToolWindowManager.getInstance(e.getProject()).getToolWindow(JekaConsoleToolWindowFactory.ID).show(null);
    }
}
