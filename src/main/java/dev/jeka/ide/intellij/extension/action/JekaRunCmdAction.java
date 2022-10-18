package dev.jeka.ide.intellij.extension.action;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.icons.AllIcons;
import com.intellij.lang.properties.psi.impl.PropertyKeyImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
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
        CmdInfo data = getCmdInfo(event);
        String cmd = data.command;
        ConfigurationRunner.run(data.module, null, cmd, debug);
    }

    @Value
    public static class CmdInfo {

        public static final DataKey<CmdInfo> KEY = DataKey.create(CmdInfo.class.getName());

        String name;
        String command;

        Module module;

    }

    static CmdInfo getCmdInfo(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        CmdInfo data = CmdInfo.KEY.getData(dataContext);
        if (data != null) {
            return data;
        }
        PsiLocation<PsiElement> location = (PsiLocation<PsiElement>) dataContext.getData(Location.DATA_KEY);
        if (location.getPsiElement() instanceof PropertyKeyImpl) {
            PsiElement namePsiEl = location.getPsiElement();
            PsiElement equals = namePsiEl.getNextSibling();
            String value = equals.getNextSibling().getText();
            return new CmdInfo(namePsiEl.getText(), value, location.getModule());
        }
        throw new IllegalStateException("Can not find info from PsiElement " + location.getPsiElement());
    }
}
