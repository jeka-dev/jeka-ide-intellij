package dev.jeka.ide.intellij.action;

import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import dev.jeka.ide.intellij.common.Constants;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.data.ModuleAndMethod;
import org.intellij.lang.annotations.JdkConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.NavigableMap;


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
        final PsiMethod psiMethod;
        final Module module;
        if (location != null) {
            psiMethod = (PsiMethod) location.getPsiElement().getParent();
            module = ModuleHelper.getModule(event);
        } else {
            ModuleAndMethod moduleAndMethod = ModuleAndMethod.KEY.getData(dataContext);
            if (moduleAndMethod == null) {
                throw new IllegalStateException("Can not find reference to Psi method");
            }
            psiMethod = moduleAndMethod.getMethod();
            module = moduleAndMethod.getModule();
        }
        String methodName = psiMethod.getName();
        String className = psiMethod.getContainingClass().getName();
        String name = module.getName() + " [jeka " + methodName + "]";
        ApplicationConfiguration configuration = new ApplicationConfiguration(name, project);
        configuration.setWorkingDirectory("$MODULE_WORKING_DIR$");
        configuration.setMainClassName("dev.jeka.core.tool.Main");
        configuration.setModule(module);
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
