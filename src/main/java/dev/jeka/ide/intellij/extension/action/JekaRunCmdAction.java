package dev.jeka.ide.intellij.extension.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.module.Module;
import dev.jeka.ide.intellij.engine.ConfigurationRunner;
import lombok.Value;
import org.jetbrains.annotations.NotNull;


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



    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        CmdInfo data = CmdInfo.KEY.getData(dataContext);
        String cmd = "$" + data.cmdName;
        ConfigurationRunner.run(data.module, null, cmd, debug);
    }

    @Value
    public static class CmdInfo {

        public static final DataKey<CmdInfo> KEY = DataKey.create(CmdInfo.class.getName());

        String cmdName;

        Module module;

    }
}
