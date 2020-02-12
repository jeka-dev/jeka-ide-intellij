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

        // Add Jeka group to popup menu
        DefaultActionGroup projectPopupGroup = (DefaultActionGroup) actionManager.getAction("ProjectViewPopupMenu");
        Constraints menuLocation = new Constraints(Anchor.BEFORE, "Maven.GlobalProjectMenu");
        //Constraints menuLocation = new Constraints(Anchor.BEFORE, "MakeModule");
        projectPopupGroup.addAction(jekaGroup, menuLocation);
        projectPopupGroup.addAction(Separator.getInstance(), menuLocation);

        // Add Sync Iml to editor context popup
        DefaultActionGroup popupGroup = (DefaultActionGroup) actionManager.getAction("EditorPopupMenu");
        Constraints actionLocation = new Constraints(Anchor.FIRST, null);
        popupGroup.addAction(SyncImlAction.INSTANCE, actionLocation);

        // Add classpath variable
        if (Utils.getPathVariable(JEKA_USER_HOME) == null) {
            String value = System.getProperty("user.home") + File.separator + ".jeka";
            Utils.setPathVariable(JEKA_USER_HOME, value);
        }
        if (Utils.getPathVariable(JEKA_HOME) == null) {
            String value = System.getenv("JEKA_HOME");
            if (value != null && !value.trim().equals("")) {
                Utils.setPathVariable(JEKA_HOME, value);
            }
        }

    }
}
