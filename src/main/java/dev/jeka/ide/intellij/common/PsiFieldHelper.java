package dev.jeka.ide.intellij.common;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;

public class PsiFieldHelper {

    public static boolean hasSetter(PsiField psiField) {
        PsiClass psiClass = psiField.getContainingClass();
        String methodName = "set" + psiField.getName().substring(0, 1).toUpperCase() + psiField.getName().substring(1);
        for (PsiMethod psiMethod : psiClass.findMethodsByName(methodName, true)) {
            if (!psiMethod.hasModifier(JvmModifier.PUBLIC) || psiMethod.hasModifier(JvmModifier.STATIC)) {
                continue;
            }
            if (psiMethod.getParameterList().getParametersCount() != 1) {
                continue;
            }
            PsiParameter parameter = psiMethod.getParameterList().getParameter(0);
            if (parameter.getType().equals(psiField.getType())) {
                return true;
            }
        }
        return false;
    }
}
