package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import dev.jeka.ide.intellij.common.PsiFieldHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class JekaFieldSetNode implements JekaModelNode {

    @Getter
    private final JekaBeanNode parent;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Nodes.ConfigFolder,
                () -> "Options", this::getParent, this::getChildren);
    }

    protected List<JekaModelNode> getChildren() {
        PsiClass containingClass = parent.getKbeanPsiClass();
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
            JekaFieldNode jekaField = new JekaFieldNode(this, psiField);
            result.add(jekaField);
        }
        return result;
    }

}
