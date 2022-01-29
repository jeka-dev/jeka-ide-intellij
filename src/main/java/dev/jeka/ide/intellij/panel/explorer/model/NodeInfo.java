package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.util.ui.UIUtil;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class NodeInfo {

    @FunctionalInterface
    public interface NodeDescriptorSupplier {

        NodeDescriptor getNodeDescriptor(Project project, NodeDescriptor parentDescriptor);

    }

    private final NodeDescriptorSupplier nodeDescriptorSupplier;

    private final Supplier<JekaModelNode> parentSupplier;

    private final Supplier<List<? extends JekaModelNode>> childrenSupplier;

    static NodeInfo simple(Object element, Icon icon, Supplier<String> textSupplier, Supplier<JekaModelNode> parentSupplier,
                           Supplier<List<? extends JekaModelNode>> childrenSupplier) {
        NodeDescriptorSupplier nodeDescriptorSupplier = (project, nodeDescriptor) ->
           new BasicNodeDescriptor(project, nodeDescriptor, element, icon, textSupplier);
        return new NodeInfo(nodeDescriptorSupplier, parentSupplier, childrenSupplier);
    }

    public NodeDescriptor getNodeDescriptor(Project project, NodeDescriptor parentDescriptor) {
        return nodeDescriptorSupplier.getNodeDescriptor(project, parentDescriptor);
    }

    public JekaModelNode getParent() {
        return parentSupplier.get();
    }

    public List<? extends JekaModelNode> getChildren() {
        return childrenSupplier.get();
    }

    static final class BasicNodeDescriptor extends NodeDescriptor {

        private final Object element;

        private final Supplier<String> textSupplier;

        BasicNodeDescriptor(final Project project, final NodeDescriptor parentDescriptor, final Object element, Icon icon,
                            Supplier<String> textSupplier) {
            super(project, parentDescriptor);
            this.element = element;
            this.textSupplier = textSupplier;
            this.myName = textSupplier.get();
            this.setIcon(icon);
        }

        @Override
        public Object getElement() {
            return element;
        }


        @Override
        public boolean update() {
            if (Objects.equals(textSupplier.get(), myName)) {
              return false;
            }
            myName = textSupplier.get();
            CompositeAppearance myHighlightedText = new CompositeAppearance();
            final Color color = UIUtil.getLabelForeground();
            TextAttributes nameAttributes = new TextAttributes(color, null, null, EffectType.BOXED, Font.PLAIN);
            myHighlightedText.getEnding().addText(myName, nameAttributes);
            return true;
        }

    }
}
