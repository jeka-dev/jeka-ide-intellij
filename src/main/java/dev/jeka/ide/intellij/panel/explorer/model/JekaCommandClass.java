package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import lombok.Getter;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

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

    @Override
    protected List<JekaModelNode> getChildren() {
        List<JekaModelNode> result = new LinkedList<>(plugins());
        result.addAll(super.getChildren());
        return result;
    }

    private List<JekaPlugin> plugins() {
        PsiClass commandPsiClass = getContainingClass();
        PsiField[] psiFields = commandPsiClass.getAllFields();
        List<JekaPlugin> result = new LinkedList<>();
        for (PsiField psiField : psiFields) {
            PsiType psiType = psiField.getType();
            if (psiType instanceof PsiClassType) {
                PsiClassType classType = (PsiClassType) psiType;
                PsiClass psiClass = classType.resolve();
                if (PsiClassHelper.isExtendingJkPlugin(psiClass)) {
                    JekaPlugin plugin = JekaPlugin.fromPsiClass(this, psiClass);
                    result.add(plugin);
                }
            }
        }
        return result;
    }
}
