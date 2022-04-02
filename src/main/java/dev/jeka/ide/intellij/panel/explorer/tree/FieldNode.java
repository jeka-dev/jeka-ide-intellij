package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.icons.AllIcons;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.ui.ColoredTreeCellRenderer;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.common.PsiFieldHelper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class FieldNode extends AbstractNode {

    private final PsiField psiField;

    private final String name;

    private final String tooltipText;

    public FieldNode(Project project, PsiField psiField) {
        super(project);
        this.psiField = psiField;
        this.name = psiField.getName();
        this.tooltipText = PsiClassHelper.getJkDoc(psiField);
        this.createChildren().forEach(this::add);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void customizeCellRenderer(ColoredTreeCellRenderer coloredTreeCellRenderer) {
        coloredTreeCellRenderer.setIcon(AllIcons.Nodes.Parameter);
        coloredTreeCellRenderer.setToolTipText(tooltipText);
    }

    @Override
    public void fillPopupMenu(DefaultActionGroup group) {
        group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    @Override
    public Object getActionData(String dataId) {
        if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
            return psiField;
        }
        return null;
    }

    private List<FieldNode> createChildren() {
        PsiType fieldType = psiField.getType();
        if (isTerminal(fieldType)) {
            return Collections.emptyList();
        }
        if (fieldType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) fieldType;
            PsiClass psiClass = psiClassReferenceType.resolve();
            return createFieldNodes(project, psiClass);
        }
        return Collections.emptyList();
    }

    private static boolean isTerminal(PsiType psiType) {
        String typeName = psiType.getCanonicalText();
        if (typeName.startsWith("java.")) {
            return true;
        }
        String firstLetter = typeName.substring(0,1);
        if (firstLetter.equals(firstLetter.toLowerCase()) && !typeName.contains(".")) {
            return true;
        }
        return false;
    }

    static List<FieldNode> createFieldNodes(Project project, PsiClass containingClass) {
        List<FieldNode> result = new LinkedList<>();
        if (!containingClass.isValid()) {
            return Collections.emptyList();
        }
        for (PsiField psiField : containingClass.getAllFields()) {
            if (!psiField.hasModifier(JvmModifier.PUBLIC) && !PsiFieldHelper.hasSetter(psiField)) {
                continue;
            }
            if (psiField.getContainingClass().getQualifiedName().equals(Object.class.getName())) {
                continue;
            }
            if (psiField.hasModifier(JvmModifier.STATIC)) {
                continue;
            }
            FieldNode fieldNode = new FieldNode(project, psiField);
            result.add(fieldNode);
        }
        return result;
    }
}
