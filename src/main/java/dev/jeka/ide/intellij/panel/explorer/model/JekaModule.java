package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import lombok.Value;

import java.util.LinkedList;
import java.util.List;

@Value
public class JekaModule implements JekaModelNode {

    String name;

    JekaModule parentProject;

    List<JekaCommandClass> buildClasses = new LinkedList<>();

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Modules.EditFolder,
                this::getName, this::getParentProject, this::getBuildClasses);
    }

}
