package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiMethod;
import dev.jeka.ide.intellij.common.JekaIcons;
import dev.jeka.ide.intellij.extension.JkIconProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;

@AllArgsConstructor
public class JekaCommand implements JekaModelNode {

    @Getter
    private JekaCommandClass buildClass;

    private String name;

    @Getter
    private PsiMethod psiMethod;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, JekaIcons.JEKA_RUN,
                this::getName, this::getBuildClass, () -> Collections.emptyList());
    }

    private String getName() {
        if (psiMethod.getContainingClass().equals(buildClass.getPsiClass())) {
            return name;
        }
        return name + " (from " + psiMethod.getContainingClass().getName() + ")";
    }



}
