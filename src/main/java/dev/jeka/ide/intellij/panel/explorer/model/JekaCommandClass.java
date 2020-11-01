package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class JekaCommandClass implements JekaModelNode {

    private String name;

    private JekaModule jekaModule;

    private List<JekaCommand> commands = new LinkedList<>();

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.FileTypes.JavaClass,
                this::getName, this::getJekaModule, this::getCommands);
    }
}
