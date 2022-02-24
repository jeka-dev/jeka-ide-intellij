package dev.jeka.ide.intellij.common;

import dev.jeka.core.api.depmanagement.JkModuleId;
import dev.jeka.core.api.depmanagement.JkRepo;
import dev.jeka.core.api.depmanagement.JkRepoFromProperties;
import dev.jeka.core.api.depmanagement.JkVersion;
import dev.jeka.core.api.depmanagement.resolution.JkDependencyResolver;
import dev.jeka.core.api.system.JkLocator;
import dev.jeka.core.api.utils.JkUtilsPath;
import dev.jeka.core.wrapper.Booter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JekaDistributions {

    private static final JkVersion LOWEST_VERSION = JkVersion.of("0.9.20.RC13");

    private static final String MAVEN_CENTRAL_URL = "https://repo.maven.apache.org/maven2/";

    public static Path getDefault() {
        Path result = getLatestInstalled();
        if (result == null) {
            String version = getLatestPublishedVersion();
            result = Booter.install(MAVEN_CENTRAL_URL, version);
        }
        return result;
    }

    public static Path getDistributionsDir() {
        return JkLocator.getCacheDir().resolve("distributions");
    }

    public static List<String> searchVersionsSortedByDesc() {
        JkDependencyResolver resolver = JkDependencyResolver.of().addRepos(JkRepoFromProperties.getDownloadRepos());
        List<String> allVersions = resolver.searchVersions(JkModuleId.of("dev.jeka", "jeka-core"));
        return allVersions.stream()
                .map(JkVersion::of)
                .filter(version -> version.isGreaterThan(LOWEST_VERSION) || version.equals(LOWEST_VERSION))
                .sorted(Comparator.reverseOrder())
                .map(JkVersion::toString)
                .collect(Collectors.toList());
    }

    public static Path install(String version) {
        return Booter.install(MAVEN_CENTRAL_URL, version);
    }

    private static Path getLatestInstalled() {
        Path wrapperCacheDir = getDistributionsDir();
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
        return searchVersionsSortedByDesc().get(0);
    }

}
