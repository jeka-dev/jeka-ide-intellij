package dev.jeka.ide.intellij.utils;

import com.intellij.openapi.application.PathMacros;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.impl.source.PsiClassReferenceType;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {

    private static final String JKCOMMANDS_NAME = "dev.jeka.core.tool.JkCommands";

    public static String getPathVariable(String varName) {
        PathMacros pathMacros = PathMacros.getInstance();
        return pathMacros.getValue(varName);
    }

    public static void setPathVariable(String varName, String value) {
        PathMacros pathMacros = PathMacros.getInstance();
        pathMacros.setMacro(varName, value);
    }

    public static void unzip(final InputStream zipSource, final Path targetFolder) {
        try (ZipInputStream zipInputStream = new ZipInputStream(zipSource)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final Path toPath = targetFolder.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectory(toPath);
                } else {
                    Files.copy(zipInputStream, toPath);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isExtendingJkCommands(PsiClass psiClass) {
        if (psiClass.getQualifiedName().equals(JKCOMMANDS_NAME)) {
            return true;
        }
        PsiClassType[] psiClassTypes = psiClass.getExtendsListTypes();
        for (PsiClassType psiClassType : psiClassTypes) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiClassType;
            PsiClass currentPsiClass = psiClassReferenceType.resolve();
            if (isExtendingJkCommands(currentPsiClass)) {
                return true;
            }
        }
        return false;
    }


}
