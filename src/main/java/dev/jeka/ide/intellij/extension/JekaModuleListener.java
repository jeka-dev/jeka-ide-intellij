package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.Function;
import dev.jeka.ide.intellij.panel.explorer.model.JekaRootManager;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JekaModuleListener implements ModuleListener {

    @SneakyThrows
    @Override
    public void moduleAdded(@NotNull Project project, @NotNull Module module) {
        JekaRootManager jekaRootManager = project.getService(JekaRootManager.class);
        if (!jekaRootManager.isInitialised()) {
            return;
        }
        jekaRootManager.addModuleAndNotify(module);
    }

    @Override
    public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
        notifyChange(project);
    }

    @Override
    public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
        notifyChange(project);
    }

    private void notifyChange(Project project) {
        System.out.println("---------------------------------------- modules change");
        JekaRootManager jekaRootManager = project.getService(JekaRootManager.class);
        if (!jekaRootManager.isInitialised()) {
            return;
        }
        if (jekaRootManager != null) {
            DumbService.getInstance(project).smartInvokeLater(jekaRootManager::init);
        }
    }
}
