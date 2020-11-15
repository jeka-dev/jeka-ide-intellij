// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.jeka.ide.intellij.panel.explorer;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import dev.jeka.ide.intellij.panel.explorer.model.JekaRootManager;


public class JekaExplorerPanel extends SimpleToolWindowPanel implements Disposable {

    private final JekaRootManager jekaRootManager;

    private final StructureTreeModel structureTreeModel;

    public JekaExplorerPanel(Project project) {
        super(true, true);
        this.jekaRootManager = new JekaRootManager(project);
        JekaExplorerTreeStructure treeStructure = new JekaExplorerTreeStructure(this.jekaRootManager);
        this.structureTreeModel = new StructureTreeModel(treeStructure, this);
        this.jekaRootManager.addChangeListener(this::refreshTree);
        Tree tree = new Tree(new AsyncTreeModel(this.structureTreeModel, this));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        setContent(ScrollPaneFactory.createScrollPane(tree));
        DumbService.getInstance(project).smartInvokeLater(this::initTree);
    }

    private void initTree() {
        System.out.println("refreshing jeka panel...");
        jekaRootManager.init();
    }

    public void refreshTree() {
        System.out.println("I am refreshing tree");
        structureTreeModel.invalidate();
    }

    @Override
    public void dispose() {
        jekaRootManager.removeChangeListener(this::initTree);
    }
}
