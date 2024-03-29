package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.SlowOperations;
import com.intellij.util.ui.tree.TreeUtil;
import dev.jeka.ide.intellij.common.ModuleHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class RootNode extends AbstractNode {

    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    private DefaultTreeModel defaultTreeModel;

    public RootNode(Project project) {
        super(project);
        this.removeAllChildren();
    }

    private void load() {
        ApplicationManager.getApplication().runReadAction(() -> {
            for (Module module : modulePaths().values()) {
                addModule(module);
            }
        });
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public void customizeCellRenderer(ColoredTreeCellRenderer coloredTreeCellRenderer) {
        // do nothing special as it is not displayed
    }

    void addModule(Module module) {
        List<ModuleNode> addedModuleNodes = (List<ModuleNode>) Optional.ofNullable(children).orElse(new Vector<>()).stream()
                .filter(ModuleNode.class::isInstance)
                .map(ModuleNode.class::cast)
                .collect(Collectors.toCollection(LinkedList::new));
        Collections.reverse(addedModuleNodes);
        boolean found = false;
        for (ModuleNode addedModuleNode : addedModuleNodes) {
            Path addedModulePath = ModuleHelper.getModuleDirPath(addedModuleNode.getModule());
            Path modulePath = ModuleHelper.getModuleDirPath(module);
            if (modulePath.startsWith(addedModulePath)) {
                ModuleNode moduleNode = new ModuleNode(module);
                addedModuleNode.add(moduleNode);
                found = true;
                break;
            }
        }
        if (!found) {
            add(new ModuleNode(module));
        }
    }

    void reloadModules(Tree tree) {
        DumbService.getInstance(project).smartInvokeLater(() -> {
            SlowOperations.allowSlowOperations(() -> {
                List<TreePath> treePaths = null;
                if (tree != null) {
                    treePaths = this.getExpandedTreePath(tree);
                }
                this.removeAllChildren();
                load();
                refresh();
                if (tree != null) {
                    treePaths.forEach(treePath -> TreeUtil.promiseExpand(tree, treePath));
                }

            });
        });
    }

    // find all Jeka modules sorted by paths
    private TreeMap<Path, Module> modulePaths() {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        TreeMap<Path, Module>  sortedModulesMap = new TreeMap<>();
        for (Module module : moduleManager.getModules()) {
            if (ModuleHelper.isJekaModule(module)) {
                sortedModulesMap.put(ModuleHelper.getModuleDirPath(module), module);
            }
        }
        return sortedModulesMap;
    }

    @Override
    protected void onFileEvents(List<? extends VFileEvent> fileEvents) {
        if (children == null) {
            return;
        }
        children.stream()
                .map(AbstractNode.class::cast)
                .forEach(child -> child.onFileEvents(fileEvents));
    }

    ModuleNode getModuleNode(Module module) {
        if (children == null) {
            return null;
        }
        return children.stream()
                .filter(ModuleNode.class::isInstance)
                .map(ModuleNode.class::cast)
                .flatMap(moduleNode -> {
                    List<ModuleNode> moduleNodes = new LinkedList<>();
                    moduleNodes.add(moduleNode);
                    moduleNodes.addAll((moduleNode).getDescendantModuleNodes());
                    return moduleNodes.stream();
                })
                .filter(moduleNode -> (moduleNode).getModule().equals(module))
                .findFirst().orElse(null);
    }



}
