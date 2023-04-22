package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.treeStructure.Tree;
import lombok.Value;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractNode extends DefaultMutableTreeNode {

    protected final Project project;

    public AbstractNode(Project project) {
        this.project = project;
        this.setUserObject(this);
    }

    @Override
    public String toString() {
        return "Please implement toString method in " + this.getClass().getName();
    }

    public abstract void customizeCellRenderer(ColoredTreeCellRenderer coloredTreeCellRenderer);

    public void fillPopupMenu(DefaultActionGroup group) {
    }

    public void onDoubleClick(DataContext dataContext) {
    }

    public Object getActionData(String dataId) {
        return null;
    }

    public final <T> T getCloserParentOfType(Class<T> clazz) {
        return getCloserParentOfType(clazz, this);
    }

    private static final <T> T getCloserParentOfType(Class<T> clazz, TreeNode treeNode) {
        if (treeNode.getParent() == null) {
            return null;
        }
        if (clazz.isAssignableFrom(treeNode.getParent().getClass())) {
            return (T) treeNode.getParent();
        }
        return getCloserParentOfType(clazz, treeNode.getParent());
    }

    protected void onFileEvents(List<? extends VFileEvent> fileEvents) {
    }

    protected void refresh() {
        RootNode rootNode = (RootNode) getRoot();
        DefaultTreeModel treeModel = rootNode.getDefaultTreeModel();
        treeModel.nodeStructureChanged(this);
    }

    TreePath getTreePath() {
        return new TreePath(this.getPath());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractNode that = (AbstractNode) o;

        return toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    protected List<TreePath> getExpandedTreePath(Tree tree) {
        TreePath treepath = new TreePath(this);
        List<TreePath> result = new LinkedList<>();
        if (tree.isExpanded(treepath)) {
            result.add(treepath);
            children.stream()
                    .map(AbstractNode.class::cast)
                    .flatMap(node -> node.children.stream())
                    .map(AbstractNode.class::cast)
                    .map(AbstractNode::getTreePath)
                    .filter(treePath -> tree.isExpanded(treePath))
                    .forEach(result::add);
        }
        return result;
    }
}
