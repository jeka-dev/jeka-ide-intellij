package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.ui.ColoredTreeCellRenderer;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.common.PsiMethodHelper;
import icons.JekaIcons;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class BeanNode extends AbstractNode {

    @Getter
    private final PsiClass psiClass;

    @Getter
    private final String name;

    private final String tooltipText;

    public BeanNode(Project project, PsiClass psiClass) {
        super(project);
        this.psiClass = psiClass;
        createFieldNodes().forEach(this::add);
        createMethodNodes().forEach(this::add);
        createNestedBeanNodes().forEach(this::add);
        name = JkExternalToolApi.getBeanName(psiClass.getQualifiedName());
        tooltipText = PsiClassHelper.getJkDoc(psiClass);
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

    private List<MethodNode> createMethodNodes() {
        List<MethodNode> result = new LinkedList<>();
        PsiMethod[] methods;
        try {
            methods = psiClass.getAllMethods();
        } catch (PsiInvalidElementAccessException e) {
            System.out.println("issue with getAllMethods on class " + psiClass.getName());
            return Collections.emptyList();
        }
        for (PsiMethod method : methods) {
            if (PsiMethodHelper.isInstancePublicVoidNoArgsNotFromObject(method)) {
                String methodName = method.getName();
                result.add(new MethodNode(project, method));
            }
        }
        return result;
    }

    private List<FieldNode> createFieldNodes() {
        return FieldNode.createFieldNodes(project, psiClass);
    }

    private List<BeanNode> createNestedBeanNodes() {
        PsiField[] psiFields = psiClass.getAllFields();
        List<BeanNode> result = new LinkedList<>();
        for (PsiField psiField : psiFields) {
            if (psiField.getModifierList().hasExplicitModifier("private")) {
                continue;
            }
            PsiType psiType = psiField.getType();
            if (psiType instanceof PsiClassType) {
                PsiClassType classType = (PsiClassType) psiType;
                PsiClass psiClass = classType.resolve();
                if (PsiClassHelper.isExtendingJkBean(psiClass)) {
                    BeanNode beanNode = new BeanNode(project, psiClass);
                    result.add(beanNode);
                }
            }
        }
        return result;
    }


}
