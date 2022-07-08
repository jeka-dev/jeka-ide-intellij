package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.ProjectTopics;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.Function;
import com.intellij.util.SlowOperations;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public final class JekaToolWindowTreeService {

    private final Project project;

    private Tree tree;

    @Setter
    private boolean listen = true;

    public Tree getTree() {
        if (tree == null) {
            tree = createTree();
        }
        return tree;
    }

    private Tree createTree() {
        RootNode rootNode = new RootNode(project);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        rootNode.setDefaultTreeModel(treeModel);
        Tree tree = new Tree(treeModel);
        tree.setRootVisible(false);
        tree.setCellRenderer(new CellRenderer());

        // handle mouse right-click
        tree.addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(final Component comp, final int x, final int y) {
                popupInvoked(tree, comp, x, y);
            }
        });

        // handle double-click
        new EditSourceOnDoubleClickHandler.TreeMouseListener(tree, null) {
            @Override
            protected void processDoubleClick(@NotNull MouseEvent e, @NotNull DataContext dataContext, @NotNull TreePath treePath) {
                doubleClick(tree, e.getX(), e.getY());
            }
        }.installOn(tree);

        // handle VSF change
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {

                @Override
                public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
                    if (!listen) {
                        return;
                    }
                    SlowOperations.allowSlowOperations(() -> rootNode.onFileEvents(events));

                }
        });

        // handle Module change
        project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {

            @Override
            public void moduleAdded(@NotNull Project project, @NotNull Module module) {
                if (!listen) {
                    return;
                }
                reloadAndRefresh();
            }

            @Override
            public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
                if (!listen) {
                    return;
                }
                reloadAndRefresh();
            }

            @Override
            public void modulesRenamed(@NotNull Project project, @NotNull List<? extends Module> modules, @NotNull Function<? super Module, String> oldNameProvider) {
                if (!listen) {
                    return;
                }
                reloadAndRefresh();
            }
        });

        rootNode.reloadModules();
        return tree;
    }

    public void reloadAndRefresh() {
       RootNode rootNode = (RootNode) getTree().getModel().getRoot();
       rootNode.reloadModules();
       rootNode.refresh();
    }

    public static class CellRenderer extends ColoredTreeCellRenderer {

        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected,
                                          boolean expanded, boolean leaf, int row, boolean hasFocus) {
            AbstractNode node = (AbstractNode) value;
            this.append(value.toString());
            node.customizeCellRenderer(this);

        }
    }

    private static void popupInvoked(Tree tree, final Component comp, final int x, final int y) {
        final AbstractNode abstractNode = getSelectedAbstractNode(tree);
        if (abstractNode == null) {
            return;
        }
        final DefaultActionGroup group = new DefaultActionGroup();
        abstractNode.fillPopupMenu(group);
        final ActionPopupMenu popupMenu = ActionManager.getInstance()
                .createActionPopupMenu(ActionPlaces.ANT_EXPLORER_POPUP, group);
        popupMenu.getComponent().show(comp, x, y);
    }

    private static void doubleClick(Tree tree, final int x, final int y) {
        final AbstractNode abstractNode = getSelectedAbstractNode(tree);
        if (abstractNode == null) {
            return;
        }
        DataContext dataContext = DataManager.getInstance().getDataContext(tree);
        abstractNode.onDoubleClick(dataContext);
    }

    private static AbstractNode getSelectedAbstractNode(Tree tree) {
        final TreePath path = tree.getSelectionPath();
        if (path == null) {
            return null;
        }
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (! (node instanceof AbstractNode)) {
            return null;
        }
        return (AbstractNode) node;
    }

    public List<String> getKbeans(Module module) {
        RootNode rootNode = (RootNode) getTree().getModel().getRoot();
        ModuleNode moduleNode = rootNode.getModuleNode(module);
        if (moduleNode == null) {
            return Collections.emptyList();
        }
        return moduleNode.kbeanClassNames();
    }
}
