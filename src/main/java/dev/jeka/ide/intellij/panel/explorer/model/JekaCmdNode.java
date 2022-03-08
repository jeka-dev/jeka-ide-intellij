package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.psi.PsiMethod;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.core.tool.JkExternalToolApi;
import icons.JekaIcons;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class JekaCmdNode extends JekaAbstractModelNode {

    @Getter
    private final String name;

    private final String value;

    public JekaCmdNode(JekaAbstractModelNode parent, String name, String value) {
        super(parent);
        this.name = name;
        this.value = value;
    }

    @Override
    protected NodeDescriptor<? extends JekaAbstractModelNode> makeNodeDescriptor() {
        return basicNodeDescriptor(JekaIcons.CMD, name);
    }

    @Override
    public List<JekaAbstractModelNode> getChildren() {
        return Collections.emptyList();
    }

    static Map<String, String> all(Path moduleDir) {
        Map<String, String> result = new HashMap<>();
        JkExternalToolApi.getCmdPropertiesContent(moduleDir).entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("_"))
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

    static List<JekaCmdNode> children(JekaAbstractModelNode parent, Path moduleDir) {
        return all(moduleDir).entrySet().stream()
                .map(entry -> new JekaCmdNode(parent, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
