package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.icons.AllIcons;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.ui.ColoredTreeCellRenderer;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.common.PsiHelper;
import dev.jeka.ide.intellij.common.model.NavigableProxy;
import lombok.Getter;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FieldNode extends AbstractNode {

    public static final Icon ICON = AllIcons.Nodes.Parameter;

    @Getter
    private final PsiField psiField;

    private final String name;

    @Getter
    private final String tooltipText;

    @Getter
    private final String declaration;

    @Getter
    private List<String> acceptedValues;

    public FieldNode(Project project, PsiField psiField) {
        super(project);
        this.psiField = psiField;
        this.name = psiField.getName();
        this.tooltipText = PsiClassHelper.getFormattedJkDoc(psiField);
        this.createChildren().forEach(this::add);
        this.acceptedValues = acceptedValues(psiField);
        String fullDeclaration = psiField.getText();
        if (psiField.getFirstChild() != null) {
            String pre = psiField.getFirstChild().getText();
            String shortDeclaration = fullDeclaration.substring(pre.length());
            this.declaration = shortDeclaration;
        } else {
            this.declaration = null;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void customizeCellRenderer(ColoredTreeCellRenderer coloredTreeCellRenderer) {
        coloredTreeCellRenderer.setIcon(ICON);
        coloredTreeCellRenderer.setToolTipText(tooltipText);
    }

    @Override
    public void fillPopupMenu(DefaultActionGroup group) {
        group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    @Override
    public Object getActionData(String dataId) {
        if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
            return new NavigableProxy(psiField);
        }
        return null;
    }

    public String prefixedName() {
        if (this.getParent() instanceof FieldNode) {
            FieldNode parent = (FieldNode) this.getParent();
            return parent.prefixedName() + "." + this;
        } else {
            return toString();
        }
    }

    public List<FieldNode> extend() {
        if (this.isLeaf()) {
            return Collections.singletonList(this);
        }
        return Collections.list(children()).stream()
                .filter(FieldNode.class::isInstance)
                .map(FieldNode.class::cast)
                .flatMap(fieldNode -> fieldNode.extend().stream())
                .collect(Collectors.toList());
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
        if (containingClass == null || !containingClass.isValid()) {
            return Collections.emptyList();
        }
        for (PsiField psiField : containingClass.getAllFields()) {
            if (!psiField.hasModifier(JvmModifier.PUBLIC) && !PsiHelper.hasSetter(psiField)) {
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

    private static List<String> acceptedValues(PsiField psiField) {
        if (psiField.getType().getPresentableText().equals("Boolean")
                || psiField.getType().getPresentableText().equals("boolean")) {
            return JkUtilsIterable.listOf("true", "false");
        }
        if (psiField.getType() instanceof  PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiField.getType();
            PsiClass psiClass = psiClassReferenceType.resolve();
            if (psiClass != null && psiClass.isEnum()) {
                return Arrays.stream(psiClass.getAllFields())
                        .filter(PsiEnumConstant.class::isInstance)
                        .map(PsiEnumConstant.class::cast)
                        .map(psiEnumConstant -> psiEnumConstant.getText())
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
