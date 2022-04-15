package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.impl.CompletionSorterImpl;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import dev.jeka.core.api.depmanagement.JkDepSuggest;
import dev.jeka.core.api.depmanagement.JkModuleSearch;
import dev.jeka.core.api.depmanagement.JkRepo;
import dev.jeka.core.api.depmanagement.JkRepoSet;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.ModuleHelper;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
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
                if (parent == null || !(parent instanceof PsiExpressionList)) {
                    return false;
                }
                PsiExpressionList expressionList = (PsiExpressionList) parent;
                parent = expressionList.getParent();
                if (parent == null || !(parent instanceof PsiMethodCallExpression)) {
                    return false;
                }
                PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) parent;
                PsiReferenceExpression method = methodCallExpression.getMethodExpression();
                PsiMethod psiMethod = (PsiMethod) method.getLastChild().findReferenceAt(0).resolve();
                PsiParameterList parameterList = psiMethod.getParameterList();
                int i = 0;
                for (; i < expressionList.getExpressionCount(); i++) {
                    if (expressionList.getExpressions()[i] == literalExpression) {
                        break;
                    }
                }
                Object jkCoordinateAutoComplete = null;
                if (parameterList.getParameters().length > 0) {
                    jkCoordinateAutoComplete = psiMethod.getParameterList().getParameter(0).getAnnotation(JkDepSuggest.class.getName());
                }
                return jkCoordinateAutoComplete != null;
            }
        };

        ElementPattern place = psiElement(JavaTokenType.STRING_LITERAL).withParent(
                psiElement(PsiLiteralExpression.class).with(condition)
        );

        extend(CompletionType.BASIC, place, completionProvider);
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
            List<String> container = new LinkedList<>(suggests);
            Collections.reverse(container);
            container.stream().forEach(item ->
                    resultSet.addElement(LookupElementBuilder.create(item)
                            .withIcon(AllIcons.Nodes.PpLibFolder)));
        }
    }

}
