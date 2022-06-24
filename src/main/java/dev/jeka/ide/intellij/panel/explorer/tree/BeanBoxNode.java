package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import dev.jeka.core.tool.JkBean;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import icons.JekaIcons;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanBoxNode extends AbstractNode {

    private final Path moduleRootPath;

    public BeanBoxNode(Project project, Path moduleRootPath, Set<String> localKBeans) {
        super(project);
        this.moduleRootPath = moduleRootPath;
        createChildren(localKBeans).forEach(this::add);
    }

    @Override
    public String toString() {
        return "Classpath KBeans";
    }

    @Override
    public void customizeCellRenderer(ColoredTreeCellRenderer coloredTreeCellRenderer) {
        coloredTreeCellRenderer.setIcon(JekaIcons.BEAN_BOX);
        coloredTreeCellRenderer.setToolTipText("KBeans present in classpath");
    }

    private List<BeanNode> createChildren(Set<String> localKbeans) {
        return JkExternalToolApi.getCachedBeanClassNames(moduleRootPath).stream()
                .filter(name -> !localKbeans.contains(name))
                .map(className -> PsiClassHelper.getPsiClass(project, className))
                .filter(Objects::nonNull)
                .map(psiClass -> new BeanNode(project, psiClass))
                .sorted()
                .collect(Collectors.toList());
    }

    List<String> beans() {
        if (children == null) {
            return new LinkedList<>();
        }
        return this.children.stream()
                .filter(BeanNode.class::isInstance)
                .map(BeanNode.class::cast)
                .map(beanNode -> beanNode.getName())
                .collect(Collectors.toList());
    }
}
