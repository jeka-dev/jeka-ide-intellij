package dev.jeka.ide.intellij.engine;

import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ModuleBasedConfigurationOptions;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SlowOperations;
import dev.jeka.ide.intellij.common.ModuleHelper;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigurationRunner {

    public static void run(Module module, String configurationName, String cmd, boolean debug) {
        SlowOperations.allowSlowOperations(() -> {
            ApplicationConfiguration configuration = new ApplicationConfiguration(configurationName, module.getProject());
            configuration.setWorkingDirectory("$MODULE_WORKING_DIR$");
            configuration.setMainClassName("dev.jeka.core.tool.Main");
            configuration.setModule(module);
            configuration.setProgramParameters(cmd);
            configuration.setBeforeRunTasks(Collections.emptyList());

            RunnerAndConfigurationSettings runnerAndConfigurationSettings =
                    RunManager.getInstance(module.getProject()).createConfiguration(configuration, configuration.getFactory());
            ApplicationConfiguration applicationRunConfiguration =
                    (ApplicationConfiguration) runnerAndConfigurationSettings.getConfiguration();

            applicationRunConfiguration.setBeforeRunTasks(Collections.emptyList());
            applyClasspathModification(applicationRunConfiguration, module);

            Executor executor = debug ? DefaultDebugExecutor.getDebugExecutorInstance() :
                    DefaultRunExecutor.getRunExecutorInstance();
            if (configurationName != null) {
                RunManager.getInstance(module.getProject()).addConfiguration(runnerAndConfigurationSettings);
                RunManager.getInstance(module.getProject()).setSelectedConfiguration(runnerAndConfigurationSettings);
            }
            ProgramRunnerUtil.executeConfiguration(runnerAndConfigurationSettings, executor);
        });
    }

    private static void applyClasspathModification(ApplicationConfiguration applicationConfiguration, Module module) {
        LinkedHashSet<ModuleBasedConfigurationOptions.ClasspathModification> excludes = new LinkedHashSet<>();
        excludes.addAll(findExclusion(module));
        ModuleManager moduleManager = ModuleManager.getInstance(module.getProject());
        List<Module> depModules = ModuleHelper.getModuleDependencies(moduleManager, module);
        depModules.forEach(mod -> excludes.addAll(findExclusion(mod)));
        applicationConfiguration.setClasspathModifications(new LinkedList<>(excludes));
    }

    private static List<ModuleBasedConfigurationOptions.ClasspathModification> findExclusion(Module module) {
        VirtualFile[] roots = ModuleRootManager.getInstance(module).orderEntries().classes().getRoots();
        return Arrays.stream(roots)
                .filter(virtualFile -> "file".equals(virtualFile.getFileSystem().getProtocol()))
                .map(VirtualFile::toNioPath)
                .map(path ->
                        new ModuleBasedConfigurationOptions.ClasspathModification(path.toString(), true))
                .collect(Collectors.toList());
    }
}
