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

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import dev.jeka.ide.intellij.JekaDoer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Jerome Angibaud
 */
public class ScaffoldAction extends AnAction {

    public ScaffoldAction() {
        super("Add Jeka folder, scripts and classes", "Add Jeka folder, scripts and classes", AllIcons.Actions.Expandall);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return;
        }
        Module module = ModuleUtil.findModuleForFile(virtualFile, event.getProject());
        Path path = moduleRootPath(module);
        if (path == null) {
            return;
        }
        toolWindow(module.getProject());
        JekaDoer jekaDoer = JekaDoer.getInstance();
        jekaDoer.scaffoldModule(path);
        virtualFile.getFileSystem().refresh(true);
        JkNotifications.info("Missing Jeka files (re)created for module " + module.getName());
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return;
        }
        Module module = ModuleUtil.findModuleForFile(virtualFile, event.getProject());
        event.getPresentation().setText(" Add Jeka folder, scripts and classes to " + module.getName());
    }

    private static Path moduleRootPath(Module module) {
        if (module == null) {
            return null;
        }
        VirtualFile virtualRoot = ModuleRootManager.getInstance(module).getContentRoots()[0];
        return Paths.get(virtualRoot.getPath());
    }

    private void toolWindow(Project project) {
        ToolWindow runToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Run");
        System.out.println(Arrays.asList(ToolWindowManager.getInstance(project).getToolWindowIds()));
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        String name = "jeka output";
        Content content = runToolWindow.getContentManager().findContent(name);
        if (content == null) {
            content = runToolWindow.getContentManager().getFactory()
                    .createContent(consoleView.getComponent(), name, false);
            content.setIcon(JkIcons.JEKA_GREY_NAKED_13);
            runToolWindow.getContentManager().addContent(content);
        }
        consoleView.print("Hello from Jeka!", ConsoleViewContentType.NORMAL_OUTPUT);
    }

}





