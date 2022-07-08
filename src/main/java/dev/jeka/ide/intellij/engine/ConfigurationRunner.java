package dev.jeka.ide.intellij.engine;

import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ModuleBasedConfigurationOptions;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SlowOperations;
import dev.jeka.core.tool.Main;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.RunConfigurationHelper;
import dev.jeka.ide.intellij.extension.runconfiguration.JekaRunConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigurationRunner {

    public static void run(Module module, String configurationName, String cmd, boolean debug) {
        SlowOperations.allowSlowOperations(() -> {
            JekaRunConfiguration configuration = new JekaRunConfiguration(configurationName, module.getProject());
            initConfiguration(configuration);
            configuration.setModule(module);
            configuration.setProgramParameters(cmd);
            RunnerAndConfigurationSettings runnerAndConfigurationSettings =
                    RunManager.getInstance(module.getProject()).createConfiguration(configuration, configuration.getFactory());
            ApplicationConfiguration applicationRunConfiguration =
                    (ApplicationConfiguration) runnerAndConfigurationSettings.getConfiguration();

            applicationRunConfiguration.setBeforeRunTasks(Collections.emptyList());
            applicationRunConfiguration.setClasspathModifications(
                    RunConfigurationHelper.computeIntellijCompiledClassExclusions( module));

            Executor executor = debug ? DefaultDebugExecutor.getDebugExecutorInstance() :
                    DefaultRunExecutor.getRunExecutorInstance();
            if (configurationName != null) {
                RunManager.getInstance(module.getProject()).addConfiguration(runnerAndConfigurationSettings);
                RunManager.getInstance(module.getProject()).setSelectedConfiguration(runnerAndConfigurationSettings);
            }
            ProgramRunnerUtil.executeConfiguration(runnerAndConfigurationSettings, executor);
        });
    }

    public static void initConfiguration(ApplicationConfiguration configuration) {
        configuration.setWorkingDirectory("$MODULE_WORKING_DIR$");
        configuration.setMainClassName(Main.class.getName());
        configuration.setBeforeRunTasks(Collections.emptyList());
    }

}
