package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.util.SlowOperations;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.ide.intellij.extension.action.ScaffoldAction;
import dev.jeka.ide.intellij.extension.action.SyncImlAction;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.panel.explorer.action.ShowRuntimeInformationAction;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ModuleNode extends AbstractNode {

    @Getter
    private final Module module;

    public ModuleNode(Module module) {
        super(module.getProject());
        this.module = module;
        load();
    }

    private void load() {
        createCmdChildren().forEach(this::add);
        List<BeanNode> beanNodes = createBeanNodes();
        beanNodes.forEach(this::add);
        Set<String> names = beanNodes.stream()
                .map(beanNode -> beanNode.getPsiClass().getQualifiedName())
                .collect(Collectors.toSet());
        this.add(new BeanBoxNode(project, ModuleHelper.getModuleDirPath(module), names));
    }

    @Override
    public String toString() {
        return module.getName();
    }

    @Override
    public void customizeCellRenderer(ColoredTreeCellRenderer cellRenderer) {
        cellRenderer.setIcon(AllIcons.Nodes.ConfigFolder);
    }

    @Override
    public void fillPopupMenu(DefaultActionGroup group) {
        group.add(SyncImlAction.get());
        group.add(ShowRuntimeInformationAction.INSTANCE);
        group.add(ScaffoldAction.get());
    }

    @Override
    public Object getActionData(String dataId) {
        if (module.isDisposed()) {
            return null;
        }
        if (ShowRuntimeInformationAction.DATA_KEY.is(dataId)) {
            return module;
        }
        if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
            VirtualFile virtualFile = ModuleHelper.getModuleDir(module);
            if (virtualFile != null) {
                return virtualFile;
            }
        }
        return null;
    }

    @Nullable
    VirtualFile getCmdfile() {
        VirtualFile moduleDir = ModuleHelper.getModuleDir(module);
        if (moduleDir == null) {
            return null;
        }
        return moduleDir.findChild(JkConstants.JEKA_DIR).findChild(JkConstants.CMD_PROPERTIES);
    }

    List<CmdNode> createCmdChildren() {
        VirtualFile cmdFile = getCmdfile();
        if (cmdFile == null) {
            return Collections.emptyList();
        }
        Document document = FileDocumentManager.getInstance().getDocument(cmdFile);
        return allCommands(document).entrySet().stream()
                .map(entry -> new CmdNode(project, entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(() -> new LinkedList<>()));
    }

    private void removeCmdNodes() {
        if (children == null) {
            return;
        }
        List<CmdNode> cmdNodes = children.stream()
                .filter(CmdNode.class::isInstance)
                .map(CmdNode.class::cast)
                .collect(Collectors.toList());
        cmdNodes.forEach(cmdNode -> this.remove(cmdNode));
    }

    private void removeBeanNodes() {
        if (children == null) {
            return;
        }
        List<BeanNode> nodes = children.stream()
                .filter(BeanNode.class::isInstance)
                .map(BeanNode.class::cast)
                .collect(Collectors.toList());
        nodes.forEach(node -> this.remove(node));
    }

    private static Map<String, String> allCommands(Document document) {
        String content = document.getText();
        Map<String, String> result = new TreeMap<>();
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = (String) entry.getKey();
                if (!key.startsWith("_")) {
                    String value = (String) entry.getValue();
                    result.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }

    @Override
    protected void onFileEvent(List<? extends VFileEvent> fileEvents) {
        Path modulePath = ModuleHelper.getModuleDirPath(module);
        Path jekaDirPath = modulePath.resolve(JkConstants.JEKA_DIR);
        boolean mustRecompute = fileEvents.stream()
                .map(fileEvent -> fileEvent.getFile().toNioPath())
                .anyMatch(file -> mustRecompute(jekaDirPath, file));
        if (mustRecompute) {
            removeAllChildren();
            DumbService.getInstance(project).smartInvokeLater(() -> {
                SlowOperations.allowSlowOperations(() -> {
                    load();
                    refresh();
                });
            });

        }

    }

    private List<BeanNode> createBeanNodes() {
       return PsiClassHelper.findKBeanClasses(module).stream()
                .map(beanClass -> new BeanNode(project, beanClass))
                .collect(Collectors.toList());
    }

    private static boolean mustRecompute(Path jekaFir, Path file) {
        if (!file.startsWith(jekaFir)) {
            return false;
        }
        if (file.startsWith(jekaFir.resolve(JkConstants.CMD_PROPERTIES))) {
            return true;
        }
        if (file.startsWith(jekaFir.resolve(JkConstants.DEF_DIR))
                && (file.endsWith(".java") || file.endsWith(".kotlin"))) {
            return true;
        }
        if (file.equals(jekaFir.resolve(JkConstants.WORK_PATH).resolve("kbean-classes.txt"))) {
            return true;
        }
        return false;
    }
}
