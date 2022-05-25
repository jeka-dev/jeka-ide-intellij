package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.Function;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JekaModuleListener implements ModuleListener {

    @SneakyThrows
    @Override
    public void moduleAdded(@NotNull Project project, @NotNull Module module) {
        notifyChange(project);
        ToolWindowManager.getInstance(project).invokeLater(() ->
                JekaToolWindows.registerIfNeeded(project, true)
        );
    }

    @Override
    public void modulesRenamed(@NotNull Project project,
                               @NotNull List modules,
                               @NotNull Function oldNameProvider) {
        notifyChange(project);
    }

    @Override
    public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
        notifyChange(project);
    }

    private void notifyChange(Project project) {
        /*
        JekaRootManager jekaRootManager = project.getService(JekaRootManager.class);
        if (!jekaRootManager.isInitialised()) {
            return;
        }
        if (jekaRootManager != null) {
            DumbService.getInstance(project).smartInvokeLater(jekaRootManager::init);
        }
         */
    }

}
