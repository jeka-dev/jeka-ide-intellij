package dev.jeka.ide.intellij.extension.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
        JekaRunMethodAction.CallContext callContext = JekaRunMethodAction.getCallContext(event);
        RunDialogWrapper runDialogWrapper = new RunDialogWrapper(callContext.getModule(), debug, callContext.cmd());
        runDialogWrapper.show();
    }

}
