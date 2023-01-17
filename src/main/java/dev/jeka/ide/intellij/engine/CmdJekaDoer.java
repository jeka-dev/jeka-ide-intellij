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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.SlowOperations;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.api.utils.JkUtilsSystem;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.FileHelper;
import dev.jeka.ide.intellij.common.JekaDistributions;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.extension.JekaApplicationSettingsConfigurable;
import dev.jeka.ide.intellij.extension.JekaConsoleToolWindowFactory;
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

/**
 * @author Jerome Angibaud
 */
@Service
@RequiredArgsConstructor
public final class CmdJekaDoer {

    private final Project project;

    public static CmdJekaDoer getInstance(Project project) {
        return project.getService(CmdJekaDoer.class);
    }

    public void generateIml(Path moduleDir, String qualifiedClassName, boolean clearConsole,
                            @Nullable  Module existingModule, Runnable onFinish) {
        doGenerateIml(moduleDir, qualifiedClassName, clearConsole, existingModule, onFinish);
    }

    public void scaffoldModule(Path moduleDir,
                               boolean createStructure,
                               boolean createWrapper,
                               Path wrapDelegate,
                               String jekaVersion,
                               Module existingModule,
                               String extraArgs) {
        Runnable afterScaffold = () -> {
            //Runnable afterGenerateIml = () -> refreshAfterIml(existingModule, moduleDir, null);
            Runnable afterGenerateIml = () -> {};
            doGenerateIml(moduleDir, null, false, existingModule, afterGenerateIml);
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
            setJekaJDKEnv(structureCmd, project, existingModule);
            structureCmd.addParameters("scaffold#run", "intellij#");
            structureCmd.setWorkDirectory(moduleDir.toFile());
            structureCmd.addParameters(JkUtilsString.translateCommandline(extraArgs));
            structureCmd.addParameters("-dci", "-ls=BRACE", "-lna", "-lri", "-ld", "-wc", "-lst", "-kb=scaffold");
            doCreateStructure = () -> start(structureCmd, !createWrapper, afterScaffold, null);
        }
        if (createWrapper) {
            GeneralCommandLine cmd = new GeneralCommandLine(jekaCmd(moduleDir, true, jekaVersion));
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

    public void showRuntimeInformation(Module module) {
        Path modulePath = Paths.get(ModuleHelper.getModuleDir(module).getPath());
        GeneralCommandLine cmd = new GeneralCommandLine(jekaCmd(modulePath, false, null));
        setJekaJDKEnv(cmd, module.getProject(), module);
        cmd.addParameters("-lri");
        cmd.setWorkDirectory(modulePath.toFile());
        start(cmd, true, () -> getView().print("Done", ConsoleViewContentType.NORMAL_OUTPUT), null);
    }

    private void doGenerateIml(Path moduleDir, String qualifiedClassName, boolean clearConsole,
                               @Nullable  Module existingModule, Runnable onFinish) {
        String execFile = jekaCmd(moduleDir, false, null);
        if (!Files.exists(Paths.get(execFile))) {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("jeka.notifGroup")
                    .createNotification("Distribution file " + execFile + " is missing. " +
                            "Please re-install the distro or suppress it.", NotificationType.ERROR)
                    .addAction(new OpenManageDistributionsAction())
                    .notify(this.project);
            return;
        }
        GeneralCommandLine cmd = new GeneralCommandLine(jekaCmd(moduleDir, false, null));
        setJekaJDKEnv(cmd, project, existingModule);
        cmd.addParameters("intellij#iml", "-dci", "-lri", "-ld", "-cw");
        cmd.setWorkDirectory(moduleDir.toFile());
        Runnable onfailure = null;
        if (qualifiedClassName != null) {
            cmd.addParameter("-kb=" + qualifiedClassName);

            // if it fails, retry without specifying the kbean
            onfailure = () -> doGenerateIml(moduleDir, null, clearConsole, existingModule, onFinish);
        }
        Runnable onSuccess = () -> refreshAfterIml(existingModule, moduleDir, onFinish);
        start(cmd, clearConsole, onSuccess,  onfailure);
    }

    private ConsoleView getView() {
        return JekaConsoleToolWindowFactory.getConsoleView(project);
    }

    private void refreshAfterIml(Module existingModule, Path moduleDir, Runnable onFinish) {
        if (existingModule == null) {
           addModule(moduleDir);
        }
        SlowOperations.allowSlowOperations(() -> {
            VirtualFile vModuleDir = VirtualFileManager.getInstance().findFileByNioPath(moduleDir);
            VfsUtil.markDirtyAndRefresh(false, true, true, vModuleDir);
        });
        if (onFinish != null) {
            onFinish.run();
        }
    }

    private void addModule(Path moduleDir) {
        SlowOperations.allowSlowOperations(() -> {
            Path iml = JkExternalToolApi.getImlFile(moduleDir);
            Path projectDir = Paths.get(project.getBasePath());
            Path modulesXml = projectDir.resolve(".idea/modules.xml");
            if (Files.exists(modulesXml)) {
                ModuleHelper.addModuleInModulesXml(projectDir, modulesXml, iml);
                VfsUtil.markDirtyAndRefresh(false, true, true, VfsUtil.findFile(modulesXml, true));
            }
        });
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
        if (ApplicationManager.getApplication().isDispatchThread()) {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(JekaConsoleToolWindowFactory.ID);
            if (toolWindow != null) {
                toolWindow.show();
            }
        }
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
        String settingDistrib = JekaApplicationSettingsConfigurable.State.getInstance().distributionDirPath;
        final Path distributionPath;
        if (JkUtilsString.isBlank(settingDistrib)) {
            distributionPath = version == null ? JekaDistributions.getDefault()
                    : JekaDistributions.fetchDistributionForVersion(version);
        } else {
            distributionPath = Paths.get(settingDistrib);
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

}
