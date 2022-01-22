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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.vfs.VirtualFile;
import icons.JekaIcons;
import dev.jeka.ide.intellij.common.ModuleHelper;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jerome Angibaud
 */
public class ProjectPopupJekaActionGroup extends DefaultActionGroup {

    public ProjectPopupJekaActionGroup() {
        super("Jeka", true);
        this.add(ScaffoldAction.INSTANCE);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setIcon(JekaIcons.JEKA_GROUP_ACTION);
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean isModuleRoot = virtualFile != null &&
                ModuleHelper.getModuleHavingRootDir(event.getProject(), virtualFile)  != null;
        event.getPresentation().setVisible(isModuleRoot);
    }
}
