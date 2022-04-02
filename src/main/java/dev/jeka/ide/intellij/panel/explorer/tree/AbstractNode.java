package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.ColoredTreeCellRenderer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

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

    protected final <T> T getCloserParentOfType(Class<T> clazz) {
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

    protected void onFileEvent(VFileEvent fileEvent) {
    }

    protected void refresh() {
        RootNode rootNode = (RootNode) getRoot();
        DefaultTreeModel treeModel = rootNode.getDefaultTreeModel();
        treeModel.nodeStructureChanged(this);
    }

}
