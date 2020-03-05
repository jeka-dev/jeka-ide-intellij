package dev.jeka.ide.intellij;

import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.openapi.actionSystem.*;

import java.io.File;

import static dev.jeka.ide.intellij.Constants.JEKA_HOME;
import static dev.jeka.ide.intellij.Constants.JEKA_USER_HOME;

public class JekaApplicationInitializedListener implements ApplicationInitializedListener {

    @Override
    public void componentsInitialized() {

        // Instantiate jeka group action
        DefaultActionGroup jekaGroup = new ProjectPopupJekaActionGroup();

        // Register jeka group action
        ActionManager actionManager = ActionManager.getInstance();
        actionManager.registerAction(ProjectPopupJekaActionGroup.class.getName(), jekaGroup);

        // Add Jeka group to Project context popup menu
        DefaultActionGroup projectPopupGroup = (DefaultActionGroup) actionManager.getAction("ProjectViewPopupMenu");
        Constraints menuLocation = new Constraints(Anchor.BEFORE, "Maven.GlobalProjectMenu");
        Constraints firstLocation = new Constraints(Anchor.FIRST, null);
        projectPopupGroup.addAction(Separator.getInstance(), firstLocation);
        projectPopupGroup.addAction(ShowCommandSetAction.INSTANCE, firstLocation);
        projectPopupGroup.addAction(SyncImlAction.INSTANCE, firstLocation);
        projectPopupGroup.addAction(jekaGroup, menuLocation);
        projectPopupGroup.addAction(Separator.getInstance(), menuLocation);

        // Add Sync Iml to editor context popup
        DefaultActionGroup popupGroup = (DefaultActionGroup) actionManager.getAction("EditorPopupMenu");
        popupGroup.addAction(Separator.getInstance(), firstLocation);
        popupGroup.addAction(SyncImlAction.INSTANCE, firstLocation);

        // Add Sync All to build menu
        DefaultActionGroup mainBuildMenu = (DefaultActionGroup) actionManager.getAction("BuildMenu");
        Constraints syncAllLocation = new Constraints(Anchor.AFTER, "GenerateAntBuild");
        mainBuildMenu.addAction(Separator.getInstance(), syncAllLocation);
        mainBuildMenu.addAction(SyncAllImlAction.INSTANCE, syncAllLocation);

        // Add classpath variable
        if (Utils.getPathVariable(JEKA_USER_HOME) == null) {
            String value = System.getProperty("user.home") + File.separator + ".jeka";
            Utils.setPathVariable(JEKA_USER_HOME, value);
        }
        String jekaHome = System.getenv("JEKA_HOME");
        if (jekaHome == null) {
            jekaHome = CmdJekaDoer.INSTANCE.createDistribIfNeeed()
                    .normalize().toAbsolutePath().toString();
        }
        if (Utils.getPathVariable(JEKA_HOME) == null && jekaHome != null) {
            Utils.setPathVariable(JEKA_HOME, jekaHome);
        }

    }
}
