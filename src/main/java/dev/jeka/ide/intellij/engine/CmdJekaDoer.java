/*
 * Copyright 2018-2019 original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.jeka.ide.intellij.engine;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.SlowOperations;
import dev.jeka.core.api.system.JkProperties;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.api.utils.JkUtilsSystem;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.FileHelper;
import dev.jeka.ide.intellij.common.JdksHelper;
import dev.jeka.ide.intellij.common.JekaDistributions;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.extension.JekaConsoleToolWindowFactory;
import dev.jeka.ide.intellij.extension.action.OpenJekaConsoleAction;
import dev.jeka.ide.intellij.extension.action.OpenManageDistributionsAction;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Jerome Angibaud
 */
@RequiredArgsConstructor
public final class CmdJekaDoer {

    private static final Logger LOGGER = Logger.getInstance(CmdJekaDoer.class);

    private enum Stage {
        first, retry
    }

    private final Project invokingProject;

    public static CmdJekaDoer getInstance(Project project) {
        return new CmdJekaDoer(project);
    }

    public void generateIml(Path moduleDir, String qualifiedClassName, boolean clearConsole,
                            @Nullable  Module existingModule, Runnable onFinish) {

        Task.Backgroundable task = new Task.Backgroundable(project(existingModule), "Sync JeKa") {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {

                // Strangely, making a System.out.println seems to fix the issue of not sync module,
                // when creating a new project from wizard
                System.out.println("--------------------- generate iml for  module " + existingModule);

                indicator.setIndeterminate(true);
                indicator.setText("JeKa : synchronizing " + moduleDir.getFileName() + "...");
                doGenerateIml(moduleDir, qualifiedClassName, clearConsole, existingModule, onFinish, Stage.first);
            }
        };
        ProgressManager.getInstance().run(task);
    }

    public void scaffoldModule(Path moduleDir,
                               boolean createStructure,
                               boolean createWrapper,
                               Path wrapDelegate,
                               String jekaVersion,
                               Module existingModule,
                               String extraArgs) {
        Task.Backgroundable task = new Task.Backgroundable(project(existingModule), "Sync JeKa") {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {

                // Strangely, making a System.out.println seems to fix the issue of not sync module,
                // when creating a new project from wizard
                System.out.println("--------------------- scaffold module " + existingModule);

                indicator.setIndeterminate(true);
                indicator.setText("JeKa :  Scaffolding " + moduleDir.getFileName() + "...");
                scaffoldModuleInternal(moduleDir, createStructure, createWrapper, wrapDelegate, jekaVersion,
                        existingModule, extraArgs);
            }
        };
        ProgressManager.getInstance().run(task);

    }

