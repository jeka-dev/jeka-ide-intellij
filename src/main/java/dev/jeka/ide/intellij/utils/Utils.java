package dev.jeka.ide.intellij.utils;

import com.intellij.openapi.application.PathMacros;
import dev.jeka.core.api.utils.JkUtilsString;

import java.io.File;
import java.util.Set;

public class Utils {

    public static String getPathVariable(String varName) {
        PathMacros pathMacros = PathMacros.getInstance();
        return pathMacros.getValue(varName);
    }

    public static void setPathVariable(String varName, String value) {
        PathMacros pathMacros = PathMacros.getInstance();
        pathMacros.setMacro(varName, value);
    }

}
