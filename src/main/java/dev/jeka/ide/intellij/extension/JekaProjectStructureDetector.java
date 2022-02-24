package dev.jeka.ide.intellij.extension;

import com.intellij.ide.util.importProject.ModuleDescriptor;
import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.util.containers.ContainerUtil;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.core.tool.JkExternalToolApi;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Currently unused
 */
public class JekaProjectStructureDetector extends ProjectStructureDetector {

    @Override
    public @NotNull DirectoryProcessingResult detectRoots(@NotNull File dir,
                                                          File @NotNull [] children,
                                                          @NotNull File base,
                                                          @NotNull List<DetectedProjectRoot> result) {
        Path dirPath = dir.toPath();
        if (JkExternalToolApi.isJekaProject(dirPath)) {
            result.add(new JekaDetectedProjectRoot(dir));
            return DirectoryProcessingResult.PROCESS_CHILDREN;
        }
        return isBlackListed(result, dirPath) ? DirectoryProcessingResult.SKIP_CHILDREN : DirectoryProcessingResult.PROCESS_CHILDREN;
    }

    @Override
    public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots,
                                      @NotNull ProjectDescriptor projectDescriptor,
                                      @NotNull ProjectFromSourcesBuilder builder) {
        List<ModuleDescriptor> modules = projectDescriptor.getModules();
        if (!modules.isEmpty()) {
            return;
        }
        modules = new LinkedList<>();
        for (DetectedProjectRoot root : roots) {
            modules.add(new ModuleDescriptor(root.getDirectory(), JavaModuleType.getModuleType(),
                    ContainerUtil.emptyList()));
        }
        projectDescriptor.setModules(modules);
    }

    @Override
    public String getDetectorId() {
        return "Jeka";
    }

    private static boolean isBlackListed(List<DetectedProjectRoot> result, Path dir) {
        for (DetectedProjectRoot detectedProjectRoot : result) {
            Path projectRoot = detectedProjectRoot.getDirectory().toPath();
            if (projectRoot.resolve("src").equals(dir)) {
                return true;
            }
            if (projectRoot.resolve("sources").equals(dir)) {
                return true;
            }
            if (projectRoot.resolve("test").equals(dir)) {
                return true;
            }
            if (projectRoot.resolve("tests").equals(dir)) {
                return true;
            }
            if (projectRoot.resolve(JkConstants.JEKA_DIR).equals(dir)) {
                return true;
            }
            if (projectRoot.resolve("target").equals(dir)) {
                return true;
            }
            if (projectRoot.resolve("build").equals(dir)) {
                return true;
            }
            if (dir.getFileName().toString().startsWith(".")) {
                return true;
            }
        }
        return false;
    }

    private static class JekaDetectedProjectRoot extends DetectedProjectRoot {

        protected JekaDetectedProjectRoot(@NotNull File directory) {
            super(directory);
        }

        @Override
        public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getRootTypeName() {
            return "Jeka";
        }
    }

}
