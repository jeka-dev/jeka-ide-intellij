package dev.jeka.ide.intellij.common;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import dev.jeka.core.api.system.JkProperties;
import dev.jeka.core.tool.JkExternalToolApi;

import java.util.List;

public final class JdksHelper {

    public static Sdk suggestJdkForMajorVersion(String javaMajorVersion) {

        ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
        List<Sdk> sdks = jdkTable.getSdksOfType(JavaSdk.getInstance());

        // first try to find matching jdk within global.properties
        JkProperties globalProps = JkExternalToolApi.getGlobalProperties();
        String globalJdkPath = globalProps.get("jeka.jdk." + javaMajorVersion);
        if (globalJdkPath != null) {
            for (Sdk sdk : sdks) {
                String path = sdk.getHomePath();
                if (globalJdkPath.trim().equals(path)) {
                    return sdk;
                }
            }
        }

        // find the first installed jdk matching the major version
        if (javaMajorVersion.equals("8")) {
            javaMajorVersion = "1.8";
        }
        for (Sdk sdk : sdks) {
            if (versionStringMatches(javaMajorVersion, sdk)) {
                return sdk;
            }
        }
        return null;
    }

    public static Sdk findSdkHavingName(String sdkName) {
        ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
        List<Sdk> sdks = jdkTable.getSdksOfType(JavaSdk.getInstance());
        for (Sdk sdk : sdks) {
            if (sdkName.trim().equals(sdk.getName())) {
                return sdk;
            }
        }
        return null;
    }

    public static List<String> availableSdkNames() {
        return  ProjectJdkTable.getInstance().getSdksOfType(JavaSdk.getInstance()).stream()
                .map(Sdk::getName)
                .toList();
    }

    private static boolean versionStringMatches(String javaMajorVersion, Sdk sdk) {
        String versionString = sdk.getVersionString();
        if (versionString.contains(" \"" + javaMajorVersion + ".")) {  // works with correto
            return true;
        }
        if (versionString.contains(" " + javaMajorVersion + ".")) {  // works with temurin
            return true;
        }
        return false;
    }


}
