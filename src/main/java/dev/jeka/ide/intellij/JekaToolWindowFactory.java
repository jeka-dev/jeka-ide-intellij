package dev.jeka.ide.intellij;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import dev.jeka.ide.intellij.gui.MainForm;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Jerome Angibaud on 22-05-17.
 */
public class JekaToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project,
                                        @NotNull ToolWindow toolWindow) {

        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        Content content = toolWindow.getContentManager().getFactory()
                .createContent(consoleView.getComponent(), "Jeka Output", false);
        toolWindow.getContentManager().addContent(content);
        consoleView.print("Hello from Jerkar!", ConsoleViewContentType.NORMAL_OUTPUT);

        toolWindow.getContentManager().addContent(content);

    }


}
