package dev.jeka.ide.intellij.action;

import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import dev.jeka.ide.intellij.common.Constants;
import dev.jeka.ide.intellij.common.ModuleHelper;
import org.intellij.lang.annotations.JdkConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;


public class JekaRunMethodAction extends AnAction {

    public static final JekaRunMethodAction RUN_JEKA_INSTANCE = new JekaRunMethodAction(false);

    public static final JekaRunMethodAction DEBUG_JEKA_INSTANCE = new JekaRunMethodAction(true);

    private final boolean debug;

    private JekaRunMethodAction(boolean debug) {
        super((debug ? "Debug" : "Run") +   " as Jeka command",
                (debug ? "Debug" : "Run") +   " as Jeka command",
                debug ? AllIcons.Actions.StartDebugger : AllIcons.RunConfigurations.TestState.Run);
        this.debug = debug;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        PsiLocation<PsiIdentifier> location = (PsiLocation<PsiIdentifier>)
                event.getDataContext().getData(Location.DATA_KEY);
        PsiMethod psiMethod = (PsiMethod) location.getPsiElement().getParent();
        String methodName = psiMethod.getName();
        String className = psiMethod.getContainingClass().getName();
        String name = location.getModule().getName() + " [jeka " + methodName + "]";
        ApplicationConfiguration configuration = new ApplicationConfiguration(name, project);
        configuration.setWorkingDirectory("$MODULE_WORKING_DIR$");
        configuration.setMainClassName("dev.jeka.core.tool.Main");
        configuration.setModule(ModuleHelper.getModule(event));
        configuration.setProgramParameters("-CC=" + className + " " + methodName);
        configuration.setBeforeRunTasks(Collections.emptyList());
        RunnerAndConfigurationSettings runnerAndConfigurationSettings =
                RunManager.getInstance(project).createConfiguration(configuration, configuration.getFactory());
        runnerAndConfigurationSettings.getConfiguration().setBeforeRunTasks(Collections.emptyList());
        Executor executor = debug ?  DefaultDebugExecutor.getDebugExecutorInstance() :
                DefaultRunExecutor.getRunExecutorInstance();
        RunManager.getInstance(project).addConfiguration(runnerAndConfigurationSettings);
        RunManager.getInstance(project).setSelectedConfiguration(runnerAndConfigurationSettings);
        ProgramRunnerUtil.executeConfiguration(runnerAndConfigurationSettings, executor);
    }

}
