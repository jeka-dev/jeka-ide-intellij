package dev.jeka.ide.intellij.engine;

import dev.jeka.core.api.utils.JkUtilsString;

import java.util.Arrays;

public enum ScaffoldNature {

    SIMPLE, JAVA_PROJECT, SPRINGBOOT, JEKA_PLUGIN;

    @Override
    public String toString() {
        return Arrays.stream(this.name().split("_"))
                .map(String::toLowerCase)
                .map(item -> JkUtilsString.capitalize(item))
                .reduce("", (s1, s2) -> s1 + " " + s2);
    }
}
