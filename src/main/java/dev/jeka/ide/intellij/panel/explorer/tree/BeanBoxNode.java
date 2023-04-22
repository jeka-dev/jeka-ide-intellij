package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTreeCellRenderer;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import icons.JekaIcons;

import java.nio.file.Path;
import java.util.*;
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
                .map(psiClass -> new BeanNode(project, psiClass, false))
                .sorted(new BeanComparator())
                .collect(Collectors.toList());
    }

    List<BeanNode> kbeans() {
        if (children == null) {
            return new LinkedList<>();
        }
        return this.children.stream()
                .filter(BeanNode.class::isInstance)
                .map(BeanNode.class::cast)
                .collect(Collectors.toList());
    }

    private static class BeanComparator implements Comparator<BeanNode> {

        @Override
        public int compare(BeanNode bean1, BeanNode bean2) {
           if (BeanNode.UNPRIORIZEDS.contains(bean1.getName())) {
                return 1;
            }
            if (BeanNode.UNPRIORIZEDS.contains(bean2.getName())) {
                return -1;
            }
            return 0;
        }
    }

}
