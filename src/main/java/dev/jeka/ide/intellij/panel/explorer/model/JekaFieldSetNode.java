package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.psi.PsiClass;

import java.util.List;


public class JekaFieldSetNode extends JekaAbstractModelNode {

    public JekaFieldSetNode(JekaBeanNode parent) {
        super(parent);
    }

    @Override
    public NodeDescriptor<? extends JekaAbstractModelNode> makeNodeDescriptor() {
        return basicNodeDescriptor(AllIcons.Nodes.ClassInitializer, "Properties");
    }

    @Override
    public List<JekaAbstractModelNode> getChildren() {
        JekaBeanNode beanNode = (JekaBeanNode) getParent();
        PsiClass containingClass = beanNode.getKbeanPsiClass();
        return JekaFieldNode.getFieldNodes(this, containingClass);
    }



}
