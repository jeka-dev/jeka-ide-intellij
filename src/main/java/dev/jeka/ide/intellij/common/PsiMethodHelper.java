package dev.jeka.ide.intellij.common;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;

public class PsiMethodHelper {

    public static boolean isInstancePublicVoidNoArgsNotFromObject(PsiMethod psiMethod) {
        PsiClass psiClass = psiMethod.getContainingClass();
        if (psiClass.getQualifiedName().equals(Object.class.getName())) {
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
