package dev.jeka.ide.intellij.common;

import com.intellij.openapi.application.PathMacros;

public class MiscHelper {

    public static String getPathVariable(String varName) {
        PathMacros pathMacros = PathMacros.getInstance();
        return pathMacros.getValue(varName);
    }

    public static void setPathVariable(String varName, String value) {
        PathMacros pathMacros = PathMacros.getInstance();
        pathMacros.setMacro(varName, value);
    }

    public static String kbeanName(String simpleClassName) {
        String name = simpleClassName.endsWith("JkBean")
                ? simpleClassName.substring(0, simpleClassName.indexOf("JkBean"))
                : simpleClassName;
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }


}
