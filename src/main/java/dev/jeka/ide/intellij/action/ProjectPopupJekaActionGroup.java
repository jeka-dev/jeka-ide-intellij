package dev.jeka.ide.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProjectPopupJekaActionGroup extends DefaultActionGroup {

    public ProjectPopupJekaActionGroup() {
        super("Jeka", true);
        ScaffoldAction scaffoldAction = new ScaffoldAction();
        SyncImlAction syncImlAction = new SyncImlAction();
        this.add(scaffoldAction);
        this.add(syncImlAction);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setIcon(Icons.JEKA_GROUP_ACTION);
    }
}
