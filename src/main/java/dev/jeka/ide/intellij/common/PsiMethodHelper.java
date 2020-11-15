package dev.jeka.ide.intellij.common;

import com.intellij.psi.*;

public class PsiMethodHelper {

    public static PsiMethod toJekaCommand(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {  // rename
            PsiMethod psiMethod = (PsiMethod) psiElement.getParent();
            PsiClass psiClass = psiMethod.getContainingClass();
            if (!PsiClassHelper.isExtendingJkCommandSet(psiClass)) {
                return null;
            }
            if (psiMethod.isConstructor() || psiMethod.hasParameters()) {
                return null;
            }
            if (psiMethod.getModifierList().hasExplicitModifier("static")) {
                return null;
            }
            if (psiMethod.getModifierList().hasExplicitModifier("public")) {
                return psiMethod;
            }
        }
        return null;
    }
}
