package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiClass;
import lombok.*;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class JekaCommandClass implements JekaModelNode {

    private final String name;

    private final JekaModule jekaModule;

    private final List<JekaCommand> commands;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.FileTypes.JavaClass,
                this::getName, this::getJekaModule, this::getCommands);
    }

    static JekaCommandClass fromPsiClass(JekaModule parent, PsiClass psiClass) {
        return new JekaCommandClass(psiClass.getName(), parent, Collections.emptyList());
    }
}
