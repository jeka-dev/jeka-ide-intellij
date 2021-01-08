package dev.jeka.ide.intellij.action;

import com.google.common.collect.ImmutableList;
import com.intellij.compiler.options.CompileStepBeforeRunNoErrorCheck;
import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.data.CommandInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;


public class JekaRunMethodAction extends AnAction {

    public static final JekaRunMethodAction RUN_JEKA_INSTANCE = new JekaRunMethodAction(false);

    public static final JekaRunMethodAction DEBUG_JEKA_INSTANCE = new JekaRunMethodAction(true);

    private final boolean debug;

    private JekaRunMethodAction(boolean debug) {
        super((debug ? "Debug" : "Run") +   " Command",
                (debug ? "Debug" : "Run") +   " Command",
                debug ? AllIcons.Actions.StartDebugger : AllIcons.RunConfigurations.TestState.Run);
        this.debug = debug;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();
        PsiLocation<PsiIdentifier> location = (PsiLocation<PsiIdentifier>) dataContext.getData(Location.DATA_KEY);
        final String methodName;
        final String simpleClassName;
        final Module module;
        final String pluginName;
        if (location != null) {
            PsiMethod psiMethod = (PsiMethod) location.getPsiElement().getParent();
            methodName = psiMethod.getName();
            simpleClassName = psiMethod.getContainingClass().getName();
            module = ModuleHelper.getModule(event);
            pluginName = null;
        } else {
            CommandInfo commandInfo = CommandInfo.KEY.getData(dataContext);
            if (commandInfo == null) {
                throw new IllegalStateException("Can not find reference to Psi method");
            }
            methodName = commandInfo.getMethodName();
            simpleClassName = commandInfo.getCommandClass().getName();
            module = commandInfo.getModule();
            pluginName = commandInfo.getPluginName();
        }
        String method = pluginName == null ? methodName : pluginName + "#" + methodName;
        String name = module.getName() + " [jeka " + method + "]";
        ApplicationConfiguration configuration = new ApplicationConfiguration(name, project);
        configuration.setWorkingDirectory("$MODULE_WORKING_DIR$");
        configuration.setMainClassName("dev.jeka.core.tool.Main");
        configuration.setModule(module);
        configuration.setProgramParameters("-CC=" + simpleClassName + " " + method);
        configuration.setBeforeRunTasks(Collections.emptyList());
        RunnerAndConfigurationSettings runnerAndConfigurationSettings =
                RunManager.getInstance(project).createConfiguration(configuration, configuration.getFactory());

        // Uncomment to not force build
        CompileStepBeforeRunNoErrorCheck check = new CompileStepBeforeRunNoErrorCheck(project);
        CompileStepBeforeRunNoErrorCheck.MakeBeforeRunTaskNoErrorCheck task =
                check.createTask(runnerAndConfigurationSettings.getConfiguration());
        runnerAndConfigurationSettings.getConfiguration().setBeforeRunTasks(Collections.singletonList(task));

        Executor executor = debug ?  DefaultDebugExecutor.getDebugExecutorInstance() :
                DefaultRunExecutor.getRunExecutorInstance();
        RunManager.getInstance(project).addConfiguration(runnerAndConfigurationSettings);
        RunManager.getInstance(project).setSelectedConfiguration(runnerAndConfigurationSettings);
        ProgramRunnerUtil.executeConfiguration(runnerAndConfigurationSettings, executor);
    }

}
