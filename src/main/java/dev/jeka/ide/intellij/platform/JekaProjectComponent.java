package dev.jeka.ide.intellij.platform;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ProjectComponent;
import dev.jeka.ide.intellij.platform.ProjectPopupJekaActionGroup;


public class JekaProjectComponent implements ProjectComponent {

    @Override
    public void projectOpened() {

        // Instantiate jeka group action
        DefaultActionGroup jekaGroup = new ProjectPopupJekaActionGroup();

        // Register jeka group action
        ActionManager actionManager = ActionManager.getInstance();
        actionManager.registerAction(ProjectPopupJekaActionGroup.class.getName(), jekaGroup);

        // Add Jeka group to popup menu
        DefaultActionGroup projectPopupGroup = (DefaultActionGroup) actionManager.getAction("ProjectViewPopupMenu");
        Constraints menuLocation = new Constraints(Anchor.BEFORE, "Maven.GlobalProjectMenu");
        projectPopupGroup.addAction(jekaGroup, menuLocation);
        projectPopupGroup.addAction(Separator.getInstance(), menuLocation);

        System.out.println("------------------------------------- project opened");
    }
}
