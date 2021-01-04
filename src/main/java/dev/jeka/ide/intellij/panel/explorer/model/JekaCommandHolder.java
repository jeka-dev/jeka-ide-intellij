package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import dev.jeka.ide.intellij.common.PsiMethodHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public abstract class JekaCommandHolder implements JekaModelNode {

    @Getter
    private final JekaModelNode parent;

    @Getter
    private final PsiClass containingClass;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, getIcon(),
                this::getName, this::getParent, this::getChildren);
    }

    protected abstract Icon getIcon();

    public abstract Module getModule();

    protected abstract String getName();

    public abstract PsiClass getCommandClass();

    protected List<JekaModelNode> getChildren() {
        PsiMethod[] methods = containingClass.getAllMethods();
        List<JekaModelNode> commands = new LinkedList<>();
        Set<String> methodNames = new HashSet<>();
        for (PsiMethod method : methods) {
            if (PsiMethodHelper.isInstancePublicVoidNoArgsNotFromObject(method)) {
                String methodName = method.getName();
                commands.add(new JekaCommand(this, methodName, method));
                methodNames.add(methodName);
            }
        }
        return commands;
    }

}
