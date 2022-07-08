package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import dev.jeka.core.api.depmanagement.JkDepSuggest;
import dev.jeka.core.api.depmanagement.JkModuleSearch;
import dev.jeka.core.api.depmanagement.JkRepoSet;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.core.tool.JkInjectClasspath;
import dev.jeka.ide.intellij.common.ModuleHelper;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class JavaDepCompletionContributor extends CompletionContributor {

    public JavaDepCompletionContributor() {

        // see https://www.plugin-dev.com/intellij/custom-language/code-completion/
        CompletionProvider completionProvider = new DependenciesCompletionProvider();

        final PatternCondition<PsiLiteralExpression> condition = new PatternCondition<PsiLiteralExpression>("") {

            @Override
            public boolean accepts(@NotNull PsiLiteralExpression literalExpression, ProcessingContext context) {
                Object parent = literalExpression.getParent();
                if (!parentIsCandidate(parent)) {
                    return false;
                }
                if (parent instanceof PsiMethodCallExpression) {
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

        ElementPattern place = psiElement(JavaTokenType.STRING_LITERAL).withParent(
                psiElement(PsiLiteralExpression.class).with(condition)
        );

        extend(CompletionType.BASIC, place, completionProvider);
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
            Module module = ModuleUtil.findModuleForFile(parameters.getOriginalFile());
            Path rootDir = ModuleHelper.getModuleDirPath(module);
            JkRepoSet repoSet = JkExternalToolApi.getDownloadRepos(rootDir);
            List<String> suggests = JkModuleSearch.of(repoSet.getRepos().get(0))
                    .setGroupOrNameCriteria(content)
                    .search();
            List<String> container = new ArrayList<>(suggests);
            Collections.reverse(container);
            for (int i=0; i < container.size(); i++ ) {
                LookupElementBuilder lookupElementBuilder = LookupElementBuilder
                        .create(container.get(i))
                        .withIcon(AllIcons.Nodes.PpLibFolder);
                LookupElement prioritizedLookupElement = PrioritizedLookupElement.withExplicitProximity(
                        lookupElementBuilder, i);
                resultSet.addElement(prioritizedLookupElement);
            }

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
