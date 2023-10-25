package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.SlowOperations;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.common.model.NavigableProxy;
import dev.jeka.ide.intellij.extension.action.JekaRunMethodAction;
import dev.jeka.ide.intellij.extension.action.JekaRunMethodParamAction;
import icons.JekaIcons;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.List;

@EqualsAndHashCode(of = "name", callSuper = false)
public class MethodNode extends AbstractNode {

    public static final Icon ICON = JekaIcons.COMMAND;

    private final PsiMethod psiMethod;

    private final String name;

    @Getter
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
        coloredTreeCellRenderer.setIcon(ICON);
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
            return new NavigableProxy(psiMethod);
        }
        if (JekaRunMethodAction.MethodInfo.KEY.is(dataId)) {
            BeanNode parent = (BeanNode) this.getParent();
            String beanName = parent.getName();
            ModuleNode moduleNode = getCloserParentOfType(ModuleNode.class);
            if (moduleNode == null) {
                throw new IllegalStateException("No ancestor of type ModuleNode found for " + this.getTreePath());
            }
            return new JekaRunMethodAction.MethodInfo(moduleNode.getModule(),
                    parent.getClassName(), beanName, name);
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
