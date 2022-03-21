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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
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
@Service
public final class JekaConsoleToolWindows2 {

    private static final String ID = "Jeka console2";

    private static final Icon ICON = JekaIcons.JEKA_GREY_NAKED_13;

    private final Project project;

    private final ConsoleView consoleView;

    private final ToolWindow toolWindow;

    public JekaConsoleToolWindows2(Project project) {
        this.project = project;
        consoleView = createConsoleView(project);
        Disposer.register(project, consoleView);
        toolWindow = registerToolWindow(project, consoleView);
    }

    public static JekaConsoleToolWindows2 getInstance(Project project) {
        return project.getService(JekaConsoleToolWindows2.class);
    }

    public void launch(GeneralCommandLine commandLine) {
        OSProcessHandler osProcessHandler;
        try {
            System.out.println("__________________-launch jeka " + commandLine.getCommandLineString());
            osProcessHandler = new OSProcessHandler(commandLine);
            osProcessHandler.startNotify();
            consoleView.attachToProcess(osProcessHandler);
        } catch (ExecutionException e) {
            throw new RuntimeException("Jeka command line '" + commandLine.getCommandLineString() + "' failed");
        }

    }

    private static ConsoleView createConsoleView(Project project) {
        TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
        TextConsoleBuilder builder = factory.createBuilder(project);
        return builder.getConsole();
    }

    private static ToolWindow registerToolWindow(Project project, ConsoleView consoleView) {
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        RegisterToolWindowTask registerToolWindowTask = RegisterToolWindowTask.closable(ID,
                ICON, ToolWindowAnchor.BOTTOM);
        ToolWindow toolWindow = manager.registerToolWindow(registerToolWindowTask);
        final ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getFactory()
                .createContent(consoleView.getComponent(), "", false);
        contentManager.addContent(content);
        return toolWindow;
    }

}
