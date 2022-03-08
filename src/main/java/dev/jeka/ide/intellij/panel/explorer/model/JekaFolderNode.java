package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import dev.jeka.ide.intellij.common.ModuleHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


public class JekaFolderNode extends JekaAbstractModelNode {

    @Getter
    private final Path folderPath;

    private final List<JekaFolderNode> subFolders = new LinkedList<>();

    @Getter(AccessLevel.PACKAGE)
    private Module module;  // null if it is not a module

    @Getter(AccessLevel.PUBLIC)
    private JekaModuleContainer jekaModuleContainer; // null if it is not a module

    JekaFolderNode(Project project, Path folderPath) {
        super(project);
        this.folderPath = folderPath;
    }

    private JekaFolderNode(JekaFolderNode parent, Path folderPath) {
        super(parent);
        this.folderPath = folderPath;
    }

    @Override
    protected NodeDescriptor<? extends JekaAbstractModelNode> makeNodeDescriptor() {
        Icon icon = jekaModuleContainer == null ? AllIcons.Nodes.Folder : AllIcons.Nodes.ConfigFolder;
        return basicNodeDescriptor(icon, getName());
    }

    @Override
    public List<JekaAbstractModelNode> getChildren() {
        List<JekaAbstractModelNode> children = new LinkedList<>();
        if (jekaModuleContainer != null) {
            children.addAll(JekaCmdNode.children(this, folderPath));
            children.addAll(jekaModuleContainer.getBeanNodes());
        }
        children.addAll(subFolders);
        return children;
    }

    private String getName() {
        return module != null ? module.getName() : folderPath.getFileName().toString();
    }

    void createJekaFolderAsDescendant(Module module) {
        Path modulePath = ModuleHelper.getModuleDirPath(module);
        Path relativePath = this.folderPath.relativize(modulePath);
        if (relativePath.getNameCount() == 1 && relativePath.getName(0).toString().equals("")) {
            this.module = module;
            JekaModuleContainer jekaModuleContainer = ModuleHelper.isJekaModule(module) ?
                    JekaModuleContainer.fromModule(this, module)
                    : null;
            this.jekaModuleContainer = jekaModuleContainer;
            return;
        }
        Path nextPath = relativePath.getName(0);
        Path childPath = this.folderPath.resolve(nextPath);
        for (JekaFolderNode subFolder : subFolders) {
            if (subFolder.folderPath.equals(childPath)) {
                subFolder.createJekaFolderAsDescendant(module);
                return;
            }
        }
        JekaFolderNode newChild = new JekaFolderNode(this, childPath);
        newChild.createJekaFolderAsDescendant(module);
        this.subFolders.add(newChild);
        if (subFolders.size() > 2) {
            Collections.sort(this.subFolders, Comparator.comparing(JekaFolderNode::getName));
        }
    }

    Stream<JekaModuleContainer> moduleStream() {
        List<JekaFolderNode> folders= new LinkedList<>();
        folders.add(this);
        folders.addAll(subFolders);
        return folders.stream()
                .filter(folder -> folder.jekaModuleContainer != null)
                .flatMap(folder -> folder.moduleStream());
    }

    List<JekaFolderNode> recursiveFolders() {
        List<JekaFolderNode> result = new LinkedList<>();
        result.add(this);
        result.addAll(subFolders);
        return result;
    }


}
