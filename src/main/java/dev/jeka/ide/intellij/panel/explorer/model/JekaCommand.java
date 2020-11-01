package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import lombok.Getter;

import java.util.Collections;

@Getter
public class JekaCommand implements JekaModelNode {

    private JekaCommandClass buildClass;

    private String name;

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Actions.Run_anything,
                this::getName, this::getBuildClass, () -> Collections.emptyList());
    }

}
