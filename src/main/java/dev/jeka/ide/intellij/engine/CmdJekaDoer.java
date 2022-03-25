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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.SlowOperations;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.api.utils.JkUtilsSystem;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.FileHelper;
import dev.jeka.ide.intellij.common.JekaDistributions;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.extension.JekaApplicationSettingsConfigurable;
import dev.jeka.ide.intellij.extension.JekaConsoleToolWindows;
import dev.jeka.ide.intellij.extension.JekaToolWindows;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jerome Angibaud
 */
@Service
@RequiredArgsConstructor
public final class CmdJekaDoer {

    private static final String SPRINGBOOT_MODULE = "dev.jeka:springboot-plugin";

    private final Project project;

    public static CmdJekaDoer getInstance(Project project) {
        return project.getService(CmdJekaDoer.class);
    }

    public void generateIml(Path moduleDir, String qualifiedClassName, boolean clearConsole,
                            @Nullable  Module existingModule, Runnable onFinish) {
        doGenerateIml(moduleDir, qualifiedClassName, clearConsole, existingModule, onFinish);
        JekaToolWindows.registerIfNeeded(project, false);
    }

    public void scaffoldModule(Path moduleDir,
                               boolean createStructure,
                               boolean createWrapper,
                               Path wrapDelegate,
                               String jekaVersion,
                               Module existingModule,
                               String extraArgs) {
        Runnable afterScaffold = () -> {
            Runnable afterGenerateIml = () -> refreshAfterIml(existingModule, moduleDir, null);
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
            GeneralCommandLine structureCmd = new GeneralCommandLine(jekaCmd(moduleDir, false));
            setJekaJDKEnv(structureCmd, project, existingModule);
            structureCmd.addParameters("scaffold#run");
            structureCmd.setWorkDirectory(moduleDir.toFile());
            structureCmd.addParameters(JkUtilsString.translateCommandline(extraArgs));
            structureCmd.addParameters("-dci", "-ls=BRACE", "-lna", "-lri", "-lb", "-wc", "-kb=scaffold");
            doCreateStructure = () -> start(structureCmd, !createWrapper, afterScaffold, null);
        }
        if (createWrapper) {
            GeneralCommandLine cmd = new GeneralCommandLine(jekaCmd(moduleDir, true));
            cmd.addParameter("scaffold#wrapper");
            if (wrapDelegate != null) {
                cmd.addParameters("scaffold#wrapDelegatePath=" + wrapDelegate);
            } else if (jekaVersion != null) {
                cmd.addParameter("scaffold#wrapperJekaVersion=" + jekaVersion);
            }
            cmd.setWorkDirectory(moduleDir.toFile());
            cmd.addParameters("-dci", "-ls=BRACE", "-lna", "-lri", "-lb");
            start(cmd, true, doCreateStructure, null);
        } else {
            doCreateStructure.run();
        }
        JekaToolWindows.registerIfNeeded(project, false);
    }


    public void showRuntimeInformation(Module module) {
        Path modulePath = Paths.get(ModuleHelper.getModuleDir(module).getPath());
        GeneralCommandLine cmd = new GeneralCommandLine(jekaCmd(modulePath, false));
        setJekaJDKEnv(cmd, module.getProject(), module);
        cmd.addParameters("-lri");
        cmd.setWorkDirectory(modulePath.toFile());
        start(cmd, true, null, null);
    }

    private void doGenerateIml(Path moduleDir, String qualifiedClassName, boolean clearConsole,
                               @Nullable  Module existingModule, Runnable onFinish) {
        GeneralCommandLine cmd = new GeneralCommandLine(jekaCmd(moduleDir, false));
        setJekaJDKEnv(cmd, project, existingModule);
        cmd.addParameters("intellij#iml", "-dci", "-lb", "-lri");
        cmd.setWorkDirectory(moduleDir.toFile());
        if (qualifiedClassName != null) {
            cmd.addParameter("-kb=" + qualifiedClassName);
        }
        Runnable onSuccess = () -> refreshAfterIml(existingModule, moduleDir, onFinish);
        start(cmd, clearConsole, onSuccess,  null );
    }

    private ConsoleView getView() {
        return JekaConsoleToolWindows.getConsoleView(project);
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
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw e;
        }
        attachView(handler, clear);
        if (ApplicationManager.getApplication().isDispatchThread()) {
            JekaConsoleToolWindows.getToolWindow(project).show();
        }
    }

    private void attachView(OSProcessHandler handler, boolean clear) {
        getView().attachToProcess(handler);
        if (clear) {
            getView().clear();
        }
        handler.startNotify();
        if (JekaConsoleToolWindows.getToolWindow(project) == null) {
            JekaConsoleToolWindows.registerToolWindow(project);
        }
    }

    private String jekaCmd(Path moduleDir, boolean forceJeka) {
        if (JkUtilsSystem.IS_WINDOWS) {
            if (forceJeka) {
                return jekaScriptPath(true);
            }
            return Files.exists(moduleDir.resolve("jekaw.bat")) ?
                    moduleDir.toAbsolutePath().resolve("jekaw.bat").toString()
                    : jekaScriptPath(true);
        }
        if (forceJeka) {
            return jekaScriptPath(false);
        }
        return Files.exists(moduleDir.resolve("jekaw")) ? "./jekaw" : jekaScriptPath(false);
    }

    private String jekaScriptPath(boolean isWindows) {
        String scriptName = isWindows ? "jeka.bat" : "jeka";
        String settingDistrib = JekaApplicationSettingsConfigurable.State.getInstance().distributionDirPath;
        final Path distributionPath;
        if (JkUtilsString.isBlank(settingDistrib)) {
            distributionPath = JekaDistributions.getDefault();
        } else {
            distributionPath = Paths.get(settingDistrib);
        }
        return distributionPath.resolve(scriptName).toAbsolutePath().toString();
    }

    private static void setJekaJDKEnv(GeneralCommandLine cmd, Project project, Module module) {
        VirtualFile sdkRoot = ModuleHelper.getSdkRoot(project, module);
        if (sdkRoot != null && sdkRoot.exists()) {
            cmd.withEnvironment("JEKA_JDK", sdkRoot.getCanonicalPath());
        }
    }

}
