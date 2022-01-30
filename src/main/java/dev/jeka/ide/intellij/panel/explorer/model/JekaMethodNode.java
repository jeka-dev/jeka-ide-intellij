package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.psi.PsiMethod;
import icons.JekaIcons;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class JekaMethodNode extends JekaAbstractModelNode {

    @Getter
    private final PsiMethod psiMethod;

    public JekaMethodNode(JekaAbstractModelNode parent, PsiMethod psiMethod) {
        super(parent);
        this.psiMethod = psiMethod;
    }

    @Override
    protected NodeDescriptor<? extends JekaAbstractModelNode> makeNodeDescriptor() {
        JekaBeanNode beanNode = (JekaBeanNode) getParent();
        final String name;
        if (psiMethod.getContainingClass().equals(beanNode.getKbeanPsiClass())) {
            name = psiMethod.getName();
        } else {
            name = psiMethod.getName() + " (from " + psiMethod.getContainingClass().getName() + ")";
        }
        return basicNodeDescriptor(JekaIcons.COMMAND, name);
    }

    @Override
    public List<JekaAbstractModelNode> getChildren() {
        return Collections.emptyList();
    }
}
