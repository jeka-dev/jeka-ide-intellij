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
import com.intellij.psi.PsiJavaFile;
import dev.jeka.ide.intellij.common.ClassUtils;
import dev.jeka.ide.intellij.common.Constants;
import dev.jeka.ide.intellij.common.FileUtils;
import dev.jeka.ide.intellij.common.ModuleUtils;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;

;

/**
 * @author Jerome Angibaud
 */
public class SyncImlAction extends AnAction {

    public static final SyncImlAction INSTANCE = new SyncImlAction();

    private SyncImlAction() {
        super("Jeka Synchronize iml File", "Jeka Synchronize iml file", AllIcons.Actions.Refresh);
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
            moduleDir = ModuleUtils.getModuleDir(existingModule);
        } else if (isJekaProperties(selectedFile)) {
            moduleDir = selectedFile.getParent().getParent().getParent();
            existingModule = ModuleUtils.getModuleHavingRootDir(event.getProject(), moduleDir);
        } else {
            moduleDir = selectedFile;
            existingModule = ModuleUtils.getModuleHavingRootDir(event.getProject(), moduleDir);
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
        PsiClass commandClass = getPsicommandClass(event);
        if (commandClass != null) {
            Module module = ModuleUtil.findModuleForFile(selectedFile, event.getProject());
            if (module == null) {
                event.getPresentation().setVisible(false);
                return;
            }
            final String text = "Jeka Synchronize '" + module.getName() + "' Module";
            event.getPresentation().setText(text);
            if ("EditorPopup".equals(event.getPlace())) {
                event.getPresentation().setIcon(Constants.JkIcons.JEKA_GROUP_ACTION);
            }
            return;
        }
        VirtualFile dir = selectedFile;
        if (isJekaProperties(selectedFile)) {
            dir = selectedFile.getParent().getParent().getParent();
           // event.getPresentation().setIcon(JkIcons.JEKA_GROUP_ACTION);
        } else if (!isJekaProject(dir)) {
            event.getPresentation().setVisible(false);
            return;
        }
        if (ModuleUtils.isPotentialModule(dir)) {
            String prefix = ModuleUtils.isExistingModuleRoot(event.getProject(), dir) ? "" : "Add and ";
            event.getPresentation().setText("Jeka " + prefix + "Synchronize '" + dir.getName() + "' Module");
        } else {
            event.getPresentation().setText("Jeka Create Module '" + dir.getName() + "'");
        }
    }

    private static boolean isJekaProperties(VirtualFile virtualFile) {
        if (virtualFile.isDirectory()) {
            return false;
        }
        return virtualFile.getName().equals("jeka.properties");
    }

    private static boolean isJekaProject(VirtualFile virtualFile) {
        return virtualFile.isDirectory() && FileUtils.containsJekaDir(virtualFile);
    }

    private static PsiClass getPsicommandClass(AnActionEvent event) {
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
            boolean isCommandsClass = ClassUtils.isExtendingJkCommandSet(psiClass);
            if (isCommandsClass) {
                return psiClass;
            }
        }
        return null;
    }

}
