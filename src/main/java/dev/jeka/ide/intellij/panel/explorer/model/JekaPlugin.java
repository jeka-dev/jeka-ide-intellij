package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;

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
        if (getParent() instanceof JekaCommandClass) {
            JekaCommandClass command = (JekaCommandClass) getParent();
            return command.getModule();
        }
        throw new IllegalStateException();
    }

    @Override
    protected String getName() {
        return getContainingClass().getName().substring("JkPlugin".length());
    }

    @Override
    public PsiClass getCommandClass() {
        if (getParent() instanceof JekaCommandClass) {
            JekaCommandClass command = (JekaCommandClass) getParent();
            return command.getCommandClass();
        }
        throw new IllegalStateException();
    }

    public String getPluginName() {
        return getName().toLowerCase();
    }
}
