package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import dev.jeka.ide.intellij.common.PsiFieldHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class JekaFieldNode implements JekaModelNode {

    @Getter
    private final JekaModelNode parent;

    @Getter
    private final PsiField field;


    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Nodes.Parameter,
                this::getName, this::getParent, () -> children(this, field));
    }

    private String getName() {
        return field.getName();
    }

    static List<JekaModelNode> getFieldNodes(JekaModelNode parent, PsiClass containingClass) {
        List<JekaModelNode> result = new LinkedList<>();
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

    private static List<JekaModelNode> children(JekaModelNode parent, PsiField psiField) {
        PsiType fieldType = psiField.getType();
        if (isTerminal(fieldType)) {
            return Collections.emptyList();
        }
        if (fieldType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) fieldType;
            PsiClass psiClass = psiClassReferenceType.resolve();
            return getFieldNodes(parent, psiClass);
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

}
