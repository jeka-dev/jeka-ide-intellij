// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.jeka.ide.intellij.panel.explorer;

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.JBColor;
import dev.jeka.ide.intellij.panel.explorer.model.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class JekaExplorerTreeStructure extends AbstractTreeStructure {

    private static final Logger LOG = Logger.getInstance(JekaExplorerTreeStructure.class);

    private final Project myProject;

    private final Object myRoot = new Object();

    private final JekaRootManager jekaProjects;

    private boolean initialized;

    JekaExplorerTreeStructure(final Project project) {
        myProject = project;
        jekaProjects = new JekaRootManager(project);
    }

    @Override
    public boolean isToBuildChildrenInBackground(@NotNull final Object element) {
        return true;
    }

    @Override
    public boolean isAlwaysLeaf(@NotNull Object element) {
        return element instanceof JekaCommand;
    }

    @Override
    @NotNull
    public NodeDescriptor createDescriptor(@NotNull Object element, NodeDescriptor parentDescriptor) {
        if (element == myRoot) {
            return new RootNodeDescriptor(myProject, parentDescriptor);
        }
        if (element instanceof String) {
            return new TextInfoNodeDescriptor(myProject, parentDescriptor, (String) element);
        }
        if (element instanceof JekaModelNode) {
            return ((JekaModelNode) element).getNodeInfo().getNodeDescriptor(myProject, parentDescriptor);
        }
        LOG.error("Unknown element for this tree structure " + element);
        return null;
    }

    @Override
    @NotNull
    public Object[] getChildElements(@NotNull Object element) {
        if (element == myRoot) {
            if (!initialized) {
                return new Object[]{"Jeka is initialising project..."};
            }
            return jekaProjects.getJekaModules().toArray(new JekaModule[0]);
        }
        if (element instanceof JekaModelNode) {
            return ((JekaModelNode) element).getNodeInfo().getChildren().toArray(new JekaCommandClass[0]);
        }
        return new Object[0];
    }

    @Override
    @Nullable
    public Object getParentElement(@NotNull Object element) {
        if (element instanceof JekaModelNode) {
            return ((JekaModelNode) element).getNodeInfo().getParent();
        }
        return null;
    }

    @Override
    public void commit() {
        PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    }

    @Override
    public boolean hasSomethingToCommit() {
        return PsiDocumentManager.getInstance(myProject).hasUncommitedDocuments();
    }

    @NotNull
    @Override
    public ActionCallback asyncCommit() {
        return asyncCommitDocuments(myProject);
    }

    @NotNull
    @Override
    public Object getRootElement() {
        return myRoot;
    }

    private final class RootNodeDescriptor extends NodeDescriptor {
        RootNodeDescriptor(Project project, NodeDescriptor parentDescriptor) {
            super(project, parentDescriptor);
        }

        @Override
        public Object getElement() {
            return myRoot;
        }

        @Override
        public boolean update() {
            myName = "";
            return false;
        }
    }

    private static final class TextInfoNodeDescriptor extends NodeDescriptor {

        TextInfoNodeDescriptor(Project project, NodeDescriptor parentDescriptor, @Nls String text) {
            super(project, parentDescriptor);
            myName = text;
            myColor = JBColor.blue;
        }

        @Override
        public Object getElement() {
            return myName;
        }

        @Override
        public boolean update() {
            return true;
        }
    }
}
