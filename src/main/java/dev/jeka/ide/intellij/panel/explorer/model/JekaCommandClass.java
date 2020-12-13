package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import dev.jeka.ide.intellij.common.PsiMethodHelper;
import lombok.*;

import java.lang.reflect.Method;
import java.util.*;

@RequiredArgsConstructor
@Getter
public class JekaCommandClass implements JekaModelNode {

    private final PsiClass psiClass;

    private final JekaFolder parent;

    private volatile List<JekaCommand> cachedCommands;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Nodes.Class,
                () -> psiClass.getName(), this::getParent, this::jekaCommands);
    }

    static JekaCommandClass fromPsiClass(JekaFolder parent, PsiClass psiClass) {
        JekaCommandClass result =  new JekaCommandClass(psiClass, parent);
        return result;
    }

    void invalidateCommands() {
        cachedCommands = null;
    }

    private List<JekaCommand> jekaCommands() {
        if (cachedCommands != null) {
            return cachedCommands;
        }
        PsiMethod[] methods = psiClass.getAllMethods();
        List<JekaCommand> commands = new LinkedList<>();
        Set<String> methodNames = new HashSet<>();
        for (PsiMethod method : methods) {
            if (PsiMethodHelper.isJekaCommand(method)) {
                String methodName = method.getName();
                commands.add(new JekaCommand(this, methodName, method));
                methodNames.add(methodName);
            }
        }
        cachedCommands = commands;
        return commands;
    }
}
