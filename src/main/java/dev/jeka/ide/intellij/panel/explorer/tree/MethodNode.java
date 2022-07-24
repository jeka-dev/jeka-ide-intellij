package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.ColoredTreeCellRenderer;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.extension.action.JekaRunMethodAction;
import dev.jeka.ide.intellij.extension.action.JekaRunMethodParamAction;
import icons.JekaIcons;

public class MethodNode extends AbstractNode {

    private final PsiMethod psiMethod;

    private final String name;

    private final String tooltipText;

    public MethodNode(Project project, PsiMethod psiMethod) {
        super(project);
        this.psiMethod = psiMethod;
        this.name = psiMethod.getName();
        this.tooltipText = PsiClassHelper.getFormattedJkDoc(psiMethod);
        this.setAllowsChildren(false);
    }

    @Override
    public void customizeCellRenderer(ColoredTreeCellRenderer coloredTreeCellRenderer) {
        coloredTreeCellRenderer.setIcon(JekaIcons.COMMAND);
        coloredTreeCellRenderer.setToolTipText(tooltipText);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void fillPopupMenu(DefaultActionGroup group) {
        group.add(JekaRunMethodAction.RUN_JEKA_INSTANCE);
        group.add(JekaRunMethodAction.DEBUG_JEKA_INSTANCE);
        group.add(JekaRunMethodParamAction.RUN_JEKA_INSTANCE);
        group.add(JekaRunMethodParamAction.DEBUG_JEKA_INSTANCE);
        group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    @Override
    public Object getActionData(String dataId) {
        if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
            return psiMethod;
        }
        if (JekaRunMethodAction.MethodInfo.KEY.is(dataId)) {
            BeanNode parent = (BeanNode) this.getParent();
            String beanName = parent.getName();
            ModuleNode moduleNode = getCloserParentOfType(ModuleNode.class);
            return new JekaRunMethodAction.MethodInfo(moduleNode.getModule(),
                    parent.getPsiClass(), beanName, name);
        }
        return null;
    }

    @Override
    public void onDoubleClick(DataContext dataContext) {
        AnActionEvent actionEvent = new AnActionEvent(null, dataContext,
                ActionPlaces.UNKNOWN, new Presentation(), ActionManager.getInstance(), 0);
        JekaRunMethodAction.RUN_JEKA_INSTANCE.actionPerformed(actionEvent);
    }
}
