package dev.jeka.ide.intellij.common;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHelper {

    public static void unzip(final InputStream zipSource, final Path targetFolder) {
        try (ZipInputStream zipInputStream = new ZipInputStream(zipSource)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.startsWith("/")) {
                    entryName = entryName.substring(1);
                }
                final Path toPath = targetFolder.resolve(entryName);
                if (entry.isDirectory()) {
                    if (!Files.exists(toPath)) {
                        Files.createDirectory(toPath);
                    }
                } else {
                    Files.copy(zipInputStream, toPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

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

    @SneakyThrows
    public static URL toUrl(VirtualFile file) {
        return Paths.get(file.getPresentableUrl()).toUri().toURL();
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
