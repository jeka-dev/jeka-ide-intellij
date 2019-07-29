package org.jerkar.ideaplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jerkar.ideaplugin.gui.MainForm;
import org.jetbrains.annotations.NotNull;

/**
 * Created by angibaudj on 22-05-17.
 */
public class JerkarToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project,
                                        @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(new MainForm().panel(), "", false);
        toolWindow.getContentManager().addContent(content);


    }


}
