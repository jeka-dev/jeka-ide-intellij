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

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import icons.JekaIcons;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

/**
 * @author Jerome Angibaud
 */
public class JekaConsoleToolWindows {

    private static final String ID = "Jeka console";

    private static final Icon ICON = JekaIcons.JEKA_GREY_NAKED_13;

    private static ConsoleView createConsoleView(Project project) {
        TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
        TextConsoleBuilder builder = factory.createBuilder(project);
        return builder.getConsole();
    }

    public static void registerToolWindow(Project project) {
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        RegisterToolWindowTask registerToolWindowTask = RegisterToolWindowTask.closable(ID,
                ICON, ToolWindowAnchor.BOTTOM);
        ToolWindow toolWindow = manager.registerToolWindow(registerToolWindowTask);
        final ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager
                .getFactory()
                .createContent(getConsoleView(project).getComponent(), "", false);
        contentManager.addContent(content);
    }

    public static ConsoleView getConsoleView(Project project) {
        ConsoleView consoleView = project.getService(JekaConsoleViewService.class).getConsoleView();
        if (consoleView == null) {
            consoleView = createConsoleView(project);
            project.getService(JekaConsoleViewService.class).setConsoleView(consoleView);
        }
        return consoleView;
    }

    public static ToolWindow getToolWindow(Project project) {
        return ToolWindowManager.getInstance(project).getToolWindow(ID);
    }

    @Service
    private static final class JekaConsoleViewService implements Disposable {

        @Getter
        @Setter
        private ConsoleView consoleView;

        @Override
        public void dispose() {
            consoleView = null;
        }
    }
}
