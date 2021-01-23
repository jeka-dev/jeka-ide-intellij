package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import dev.jeka.ide.intellij.common.ModuleHelper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class JekaRootManager implements Disposable {

    @Getter
    private final Project project;

    private final PsiAdapter psiAdapter;;

    @Getter
    private volatile boolean initialised = false;

    private EventFilter eventFilter;

    @Getter
    private final List<JekaFolder> jekaFolders = new LinkedList<>();

    private final List<Runnable> changeListeners = new LinkedList<>();

    public JekaRootManager(Project project) {
        this.project = project;
        this.psiAdapter = new PsiAdapter();
        this.eventFilter = new EventFilter(project);
        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiAdapter());
    }

    public void addChangeListener(Runnable runnable) {
        changeListeners.add(runnable);
    }

    public void removeChangeListener(Runnable runnable) {
        changeListeners.remove(runnable);
    }

    private void notifyChange(PsiTreeChangeEvent event) {
        changeListeners.forEach(Runnable::run);
    }

    public void refreshModule(JekaFolder jekaFolder) {
        jekaFolders.stream()
                .filter(folder -> folder.equals(jekaFolder))
                .forEach(folder -> folder.getJekaModule().refresh());
        notifyChange(null);
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
                notifyChange(null);
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

    @Override
    public void dispose() {
        PsiManager.getInstance(project).removePsiTreeChangeListener(this.psiAdapter);
    }


    private class PsiAdapter extends PsiTreeChangeAdapter {

        @Override
        public void childAdded(@NotNull PsiTreeChangeEvent event) {
            JekaRootManager.this.notifyChange(event);
        }

        @Override
        public void childMoved(@NotNull PsiTreeChangeEvent event) {
            JekaRootManager.this.notifyChange(event);
        }

        @Override
        public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
            JekaRootManager.this.notifyChange(event);
        }

        @Override
        public void childRemoved(@NotNull PsiTreeChangeEvent event) {
            Module module = eventFilter.moduleFromChildIsAJekaJavaClassFile(event);
            if (module != null) {
                //refreshModule(module);
            }
            JekaRootManager.this.notifyChange(event);
        }

        // renamed
        @Override
        public void childReplaced(@NotNull PsiTreeChangeEvent event) {
            JekaRootManager.this.notifyChange(event);
        }
    }

    Stream<JekaCommandClass> allClasses() {
        return jekaFolders.stream()
                .flatMap(folder -> folder.moduleStream())
                .flatMap(jekaModule -> jekaModule.getCommandClasses().stream());

    }

}
