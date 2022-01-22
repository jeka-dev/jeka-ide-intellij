package dev.jeka.ide.intellij.extension;

import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.openapi.actionSystem.*;
import dev.jeka.core.api.system.JkLocator;
import dev.jeka.ide.intellij.action.ProjectPopupJekaActionGroup;
import dev.jeka.ide.intellij.action.ShowJekaClassAction;
import dev.jeka.ide.intellij.action.SyncImlAction;
import dev.jeka.ide.intellij.common.JekaDistributions;
import dev.jeka.ide.intellij.common.MiscHelper;

import java.io.File;

import static dev.jeka.ide.intellij.common.Constants.*;

public class JekaApplicationInitializedListener implements ApplicationInitializedListener {

    private final static String JK_USER_HOME_ENV_NAME = "JEKA_USER_HOME";

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
        projectPopupGroup.addAction(jekaGroup, menuLocation);
        projectPopupGroup.addAction(Separator.getInstance(), menuLocation);
        Constraints firstLocation = new Constraints(Anchor.FIRST, null);
        projectPopupGroup.addAction(ShowJekaClassAction.INSTANCE, firstLocation);
        projectPopupGroup.addAction(SyncImlAction.INSTANCE, firstLocation);

        // Add Sync Iml to editor context popup
        DefaultActionGroup popupGroup = (DefaultActionGroup) actionManager.getAction("EditorPopupMenu");
        Constraints actionLocation = new Constraints(Anchor.FIRST, null);
        popupGroup.addAction(SyncImlAction.INSTANCE, actionLocation);

        // Add classpath variable
        if (MiscHelper.getPathVariable(JEKA_USER_HOME) == null) {
            String value = JkLocator.getJekaUserHomeDir().toString();
            MiscHelper.setPathVariable(JEKA_USER_HOME, value);
        }
        if (MiscHelper.getPathVariable(JEKA_HOME) == null) {
            MiscHelper.setPathVariable(JEKA_HOME, JekaDistributions.getDefault()
                    .normalize().toAbsolutePath().toString());
        }
        if (MiscHelper.getPathVariable(JEKA_CACHE_DIR) == null) {
            MiscHelper.setPathVariable(JEKA_CACHE_DIR, JkLocator.getCacheDir().toString());
        }

    }

}
