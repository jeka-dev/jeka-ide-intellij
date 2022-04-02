// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.jeka.ide.intellij.panel.explorer;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.tree.TreeUtil;
import dev.jeka.ide.intellij.action.RefreshWindowToolAction;
import dev.jeka.ide.intellij.action.SyncAllImlAction;
import dev.jeka.ide.intellij.panel.explorer.tree.AbstractNode;
import dev.jeka.ide.intellij.panel.explorer.tree.JekaToolWindowTreeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;


public class JekaExplorerPanel extends SimpleToolWindowPanel {

    private final Tree tree;

    private final Project project;

    public JekaExplorerPanel(Project project) {
        super(true, true);
        this.project = project;
        this.tree = project.getService(JekaToolWindowTreeService.class).getTree();

        setupActions();
        setContent(ScrollPaneFactory.createScrollPane(tree));

        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360006504879-Add-an-action-buttons-to-my-custom-tool-window
        final ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = new DefaultActionGroup("ACTION_GROUP", false);
        actionGroup.add(SyncAllImlAction.INSTANCE);
        actionGroup.add(RefreshWindowToolAction.INSTANCE);
        ActionToolbar actionToolbar = actionManager.createActionToolbar("ACTION_TOOLBAR", actionGroup, true);
        actionToolbar.setTargetComponent(this);  // https://youtrack.jetbrains.com/issue/MPS-33808
        actionToolbar.setOrientation(SwingConstants.HORIZONTAL);
        this.setToolbar(actionToolbar.getComponent());
    }

    private void setupActions() {
        TreeUtil.installActions(tree);
        new TreeSpeedSearch(tree);
        tree.addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(final Component comp, final int x, final int y) {
                //popupInvoked(tree, comp, x, y);
            }
        });
        new EditSourceOnDoubleClickHandler.TreeMouseListener(tree, null) {
            @Override
            protected void processDoubleClick(@NotNull MouseEvent e, @NotNull DataContext dataContext, @NotNull TreePath treePath) {
                //doubleClick(tree, e.getX(), e.getY());
            }
        }.installOn(tree);
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        TreePath treePath = tree.getSelectionModel().getLeadSelectionPath();
        if (treePath == null) {
            return super.getData(dataId);
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        if (node == null || !(node instanceof AbstractNode)) {
            return super.getData(dataId);
        }
        AbstractNode abstractNode = (AbstractNode) node;
        return Optional.ofNullable(abstractNode.getActionData(dataId)).orElseGet(() -> super.getData(dataId));
    }


}
