package dev.jeka.ide.intellij.extension;

import com.intellij.openapi.application.PathMacroContributor;
import dev.jeka.core.api.system.JkLocator;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static dev.jeka.ide.intellij.common.Constants.JEKA_CACHE_DIR;

public class JekaPathMacroContributor implements PathMacroContributor {

    @Override
    public void registerPathMacros(@NotNull Map<String, String> macros, @NotNull Map<String, String> legacyMacros) {
        macros.put(JEKA_CACHE_DIR, JkLocator.getCacheDir().toString());
    }
}
