package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import dev.jeka.core.api.depmanagement.JkDepSuggest;
import dev.jeka.core.tool.JkInjectClasspath;
import dev.jeka.ide.intellij.common.PsiHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
                if (!parentIsCandidate(parent)) {   // try to return early
                    return false;
                }
                if (parent instanceof PsiExpressionList) {  // method parameter
                    PsiParameter psiParameter = PsiHelper.getMethodParameter(literalExpression);
                    if (psiParameter == null) {
                        return false;
                    }
                    return psiParameter.getAnnotation(JkDepSuggest.class.getName()) != null;
                } else if (parent instanceof PsiAssignmentExpression assignmentExpression) {
                    PsiField psifield = PsiHelper.getFieldFromAssignment(assignmentExpression);
                    return psifield.getAnnotation(JkDepSuggest.class.getName()) != null;
                } else if (parent instanceof PsiNameValuePair parentPsi) {
                    PsiReference psiReference = parentPsi.findReferenceAt(0); // method
                    if (psiReference == null) {
                        return false;
                    }
                    PsiElement psiElement = psiReference.resolve();
                    if (psiElement instanceof PsiMethod psiMethod) {
                        PsiElement psiMethodParent = psiMethod.getParent();
                        if (psiMethodParent instanceof PsiClass psiClass) {
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


    private static class DependenciesCompletionProvider extends CompletionProvider {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet resultSet) {

            PsiHelper.DependencySuggest depSuggest = new PsiHelper.DependencySuggest(false, "");
            PsiElement psiElement = parameters.getOriginalPosition();
            if (psiElement.getParent() instanceof PsiLiteralExpression psiLiteralExpression) {
                PsiParameter psiParameter = PsiHelper.getMethodParameter(psiLiteralExpression);
                if (psiParameter != null) {
                    PsiAnnotation psiAnnotation = psiParameter.getAnnotation(JkDepSuggest.class.getName());
                    depSuggest = PsiHelper.toDepSuggest(psiAnnotation);
                } else if (psiLiteralExpression.getParent() instanceof PsiAssignmentExpression assignment) {
                    PsiField psiField = PsiHelper.getFieldFromAssignment(assignment);
                    if (psiField != null) {
                        PsiAnnotation psiAnnotation = psiField.getAnnotation(JkDepSuggest.class.getName());
                        depSuggest = PsiHelper.toDepSuggest(psiAnnotation);
                    }
                }
            }
            String content = psiElement.getText();
            content = PsiHelper.sanitizeByRemovingQuotes(content);
            if (content.equals("") || depSuggest.versionOnly()) {
                content = depSuggest.hint();
            }

            Module module = ModuleUtil.findModuleForFile(parameters.getOriginalFile());
            final List<LookupElement> result ;;
            if (depSuggest.versionOnly()) {
                result = CompletionHelper.findVersions(module, content);
            } else {
                result = CompletionHelper.findDependenciesVariants(module, content, false);
            }
            resultSet.addAllElements(result);
            resultSet.stopHere();
        }
    }

    private static boolean parentIsCandidate(Object parent) {
        if (parent == null) {
            return false;
        }
        if (parent instanceof PsiExpressionList) {
            return true;
        }
        if ((parent instanceof PsiAssignmentExpression assignmentExpression)) {
            PsiType psiType = assignmentExpression.getType();
            return psiType != null && "String".equals(psiType.getPresentableText());
        }
        if ((parent instanceof PsiNameValuePair)) {
            return true;
        }
        return false;
    }


}
