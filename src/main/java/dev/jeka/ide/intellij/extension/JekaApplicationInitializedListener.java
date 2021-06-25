package dev.jeka.ide.intellij.extension;

import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.openapi.actionSystem.*;
import dev.jeka.ide.intellij.action.ProjectPopupJekaActionGroup;
import dev.jeka.ide.intellij.action.ShowJekaClassAction;
import dev.jeka.ide.intellij.action.SyncImlAction;
import dev.jeka.ide.intellij.common.MiscHelper;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;

import java.io.File;

import static dev.jeka.ide.intellij.common.Constants.JEKA_HOME;
import static dev.jeka.ide.intellij.common.Constants.JEKA_USER_HOME;

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

        // Add classpath variable
        if (MiscHelper.getPathVariable(JEKA_USER_HOME) == null) {
            String value = getJekaUserHomeDir();
            MiscHelper.setPathVariable(JEKA_USER_HOME, value);
        }
        String jekaHome = System.getenv("JEKA_HOME");
        if (jekaHome == null) {
            jekaHome = CmdJekaDoer.INSTANCE.createDistribIfNeeded()
                    .normalize().toAbsolutePath().toString();
        }
        if (MiscHelper.getPathVariable(JEKA_HOME) == null && jekaHome != null) {
            MiscHelper.setPathVariable(JEKA_HOME, jekaHome);
        }

    }

    private static String getJekaUserHomeDir() {
        final String env = System.getenv(JK_USER_HOME_ENV_NAME);
        if (env != null && env.trim().length() > 0) {
            return env;
        }
        return System.getProperty("user.home") + File.separator + ".jeka";
    }
}
