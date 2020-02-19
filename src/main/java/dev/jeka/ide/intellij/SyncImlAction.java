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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
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
        VirtualFile moduleDir = moduleClass.module != null ?
                ModuleRootManager.getInstance(moduleClass.module).getContentRoots()[0]
                : event.getData(CommonDataKeys.VIRTUAL_FILE);
        Path path = Paths.get(moduleDir.getPath());
        CmdJekaDoer jekaDoer = CmdJekaDoer.INSTANCE;
        Project project = event.getProject();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            FileDocumentManager.getInstance().saveAllDocuments();
            jekaDoer.generateIml(project, moduleDir, className, true, moduleClass.module);
        });
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
            if (psiFile == null) {
                return new ModuleClass(module, null);
            }
            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                if (psiJavaFile.getClasses().length == 0) {
                    return new ModuleClass(module, null);
                }
                PsiClass psiClass = psiJavaFile.getClasses()[0];
                boolean isCommandsClass = Utils.isExtendingJkCommands(psiClass);
                if (isCommandsClass) {
                    return new ModuleClass(module, psiClass);
                }
            }
            if (module != null && getModuleRootDir(module).equals(virtualFile)) {
                return new ModuleClass(module, null);
            }
            return new ModuleClass(module, null);
        }
    }

    private static VirtualFile getModuleRootDir(Module module) {
        VirtualFile imlParent = module.getModuleFile().getParent();
        if (imlParent.getName().equals(".idea")) {
            return imlParent.getParent();
        }
        return imlParent;
    }

}
