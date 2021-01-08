package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiField;
import dev.jeka.ide.intellij.common.JkCommandSetHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JekaField implements JekaModelNode {

    @Getter
    private final JekaFieldSet parent;

    @Getter
    private final PsiField field;


    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Nodes.C_plocal,
                this::getName, this::getParent, () -> Collections.emptyList());
    }

    private String getName() {
        return field.getName();
    }

}
