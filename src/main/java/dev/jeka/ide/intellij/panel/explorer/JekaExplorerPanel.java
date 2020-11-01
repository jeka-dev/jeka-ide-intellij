// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.jeka.ide.intellij.panel.explorer;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;


public class JekaExplorerPanel extends SimpleToolWindowPanel implements Disposable {

    private final Project project;

    private final Tree myTree;

    private final JekaExplorerTreeStructure treeStructure;

    public JekaExplorerPanel(Project project) {
        super(true, true);
        this.project = project;
        this.treeStructure = new JekaExplorerTreeStructure(project);
        StructureTreeModel structureTreeModel = new StructureTreeModel(treeStructure, this);
        myTree = new Tree(new AsyncTreeModel(structureTreeModel, this));
        myTree.setRootVisible(false);
        myTree.setShowsRootHandles(true);
        setContent(ScrollPaneFactory.createScrollPane(myTree));
    }

    @Override
    public void dispose() {

    }
}
