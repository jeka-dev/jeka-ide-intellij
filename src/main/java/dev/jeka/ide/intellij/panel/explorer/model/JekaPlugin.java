package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import dev.jeka.ide.intellij.common.MiscHelper;

import javax.swing.*;

public class JekaPlugin extends JekaCommandHolder {

    private JekaPlugin(JekaModelNode parent, PsiClass psiClass) {
        super(parent, psiClass);
    }

    static JekaPlugin fromPsiClass(JekaModelNode parent, PsiClass psiClass) {
        return new JekaPlugin(parent, psiClass);
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

    private JekaCommandClass getJekaCommandClass() {
        if (getParent() instanceof JekaCommandClass) {
            return (JekaCommandClass) getParent();
        }
        if (getParent() instanceof JekaPlugin) {
            return ((JekaPlugin) getParent()).getJekaCommandClass();
        }
        if (getParent() instanceof JekaUnboundPlugins) {
            JekaUnboundPlugins jekaUnboundPlugins = (JekaUnboundPlugins) getParent();
            return jekaUnboundPlugins.getParent();
        }
        throw new IllegalStateException();
    }

    public String getPluginName() {
        return getName();
    }
}
