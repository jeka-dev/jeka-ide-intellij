package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.util.SlowOperations;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.tool.JkBean;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.extension.action.ScaffoldAction;
import dev.jeka.ide.intellij.extension.action.SyncImlAction;
import dev.jeka.ide.intellij.panel.explorer.action.ShowRuntimeInformationAction;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

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

    void load() {
        this.removeAllChildren();
        createCmdChildren().forEach(this::add);
        List<BeanNode> beanNodes = createBeanNodes();
        beanNodes.forEach(this::add);
        Set<String> names = beanNodes.stream()
                .map(beanNode -> beanNode.getClassName())
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
    VirtualFile getProjectPropFile() {
        VirtualFile moduleDir = ModuleHelper.getModuleDir(module);
        if (moduleDir == null) {
            return null;
        }
        return moduleDir.findChild(JkConstants.JEKA_DIR).findChild(JkConstants.PROPERTIES_FILE);
    }

    List<CmdNode> createCmdChildren() {
        Path baseDir = ModuleHelper.getModuleDirPath(module);
        Map<String, String> commands = JkExternalToolApi.getCmdShortcutsProperties(baseDir);
        return commands.entrySet().stream()
                .map(entry -> new CmdNode(project, entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(() -> new LinkedList<>()));
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
        final List<BeanNode> result = new LinkedList<>();
        List<BeanNode> localBeans  = PsiClassHelper.findLocalBeanClasses(module).stream()
                .map(beanClass -> new BeanNode(project, beanClass, true))
                .collect(Collectors.toList());
        result.addAll(localBeans);

        // Add the default kbean if any specified
        Path moduleDir = ModuleHelper.getModuleDirPath(module);
        String defaultBean = JkExternalToolApi.getProperties(moduleDir).get("jeka.default.kbean");
        if (!JkUtilsString.isBlank(defaultBean)) {
            List<String> localBeanNames = localBeans.stream().map(BeanNode::getName).toList();
            Optional<BeanNode> defaultBeanNode = JkExternalToolApi.getCachedBeanClassNames(moduleDir).stream()
                    .filter(className -> JkExternalToolApi.kbeanNameMatches(className, defaultBean))
                    .filter(className -> !localBeanNames.contains(className))
                    .map(className -> PsiClassHelper.getPsiClass(project, className))
                    .filter(Objects::nonNull)
                    .map(psiClass -> new BeanNode(project, psiClass, false))
                    .findFirst();
            defaultBeanNode.ifPresent(beanNode -> result.add(beanNode));
        }
        return result;
    }

    private static boolean mustRecompute(Path jekaDir, Path file) {
        if (file.equals(jekaDir.getParent().resolve(JkConstants.WORK_PATH)
                .resolve(JkConstants.KBEAN_CLASSES_CACHE_FILE_NAME))) {
            return true;
        }
        if (!file.startsWith(jekaDir)) {
            return false;
        }
        if (file.startsWith(jekaDir.resolve(JkConstants.PROPERTIES_FILE))) {
            return true;
        }
        if (file.startsWith(jekaDir.getParent().resolve(JkConstants.DEF_DIR))
                && (file.toString().endsWith(".java") || file.toString().endsWith(".kt"))) {
            return true;
        }

        return false;
    }

    List<BeanNode> kbeans() {
        if (children == null) {
            return new LinkedList<>();
        }
        List<BeanNode> defBeans = (List<BeanNode>) this.children.stream()
                .filter(BeanNode.class::isInstance)
                .map(BeanNode.class::cast)
                .collect(Collectors.toList());
        List<BeanNode> classpathBeans = (List<BeanNode>) this.children.stream()
                .filter(BeanBoxNode.class::isInstance)
                .map(BeanBoxNode.class::cast)
                .flatMap(beanBoxNode -> ((BeanBoxNode)beanBoxNode).kbeans().stream())
                .collect(Collectors.toList());
        return JkUtilsIterable.concatLists(defBeans, classpathBeans);
    }

    List<ModuleNode> getDescendantModuleNodes() {
        List<ModuleNode> result = new LinkedList<>();
        for (Object treeNode : children) {
            if (treeNode instanceof ModuleNode) {
                ModuleNode moduleNode = (ModuleNode) treeNode;
                result.add(moduleNode);
                result.addAll(moduleNode.getDescendantModuleNodes());
            }
        }
        return result;
    }
}
