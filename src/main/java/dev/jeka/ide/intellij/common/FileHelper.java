package dev.jeka.ide.intellij.common;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileHelper {

    public static void deleteDir(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .peek(System.out::println)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean containsJekaDir(VirtualFile dir) {
        for (VirtualFile virtualFile : dir.getChildren()) {
            if ("jeka".equals(virtualFile.getName()) && virtualFile.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    public static boolean containsJekaDir(Path dir) {
        return Files.walk(dir, 1)
                .filter(path -> Files.isDirectory(path))
                .filter(path -> path.getFileName().endsWith("jeka"))
                .findAny().isPresent();
    }

    public static boolean isProjectJekaFile(Module module, VirtualFile virtualFile) {
        VirtualFile moduleRoot = ModuleHelper.getModuleDir(module);
        VirtualFile jekaDir = moduleRoot.findChild("jeka");
        if (jekaDir == null) {
            return false;
        }
        return virtualFile.getPath().startsWith(jekaDir.getPath());
    }

    public static String toUnixPath(String path) {
        if (path == null) {
            return null;
        }
        if (File.separatorChar=='\\') {
            return path.replace('\\', '/');
        }
        return path;
    }

}
