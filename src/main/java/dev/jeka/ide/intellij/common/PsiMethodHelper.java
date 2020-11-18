package dev.jeka.ide.intellij.common;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;

public class PsiMethodHelper {

    public static PsiMethod toJekaCommand(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {  // rename
            PsiMethod psiMethod = (PsiMethod) psiElement.getParent();
            if (isJekaCommand(psiMethod)) {
                return psiMethod;
            }
            return null;
        }
        return null;
    }

    public static boolean isJekaCommand(PsiMethod psiMethod) {
        PsiClass psiClass = psiMethod.getContainingClass();
        if (!PsiClassHelper.isExtendingJkCommandSet(psiClass)) {
            return false;
        }
        if (psiMethod.isConstructor() || psiMethod.hasParameters()) {
            return false;
        }
        if (psiMethod.getModifierList().hasExplicitModifier("static")) {
            return false;
        }
        if (!psiMethod.getReturnType().equals(PsiType.VOID)) {
            return false;
        }
        if (psiMethod.getModifierList().hasExplicitModifier("public")) {
            return true;
        }
        return false;
    }

}
