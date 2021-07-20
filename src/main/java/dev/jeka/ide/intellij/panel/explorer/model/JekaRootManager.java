package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import dev.jeka.ide.intellij.common.ModuleHelper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.psi.PsiAdapter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class JekaRootManager implements Disposable {

    @Getter
    private final Project project;

    @Getter
    private volatile boolean initialised = false;

    @Getter
    private final List<JekaFolderNode> jekaFolders = new LinkedList<>();

    private final List<Runnable> changeListeners = new LinkedList<>();

    private final PsiAdapter psiAdapter;

    private boolean listeningPsi;

    private EventFilter eventFilter;

    public JekaRootManager(Project project) {
        this.project = project;
        this.psiAdapter = new PsiAdapter();
        this.eventFilter = new EventFilter(project);
        PsiManager.getInstance(project).addPsiTreeChangeListener(psiAdapter);
        listeningPsi = true;
    }

    public void listenPsi(boolean listen) {
        if (!listeningPsi && listen) {
            PsiManager.getInstance(project).addPsiTreeChangeListener(psiAdapter);
            listeningPsi = true;
            return;
        }
        if (!listen && listeningPsi) {
            PsiManager.getInstance(project).removePsiTreeChangeListener(psiAdapter);
            listeningPsi = false;
        }
    }

    public void addChangeListener(Runnable runnable) {
        changeListeners.add(runnable);
    }

    public void removeChangeListener(Runnable runnable) {
        changeListeners.remove(runnable);
    }

    public void notifyChange() {
        System.out.println("A change has been notified !!!!!!!!!!!!!!!!!!!!!!!");
        changeListeners.forEach(Runnable::run);
    }

    private void notifyChange(PsiTreeChangeEvent event) {
        System.out.println("A change has been notified !!!!!!!!!!!!!!!!!!!!!!!");
        changeListeners.forEach(Runnable::run);
    }

    public void refreshModule(JekaFolderNode jekaFolder) {
        _refreshModule(jekaFolder);
        notifyChange(null);
    }

    private void _refreshModule(JekaFolderNode jekaFolder) {
        jekaFoldersRecursive()
                .filter(folder -> folder.equals(jekaFolder))
                .forEach(folder -> folder.getJekaModuleContainer().refresh());
    }

    public void refreshAllModules() {
        init();
    }

    public void init() {
        ProgressManager progressManager = ProgressManager.getInstance();
        progressManager.run(initTask());
    }

    private Stream<JekaFolderNode> jekaFoldersRecursive() {
        return jekaFolders.stream()
                .flatMap(jekaFolderNode -> jekaFolderNode.recursiveFolders().stream());
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

    private List<JekaFolderNode> jekaFolderTree() {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        TreeMap<String, Module>  sortedModulesMap = new TreeMap<>();
        for (Module module : moduleManager.getModules()) {
            VirtualFile moduleVirtualFile = ModuleHelper.getModuleDir(module);
            if (moduleVirtualFile == null) {
                continue;
            }
            sortedModulesMap.put(ModuleHelper.getModuleDir(module).getPath(), module);
        }
        Path projectRoot = Paths.get(project.getBasePath());
        JekaFolderNode root = JekaFolderNode.ofSimpleDir(null, projectRoot);
        for(Map.Entry<String, Module> moduleEntry : sortedModulesMap.entrySet()) {
            Module module = moduleEntry.getValue();
            root.createJekaFolderAsDescendant(module);
        }
        return Collections.singletonList(root);
    }

    @Override
    public void dispose() {
        System.out.println("Jeka Root Manager has been disposed -----------------");
        this.changeListeners.clear();
        PsiManager.getInstance(project).removePsiTreeChangeListener(psiAdapter);
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

    Stream<JekaCommandClassNode> allClasses() {
        return jekaFolders.stream()
                .flatMap(folder -> folder.moduleStream())
                .flatMap(jekaModule -> jekaModule.getCommandClasses().stream());

    }


}
