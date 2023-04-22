package dev.jeka.ide.intellij.extension;

import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.custom.SyntaxTable;
import com.intellij.openapi.fileTypes.impl.AbstractFileType;
import com.intellij.util.Icons;
import dev.jeka.core.api.utils.JkUtilsString;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProjectDependenciesFileType extends AbstractFileType {

    public ProjectDependenciesFileType() {
        super(syntaxTable());
    }

    @Override
    public @NotNull String getDescription() {
        return getName();
    }

    @Override
    public @NotNull String getName() {
        return "Jeka project dependencies txt";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "txt";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.General.TreeHovered;
    }

    private static SyntaxTable syntaxTable() {
        SyntaxTable syntaxTable = new SyntaxTable();
        for (int i = 2; i <= 4; i++) {
            String pipe = JkUtilsString.repeat("=", i);
            syntaxTable.addKeyword3(pipe + " COMPILE " + pipe);
        }
        for (int i = 2; i <= 4; i++) {
            String pipe = JkUtilsString.repeat("=", i);
            syntaxTable.addKeyword1(pipe + " RUNTIME " + pipe);
        }
        for (int i = 2; i <= 4; i++) {
            String pipe = JkUtilsString.repeat("=", i);
            syntaxTable.addKeyword4(pipe + " TEST " + pipe);
        }
        syntaxTable.setLineComment("#");
        syntaxTable.setHexPrefix("-");
        return syntaxTable;
    }

}
