package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPlainText;
import com.intellij.util.ProcessingContext;
import dev.jeka.core.tool.JkExternalToolApi;
import org.jetbrains.annotations.NotNull;

public class DependenciesTxtCompletionContributor extends CompletionContributor {

    public DependenciesTxtCompletionContributor() {

        ElementPattern dependenciesTxtPlace = PlatformPatterns.psiElement(PsiPlainText.class)
                .with(new PatternCondition<PsiPlainText>("") {
            @Override
            public boolean accepts(@NotNull PsiPlainText psiPlainText, ProcessingContext context) {
                PsiFile psiFile = psiPlainText.getContainingFile();
                if (psiFile == null) {
                    return false;
                }
                return psiFile.getName().equals("project-dependencies.txt");
            }


        });
        extend(CompletionType.BASIC, dependenciesTxtPlace, new TxtDependenciesCompletionProvider());
    }

    private static class TxtDependenciesCompletionProvider extends CompletionProvider {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet resultSet) {
            final int pos = parameters.getOffset() -1;
            PsiElement element = parameters.getOriginalPosition();
            String fullText = element.getText();
            String prefix = CompletionHelper.prefix(fullText, pos);
            resultSet.withPrefixMatcher(prefix)
                    .addAllElements(CompletionHelper.findDependenciesVariants(parameters, prefix));

        }
    }


}
