package dev.jeka.ide.intellij.common;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PsiClassHelper {

    private static final String JKCLASS_NAME = "dev.jeka.core.tool.JkClass";

    public static final String JKPLUGIN_CLASS_NAME = "dev.jeka.core.tool.JkPlugin";

    public static boolean isExtendingJkClass(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        if (JKCLASS_NAME.equals(psiClass.getQualifiedName())) {
            return true;
        }
        PsiClassType[] psiClassTypes = psiClass.getExtendsListTypes();
        for (PsiClassType psiClassType : psiClassTypes) {
            if (psiClassType == null) {
                return false;
            }
            PsiClass currentPsiClass = psiClassType.resolve();
            if (isExtendingJkClass(currentPsiClass)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExtendingJkPlugin(PsiClass psiClass) {
        return isExtending(psiClass, JKPLUGIN_CLASS_NAME);
    }

    public static boolean isExtending(PsiClass psiClass, String superclassName) {
        if (psiClass == null) {
            return false;
        }
        if (superclassName.equals(psiClass.getQualifiedName())) {
            return true;
        }
        for (PsiClass superPsiClass : psiClass.getSupers()) {
            if (superclassName.equals(superPsiClass.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    public static List<PsiClass> findJekaCommandClasses(Module module) {
        VirtualFile rootDir = ModuleHelper.getModuleDir(module);
        VirtualFile jekaDefFolder = rootDir.findFileByRelativePath(Constants.JEKA_DIR_NAME + "/" + Constants.JEKA_DEF_DIR_NAME);
        if (jekaDefFolder == null) {
            return Collections.emptyList();
        }
        PsiManager psiManager = PsiManager.getInstance(module.getProject());
        PsiDirectory jekaDir = psiManager.findDirectory(jekaDefFolder);
        return findJekaCommandClasses(jekaDir);
    }

    private static List<PsiClass> findJekaCommandClasses(PsiDirectory dir) {
        List<PsiClass> result = new LinkedList<>();
        for (PsiFile psiFile : dir.getFiles()) {
            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                for (PsiClass psiClass : psiJavaFile.getClasses()) {
                    if (PsiClassHelper.isExtendingJkClass(psiClass)) {
                        result.add(psiClass);
                    }
                }
            }
        }
        for (PsiDirectory subDir : dir.getSubdirectories()) {
            result.addAll(findJekaCommandClasses(subDir));
        }
        return result;
    }

}
