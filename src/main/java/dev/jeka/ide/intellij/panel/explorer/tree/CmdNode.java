package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import dev.jeka.ide.intellij.extension.action.JekaRunCmdAction;
import dev.jeka.ide.intellij.extension.action.JekaRunCmdParamAction;
import icons.JekaIcons;

public class CmdNode extends AbstractNode {

    private final String name;

    private final String cmd;

    public CmdNode(Project project, String name, String cmd) {
        super(project);
        this.name = name;
        this.cmd = cmd;
        this.setAllowsChildren(false);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void customizeCellRenderer(ColoredTreeCellRenderer coloredTreeCellRenderer) {
        coloredTreeCellRenderer.setIcon(JekaIcons.CMD);
        coloredTreeCellRenderer.setToolTipText(cmd);
    }

    @Override
    public void fillPopupMenu(DefaultActionGroup group) {
        group.add(JekaRunCmdAction.RUN_JEKA_INSTANCE);
        group.add(JekaRunCmdAction.DEBUG_JEKA_INSTANCE);
        group.add(JekaRunCmdParamAction.RUN_JEKA_INSTANCE);
        group.add(JekaRunCmdParamAction.DEBUG_JEKA_INSTANCE);
        group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    @Override
    public Object getActionData(String dataId) {
        if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
            ModuleNode parent = (ModuleNode) getParent();
            return PsiManager.getInstance(project).findFile(parent.getProjectPropFile());
        }
        if (JekaRunCmdAction.CmdInfo.KEY.is(dataId)) {
            ModuleNode parent = (ModuleNode) getParent();
            return new JekaRunCmdAction.CmdInfo(name, cmd, parent.getModule());
        }
        return null;
    }

    @Override
    public void onDoubleClick(DataContext dataContext) {
        AnActionEvent actionEvent = new AnActionEvent(null, dataContext,
                ActionPlaces.UNKNOWN, new Presentation(), ActionManager.getInstance(), 0);
        JekaRunCmdAction.RUN_JEKA_INSTANCE.actionPerformed(actionEvent);
    }
}
