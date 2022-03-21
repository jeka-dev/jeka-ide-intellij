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
import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.api.utils.JkUtilsSystem;
import dev.jeka.ide.intellij.common.JekaDistributions;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import dev.jeka.ide.intellij.extension.JekaApplicationSettingsConfigurable;
import dev.jeka.ide.intellij.extension.JekaConsoleToolWindows2;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jerome Angibaud
 */
@RequiredArgsConstructor
@Service
public final class CmdJekaDoer2 {

    private static final String SPRINGBOOT_MODULE = "dev.jeka:springboot-plugin";

    private final Project project;

    public static CmdJekaDoer2 getInstance(Project project) {
        return project.getService(CmdJekaDoer2.class);
    }

    public void scaffoldModule(Path moduleDir,
                               boolean createStructure,
                               boolean createWrapper,
                               Path wrapDelegate,
                               String jekaVersion,
                               Module existingModule,
                               JekaTemplate template) {

        // see https://intellij-support.jetbrains.com/hc/en-us/community/posts/206118439-Refresh-after-external-changes-to-project-structure-and-sources
        //project.save();
        //StoreReloadManager.getInstance().blockReloadingProjectOnExternalChanges();

        if (createStructure) {
            GeneralCommandLine scaffoldCmd = scaffoldCmd(project, moduleDir, template);
            if (createWrapper) {
                addWrapper(scaffoldCmd, wrapDelegate, jekaVersion);
            }
            runWithProgress(scaffoldCmd, "Jeka Scaffold " + moduleDir.getFileName());
        } else if (createWrapper) {
            GeneralCommandLine wrapperCmd = wrapperCommandLine(moduleDir, wrapDelegate, jekaVersion);
            runWithProgress(wrapperCmd, "Jeka Wrapper " + moduleDir.getFileName());
        }
        runWithProgress(imlCommandLine(project, moduleDir, existingModule, null),
                "Jeka Generate Iml " + moduleDir.getFileName());
    }

    private GeneralCommandLine scaffoldCmd(Project project, Path moduleDir, JekaTemplate template) {
        GeneralCommandLine scaffoldCmd = new GeneralCommandLine(jekaCmd(moduleDir, false));
        setJekaJDKEnv(scaffoldCmd, project, null);
        scaffoldCmd.addParameters("scaffold#run");
        scaffoldCmd.setWorkDirectory(moduleDir.toFile());
        scaffoldCmd.addParameters(JkUtilsString.translateCommandline(template.getCommandArgs()));
        scaffoldCmd.addParameters("-dci", "-ls=BRACE", "-lna", "-lri", "-lb", "-wc", "-kb=scaffold");
        return scaffoldCmd;
    }

    private GeneralCommandLine wrapperCommandLine(Path moduleDir, Path wrapDelegate, String jekaVersion) {
        GeneralCommandLine wrapperCmd = new GeneralCommandLine(jekaCmd(moduleDir, true));
        addWrapper(wrapperCmd, wrapDelegate, jekaVersion);
        wrapperCmd.setWorkDirectory(moduleDir.toFile());
        wrapperCmd.addParameters("-dci", "-ls=BRACE", "-lna", "-lri", "-lb");
        return wrapperCmd;
    }

    private void addWrapper(GeneralCommandLine cmd, Path wrapDelegate, String jekaVersion) {
        cmd.addParameter("scaffold#wrapper");
        if (wrapDelegate != null) {
            cmd.addParameters("scaffold#wrapDelegatePath=" + wrapDelegate);
        } else if (jekaVersion != null) {
            cmd.addParameter("scaffold#wrapperJekaVersion=" + jekaVersion);
        }
    }

    private GeneralCommandLine imlCommandLine(Project project, Path moduleDir, Module module, String qualifiedClassName) {
        GeneralCommandLine cmd = new GeneralCommandLine(jekaCmd(moduleDir, false));
        setJekaJDKEnv(cmd, project, module);
        cmd.addParameters("intellij#iml", "-dci", "-lb", "-lri");
        cmd.setWorkDirectory(moduleDir.toFile());
        if (qualifiedClassName != null) {
            cmd.addParameter("-kb=" + qualifiedClassName);
        }
        return cmd;
    }

    private void run(GeneralCommandLine generalCommandLine) {
        try {
            generalCommandLine.toProcessBuilder().inheritIO().start().waitFor();
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runWithProgress(GeneralCommandLine generalCommandLine, String name) {
        JekaConsoleToolWindows2.getInstance(project).launch(generalCommandLine);
        /*
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
                () -> JekaConsoleToolWindows2.getInstance(project).launch(generalCommandLine),
                name,
                false,
                project);

         */
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
