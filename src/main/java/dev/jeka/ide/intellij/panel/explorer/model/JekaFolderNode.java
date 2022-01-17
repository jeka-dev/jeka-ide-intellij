package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import dev.jeka.ide.intellij.common.ModuleHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public class JekaFolderNode implements JekaModelNode {

    private final JekaFolderNode parent;

    private final Path folderPath;

    private final List<JekaFolderNode> subFolders = new LinkedList<>();

    private Module module;

    private JekaModuleContainer jekaModuleContainer; // null if it is not a module

    static JekaFolderNode ofSimpleDir(JekaFolderNode parent, Path folderPath) {
        return new JekaFolderNode(parent, folderPath);
    }

    @Override
    public NodeInfo getNodeInfo() {
        Icon icon = jekaModuleContainer == null ? AllIcons.Nodes.Folder : AllIcons.Nodes.ConfigFolder;
        List<JekaModelNode> children = new LinkedList<>();
        if (jekaModuleContainer != null) {
            children.addAll(jekaModuleContainer.getBeanNodes());
        }
        children.addAll(subFolders);
        return NodeInfo.simple(this, icon, this::getName, this::getParent, () -> children);
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
            if (subFolder.getFolderPath().equals(childPath)) {
                subFolder.createJekaFolderAsDescendant(module);
                return;
            }
        }
        JekaFolderNode newChild = JekaFolderNode.ofSimpleDir(this, childPath);
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
