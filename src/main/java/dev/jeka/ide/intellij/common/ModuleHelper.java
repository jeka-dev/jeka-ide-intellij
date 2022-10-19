package dev.jeka.ide.intellij.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import dev.jeka.core.api.marshalling.xml.JkDomDocument;
import dev.jeka.core.api.marshalling.xml.JkDomElement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ModuleHelper {

    private static final String ALLOWED_CHARS = "_-. 0123456789azertyuiopqsdfghjklmwxcvbnAZERTYUIOPQSDFGHJKLMWXCVBN";

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


    public static void addModuleInModulesXml(Path projectDir, Path modulesXmlPath, Path moduleImlFile) {
        String modulesQuery = "/project/component[@name='ProjectModuleManager']/modules";
        JkDomDocument doc = JkDomDocument.parse(modulesXmlPath);
        List<JkDomElement> modulesEl = doc.root().xPath(modulesQuery);
        if (modulesEl.isEmpty()) {
            return;
        }
        JkDomElement modules = modulesEl.get(0);
        String relativePath = projectDir.relativize(moduleImlFile).toString().replace('\\', '/');
        String filepath = "$PROJECT_DIR$/" + relativePath;
        String fileurl = "file://" + filepath;
        boolean alreadyExist = modules.children("module").stream()
                .anyMatch(el -> filepath.equals(el.attr("filepath")));
        if (alreadyExist) {
            return;
        }
        modules.add("module")
                .attr("fileurl", fileurl)
                .attr("filepath", filepath);
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
        if (!Files.exists(moduleRoot)) {
            return false;
        }
        return FileHelper.containsJekaDir(moduleRoot);
    }

    public static VirtualFile getSdkRoot(Project project, Module module) {
        if (module != null) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            Sdk sdk = moduleRootManager.getSdk();
            if (sdk != null) {
                return sdk.getHomeDirectory();
            }
        }
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        Sdk sdk = projectRootManager.getProjectSdk();
        if (sdk == null) {
            return null;
        }
        return sdk.getHomeDirectory();
    }

    public static List<Module> getModuleDependencies(ModuleManager moduleManager, Module module) {
        List<Module> deps = Arrays.stream(moduleManager.getModules())
                .filter(mod -> moduleManager.isModuleDependent(module, mod))
                .collect(Collectors.toList());
        LinkedHashSet<Module> result = new LinkedHashSet<>();
        result.addAll(deps);
        deps.forEach(dep -> result.addAll(getModuleDependencies(moduleManager, dep)));
        return new LinkedList<>(result);
    }

    public static Optional<String> validateName(String candidate) {
        for (int i= 0; i<candidate.length(); i++) {
            String charat = candidate.substring(i, i+1);
            if (!ALLOWED_CHARS.contains(charat)) {
                return Optional.of("'" + charat + "' is not legal.");
            }
        }
        return Optional.empty();
    }

    public static String findNonExistingModuleName(Project project, String prefix) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        if (moduleManager.findModuleByName(prefix) == null) {
            return prefix;
        }
        for (int i=1; i<10000; i++) {
            String candidate = prefix + i;
            if (moduleManager.findModuleByName(candidate) == null) {
                return candidate;
            }
        }
        throw new IllegalStateException("Haven't got a suggestion for a module name.");
    }

}
