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
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.extension.action.ScaffoldAction;
import dev.jeka.ide.intellij.extension.action.SyncImlAction;
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
    protected void onFileEvents(List<? extends VFileEvent> fileEvents) {
        Path modulePath = ModuleHelper.getModuleDirPath(module);
        Path jekaDirPath = modulePath.resolve(JkConstants.JEKA_DIR);
        boolean mustRecompute = fileEvents.stream()
                .filter(fileEvent -> fileEvent.getFileSystem().getProtocol().equals("file"))
                .map(fileEvent -> fileEvent.getFile().toNioPath())
                .anyMatch(file -> mustRecompute(jekaDirPath, file));
        if (mustRecompute) {
            removeAllChildren();
            DumbService.getInstance(project).smartInvokeLater(() -> {
                SlowOperations.allowSlowOperations(() -> {
                    removeAllChildren();
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

    private static boolean mustRecompute(Path jekaDir, Path file) {
        if (!file.startsWith(jekaDir)) {
            return false;
        }
        if (file.startsWith(jekaDir.resolve(JkConstants.CMD_PROPERTIES))) {
            return true;
        }
        if (file.startsWith(jekaDir.getParent().resolve(JkConstants.DEF_DIR))
                && (file.toString().endsWith(".java") || file.toString().endsWith(".kt"))) {
            return true;
        }
        if (file.equals(jekaDir.resolve(JkConstants.WORK_PATH).resolve("kbean-classes.txt"))) {
            return true;
        }
        return false;
    }

    List<String> kbeans() {
        if (children == null) {
            return new LinkedList<>();
        }
        List<String> defBeans = this.children.stream()
                .filter(BeanNode.class::isInstance)
                .map(BeanNode.class::cast)
                .map(beanNode -> beanNode.getName())
                .collect(Collectors.toList());
        List<String> availableBeans = this.children.stream()
                .filter(BeanBoxNode.class::isInstance)
                .map(BeanBoxNode.class::cast)
                .flatMap(beanBoxNode -> beanBoxNode.beans().stream())
                .collect(Collectors.toList());
        return JkUtilsIterable.concatLists(defBeans, availableBeans);
    }
}
