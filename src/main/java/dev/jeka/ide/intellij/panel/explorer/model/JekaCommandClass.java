package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import dev.jeka.ide.intellij.common.JkCommandSetHelper;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import lombok.Getter;

import javax.swing.*;

@Getter
public class JekaCommandClass extends JekaCommandHolder {

    private JekaCommandClass(JekaModelNode parent, PsiClass psiClass) {
        super(parent, psiClass);
    }

    static JekaCommandClass fromPsiClass(JekaFolder parent, PsiClass psiClass) {
        return  new JekaCommandClass(parent, psiClass);
    }

    @Override
    protected Icon getIcon() {
        return AllIcons.Nodes.Class;
    }

    @Override
    public Module getModule() {
        JekaFolder jekaFolder = (JekaFolder) this.getParent();
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

}
