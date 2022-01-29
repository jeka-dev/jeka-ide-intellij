package dev.jeka.ide.intellij.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import dev.jeka.core.api.marshalling.JkDomDocument;
import dev.jeka.core.api.marshalling.JkDomElement;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ModuleHelper {

    public static Module getModule(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return null;
        }
        return ModuleUtil.findModuleForFile(virtualFile, event.getProject());
    }

    public static VirtualFile getModuleDir(Module module) {
        Path path = getModuleDirPath(module);
        return VirtualFileManager.getInstance().findFileByNioPath(path);
    }

    // Sometime, when modules are newly created, module#getModuleFile returns null
    // while module#getModuleFilePath not.
    public static Path getModuleDirPath(Module module) {
        Path candidate = Paths.get(ModuleUtil.getModuleDirPath(module));
        if (candidate.getFileName().toString().equals(".idea")) {
            return candidate.getParent();
        }
        return candidate;
    }

    public static void addModule(Path projectDir, Path modulesXmlPath, Path moduleImlFile) {
        String modulesQuery = "/project/component[@name='ProjectModuleManager']/modules";
        JkDomDocument doc = JkDomDocument.parse(modulesXmlPath);
        List<JkDomElement> modulesEl = doc.root().xPath(modulesQuery);
        if (modulesEl.isEmpty()) {
            return;
        }
        JkDomElement modules = modulesEl.get(0);
        String relativePath = projectDir.relativize(moduleImlFile).toString();
        modules.add("module")
                .attr("fileurl", "file://$PROJECT_DIR$/" + relativePath)
                .attr("filepath", "$PROJECT_DIR$/" + relativePath);
        doc.save(modulesXmlPath);
    }

    // has .iml file at its direct child so "is" or "can become" a module
    public static boolean isPotentialModule(VirtualFile directory) {
        String name = directory.getName();
        if (directory.findChild(name + ".iml") != null) {
            return true;
        }
        VirtualFile ideaDir = directory.findChild(".idea");
        if (ideaDir == null) {
            return false;
        }
        return ideaDir.findChild(name + ".iml") != null;
    }

    public static boolean isExistingModuleRoot(Project project, VirtualFile directory) {
        return getModuleHavingRootDir(project, directory) != null;
    }

    public static Module getModuleHavingRootDir(Project project, VirtualFile directory) {
        Module module = ModuleUtil.findModuleForFile(directory, project);
        if (module == null) {
            return null;
        }
        VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        if (roots.length == 0) {
            return null;
        }
        for (VirtualFile root : roots) {
            if (root.equals(directory) && directory.getName().equals(module.getName())) {
                return module;
            }
        }
        return null;
    }

    public static boolean isJekaModule(Module module) {
        Path moduleRoot = getModuleDirPath(module);
        return FileHelper.containsJekaDir(moduleRoot);
    }

    public static VirtualFile getSdkRoot(Project project, Module module) {
        if (module != null) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            Sdk sdk = moduleRootManager.getSdk();
            return sdk.getHomeDirectory();
        }
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        Sdk sdk = projectRootManager.getProjectSdk();
        return sdk.getHomeDirectory();
    }

}
