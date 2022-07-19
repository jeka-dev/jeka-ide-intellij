package dev.jeka.ide.intellij.extension.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.panel.RunDialogWrapper;
import org.jetbrains.annotations.NotNull;

public class JekaRunMethodParamAction extends AnAction {

    public static final JekaRunMethodParamAction RUN_JEKA_INSTANCE = new JekaRunMethodParamAction(false);

    public static final JekaRunMethodParamAction DEBUG_JEKA_INSTANCE = new JekaRunMethodParamAction(true);

    private final boolean debug;

    private JekaRunMethodParamAction(boolean debug) {
        super((debug ? "Debug" : "Run") +   " method ...",
                (debug ? "Debug " : "Run") +   " method ...",
                debug ? AllIcons.Actions.StartDebugger : AllIcons.RunConfigurations.TestState.Run);
        this.debug = debug;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ApplicationManager.getApplication().runReadAction(() -> {
            JekaRunMethodAction.CallContext callContext = JekaRunMethodAction.getCallContext(event);
            String configurationName = callContext.toConfigName();
            RunDialogWrapper runDialogWrapper = new RunDialogWrapper(callContext.getModule(), debug, callContext.cmd(),
                    configurationName);
            runDialogWrapper.show();
        });
    }

}
