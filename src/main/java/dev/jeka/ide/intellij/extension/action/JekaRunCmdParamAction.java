package dev.jeka.ide.intellij.extension.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.ide.intellij.panel.RunDialogWrapper;
import org.jetbrains.annotations.NotNull;


public class JekaRunCmdParamAction extends AnAction {

    public static final JekaRunCmdParamAction RUN_JEKA_INSTANCE = new JekaRunCmdParamAction(false);

    public static final JekaRunCmdParamAction DEBUG_JEKA_INSTANCE = new JekaRunCmdParamAction(true);

    private final boolean debug;

    private JekaRunCmdParamAction(boolean debug) {
        super((debug ? "Debug" : "Run") +   " Command ...",
                (debug ? "Debug" : "Run") +   " Command ...",
                debug ? AllIcons.Actions.StartDebugger : AllIcons.RunConfigurations.TestState.Run);
        this.debug = debug;
    }

    private static String configurationName(Module module, String cmdName) {
        boolean multiModule = ModuleManager.getInstance(module.getProject()).getModules().length > 1;
        String prefix = multiModule ? "[" + module.getName() + "] " : "";
        return prefix + JkConstants.CMD_SUBSTITUTE_SYMBOL + cmdName;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        JekaRunCmdAction.CmdInfo data = JekaRunCmdAction.getCmdInfo(event);
        String configurationName = configurationName(data.getModule(),  data.getName());
        String cmd = data.getInterpolatedCommand();

        RunDialogWrapper runDialogWrapper = new RunDialogWrapper(data.getModule(), debug, cmd, configurationName);
        runDialogWrapper.show();
    }


}
