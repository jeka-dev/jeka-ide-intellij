package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValue;
import com.intellij.ui.ColoredTreeCellRenderer;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.common.PsiMethodHelper;
import icons.JekaIcons;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class BeanNode extends AbstractNode implements Comparable<BeanNode> {

    @Getter
    private final PsiClass psiClass;

    @Getter
    private final String name;

    @Getter
    private final String className;

    private final String tooltipText;

    public BeanNode(Project project, PsiClass psiClass) {
        super(project);
        this.psiClass = psiClass;
        createFieldNodes(psiClass).forEach(this::add);
        createMethodNodes(psiClass).forEach(this::add);
        createNestedBeanNodes(psiClass).forEach(this::add);
        name = JkExternalToolApi.getBeanName(psiClass.getQualifiedName());
        className = psiClass.getQualifiedName();
        String tooltipContent = "<b>" + psiClass.getQualifiedName() + "</b><";
        String doc = PsiClassHelper.getFormattedJkDoc(psiClass);
        if (!JkUtilsString.isBlank(doc)) {
            tooltipContent = tooltipContent + "<br/>" + doc;
        }
        tooltipText = tooltipContent;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void customizeCellRenderer(ColoredTreeCellRenderer coloredTreeCellRenderer) {
        coloredTreeCellRenderer.setIcon(JekaIcons.KBEAN);
        coloredTreeCellRenderer.setToolTipText(tooltipText);
    }

    @Override
    public void fillPopupMenu(DefaultActionGroup group) {
        group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    @Override
    public Object getActionData(String dataId) {
        if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
            return psiClass;
        }
        return null;
    }

    private List<MethodNode> createMethodNodes(PsiClass psiClass) {
        List<MethodNode> result = new LinkedList<>();
        PsiMethod[] methods;
        try {
            methods = psiClass.getAllMethods();
        } catch (PsiInvalidElementAccessException e) {
            return Collections.emptyList();
        }
        for (PsiMethod method : methods) {
            if (PsiMethodHelper.isInstancePublicVoidNoArgsNotFromObject(method)) {
                result.add(new MethodNode(project, method));
            }
        }
        return result;
    }

    private List<FieldNode> createFieldNodes(PsiClass psiClass) {
        return FieldNode.createFieldNodes(project, psiClass);
    }

    private List<BeanNode> createNestedBeanNodes(PsiClass psiClass) {
        PsiField[] psiFields = psiClass.getAllFields();
        List<BeanNode> result = new LinkedList<>();
        for (PsiField psiField : psiFields) {
            if (psiField.getModifierList().hasExplicitModifier("private")) {
                continue;
            }
            PsiType psiType = psiField.getType();
            if (psiType instanceof PsiClassType) {
                PsiClassType classType = (PsiClassType) psiType;
                PsiClass childPsiClass = classType.resolve();
                if (PsiClassHelper.isExtendingJkBean(childPsiClass)) {
                    BeanNode beanNode = new BeanNode(project, childPsiClass);
                    result.add(beanNode);
                }
            }
        }
        return result;
    }


    @Override
    public int compareTo(@NotNull BeanNode o) {
        return this.name.compareTo(o.name);
    }
}
