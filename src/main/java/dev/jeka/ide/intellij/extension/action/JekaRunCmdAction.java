package dev.jeka.ide.intellij.extension.action;

import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ModuleBasedConfigurationOptions;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.engine.ConfigurationRunner;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


public class JekaRunCmdAction extends AnAction {

    public static final JekaRunCmdAction RUN_JEKA_INSTANCE = new JekaRunCmdAction(false);

    public static final JekaRunCmdAction DEBUG_JEKA_INSTANCE = new JekaRunCmdAction(true);

    private final boolean debug;

    private JekaRunCmdAction(boolean debug) {
        super((debug ? "Debug" : "Run") +   " Command",
                (debug ? "Debug" : "Run") +   " Command",
                debug ? AllIcons.Actions.StartDebugger : AllIcons.RunConfigurations.TestState.Run);
        this.debug = debug;
    }

    private static String configurationName(Module module, String cmdName) {
        return module.getName() + " [jeka $" + cmdName + "]";
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        CmdInfo data = CmdInfo.KEY.getData(dataContext);
        String configurationName = configurationName(data.module,  data.cmdName);
        String cmd = "$" + data.cmdName;
        ConfigurationRunner.run(data.module, configurationName, cmd, debug);
    }

    @Value
    public static class CmdInfo {

        public static final DataKey<CmdInfo> KEY = DataKey.create(CmdInfo.class.getName());

        String cmdName;

        Module module;

    }
}
