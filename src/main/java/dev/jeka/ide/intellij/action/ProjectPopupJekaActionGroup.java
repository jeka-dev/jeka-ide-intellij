package dev.jeka.ide.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;

public class ProjectPopupJekaActionGroup extends DefaultActionGroup {

    public ProjectPopupJekaActionGroup() {
        super("Jeka", true);
        ScaffoldAction scaffoldAction = new ScaffoldAction();
        SyncImlAction syncImlAction = new SyncImlAction();
        this.add(syncImlAction);
        this.add(scaffoldAction);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setIcon(JkIcons.JEKA_GROUP_ACTION);
    }
}
