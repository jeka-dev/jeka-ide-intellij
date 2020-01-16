package dev.jeka.ide.intellij.platform;

import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.PathMacros;
import dev.jeka.core.api.utils.JkUtilsString;

import java.io.File;
import java.util.Set;

public class JekaApplicationInitializedListener implements ApplicationInitializedListener {

    private static final String JEKA_USER_HOME = "JEKA_USER_HOME";

    private static final String JEKA_HOME = "JEKA_HOME";

    @Override
    public void componentsInitialized() {

        // Instantiate jeka group action
        DefaultActionGroup jekaGroup = new ProjectPopupJekaActionGroup();

        // Register jeka group action
        ActionManager actionManager = ActionManager.getInstance();
        actionManager.registerAction(ProjectPopupJekaActionGroup.class.getName(), jekaGroup);

        // Add Jeka group to popup menu
        DefaultActionGroup projectPopupGroup = (DefaultActionGroup) actionManager.getAction("ProjectViewPopupMenu");
        Constraints menuLocation = new Constraints(Anchor.BEFORE, "Maven.GlobalProjectMenu");
        //Constraints menuLocation = new Constraints(Anchor.BEFORE, "MakeModule");
        projectPopupGroup.addAction(jekaGroup, menuLocation);
        projectPopupGroup.addAction(Separator.getInstance(), menuLocation);

        // Add classpath variable
        PathMacros pathMacros = PathMacros.getInstance();
        Set<String> macros = pathMacros.getAllMacroNames();
        if (!macros.contains(JEKA_USER_HOME)) {
            String value = System.getProperty("user.home") + File.separator + ".jeka";
            pathMacros.setMacro(JEKA_USER_HOME, value);
        }
        if (!macros.contains(JEKA_HOME)) {
            String value = System.getenv("JEKA_HOME");
            if (!JkUtilsString.isBlank(value)) {
                pathMacros.setMacro(JEKA_HOME, value);
            }
        }

    }
}
