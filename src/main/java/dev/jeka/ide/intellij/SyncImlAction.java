/*
 * Copyright 2018-2019 original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.jeka.ide.intellij;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jerome Angibaud
 */
public class SyncImlAction extends AnAction {

    public static final SyncImlAction INSTANCE = new SyncImlAction();

    private SyncImlAction() {
        super("Synchronize", "Synchronize iml file", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        ModuleClass moduleClass = ModuleClass.of(event);
        String className = moduleClass.psiClass == null ? null : moduleClass.psiClass.getQualifiedName();
        if (className != null) {
            VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            FileDocumentManager.getInstance().saveDocument(document);
        }
        VirtualFile virtualRoot = moduleClass.module != null ?
                ModuleRootManager.getInstance(moduleClass.module).getContentRoots()[0]
                : event.getData(CommonDataKeys.VIRTUAL_FILE);
        Path path = Paths.get(virtualRoot.getPath());
        JekaDoer jekaDoer = JekaDoer.getInstance();
        Project project = event.getProject();
        final Object lock = new Object();
            ApplicationManager.getApplication().invokeAndWait(() -> {
                Runnable onSuccess = moduleClass.module != null ?
                        () -> refreshModule(moduleClass.module) :
                        () -> {
                            synchronized (lock) {
                                lock.notify();
                            }
                        };
                jekaDoer.generateIml(project, path, className, onSuccess);
            });
            if (moduleClass.module == null) {
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                    addModule(virtualRoot.getName(), virtualRoot, project);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }


    }

    @Override
    public void update(AnActionEvent event) {
        ModuleClass moduleClass = ModuleClass.of(event);
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (moduleClass.psiClass != null) {
            final String text = "Synchronize '" + moduleClass.module.getName() + "' module";
            event.getPresentation().setText(text);
            if ("EditorPopup".equals(event.getPlace())) {
                event.getPresentation().setIcon(JkIcons.JEKA_GROUP_ACTION);
            }
        } else if (moduleClass.module != null) {
            event.getPresentation().setText("Synchronize '" + moduleClass.module.getName() + "' module");
        } else if (virtualFile.isDirectory() && containsJekaDir(virtualFile.getChildren())) {
            event.getPresentation().setText("Create module '" + virtualFile.getName() + "'");
        }  else {
            event.getPresentation().setVisible(false);
        }
    }

    private static boolean containsJekaDir(VirtualFile[] virtualFiles) {
        for (VirtualFile virtualFile : virtualFiles) {
            if ("jeka".equals(virtualFile.getName()) && virtualFile.isDirectory()) {
                return true;
            }
        }
        return false;
    }



    private static class ModuleClass {
        final Module module;
        final PsiClass psiClass;

        private ModuleClass(Module module, PsiClass psiClass) {
            this.module = module;
            this.psiClass = psiClass;
        }

        static ModuleClass of(AnActionEvent event) {
            VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
            Module module = ModuleUtil.findModuleForFile(virtualFile, event.getProject());
            PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                if (psiJavaFile.getClasses().length == 0) {
                    return new ModuleClass(null, null);
                }
                PsiClass psiClass = psiJavaFile.getClasses()[0];
                boolean isCommandsClass = Utils.isExtendingJkCommands(psiClass);
                if (isCommandsClass) {
                    return new ModuleClass(module, psiClass);
                }
            }
            if (getModuleRootDir(module).equals(virtualFile)) {
                return new ModuleClass(module, null);
            }
            return new ModuleClass(null, null);
        }
    }

    private static VirtualFile getModuleRootDir(Module module) {
        VirtualFile imlParent = module.getModuleFile().getParent();
        if (imlParent.getName().equals(".idea")) {
            return imlParent.getParent();
        }
        return imlParent;
    }

    private static VirtualFile findImlFile(VirtualFile moduleDir) {
        String name = moduleDir.getName() + ".iml";
        VirtualFile candidate = moduleDir.findChild(name);
        if (candidate != null) {
            return candidate;
        }
        VirtualFile ideaDir = moduleDir.findChild(".idea");
        if (ideaDir == null) {
            return null;
        }
        return ideaDir.findChild(name);
    }

    private static void addModule(String moduleName, VirtualFile moduleDir, Project project) {
        moduleDir.getFileSystem().refresh(false);
        VirtualFile imlFile = findImlFile(moduleDir);
        ModifiableModuleModel modifiableModuleModel = ModuleManager.getInstance(project).getModifiableModel();
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                modifiableModuleModel.loadModule(imlFile.getPath());
                modifiableModuleModel.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void refreshModule(Module module) {
        VirtualFile imlFile = module.getModuleFile();
        VfsUtil.markDirtyAndRefresh(false, false, false, imlFile);
    }

}
