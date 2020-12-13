// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.jeka.ide.intellij.panel.explorer;

import com.intellij.ide.DataManager;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.tree.TreeUtil;
import dev.jeka.ide.intellij.action.JekaRunMethodAction;
import dev.jeka.ide.intellij.common.data.ModuleAndMethod;
import dev.jeka.ide.intellij.panel.explorer.model.JekaCommand;
import dev.jeka.ide.intellij.panel.explorer.model.JekaRootManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;


public class JekaExplorerPanel extends SimpleToolWindowPanel implements Disposable {

    private final JekaRootManager jekaRootManager;

    private final StructureTreeModel structureTreeModel;

    private final Tree tree;

    public JekaExplorerPanel(Project project) {
        super(true, true);
        this.jekaRootManager = new JekaRootManager(project);
        JekaExplorerTreeStructure treeStructure = new JekaExplorerTreeStructure(this.jekaRootManager);
        this.structureTreeModel = new StructureTreeModel(treeStructure, this);
        this.jekaRootManager.addChangeListener(this::refreshTree);
        this.tree = new Tree(new AsyncTreeModel(this.structureTreeModel, this));
        this.tree.setRootVisible(false);
        this.tree.setShowsRootHandles(true);
        setupActions();
        setContent(ScrollPaneFactory.createScrollPane(tree));
        DumbService.getInstance(project).smartInvokeLater(this::populateTree);
    }

    private void populateTree() {
        jekaRootManager.init();
    }

    public void refreshTree() {
        tree.invalidate();
        structureTreeModel.invalidate();
    }

    @Override
    public void dispose() {
        jekaRootManager.removeChangeListener(this::refreshTree);
        jekaRootManager.dispose();
    }

    private void setupActions() {
        TreeUtil.installActions(tree);
        new TreeSpeedSearch(tree);
        tree.addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(final Component comp, final int x, final int y) {
                popupInvoked(tree, comp, x, y);
            }
        });
        new EditSourceOnDoubleClickHandler.TreeMouseListener(tree, null) {
            @Override
            protected void processDoubleClick(@NotNull MouseEvent e, @NotNull DataContext dataContext, @NotNull TreePath treePath) {
                doubleClick(tree, e.getX(), e.getY());
            }
        }.installOn(tree);
    }

    private static void doubleClick(Tree tree, final int x, final int y) {
        Object userObject = null;
        final TreePath path = tree.getSelectionPath();
        if (path != null) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node != null) {
                userObject = node.getUserObject();
            }
        }
        NodeDescriptor nodeDescriptor = (NodeDescriptor) userObject;
        if (nodeDescriptor.getElement() instanceof JekaCommand) {
            AnActionEvent actionEvent = new AnActionEvent(null, DataManager.getInstance().getDataContext(tree),
                    ActionPlaces.UNKNOWN, new Presentation(), ActionManager.getInstance(), 0);
            JekaRunMethodAction.RUN_JEKA_INSTANCE.actionPerformed(actionEvent);
        }
    }

    private static void popupInvoked(Tree tree, final Component comp, final int x, final int y) {
        Object userObject = null;
        final TreePath path = tree.getSelectionPath();
        if (path != null) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node != null) {
                userObject = node.getUserObject();
            }
        }
        NodeDescriptor nodeDescriptor = (NodeDescriptor) userObject;
        final DefaultActionGroup group = new DefaultActionGroup();
        if (nodeDescriptor.getElement() instanceof JekaCommand) {
            group.add(JekaRunMethodAction.RUN_JEKA_INSTANCE);
            group.add(JekaRunMethodAction.DEBUG_JEKA_INSTANCE);
            group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
        }
        final ActionPopupMenu popupMenu = ActionManager.getInstance()
                .createActionPopupMenu(ActionPlaces.ANT_EXPLORER_POPUP, group);
        popupMenu.getComponent().show(comp, x, y);
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        if (CommonDataKeys.NAVIGATABLE.is(dataId) || ModuleAndMethod.KEY.is(dataId)) {
            TreePath treePath = tree.getSelectionModel().getLeadSelectionPath();
            if (treePath == null) {
                return null;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            NodeDescriptor nodeDescriptor = (NodeDescriptor) node.getUserObject();
            Object element = nodeDescriptor.getElement();
            if (element instanceof JekaCommand) {
                JekaCommand command = (JekaCommand) element;
                if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
                    return command.getPsiMethod();
                }
                return new ModuleAndMethod(command.getBuildClass().getParent().getModule(), command.getPsiMethod());
            }
        }
        return super.getData(dataId);
    }
}
