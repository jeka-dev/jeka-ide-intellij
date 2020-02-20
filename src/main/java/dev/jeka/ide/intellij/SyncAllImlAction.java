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
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

;

/**
 * @author Jerome Angibaud
 */
public class SyncAllImlAction extends AnAction {

    public static final SyncAllImlAction INSTANCE = new SyncAllImlAction();

    private SyncAllImlAction() {
        super("Synchronize All", "Synchronize all iml files", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        Module[] modules = ModuleManager.getInstance(project).getSortedModules();
        Runnable nextStep = null;
        CmdJekaDoer jekaDoer = CmdJekaDoer.INSTANCE;
        for (int i = modules.length-1; i >=0; i--) {
            Module module = modules[i];
            VirtualFile moduleDir = Utils.getModuleDir(module);
            if (!Utils.containsJekaDir(moduleDir)) {
                continue;
            }
            final Runnable next = nextStep;
            final boolean clear = i == 0;
            Runnable step = () ->
                    jekaDoer.generateIml(project, moduleDir, null,
                            clear, module, next);
            nextStep = step;
        }
        final Runnable runnable = nextStep;
        ApplicationManager.getApplication().invokeAndWait(() -> {
            runnable.run();
        });
    }


}
