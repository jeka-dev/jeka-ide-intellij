package dev.jeka.ide.intellij.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JekaWrapperInfo {

    public static boolean hasWrapperShellFiles(Path moduleDir) {
        return Files.exists(moduleDir.resolve("jekaw.bat")) &&
                Files.exists(moduleDir.resolve("jekaw"));
    }
}
