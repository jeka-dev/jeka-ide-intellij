package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import dev.jeka.core.api.depmanagement.JkDepSuggest;
import dev.jeka.core.tool.JkInjectClasspath;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class JavaCodeDependenciesCompletionContributor extends CompletionContributor {

    public JavaCodeDependenciesCompletionContributor() {

        // Add completion in Java code
        // see https://www.plugin-dev.com/intellij/custom-language/code-completion/
        CompletionProvider completionProvider = new DependenciesCompletionProvider();

        final PatternCondition<PsiLiteralExpression> javaEditCondition = new PatternCondition<PsiLiteralExpression>("") {

            @Override
            public boolean accepts(@NotNull PsiLiteralExpression literalExpression, ProcessingContext context) {
                Object parent = literalExpression.getParent();
                if (!parentIsCandidate(parent)) {
                    return false;
                }
                if (parent instanceof PsiExpressionList) {
                    PsiExpressionList expressionList = (PsiExpressionList) parent;
                    parent = expressionList.getParent();
                    PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) parent;
                    PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();
                    int i = 0;
                    for (; i < expressionList.getExpressionCount(); i++) {
                        if (expressionList.getExpressions()[i] == literalExpression) {
                            break;
                        }
                    }
                    PsiMethod psiMethod = (PsiMethod) methodExpression.getLastChild().findReferenceAt(0).resolve();
                    return hasDepSuggestAnnotation(psiMethod);
                } else if (parent instanceof PsiNameValuePair) {
                    PsiNameValuePair parentPsi = (PsiNameValuePair) parent;
                    PsiReference psiReference = parentPsi.findReferenceAt(0); // method
                    if (psiReference == null) {
                        return false;
                    }
                    PsiElement psiElement = psiReference.resolve();
                    if (psiElement instanceof PsiMethod) {
                        PsiMethod psiMethod = (PsiMethod) psiElement;
                        PsiElement psiMethodParent = psiMethod.getParent();
                        if (psiMethodParent instanceof PsiClass) {
                            PsiClass psiClass = (PsiClass) psiMethodParent;
                            return psiClass.isAnnotationType() &&
                                    JkInjectClasspath.class.getName().equals(psiClass.getQualifiedName());
                        }
                    }
                    return false;
                } else {
                    return false;
                }
            }
        };

        ElementPattern javaSourcePlace = psiElement(JavaTokenType.STRING_LITERAL).withParent(
                psiElement(PsiLiteralExpression.class).with(javaEditCondition)
        );
        extend(CompletionType.BASIC, javaSourcePlace, completionProvider);
    }

    private static boolean hasDepSuggestAnnotation(PsiMethod psiMethod) {
        PsiParameterList parameterList = psiMethod.getParameterList();
        Object jkCoordinateAutoComplete = null;
        if (parameterList.getParameters().length > 0) {
            jkCoordinateAutoComplete = psiMethod.getParameterList().getParameter(0).getAnnotation(
                    JkDepSuggest.class.getName());
        }
        return jkCoordinateAutoComplete != null;
    }

    private static class DependenciesCompletionProvider extends CompletionProvider {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet resultSet) {

            String content = parameters.getOriginalPosition().getText();
            if (content.startsWith("\"")) {
                content = content.substring(1);
            }
            if (content.endsWith("\"")) {
                content = content.substring(0, content.length()-1);
            }
            resultSet.addAllElements(CompletionHelper.findDependenciesVariants(parameters, content));
        }
    }

    private static boolean parentIsCandidate(Object parent) {
        if (parent == null) {
            return false;
        }
        if (parent instanceof PsiExpressionList) {
            return true;
        }
        if ((parent instanceof PsiNameValuePair)) {
            return true;
        }
        return false;
    }

}
