package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.panel.explorer.JekaExplorerPanel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class JekaExplorerToolWindowsFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JekaExplorerPanel panel = new JekaExplorerPanel(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> {
            boolean result = Arrays.stream(moduleManager.getModules())
                    .anyMatch(ModuleHelper::isJekaModule);
            System.out.println("--------------------------isapplicable?" + result);
            return result;
        });
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return this.isApplicable(project);
    }
}
