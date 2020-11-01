package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import dev.jeka.ide.intellij.common.ModuleHelper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class JekaRootManager {

    private final Project project;

    private final PsiManager psiManager;

    @Getter
    private final List<JekaModule> jekaModules = new LinkedList<>();

    public JekaRootManager(Project project) {
        this.project = project;
        this.psiManager = PsiManager.getInstance(project);
    }

    private Task initTask() {
        return new Task.Backgroundable(project, "Initialising Jeka module...", false) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                indicator.pushState();
                jekaModules.clear();
                jekaModules.addAll(extractJekaModules());
                indicator.stop();
            }
        };
    }

    private List<JekaModule> extractJekaModules() {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        List<JekaModule> result = new LinkedList<>();
        for (Module module : moduleManager.getModules()) {
            if (ModuleHelper.isJekaModule(module)) {
                result.add(jekaModule(module));
            }
        }
        return result;
    }

    private JekaModule jekaModule(Module module) {
        return new JekaModule(module.getName(), null);
    }

}
