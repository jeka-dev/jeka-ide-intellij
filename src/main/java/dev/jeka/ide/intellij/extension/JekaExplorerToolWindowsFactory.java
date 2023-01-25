package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.panel.explorer.JekaExplorerPanel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class JekaExplorerToolWindowsFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JekaExplorerPanel panel = new JekaExplorerPanel(project);
        final ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getFactory().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
        return hasJekaModules(project);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return this.isApplicable(project);
    }

    static boolean hasJekaModules(@NotNull Project project) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        return DumbService.getInstance(project).runReadActionInSmartMode(
                () -> Arrays.stream(moduleManager.getModules()).anyMatch(ModuleHelper::isJekaModule)
        );
    }
}
