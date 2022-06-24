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

package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.panel.explorer.JekaExplorerPanel;
import icons.JekaIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Jerome Angibaud
 */
public class JekaToolWindows {

    static final String ID = "Jeka";

    private static boolean isApplicable(@NotNull Project project) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        return Arrays.stream(moduleManager.getModules())
                .anyMatch(ModuleHelper::isJekaModule);
    }

    public static void registerIfNeeded(Project project, boolean checkIfApplicable) {
        if (ToolWindowManager.getInstance(project).getToolWindow(ID) == null) {
            if (!checkIfApplicable || isApplicable(project)) {
                registerToolWindow(project);
            }
        }
    }

    private static void registerToolWindow(Project project) {
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        RegisterToolWindowTask registerToolWindowTask = RegisterToolWindowTask.notClosable(ID,
                JekaIcons.JEKA_GREY, ToolWindowAnchor.RIGHT);
        ToolWindow toolWindow = manager.registerToolWindow(registerToolWindowTask);
        JekaExplorerPanel panel = new JekaExplorerPanel(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

}
