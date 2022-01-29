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

package dev.jeka.ide.intellij.action;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import dev.jeka.ide.intellij.common.FileHelper;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;

/**
 * @author Jerome Angibaud
 */
public class SyncImlAction extends AnAction {

    public static final String ID = "JEKA_SYNC_IML";

    public static AnAction get() {
        return ActionManager.getInstance().getAction(ID);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        ////ModuleClass moduleClass = ModuleClass.of(event);
        VirtualFile selectedFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (selectedFile.getName().equals("jeka")) {
            selectedFile = selectedFile.getParent();
        }
        PsiClass beanClass = getPsiJkBeanClass(event);
        String className = beanClass == null ? null : beanClass.getQualifiedName();
        if (className != null) {
            VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            FileDocumentManager.getInstance().saveDocument(document);
        }
        final Module existingModule;
        final VirtualFile moduleDir;
        if (beanClass != null) {
            existingModule = ModuleUtil.findModuleForFile(selectedFile, event.getProject());
            moduleDir = ModuleHelper.getModuleDir(existingModule);
        } else if (isWrapperProperties(selectedFile)) {
            moduleDir = selectedFile.getParent().getParent().getParent();
            existingModule = ModuleHelper.getModuleHavingRootDir(event.getProject(), moduleDir);
        } else if ("EditorPopup".equals(event.getPlace())) {
            existingModule = ModuleUtil.findModuleForFile(selectedFile, event.getProject());
            moduleDir = ModuleHelper.getModuleDir(existingModule);
        } else {
            moduleDir = selectedFile;
            existingModule = ModuleHelper.getModuleHavingRootDir(event.getProject(), moduleDir);
        }
        CmdJekaDoer jekaDoer = CmdJekaDoer.INSTANCE;
        Project project = event.getProject();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            FileDocumentManager.getInstance().saveAllDocuments();
            jekaDoer.generateIml(project, moduleDir, className, true, existingModule, null);
        });
    }

    @Override
    public void update(AnActionEvent event) {
        VirtualFile selectedFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (selectedFile == null) {
            return;
        }
        if ("EditorPopup".equals(event.getPlace())) {
            Module module = ModuleUtil.findModuleForFile(selectedFile, event.getProject());
            event.getPresentation().setVisible(module != null && FileHelper.isProjectJekaFile(module, selectedFile));
            return;
        }

        if (selectedFile.getName().equals("jeka")) {
            selectedFile = selectedFile.getParent();
        } else if ("ProjectViewPopup".equals(event.getPlace())) {
            event.getPresentation().setVisible(false);
            return;
        }
        PsiClass commandClass = getPsiJkBeanClass(event);
        if (commandClass != null) {
            Module module = ModuleUtil.findModuleForFile(selectedFile, event.getProject());
            if (module == null) {
                event.getPresentation().setVisible(false);
                return;
            }
            final String text = "Jeka Synchronize Module";
            event.getPresentation().setText(text);
            return;
        }
        VirtualFile dir = selectedFile;
        if (isWrapperProperties(selectedFile)) {
            dir = selectedFile.getParent().getParent().getParent();
        } else if (!isJekaModuleDir(dir)) {
            event.getPresentation().setVisible(true);
            return;
        }
        if (ModuleHelper.isPotentialModule(dir)) {
            event.getPresentation().setText("Jeka Synchronize Module");
        } else {
            event.getPresentation().setText("Jeka Create Module '" + dir.getName() + "'");
        }
    }

    private static boolean isWrapperProperties(VirtualFile virtualFile) {
        if (virtualFile.isDirectory()) {
            return false;
        }
        return virtualFile.getName().equals("wrapper.properties");
    }

    static boolean isJekaModuleDir(VirtualFile virtualFile) {
        return virtualFile.isDirectory() && FileHelper.containsJekaDir(virtualFile);
    }

    private static PsiClass getPsiJkBeanClass(AnActionEvent event) {
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return null;
        }
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            if (psiJavaFile.getClasses().length == 0) {
                return null;
            }
            PsiClass psiClass = psiJavaFile.getClasses()[0];
            boolean isCommandsClass = PsiClassHelper.isExtendingJkBean(psiClass);
            if (isCommandsClass) {
                return psiClass;
            }
        }
        return null;
    }

}
