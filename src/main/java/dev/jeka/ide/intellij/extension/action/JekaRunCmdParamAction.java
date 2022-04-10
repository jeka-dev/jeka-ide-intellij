package dev.jeka.ide.intellij.extension.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.module.Module;
import dev.jeka.ide.intellij.engine.ConfigurationRunner;
import dev.jeka.ide.intellij.panel.RunDialogWrapper;
import lombok.Value;
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
        return module.getName() + " [jeka $" + cmdName + "]";
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        JekaRunCmdAction.CmdInfo data = JekaRunCmdAction.CmdInfo.KEY.getData(dataContext);
        String configurationName = configurationName(data.getModule(),  data.getCmdName());
        String cmd = "$" + data.getCmdName();
        RunDialogWrapper runDialogWrapper = new RunDialogWrapper(data.getModule(), debug, cmd, configurationName);
        runDialogWrapper.show();
    }


}
