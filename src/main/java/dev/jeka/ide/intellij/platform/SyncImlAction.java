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

package dev.jeka.ide.intellij.platform;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import dev.jeka.ide.intellij.JekaDoer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jerome Angibaud
 */
public class SyncImlAction extends AnAction {

    public static final SyncImlAction INSTANCE = new SyncImlAction();

    private static final String JKCOMMANDS_NAME = "dev.jeka.core.tool.JkCommands";

    private SyncImlAction() {
        super("Synchronize", "Synchronize iml file", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        ModuleClass moduleClass = ModuleClass.of(event);
        String className = moduleClass.psiClass == null ? null : moduleClass.psiClass.getQualifiedName();
        VirtualFile virtualRoot = ModuleRootManager.getInstance(moduleClass.module).getContentRoots()[0];
        Path path = Paths.get(virtualRoot.getPath());
        JekaDoer jekaDoer = JekaDoer.getInstance();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            jekaDoer.generateIml(moduleClass.module.getProject(), path, className);
        });
        virtualRoot.getFileSystem().refresh(true);
    }

    @Override
    public void update(AnActionEvent event) {
        ModuleClass moduleClass = ModuleClass.of(event);
        if (moduleClass.psiClass != null) {
            final String text = "Synchronize '" + moduleClass.module.getName() + "' module";
            event.getPresentation().setText(text);
            if (event.getPlace().equals("EditorPopup")) {
                event.getPresentation().setIcon(JkIcons.JEKA_GROUP_ACTION);
            }
        } else if (moduleClass.module != null) {
            event.getPresentation().setText("Synchronize '" + moduleClass.module.getName() + "' module");
        } else {
            event.getPresentation().setVisible(false);
        }
    }

    private static boolean isExtendingJkCommands(PsiClass psiClass) {
        if (psiClass.getQualifiedName().equals(JKCOMMANDS_NAME)) {
            return true;
        }
        PsiClassType[] psiClassTypes = psiClass.getExtendsListTypes();
        for (PsiClassType psiClassType : psiClassTypes) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiClassType;
            PsiClass currentPsiClass = psiClassReferenceType.resolve();
            if (isExtendingJkCommands(currentPsiClass)) {
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
                PsiClass psiClass = psiJavaFile.getClasses()[0];
                boolean isCommandsClass = isExtendingJkCommands(psiClass);
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

}





