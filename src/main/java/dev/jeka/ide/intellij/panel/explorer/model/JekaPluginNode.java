package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import dev.jeka.ide.intellij.common.MiscHelper;

import javax.swing.*;

public class JekaPluginNode extends JekaCommandHolderNode {

    private JekaPluginNode(JekaModelNode parent, PsiClass psiClass) {
        super(parent, psiClass);
    }

    static JekaPluginNode fromPsiClass(JekaModelNode parent, PsiClass psiClass) {
        return new JekaPluginNode(parent, psiClass);
    }

    @Override
    protected Icon getIcon() {
        return AllIcons.Nodes.Plugin;
    }

    @Override
    public Module getModule() {
        return getJekaCommandClass().getModule();
    }

    @Override
    protected String getName() {
        return MiscHelper.pluginName(getContainingClass().getName());
    }

    @Override
    public PsiClass getCommandClass() {
        return getJekaCommandClass().getCommandClass();
    }

    private JekaCommandClassNode getJekaCommandClass() {
        if (getParent() instanceof JekaCommandClassNode) {
            return (JekaCommandClassNode) getParent();
        }
        if (getParent() instanceof JekaPluginNode) {
            return ((JekaPluginNode) getParent()).getJekaCommandClass();
        }
        if (getParent() instanceof JekaUnboundPluginsNode) {
            JekaUnboundPluginsNode jekaUnboundPlugins = (JekaUnboundPluginsNode) getParent();
            return jekaUnboundPlugins.getParent();
        }
        throw new IllegalStateException();
    }

    public String getPluginName() {
        return getName();
    }
}
