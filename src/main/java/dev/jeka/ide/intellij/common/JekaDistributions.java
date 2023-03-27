package dev.jeka.ide.intellij.common;

import dev.jeka.core.api.depmanagement.JkModuleId;
import dev.jeka.core.api.depmanagement.JkRepoProperties;
import dev.jeka.core.api.depmanagement.JkVersion;
import dev.jeka.core.api.depmanagement.resolution.JkDependencyResolver;
import dev.jeka.core.api.file.JkPathTree;
import dev.jeka.core.api.system.JkLocator;
import dev.jeka.core.api.system.JkProperties;
import dev.jeka.core.api.utils.JkUtilsPath;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.api.utils.JkUtilsSystem;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.core.wrapper.Booter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class JekaDistributions {

    private static final JkVersion LOWEST_VERSION = JkVersion.of("0.10.11");

    private static final String MAVEN_CENTRAL_URL = "https://repo.maven.apache.org/maven2/";

    public static Path getDefault() {
        Path result = getLatestInstalled();
        if (result == null) {
            String version = getLatestPublishedVersion();
            result = Booter.install(MAVEN_CENTRAL_URL, version);
        }
        return result;
    }

    public static Path fetchDistributionForVersion(String version) {
        Path path = getDistributionsDir().resolve(version);
        if (!Files.exists(path)) {
            Booter.install(MAVEN_CENTRAL_URL, version);
        } else {
            String missingFile = missingFileInDistrib(path);
            if (missingFile != null) {
                System.err.println("File " + missingFile + " is missing in distrib " + path +
                        ". This distrib will be reinstalled.");
                JkPathTree.of(path).deleteRoot();
                Booter.install(MAVEN_CENTRAL_URL, version);
            }
        }
        return path;
    }

    private static String missingFileInDistrib(Path distrib) {
        String cmd = JkUtilsSystem.IS_WINDOWS ? "jeka.bat" : "jeka";
        if (!Files.exists(distrib.resolve(cmd))) {
            return cmd;
        }
        return null;
    }

    public static Path getDistributionsDir() {
        return JkLocator.getCacheDir().resolve("distributions");
    }

    public static List<String> searchVersionsSortedByDesc() {
        JkProperties props = JkExternalToolApi.getGlobalProperties();
        JkDependencyResolver resolver = JkDependencyResolver.of().addRepos(JkRepoProperties.of(props).getDownloadRepos());
        List<String> allVersions = resolver.searchVersions(JkModuleId.of("dev.jeka", "jeka-core"));
        List<String> sorted = allVersions.stream()
                .map(JkVersion::of)
                .filter(version -> version.isGreaterThan(LOWEST_VERSION) || version.equals(LOWEST_VERSION))
                .sorted(Comparator.reverseOrder())
                .map(JkVersion::toString)
                .collect(Collectors.toList());
        List<String> result = new LinkedList<>(sorted);
        boolean modifierFound = false;
        for (ListIterator<String> li = result.listIterator(); li.hasNext();) {
            String ver = li.next();
            if (ver.contains("-") || JkUtilsString.countOccurrence(ver, '.') > 2) {
                if (modifierFound) {
                    li.remove();
                }
                modifierFound = true;
            }
        }
        return result;
    }

    public static Path install(String version) {
        return Booter.install(MAVEN_CENTRAL_URL, version);
    }

    private static void removeCorruptedDistrib() {

        Path wrapperCacheDir = getDistributionsDir();
        if (!Files.exists(wrapperCacheDir)) {
            return;
        }
        for (Path path : JkUtilsPath.listDirectChildren(wrapperCacheDir)) {
            String missingFile = missingFileInDistrib(path);
            if (missingFile != null) {
                System.err.println("File " + missingFile + " is missing in distrib " + path +
                        ". This distrib will be removed.");
                JkPathTree.of(path).deleteRoot();
            }
        }
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
