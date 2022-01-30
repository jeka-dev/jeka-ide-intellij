package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.util.ui.UIUtil;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public abstract class JekaAbstractModelNode {

    @Getter
    private final Project project;

    @Getter
    private final JekaAbstractModelNode parent;

    private NodeDescriptor nodeDescriptor;

    protected JekaAbstractModelNode(Project project) {
        this.parent = null;
        this.project = project;
    }

    protected JekaAbstractModelNode(JekaAbstractModelNode parent) {
        this.project = parent.project;
        this.parent = parent;
    }

    public NodeDescriptor getNodeDescriptor() {
        if (nodeDescriptor == null) {
            nodeDescriptor = makeNodeDescriptor();
        }
        return nodeDescriptor;
    }

    protected abstract NodeDescriptor<? extends JekaAbstractModelNode> makeNodeDescriptor();

    public abstract List<JekaAbstractModelNode> getChildren();

    BasicNodeDescriptor basicNodeDescriptor(Icon icon, String text) {
        NodeDescriptor parentNodeDescriptor = parent == null ? null : parent.getNodeDescriptor();
        return new BasicNodeDescriptor(project, parentNodeDescriptor, this, icon, text);
    }

    final class BasicNodeDescriptor extends NodeDescriptor {

        private final Object element;

        private final String text;

        BasicNodeDescriptor(final Project project, final NodeDescriptor parent, final Object element, Icon icon,
                            String text) {
            super(project, parent);
            this.element = element;
            this.text = text;
            this.myName = text;
            this.setIcon(icon);
        }

        @Override
        public Object getElement() {
            return element;
        }


        @Override
        public boolean update() {
            CompositeAppearance myHighlightedText = new CompositeAppearance();
            final Color color = UIUtil.getLabelForeground();
            TextAttributes nameAttributes = new TextAttributes(color, null, null,
                    EffectType.BOXED, Font.PLAIN);
            myHighlightedText.getEnding().addText(myName, nameAttributes);
            return true;
        }

    }

}
