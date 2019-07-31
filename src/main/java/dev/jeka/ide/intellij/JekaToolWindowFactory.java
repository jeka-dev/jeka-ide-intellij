package dev.jeka.ide.intellij;

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
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(new MainForm().panel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }


}
