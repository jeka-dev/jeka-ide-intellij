package dev.jeka.ide.intellij.extension.projectwizard;

import com.intellij.openapi.module.Module;
import dev.jeka.ide.intellij.engine.ScaffoldNature;
import dev.jeka.ide.intellij.panel.ScaffoldFormPanel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.file.Paths;

@Data
class JekaModuleData {

    private String path;

    private ScaffoldNature scaffoldNature;

    private Module wrapperDelegate;

    private String wrapperVersion;
}
