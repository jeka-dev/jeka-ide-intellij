package dev.jeka.ide.intellij.common;

import com.intellij.execution.configurations.ModuleBasedConfigurationOptions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.core.api.utils.JkUtilsString;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RunConfigurationHelper {

    public static List<ModuleBasedConfigurationOptions.ClasspathModification> computeIntellijCompiledClassExclusions(
            Module module) {
        LinkedHashSet<ModuleBasedConfigurationOptions.ClasspathModification> excludes = new LinkedHashSet<>();
        excludes.addAll(findExclusions(module));
        excludes.addAll(findEDepExclusions(module));
        ModuleManager moduleManager = ModuleManager.getInstance(module.getProject());
        List<Module> depModules = ModuleHelper.getModuleDependencies(moduleManager, module);
        depModules.forEach(mod -> excludes.addAll(findExclusions(mod)));
        return new LinkedList<>(excludes);
    }

    private static List<ModuleBasedConfigurationOptions.ClasspathModification> findExclusions(Module module) {
        VirtualFile[] roots = ModuleRootManager.getInstance(module).orderEntries().classes().getRoots();
        return Arrays.stream(roots)
                .filter(virtualFile -> "file".equals(virtualFile.getFileSystem().getProtocol()))
                .map(VirtualFile::toNioPath)
                .map(path ->
                        new ModuleBasedConfigurationOptions.ClasspathModification(path.toString(), true))
                .collect(Collectors.toList());
    }

    private static List<ModuleBasedConfigurationOptions.ClasspathModification> findEDepExclusions(Module module) {
        VirtualFile[] roots = ModuleRootManager.getInstance(module).orderEntries().classes().getRoots();
        return Arrays.stream(roots)
                .filter(virtualFile -> "jar".equals(virtualFile.getFileSystem().getProtocol()))
                .filter(virtualFile -> !virtualFile.getPath().endsWith("dev.jeka.jeka-core.jar!/"))
                .map(VirtualFile::getPath)
                .map(path -> JkUtilsString.substringBeforeLast(path, "!/"))
                .map(sanitizedPath ->
                        new ModuleBasedConfigurationOptions.ClasspathModification(sanitizedPath, true))
                .collect(Collectors.toList());
    }



}
