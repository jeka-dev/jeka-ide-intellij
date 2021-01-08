package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import dev.jeka.ide.intellij.common.JkCommandSetHelper;
import dev.jeka.ide.intellij.common.PsiFieldHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class JekaFieldSet implements JekaModelNode {

    @Getter
    private final JekaCommandHolder parent;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Nodes.ConfigFolder,
                () -> "Options", this::getParent, this::getChildren);
    }

    protected List<JekaModelNode> getChildren() {
        PsiClass containingClass = parent.getContainingClass();
        List<JekaModelNode> result = new LinkedList<>();
        final String pluginName;
        if (parent instanceof JekaPlugin) {
            JekaPlugin jekaPlugin = (JekaPlugin) parent;
            pluginName = jekaPlugin.getPluginName();
        } else {
            pluginName = null;
        }
        for (PsiField psiField : containingClass.getAllFields()) {
            if (!psiField.hasModifier(JvmModifier.PUBLIC) && !PsiFieldHelper.hasSetter(psiField)) {
                continue;
            }
            if (psiField.getContainingClass().getQualifiedName().equals(Object.class.getName())) {
                continue;
            }
            if (psiField.hasModifier(JvmModifier.STATIC)) {
                continue;
            }
            JekaField jekaField = new JekaField(this, psiField);
            result.add(jekaField);
        }
        return result;
    }

}
