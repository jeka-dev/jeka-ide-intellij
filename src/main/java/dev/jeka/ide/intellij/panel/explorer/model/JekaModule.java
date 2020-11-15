package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class JekaModule implements JekaModelNode {

    private final String name;

    private final JekaModule parentProject;

    private final List<JekaCommandClass> commandClasses = new LinkedList<>();

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Nodes.Folder,
                this::getName, this::getParentProject, this::getCommandClasses);
    }

    static JekaModule fromModule(JekaModule parent, Module module) {
        List<PsiClass> jekaPsilasses = PsiClassHelper.findJekaCommandClasses(module);
        JekaModule result = new JekaModule(module.getName(), parent);
        List<JekaCommandClass> jekaCommandClasses = jekaPsilasses.stream().map(psiClass ->
                JekaCommandClass.fromPsiClass(result, psiClass)).collect(Collectors.toList());
        result.getCommandClasses().addAll(jekaCommandClasses);
        return result;
    }

}

