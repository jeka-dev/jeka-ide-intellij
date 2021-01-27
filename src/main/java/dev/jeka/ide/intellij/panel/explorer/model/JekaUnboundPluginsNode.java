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
public class JekaUnboundPluginsNode implements JekaModelNode {

    @Getter
    private final JekaCommandClassNode parent;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Nodes.Plugin,
                () -> "Unbound Plugins", this::getParent, this::getChildren);
    }

    private List<? extends JekaModelNode> getChildren() {
        return unboundPlugins();
    }

    private List<JekaPluginNode> unboundPlugins() {
        JekaFolderNode jekaFolder = (JekaFolderNode) parent.getParent();
        JekaModuleContainer moduleContainer = jekaFolder.getJekaModuleContainer();
        if (moduleContainer == null) {
            return Collections.emptyList();
        }
        Collection<PsiClass> psiClasses = moduleContainer.getAllPluginClasses();
        List<JekaPluginNode> result = new LinkedList<>();
        List<JekaPluginNode> boundPlugins = parent.pluginsRecursive();
        for (PsiClass psiClass : psiClasses) {
            boolean isBound = boundPlugins.stream()
                    .filter(pluginNode -> pluginNode.getContainingClass().equals(psiClass))
                    .findAny().isPresent();
            if (! isBound) {
                JekaPluginNode plugin = JekaPluginNode.fromPsiClass(this, psiClass);
                result.add(plugin);
            }
        }
        return result;
    }

}
