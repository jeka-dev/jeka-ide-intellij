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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;;

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
        ////ModuleClass moduleClass = ModuleClass.of(event);
        VirtualFile selectedFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        PsiClass commandClass = getPsicommandClass(event);
        String className = commandClass == null ? null : commandClass.getQualifiedName();
        if (className != null) {
            VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            FileDocumentManager.getInstance().saveDocument(document);
        }
        final Module existingModule;
        final VirtualFile moduleDir;
        if (commandClass != null) {
            existingModule = ModuleUtil.findModuleForFile(selectedFile, event.getProject());
            moduleDir = Utils.getModuleDir(existingModule);
        } else {
            moduleDir = selectedFile;
            existingModule = Utils.getModuleHavingRootDir(event.getProject(), selectedFile);
        }
        CmdJekaDoer jekaDoer = CmdJekaDoer.INSTANCE;
        Project project = event.getProject();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            FileDocumentManager.getInstance().saveAllDocuments();
            jekaDoer.generateIml(project, moduleDir, className, true, existingModule);
        });
    }

    @Override
    public void update(AnActionEvent event) {
        VirtualFile selectedFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        PsiClass commandClass = getPsicommandClass(event);
        if (commandClass != null) {
            Module module = ModuleUtil.findModuleForFile(selectedFile, event.getProject());
            if (module == null) {
                event.getPresentation().setVisible(false);
                return;
            }
            final String text = "Synchronize '" + module.getName() + "' module";
            event.getPresentation().setText(text);
            if ("EditorPopup".equals(event.getPlace())) {
                event.getPresentation().setIcon(JkIcons.JEKA_GROUP_ACTION);
            }
            return;
        }
        if (!selectedFile.isDirectory() || !containsJekaDir(selectedFile)) {
            event.getPresentation().setVisible(false);
            return;
        }
        if (Utils.isPotentialModule(selectedFile)) {
            String prefix = Utils.isExistingModuleRoot(event.getProject(), selectedFile) ? "" : "Add and ";
            event.getPresentation().setText(prefix + "Synchronize '" + selectedFile.getName() + "' Module");
        } else {
            event.getPresentation().setText("Create Jeka Module '" + selectedFile.getName() + "'");
        }
    }

    private static boolean containsJekaDir(VirtualFile dir) {
        for (VirtualFile virtualFile : dir.getChildren()) {
            if ("jeka".equals(virtualFile.getName()) && virtualFile.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    private static PsiClass getPsicommandClass(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        Module module = ModuleUtil.findModuleForFile(virtualFile, event.getProject());
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
            boolean isCommandsClass = Utils.isExtendingJkCommands(psiClass);
            if (isCommandsClass) {
                return psiClass;
            }
        }
        return null;
    }

}
