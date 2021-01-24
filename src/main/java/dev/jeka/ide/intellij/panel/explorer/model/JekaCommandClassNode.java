package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import lombok.Getter;

import javax.swing.*;
import java.util.List;

@Getter
public class JekaCommandClassNode extends JekaCommandHolderNode {

    private JekaCommandClassNode(JekaModelNode parent, PsiClass psiClass) {
        super(parent, psiClass);
    }

    static JekaCommandClassNode fromPsiClass(JekaFolderNode parent, PsiClass psiClass) {
        return  new JekaCommandClassNode(parent, psiClass);
    }

    @Override
    protected Icon getIcon() {
        return AllIcons.Nodes.Class;
    }

    @Override
    public Module getModule() {
        JekaFolderNode jekaFolder = (JekaFolderNode) this.getParent();
        return jekaFolder.getModule();
    }

    @Override
    protected String getName() {
        return getContainingClass().getName();
    }

    @Override
    public PsiClass getCommandClass() {
        return this.getContainingClass();
    }

    @Override
    protected List<JekaModelNode> getChildren() {
        List<JekaModelNode> result = super.getChildren();
        result.add(new JekaUnboundPluginsNode(this));
        return result;
    }
}
