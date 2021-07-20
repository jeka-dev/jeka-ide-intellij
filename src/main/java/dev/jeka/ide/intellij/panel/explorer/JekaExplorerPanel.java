// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.jeka.ide.intellij.panel.explorer;

import com.intellij.ide.DataManager;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.content.Content;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.tree.TreeUtil;
import dev.jeka.ide.intellij.action.JekaRunMethodAction;
import dev.jeka.ide.intellij.action.SyncAllImlAction;
import dev.jeka.ide.intellij.action.SyncImlAction;
import dev.jeka.ide.intellij.common.data.CommandInfo;
import dev.jeka.ide.intellij.panel.explorer.action.RootAndJekaFolder;
import dev.jeka.ide.intellij.panel.explorer.action.ShowRuntimeInformationAction;
import dev.jeka.ide.intellij.panel.explorer.model.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;


public class JekaExplorerPanel extends SimpleToolWindowPanel implements Disposable {

    @Getter
    private final JekaRootManager jekaRootManager;

    private final StructureTreeModel structureTreeModel;

    private final Tree tree;

    public JekaExplorerPanel(Project project) {
        super(true, true);
        this.jekaRootManager = new JekaRootManager(project);
        Disposer.register(this, jekaRootManager);
        JekaExplorerTreeStructure treeStructure = new JekaExplorerTreeStructure(this.jekaRootManager);
        this.structureTreeModel = new StructureTreeModel(treeStructure, this);
        this.jekaRootManager.addChangeListener(this::refreshTree);
        this.tree = new Tree(new AsyncTreeModel(this.structureTreeModel, this));
        this.tree.setRootVisible(false);
        this.tree.setShowsRootHandles(true);
        setupActions();
        setContent(ScrollPaneFactory.createScrollPane(tree));
        DumbService.getInstance(project).smartInvokeLater(this::populateTree);

        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360006504879-Add-an-action-buttons-to-my-custom-tool-window
        final ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = new DefaultActionGroup("ACTION_GROUP", false);
        actionGroup.add(SyncAllImlAction.INSTANCE);
        ActionToolbar actionToolbar = actionManager.createActionToolbar("ACTION_TOOLBAR", actionGroup, true);
        actionToolbar.setOrientation(SwingConstants.HORIZONTAL);
        this.setToolbar(actionToolbar.getComponent());
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
        System.out.println(this.getClass() + " disposed !!!!!!!!!!!!");
        jekaRootManager.removeChangeListener(this::refreshTree);
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
        if (nodeDescriptor.getElement() instanceof JekaCommandNode) {
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
        if (nodeDescriptor.getElement() instanceof JekaCommandNode) {
            group.add(JekaRunMethodAction.RUN_JEKA_INSTANCE);
            group.add(JekaRunMethodAction.DEBUG_JEKA_INSTANCE);
            group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));

        } else if (nodeDescriptor.getElement() instanceof JekaFieldNode) {
            group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));

        } else if (nodeDescriptor.getElement() instanceof JekaCommandHolderNode) {
            group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));

        } else if (nodeDescriptor.getElement() instanceof JekaFolderNode) {
            JekaFolderNode jekaFolder = (JekaFolderNode) nodeDescriptor.getElement();
            if (jekaFolder.getJekaModuleContainer() != null) {
                group.add(SyncImlAction.INSTANCE);
                group.add(ShowRuntimeInformationAction.INSTANCE);
            }
        }
        final ActionPopupMenu popupMenu = ActionManager.getInstance()
                .createActionPopupMenu(ActionPlaces.ANT_EXPLORER_POPUP, group);
        popupMenu.getComponent().show(comp, x, y);
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        if (CommonDataKeys.NAVIGATABLE.is(dataId)
                || CommandInfo.KEY.is(dataId)
                || CommonDataKeys.VIRTUAL_FILE.is(dataId)
                || RootAndJekaFolder.DATA_KEY.is(dataId)) {
            TreePath treePath = tree.getSelectionModel().getLeadSelectionPath();
            if (treePath == null) {
                return null;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            NodeDescriptor nodeDescriptor = (NodeDescriptor) node.getUserObject();
            Object element = nodeDescriptor.getElement();
            if (element instanceof JekaCommandNode) {
                JekaCommandNode command = (JekaCommandNode) element;
                if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
                    return command.getPsiMethod();
                }
                if (CommandInfo.KEY.is(dataId)) {
                    JekaModelNode parent = command.getHolder();
                    String pluginName = null;
                    if (parent instanceof JekaPluginNode) {
                        JekaPluginNode jekaPlugin = (JekaPluginNode) parent;
                        pluginName = jekaPlugin.getPluginName();
                    }
                    return new CommandInfo(command.getHolder().getModule(),
                            command.getHolder().getCommandClass(), pluginName, command.getPsiMethod().getName());
                }
            }
            if (element instanceof JekaFieldNode) {
                JekaFieldNode jekaField = (JekaFieldNode) element;
                if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
                    return jekaField.getField();
                }
            }
            if (element instanceof JekaCommandHolderNode) {
                JekaCommandHolderNode holder = (JekaCommandHolderNode) element;
                if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
                    return holder.getContainingClass();
                }
            }
            if (element instanceof JekaFolderNode) {
                JekaFolderNode folder = (JekaFolderNode) element;
                if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
                    VirtualFile virtualFile =
                            LocalFileSystem.getInstance().findFileByIoFile(folder.getFolderPath().toFile());
                    if (virtualFile != null) {
                        return virtualFile;
                    }
                }
                if (RootAndJekaFolder.DATA_KEY.is(dataId)) {
                    return new RootAndJekaFolder(jekaRootManager, folder);
                }
            }
        }
        return super.getData(dataId);
    }
}
