package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.ModuleHelper;
import icons.JekaIcons;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class JekaCmdNode extends JekaAbstractModelNode {

    @Getter
    private final String cmdName;

    @Getter
    private final VirtualFile file;

    @Getter
    private final Module module;

    public JekaCmdNode(JekaAbstractModelNode parent, Module module, String name, VirtualFile file) {
        super(parent);
        this.cmdName = name;
        this.module = module;
        this.file = file;
    }

    @Override
    protected NodeDescriptor<? extends JekaAbstractModelNode> makeNodeDescriptor() {
        return basicNodeDescriptor(JekaIcons.CMD, cmdName);
    }

    @Override
    public List<JekaAbstractModelNode> getChildren() {
        return Collections.emptyList();
    }


    private static Map<String, String> all(Document document) {
        String content = document.getText();
        Map<String, String> result = new TreeMap<>();
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
            Enumeration<String> e = (Enumeration<String>) properties.propertyNames();
            while (e.hasMoreElements()) {
                System.out.println(e.nextElement());
            }
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

    static List<JekaCmdNode> children(JekaAbstractModelNode parent, Module module, VirtualFile moduleDir) {
        VirtualFile cmdFile = moduleDir.findChild(JkConstants.JEKA_DIR).findChild(JkConstants.CMD_PROPERTIES);
        if (cmdFile == null) {
            return Collections.emptyList();
        }
        Document document = FileDocumentManager.getInstance().getDocument(cmdFile);
        return all(document).entrySet().stream()
                .map(entry -> new JekaCmdNode(parent, module, entry.getKey(), cmdFile))
                .collect(Collectors.toList());
    }
}
