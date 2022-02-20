package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import dev.jeka.ide.intellij.panel.explorer.JekaExplorerPanel;
import dev.jeka.ide.intellij.panel.explorer.model.JekaRootManager;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class JekaToolWindowManagerListener implements ToolWindowManagerListener {

    private boolean unactivatedOnce;

    private boolean unactivated;

    @Override
    public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
        ToolWindow jekaTool = toolWindowManager.getToolWindow(JekaToolWindows.ID);
        if (jekaTool == null) {
            return;
        }
        if (!jekaTool.isVisible() && jekaTool.getContentManager().getContent(0) != null) {
            unactivatedOnce = true;
            unactivated = true;
            jekaExplorerPanel(jekaTool).getJekaRootManager().listenPsi(false);
            return;
        }

        if (!unactivatedOnce) {
            return;
        }
        if (unactivated && jekaTool.isVisible()) {
            unactivated = false;
            JekaRootManager jekaRootManager = jekaExplorerPanel(jekaTool).getJekaRootManager();
            jekaRootManager.notifyChange();
            jekaRootManager.listenPsi(true);
        }
    }

    private JekaExplorerPanel jekaExplorerPanel(ToolWindow toolWindow) {
        return (JekaExplorerPanel) toolWindow.getContentManager().getContent(0).getComponent();
    }

    private static void toolState(ToolWindowManager toolWindowManager, String id) {
        ToolWindow toolWindow = toolWindowManager.getToolWindow(id);
        System.out.println(id + " active=" +toolWindow.isActive() + ",visible=" + toolWindow.isVisible());
    }

}
