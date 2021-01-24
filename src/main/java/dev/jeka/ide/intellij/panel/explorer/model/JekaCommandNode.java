package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.psi.PsiMethod;
import dev.jeka.ide.intellij.common.JekaIcons;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;

@AllArgsConstructor
public class JekaCommandNode implements JekaModelNode {

    @Getter
    private JekaCommandHolderNode holder;

    private String name;

    @Getter
    private PsiMethod psiMethod;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, JekaIcons.JEKA_RUN,
                this::getName, this::getHolder, () -> Collections.emptyList());
    }

    private String getName() {
        if (psiMethod.getContainingClass().equals(holder.getContainingClass())) {
            return name;
        }
        return name + " (from " + psiMethod.getContainingClass().getName() + ")";
    }



}
