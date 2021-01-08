package dev.jeka.ide.intellij.common;

import com.intellij.openapi.application.PathMacros;
import lombok.SneakyThrows;

import java.net.URL;

public class MiscHelper {

    public static String getPathVariable(String varName) {
        PathMacros pathMacros = PathMacros.getInstance();
        return pathMacros.getValue(varName);
    }

    public static void setPathVariable(String varName, String value) {
        PathMacros pathMacros = PathMacros.getInstance();
        pathMacros.setMacro(varName, value);
    }

    public static String pluginName(String simpleClassName) {
        String name = simpleClassName.substring("JkPlugin".length());
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }


}
