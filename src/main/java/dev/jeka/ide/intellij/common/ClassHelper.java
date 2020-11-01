package dev.jeka.ide.intellij.common;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;

public class ClassHelper {

    private static final String JKCOMMANDS_NAME = "dev.jeka.core.tool.JkCommandSet";

    public static boolean isExtendingJkCommandSet(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        if (JKCOMMANDS_NAME.equals(psiClass.getQualifiedName())) {
            return true;
        }
        PsiClassType[] psiClassTypes = psiClass.getExtendsListTypes();
        for (PsiClassType psiClassType : psiClassTypes) {
            if (psiClassType == null) {
                return false;
            }
            PsiClass currentPsiClass = psiClassType.resolve();
            if (isExtendingJkCommandSet(currentPsiClass)) {
                return true;
            }
        }
        return false;
    }
}
