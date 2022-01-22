package dev.jeka.ide.intellij.common;

import dev.jeka.core.api.depmanagement.JkModuleId;
import dev.jeka.core.api.depmanagement.JkRepo;
import dev.jeka.core.api.depmanagement.JkVersion;
import dev.jeka.core.api.depmanagement.resolution.JkDependencyResolver;
import dev.jeka.core.api.system.JkLocator;
import dev.jeka.core.api.utils.JkUtilsPath;
import dev.jeka.core.wrapper.Booter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class JekaDistributions {

    private static final JkVersion LOWEST_VERSION = JkVersion.of("0.9.20.RC13");

    public static Path getDefault() {
        Path result = getLatestInstalled();
        if (result == null) {
            String version = getLatestPublishedVersion();
            result = Booter.install(version);
        }
        return result;
    }

    private static Path getLatestInstalled() {
        Path wrapperCacheDir = JkLocator.getCacheDir().resolve("wrapper");
        if (!Files.exists(wrapperCacheDir)) {
            return null;
        }
        List<Path> distribRoots = JkUtilsPath.listDirectChildren(wrapperCacheDir).stream()
                .filter(path -> JkVersion.of(path.getFileName().toString()).compareTo(LOWEST_VERSION) >= 0)
                .sorted()
                .collect(Collectors.toList());
        if (distribRoots.isEmpty()) {
            return null;
        }
        return distribRoots.get(distribRoots.size() -1);
    }

    private static String getLatestPublishedVersion() {
        List<String> versions = JkDependencyResolver.of().addRepos(JkRepo.ofMavenCentral())
                .searchVersions(JkModuleId.of("dev.jeka", "jeka-core"));
        return versions.get(versions.size() -1);
    }

}
