package dev.jeka.ide.intellij.common;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import dev.jeka.core.api.depmanagement.JkDepSuggest;

import java.util.Arrays;
import java.util.List;

public class PsiHelper {

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

    public static boolean isJavaStringLiteral(PsiElement psiElement) {
        if (psiElement instanceof PsiJavaToken javaToken) {
            return "STRING_LITERAL".equals(javaToken.getTokenType().toString());
        } else {
            return false;
        }
    }

    /**
     * Returns the method parameter matching with the specified literalExpression.
     * If the literral expression is not coming from a method parameter, this methods returns null.
     */
    public static PsiParameter getMethodParameter(PsiLiteralExpression psiLiteralExpression) {
        PsiElement parent = psiLiteralExpression.getParent();if (parent instanceof PsiExpressionList expressionList) {
            PsiExpression[] psiExpressions = expressionList.getExpressions();
            if (expressionList.getParent() instanceof PsiMethodCallExpression methodCallExpression) {
                int paramIndex = 0;
                for (; paramIndex < expressionList.getExpressionCount(); paramIndex++) {
                    if (psiExpressions[paramIndex] == psiLiteralExpression) {
                        break;
                    }
                }
                PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();
                PsiMethod psiMethod = (PsiMethod) methodExpression.getLastChild().findReferenceAt(0).resolve();
                PsiParameterList parameterList = psiMethod.getParameterList();
                if (parameterList.getParametersCount() > paramIndex) {
                    return parameterList.getParameter(paramIndex);
                } else {
                    return parameterList.getParameter(parameterList.getParametersCount() -1);
                }
            }
        }
        return null;
    }

    public static DependencySuggest toDepSuggest(PsiAnnotation psiAnnotation) {
        PsiAnnotationMemberValue versionOnlyValue = psiAnnotation.findAttributeValue("versionOnly");
        boolean versionOnly =versionOnlyValue == null ? false :  "true".equals(versionOnlyValue.getText());
        PsiAnnotationMemberValue hintValue = psiAnnotation.findAttributeValue("hint");
        String hint = hintValue == null ? null : hintValue.getText();
        if (hint == null) {
            hint = "";
        }
        hint = sanitizeByRemovingQuotes(hint);
        hint = hint.trim();
        if (versionOnly && !hint.endsWith(":")) {
            hint = hint + ":";
        }
        return new DependencySuggest(versionOnly, hint);
    }

    public record DependencySuggest(boolean versionOnly, String hint) {

        public boolean isEnumerated() {
            return hint.contains(",");
        }

        public List<String> enumeration() {
            return Arrays.asList(hint.split(","));
        }
    }

    public static String sanitizeByRemovingQuotes(String value) {
        String result = value;
        if (result.startsWith("\"")) {
            result = result.substring(1);
        }
        if (result.endsWith("\"")) {
            result = result.substring(0, result.length()-1);
        }
        return result;
    }

    public static PsiField getFieldFromAssignment(PsiAssignmentExpression assignmentExpression) {
        if (assignmentExpression.getLExpression() instanceof PsiReferenceExpression left) {
            PsiExpression psiExpression = left.getQualifierExpression();
            PsiElement lastOfLeftEl = left.getLastChild();
            final String fieldName;
            if (lastOfLeftEl instanceof PsiIdentifier lastOfLeft) {
                fieldName = lastOfLeft.getText();
            } else {
                return null;
            }
            PsiType psiType = psiExpression.getType();
            if (psiType instanceof PsiClassReferenceType classReferenceType) {
                PsiClass psiClass = classReferenceType.resolve();
                for (PsiField psiField : psiClass.getAllFields()) {
                   if (psiField.getName().equals(fieldName)) {
                       return psiField;
                   }
                }
            }
        }
        return null;
    }
}
