package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import dev.jeka.ide.intellij.common.PsiFieldHelper;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JekaFieldNode extends JekaAbstractModelNode {

    @Getter
    private final PsiField field;

    public JekaFieldNode(JekaAbstractModelNode parent, PsiField field) {
        super(parent);
        this.field = field;
    }

    @Override
    public NodeDescriptor makeNodeDescriptor() {
        return basicNodeDescriptor(AllIcons.Nodes.Parameter, field.getName());
    }

    @Override
    public List<JekaAbstractModelNode> getChildren() {
        PsiType fieldType = field.getType();
        if (isTerminal(fieldType)) {
            return Collections.emptyList();
        }
        if (fieldType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) fieldType;
            PsiClass psiClass = psiClassReferenceType.resolve();
            return getFieldNodes(getParent(), psiClass);
        }
        return Collections.emptyList();
    }

    static List<JekaAbstractModelNode> getFieldNodes(JekaAbstractModelNode parent, PsiClass containingClass) {
        List<JekaAbstractModelNode> result = new LinkedList<>();
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

            JekaFieldNode jekaField = new JekaFieldNode(parent, psiField);
            result.add(jekaField);
        }
        return result;
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

}
