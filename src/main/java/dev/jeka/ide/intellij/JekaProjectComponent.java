package dev.jeka.ide.intellij;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.keymap.impl.ui.Group;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.actions.ShowAnnotateOperationsPopup;
import dev.jeka.ide.intellij.action.ProjectPopupJekaActionGroup;
import dev.jeka.ide.intellij.action.ScaffoldAction;
import dev.jeka.ide.intellij.action.SyncImlAction;

import javax.swing.*;


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
