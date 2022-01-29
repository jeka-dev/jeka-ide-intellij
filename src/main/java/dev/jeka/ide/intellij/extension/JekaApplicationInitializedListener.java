package dev.jeka.ide.intellij.extension;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.actionSystem.*;
import dev.jeka.ide.intellij.action.ProjectPopupJekaActionGroup;
import dev.jeka.ide.intellij.action.GotoJkBeanAction;
import dev.jeka.ide.intellij.action.SyncImlAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JekaApplicationInitializedListener implements AppLifecycleListener {

    @Override
    public void  appFrameCreated(@NotNull List<String> commandLineArgs) {

        // Instantiate jeka group action
        DefaultActionGroup jekaGroup = new ProjectPopupJekaActionGroup();

        // Register jeka group action
        ActionManager actionManager = ActionManager.getInstance();
        actionManager.registerAction(ProjectPopupJekaActionGroup.class.getName(), jekaGroup);

        // Add Jeka group to Project context popup menu
        DefaultActionGroup projectPopupGroup = (DefaultActionGroup) actionManager.getAction("ProjectViewPopupMenu");
        Constraints menuLocation = new Constraints(Anchor.LAST, null);
        projectPopupGroup.addAction(jekaGroup, menuLocation);
        projectPopupGroup.addAction(Separator.getInstance(), menuLocation);
        Constraints firstLocation = new Constraints(Anchor.FIRST, null);
        projectPopupGroup.addAction(GotoJkBeanAction.INSTANCE, firstLocation);
        projectPopupGroup.addAction(SyncImlAction.INSTANCE, firstLocation);

        // Add Sync Iml to editor context popup
        DefaultActionGroup popupGroup = (DefaultActionGroup) actionManager.getAction("EditorPopupMenu");
        Constraints actionLocation = new Constraints(Anchor.FIRST, null);
        popupGroup.addAction(SyncImlAction.INSTANCE, actionLocation);



    }



}