    public void showRuntimeInformation(Module module) {
        Project project = project(module);
        Task.Backgroundable task = new Task.Backgroundable(project, "Sync JeKa") {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                Path modulePath = Paths.get(ModuleHelper.getModuleDir(module).getPath());
                GeneralCommandLine cmd = new GeneralCommandLine(jekaCmd(modulePath, false, null));
                setJekaJDKEnv(cmd, module.getProject(), module);
                cmd.addParameters("-lri", "-dci", "-lst");
                cmd.setWorkDirectory(modulePath.toFile());
                start(cmd, true, () -> getView().print("Done", ConsoleViewContentType.NORMAL_OUTPUT), null);
            }
        };
        ProgressManager.getInstance().run(task);
        ToolWindowManager.getInstance(project).getToolWindow(JekaConsoleToolWindowFactory.ID).show(null);
    }

    private void scaffoldModuleInternal(Path moduleDir,
                                       boolean createStructure,
                                       boolean createWrapper,
                                       Path wrapDelegate,
                                       String jekaVersion,
                                       Module existingModule,
                                       String extraArgs) {
        Runnable afterScaffold = () -> {

            // When creating new project, if we do nothing after iml generated, the new module
            // does not take in account the generated iml
            Runnable afterGenerateIml = () -> refreshAfterIml(existingModule, moduleDir, null);
            //Runnable afterGenerateIml = () -> {};

            doGenerateIml(moduleDir, null, false, existingModule, afterGenerateIml, Stage.first);
            if (wrapDelegate != null) {
                FileHelper.deleteDir(moduleDir.resolve("jeka/wrapper"));
            }
        };
        Runnable doCreateStructure = () -> {};

        // see https://intellij-support.jetbrains.com/hc/en-us/community/posts/206118439-Refresh-after-external-changes-to-project-structure-and-sources
        //project.save();
        //StoreReloadManager.getInstance().blockReloadingProjectOnExternalChanges();

        if (createStructure) {
            GeneralCommandLine structureCmd = new GeneralCommandLine(jekaCmd(moduleDir, false, jekaVersion));
            structureCmd.addParameter("-kb=scaffold");   // avoid jeka.default.kbean that may be declared in parent modules
            setJekaJDKEnv(structureCmd, project(existingModule), existingModule);
            structureCmd.addParameters("scaffold#run", "intellij#");
            structureCmd.setWorkDirectory(moduleDir.toFile());
            structureCmd.addParameters(JkUtilsString.translateCommandline(extraArgs));
            structureCmd.addParameters("-dci", "-ls=BRACE", "-lna", "-lri", "-ld", "-wc", "-lst");
            doCreateStructure = () -> start(structureCmd, !createWrapper, afterScaffold, null);
        }
        if (createWrapper) {
            GeneralCommandLine cmd = new GeneralCommandLine(jekaCmd(moduleDir, true, jekaVersion));
            cmd.addParameter("-kb=scaffold");   // avoid jeka.default.kbean that may be declared in parent modules
            cmd.addParameter("scaffold#wrapper");
            if (wrapDelegate != null) {
                cmd.addParameters("scaffold#wrapDelegatePath=" + wrapDelegate);
            } else if (jekaVersion != null) {
                cmd.addParameter("scaffold#wrapperJekaVersion=" + jekaVersion);
            }
            cmd.setWorkDirectory(moduleDir.toFile());
            cmd.addParameters("-dci", "-ls=BRACE", "-lna", "-lri", "-ld");
            start(cmd, true, doCreateStructure, null);
        } else {
            doCreateStructure.run();
        }
    }

    private void doGenerateIml(Path moduleDir,
                               String qualifiedClassName,
                               boolean clearConsole,
                               @Nullable  Module existingModule,
                               Runnable onFinish,
                               Stage stage) {

        Project project = project(existingModule);
        String execFile = jekaCmd(moduleDir, false, null);
        if (!Files.exists(Paths.get(execFile))) {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("jeka.notifGroup")
                    .createNotification("Distribution file " + execFile + " is missing. " +
                            "Please re-install the distro or suppress it.", NotificationType.ERROR)
                    .addAction(new OpenManageDistributionsAction())
                    .notify(project);
            return;
        }
        GeneralCommandLine cmd = new GeneralCommandLine(jekaCmd(moduleDir, false, null));
        setJekaJDKEnv(cmd, project, existingModule);
        cmd.addParameters("intellij#iml", "-ld", "-cw", "-lst");
        Sdk sdk = getSuggestedSdk(moduleDir, project);
        if (sdk != null) {
            if (sdk.getName().contains(" ")) {
                NotificationGroupManager.getInstance()
                        .getNotificationGroup("jeka.notifGroup")
                        .createNotification("The selected JDK '" + sdk.getName() + "' contains spaces (' ') "
                                        + " This leads in sync failure. "
                                        + ". Please rename this JDK in IntelliJ platform setting."
                                , NotificationType.WARNING)
                        .notify(project);
            } else {
                cmd.addParameters("intellij#suggestedJdkName=" + sdk.getName());
            }
        }
        if (stage == Stage.retry) {
            cmd.addParameters("-lri", "-dci", "-lv", "-lsu");  // clean cache when retrying
        }
        cmd.setWorkDirectory(moduleDir.toFile());

        if (qualifiedClassName != null && stage == Stage.first) {
            cmd.addParameter("-kb=" + qualifiedClassName);  // if can not compile, the bean main not  e available cause not compiled
        }

        Runnable onFail = null;
        if (stage == Stage.first) {

            // if it fails, retry in safe mode (cleaning cache)
            onFail = () -> {
                NotificationGroupManager.getInstance()
                        .getNotificationGroup("jeka.notifGroup")
                        .createNotification("Jeka sync first attempt failed on " + moduleDir.getFileName()
                                        + ".\n\nJeka has rerun sync by ignoring compilation error."
                                , NotificationType.WARNING)
                        .addAction(new OpenJekaConsoleAction())
                        .notify(project);
                doGenerateIml(moduleDir, qualifiedClassName, clearConsole, existingModule, onFinish,
                        Stage.retry);
            };
        } else if (stage == Stage.retry) {
            onFail = () -> {
                NotificationGroupManager.getInstance()
                        .getNotificationGroup("jeka.notifGroup")
                        .createNotification("Jeka sync failed on " + moduleDir.getFileName() + ".\nOpen Jeka Console to see details."
                                , NotificationType.ERROR)
                        .addAction(new OpenJekaConsoleAction())
                        .notify(project);
            };
        }

        Runnable onSuccess = () -> refreshAfterIml(existingModule, moduleDir, onFinish);
        start(cmd, clearConsole, onSuccess, onFail);
    }

    private Project project(@Nullable Module existingModule) {
        if (existingModule != null) {
            return existingModule.getProject();
        }
        return invokingProject;
    }

    private ConsoleView getView() {
        return JekaConsoleToolWindowFactory.getConsoleView(invokingProject);
    }

    private void refreshAfterIml(Module existingModule, Path moduleDir, Runnable onFinish) {
        Project project = project(existingModule);
        if (existingModule == null && ModuleHelper.getModuleHavingRootDir(project, moduleDir) == null) {
           addModule(moduleDir);
        }
        VirtualFile vModuleDir = VirtualFileManager.getInstance().findFileByNioPath(moduleDir);
        VfsUtil.markDirtyAndRefresh(false, true, true, vModuleDir);
        if (onFinish != null) {
            onFinish.run();
        }
    }

    private void addModule(Path moduleDir) {
        Path iml = JkExternalToolApi.getImlFile(moduleDir);
        Path projectDir = Paths.get(invokingProject.getBasePath());
        Path modulesXml = projectDir.resolve(".idea/modules.xml");
        if (Files.exists(modulesXml)) {
            ModuleHelper.addModuleInModulesXml(projectDir, modulesXml, iml);
            VfsUtil.markDirtyAndRefresh(false, true, true, VfsUtil.findFile(modulesXml, true));
        }
    }

    private void start(GeneralCommandLine cmd, boolean clear, Runnable onSuccess, Runnable onFailure) {
        OSProcessHandler handler;
        JekaDistributions.getDefault();
        try {
            handler = new OSProcessHandler(cmd);
            handler.addProcessListener(new ProcessAdapter() {

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    if (event.getExitCode() != 0 && onFailure != null) {
                        getView().print("\nSync has failed.\n",
                                ConsoleViewContentType.ERROR_OUTPUT);
                        onFailure.run();
                    } else if (event.getExitCode() == 0 && onSuccess != null) {
                        getView().print("\n", ConsoleViewContentType.NORMAL_OUTPUT);
                        onSuccess.run();
                    }
                }

            });
        } catch (ExecutionException e) {
            logError(e);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logError(e);
            throw e;
        }
        attachView(handler, clear);
        handler.waitFor();
    }

    private void logError(Exception e) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        e.printStackTrace(ps);
        try {
            String output = os.toString("UTF8");
            getView().print(output, ConsoleViewContentType.ERROR_OUTPUT);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void attachView(OSProcessHandler handler, boolean clear) {
        getView().attachToProcess(handler);
        if (clear) {
            getView().clear();
        }
        handler.startNotify();
    }

    private String jekaCmd(Path moduleDir, boolean forceJeka, String version) {
        if (JkUtilsSystem.IS_WINDOWS) {
            if (forceJeka) {
                return jekaScriptPath(true, version);
            }
            return Files.exists(moduleDir.resolve("jekaw.bat")) ?
                    moduleDir.toAbsolutePath().resolve("jekaw.bat").normalize().toString()
                    : jekaScriptPath(true, version);
        }
        if (forceJeka) {
            return jekaScriptPath(false, version);
        }
        return Files.exists(moduleDir.resolve("jekaw")) ?
                moduleDir.toAbsolutePath().resolve("./jekaw").normalize().toString()
                : jekaScriptPath(false, version);
    }

    private String jekaScriptPath(boolean isWindows, String version) {
        String scriptName = isWindows ? "jeka.bat" : "jeka";
        final Path distributionPath;
        if (version == null) {
            distributionPath = JekaDistributions.getDefault();
            Path scriptPath = distributionPath.resolve(scriptName);
            if (!Files.exists(scriptPath)) {
                JekaDistributions.fetchDistributionForVersion(JekaDistributions.getLatestPublishedVersion());
            }
        } else {
            distributionPath = JekaDistributions.fetchDistributionForVersion(version);
        }
        String result = distributionPath.resolve(scriptName).toAbsolutePath().toString();
        return result;
    }

    private static void setJekaJDKEnv(GeneralCommandLine cmd, Project project, Module module) {
        VirtualFile sdkRoot = ModuleHelper.getSdkRoot(project, module);
        if (sdkRoot != null && sdkRoot.exists()) {
            cmd.withEnvironment("JEKA_JDK", sdkRoot.getCanonicalPath());
        }
    }

    private Sdk getSuggestedSdk(Path moduleDir, Project project) {
        JkProperties jkProperties = JkExternalToolApi.getProperties(moduleDir);
        String javaVersion = jkProperties.get("jeka.java.version");
        String specifiedJdkName = jkProperties.get("intellij#jdkName");
        if (!JkUtilsString.isBlank(specifiedJdkName)) {
            specifiedJdkName = specifiedJdkName.trim();
            if (JdksHelper.findSdkHavingName(specifiedJdkName) == null && !"inheritedJdk".equals(specifiedJdkName)) {
                NotificationGroupManager.getInstance()
                        .getNotificationGroup("jeka.notifGroup")
                        .createNotification("Cannot find a installed JDK called " + specifiedJdkName + " declared in  module "
                                        + moduleDir.getFileName()
                                        + ". Please install this JDK using Intellij platform settings."
                                , NotificationType.WARNING)
                        .notify(project);
            }
            return null;  // JdkName is specified so we don't need to guess one.
        }
        if (javaVersion != null) {
            Sdk sdk = JdksHelper.suggestJdkForMajorVersion(javaVersion.trim());
            if (sdk == null) {
                NotificationGroupManager.getInstance()
                        .getNotificationGroup("jeka.notifGroup")
                        .createNotification("Cannot find a installed JDK " + javaVersion + " for module "
                                        + moduleDir.getFileName()
                                        + ". Please install such JDK using Intellij platform settings."
                                , NotificationType.WARNING)
                        .notify(project);
            }
            return sdk;
        }
        return null;
    }

}
