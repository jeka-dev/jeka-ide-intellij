package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import dev.jeka.core.api.depmanagement.JkVersion;
import dev.jeka.ide.intellij.common.JekaDistributions;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class WrapperCompletionContributor extends CompletionContributor {

    WrapperCompletionContributor() {
        // see https://www.plugin-dev.com/intellij/custom-language/code-completion/


        // Add completion in dependencies.txt files
        ElementPattern place = PlatformPatterns.psiElement(PsiElement.class)
                .with(new PatternCondition<PsiElement>("") {
                    @Override
                    public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
                        PsiFile psiFile = psiElement.getContainingFile();
                        if (psiFile == null) {
                            return false;
                        }
                        return psiFile.getName().equals("wrapper.properties");
                    }


                });
        extend(CompletionType.BASIC, place, new WrapperCompletionProvider());
    }

    private static class WrapperCompletionProvider extends CompletionProvider {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {

            final int pos = parameters.getOffset() -1;
            PsiFile psiFile = parameters.getOriginalFile();
            String fullText = psiFile.getText();
            List<LookupElementBuilder> elements = new LinkedList<>();
            String prefix = CompletionHelper.prefix(fullText, pos);
            if (prefix.startsWith("jeka.version=")) {
                List<String> jekaVersions =
                JekaDistributions.searchVersionsSortedByDesc().stream()
                        .sorted(JkVersion.VERSION_COMPARATOR.reversed())
                        .toList();
                for (int i=0; i < jekaVersions.size(); i++) {
                    String version = jekaVersions.get(i);
                    CompletionHelper.addElement(elements, 1000-i,
                            LookupElementBuilder.create("jeka.version=" + version)
                            .withPresentableText(version));
                }

            } else {
                CompletionHelper.addElement(elements, 100, LookupElementBuilder.create("jeka.version=")
                        .withTailText(" Jeka version used by the wrapper"));
                CompletionHelper.addElement(elements, 100, LookupElementBuilder.create("jeka.distrib.repo=")
                        .withTailText(" Optional. A specific repo url to fetch jeka distributions (e.g. https://maven.pkg.github.com/jeka-dev/jeka"));
                CompletionHelper.addElement(elements, 100, LookupElementBuilder.create("jeka.distrib.location=")
                        .withTailText(" Optional. A file system location for Jeka distribution to use (e.g. c:/software/jeka)"));
            }
            result.withPrefixMatcher(prefix).addAllElements(elements);
            result.stopHere();
        }


    }
}
