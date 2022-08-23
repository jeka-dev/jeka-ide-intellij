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
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import icons.JekaIcons;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Jerome Angibaud
 */
public class JekaConsoleToolWindowFactory implements ToolWindowFactory {

    public static final String ID = "Jeka console";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        final ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager
                .getFactory()
                .createContent(getConsoleView(project).getComponent(), "", false);
        contentManager.addContent(content);
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
        return JekaExplorerToolWindowsFactory.hasJekaModules(project);
    }

    is

    public static ConsoleView getConsoleView(Project project) {
        ConsoleView consoleView = project.getService(JekaConsoleViewService.class).getConsoleView();
        if (consoleView == null) {
            consoleView = createConsoleView(project);
            project.getService(JekaConsoleViewService.class).setConsoleView(consoleView);
        }
        return consoleView;
    }


    private static ConsoleView createConsoleView(Project project) {
        TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
        TextConsoleBuilder builder = factory.createBuilder(project);
        return builder.getConsole();
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
