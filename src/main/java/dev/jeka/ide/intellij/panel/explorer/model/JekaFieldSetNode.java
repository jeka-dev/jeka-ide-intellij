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
public class JekaFieldSetNode implements JekaModelNode {

    @Getter
    private final JekaBeanNode parent;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Nodes.ClassInitializer,
                () -> "Properties", this::getParent, this::getChildren);
    }

    protected List<JekaModelNode> getChildren() {
        PsiClass containingClass = parent.getKbeanPsiClass();
        return JekaFieldNode.getFieldNodes(this, containingClass);
    }



}
