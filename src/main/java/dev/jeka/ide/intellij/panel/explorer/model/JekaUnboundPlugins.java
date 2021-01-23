package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class JekaUnboundPlugins implements JekaModelNode {

    @Getter
    private final JekaCommandClass  parent;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Nodes.Plugin,
                () -> "Unbound Plugins", this::getParent, this::getChildren);
    }

    private List<? extends JekaModelNode> getChildren() {
        return unboundPlugins();
    }

    private List<JekaPlugin> unboundPlugins() {
        JekaFolder jekaFolder = (JekaFolder) (getParent().getParent());
        if (jekaFolder.getJekaModule() == null) {
            return Collections.emptyList();
        }
        Collection<PsiClass> psiClasses = jekaFolder.getJekaModule().getCachedPluginClasses();
        List<JekaPlugin> result = new LinkedList<>();
        for (PsiClass psiClass : psiClasses) {
            JekaPlugin plugin = JekaPlugin.fromPsiClass(this, psiClass);
            result.add(plugin);
        }
        return result;
    }

}
