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

package dev.jeka.ide.intellij.extension.action;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.panel.ScaffoldDialogWrapper;

/**
 * @author Jerome Angibaud
 */
public class ScaffoldAction extends AnAction {

    public static final String ID = "JEKA_SCAFFOLD";

    public static AnAction get() {
        return ActionManager.getInstance().getAction(ID);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        VirtualFile selectedDir = event.getData(CommonDataKeys.VIRTUAL_FILE);
        Project project = event.getProject();
        Module module = ModuleHelper.getModuleHavingRootDir(project, selectedDir);
        ScaffoldDialogWrapper dialogWrapper = new ScaffoldDialogWrapper(project, selectedDir, module);
        dialogWrapper.show();
    }

    @Override
    public void update(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean isModuleRoot = virtualFile != null &&
                ModuleHelper.getModuleHavingRootDir(event.getProject(), virtualFile)  != null;
        event.getPresentation().setVisible(isModuleRoot);
    }

}

