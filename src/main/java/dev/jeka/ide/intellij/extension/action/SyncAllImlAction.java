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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.ide.intellij.common.FileHelper;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;

import java.nio.file.Path;


/**
 * @author Jerome Angibaud
 */
public class SyncAllImlAction extends AnAction {

    public static final SyncAllImlAction INSTANCE = new SyncAllImlAction();

    private SyncAllImlAction() {
        super("Synchronize all Jeka Modules", "Synchronize all iml files in aka modules", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        Module[] modules = ModuleManager.getInstance(project).getSortedModules();
        Runnable nextStep = null;
        CmdJekaDoer jekaDoer = CmdJekaDoer.getInstance(project);
        for (int i = modules.length-1; i >=0; i--) {
            Module module = modules[i];
            VirtualFile moduleDir = ModuleHelper.getModuleDir(module);
            if (!FileHelper.containsJekaDir(moduleDir)) {
                continue;
            }
            final Runnable next = nextStep;
            final boolean clear = i == 0;
            Path modulePath = moduleDir.toNioPath();
            Runnable step = () -> jekaDoer.generateIml(modulePath, null, clear, module, next);
            nextStep = step;
        }
        final Runnable runnable = nextStep;
        ApplicationManager.getApplication().invokeAndWait(() -> {
            if (runnable != null) {
                runnable.run();
            }
        });
    }

}
