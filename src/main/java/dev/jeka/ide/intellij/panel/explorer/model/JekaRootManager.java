package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.*;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.PsiMethodHelper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class JekaRootManager {

    @Getter
    private final Project project;

    private final PsiManager psiManager;

    @Getter
    private volatile boolean initialised = false;

    @Getter
    private final List<JekaFolder> jekaFolders = new LinkedList<>();

    private final List<Runnable> changeListeners = new LinkedList<>();

    public JekaRootManager(Project project) {
        this.project = project;
        this.psiManager = PsiManager.getInstance(project);
        this.psiManager.addPsiTreeChangeListener(new MyPsiAdapter());
    }

    public void addChangeListener(Runnable runnable) {
        changeListeners.add(runnable);
    }

    public void removeChangeListener(Runnable runnable) {
        changeListeners.remove(runnable);
    }

    private void notifyChange() {
        changeListeners.forEach(Runnable::run);
    }

    public void init() {
        ProgressManager progressManager = ProgressManager.getInstance();
        progressManager.run(initTask());
    }

    private Task initTask() {
        return new Task.Modal(project, "Initialising Jeka module...", false) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                indicator.pushState();
                jekaFolders.clear();
                ApplicationManager.getApplication().runReadAction(() -> {
                    jekaFolders.addAll(jekaFolderTree());
                });
                indicator.stop();
                initialised = true;
                notifyChange();
            }
        };
    }

    private List<JekaFolder> jekaFolderTree() {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        TreeMap<String, Module>  sortedModulesMap = new TreeMap<>();
        for (Module module : moduleManager.getModules()) {
            sortedModulesMap.put(ModuleHelper.getModuleDir(module).getPath(), module);
        }
        Path projectRoot = Paths.get(project.getBasePath());
        JekaFolder root = JekaFolder.ofSimpleDir(null, projectRoot);
        for(Map.Entry<String, Module> moduleEntry : sortedModulesMap.entrySet()) {
            Module module = moduleEntry.getValue();
            root.createJekaFolderAsDescendant(module);
        }
        return Collections.singletonList(root);
    }




    private class MyPsiAdapter extends PsiTreeChangeAdapter {

        @Override
        public void childAdded(@NotNull PsiTreeChangeEvent event) {
            super.childAdded(event);
        }

        @Override
        public void childMoved(@NotNull PsiTreeChangeEvent event) {
            super.childMoved(event);
        }

        @Override
        public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
            super.childrenChanged(event);
        }

        @Override
        public void childRemoved(@NotNull PsiTreeChangeEvent event) {
            super.childRemoved(event);
        }

        // renamed
        @Override
        public void childReplaced(@NotNull PsiTreeChangeEvent event) {
            PsiMethod psiMethod = PsiMethodHelper.toJekaCommand(event.getParent());
            if (psiMethod == null) {
                return;
            }
            super.childReplaced(event);
        }
    }

}
