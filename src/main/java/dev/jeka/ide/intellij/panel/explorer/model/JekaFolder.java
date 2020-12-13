package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import dev.jeka.ide.intellij.common.ModuleHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.batik.parser.PathHandler;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Getter
public class JekaFolder implements JekaModelNode {

    private final JekaFolder parent;

    private final Path folderPath;

    private final List<JekaFolder> subFolders = new LinkedList<>();

    private Module module;

    private JekaModule jekaModule; // null if it is not a module

    static JekaFolder ofSimpleDir(JekaFolder parent, Path folderPath) {
        return new JekaFolder(parent, folderPath);
    }

    @Override
    public NodeInfo getNodeInfo() {
        Icon icon = jekaModule == null ? AllIcons.Nodes.Folder : AllIcons.Nodes.ConfigFolder;
        List<JekaModelNode> children = new LinkedList<>();
        if (jekaModule != null) {
            children.addAll(jekaModule.getCommandClasses());
        }
        children.addAll(subFolders);
        return NodeInfo.simple(this, icon, this::getName, this::getParent, () -> children);
    }

    private String getName() {
        return module != null ? module.getName() : folderPath.getFileName().toString();
    }

    void createJekaFolderAsDescendant(Module module) {
        Path modulePath = Paths.get(ModuleHelper.getModuleDir(module).getPath());
        Path relativePath = this.folderPath.relativize(modulePath);
        if (relativePath.getNameCount() == 1 && relativePath.getName(0).toString().equals("")) {
            this.module = module;
            JekaModule jekaModule = ModuleHelper.isJekaModule(module) ? JekaModule.fromModule(this, module) : null;
            this.jekaModule = jekaModule;
            return;
        }
        Path nextPath = relativePath.getName(0);
        Path childPath = this.folderPath.resolve(nextPath);
        for (JekaFolder subFolder : subFolders) {
            if (subFolder.getFolderPath().equals(childPath)) {
                subFolder.createJekaFolderAsDescendant(module);
                return;
            }
        }
        JekaFolder newChild = JekaFolder.ofSimpleDir(this, childPath);
        newChild.createJekaFolderAsDescendant(module);
        this.subFolders.add(newChild);
    }

    Stream<JekaModule> moduleStream() {
        List<JekaFolder> folders= new LinkedList<>();
        folders.add(this);
        folders.addAll(subFolders);
        return folders.stream()
                .filter(folder -> folder.jekaModule != null)
                .flatMap(folder -> folder.moduleStream());
    }

}
