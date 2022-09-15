package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class ProjectPropertiesContributor extends CompletionContributor {

    ProjectPropertiesContributor() {
        // see https://www.plugin-dev.com/intellij/custom-language/code-completion/


        // Add completion in dependencies.txt files
        ElementPattern cmdPropertyPlace = PlatformPatterns.psiElement(PsiElement.class)
                .with(new PatternCondition<PsiElement>("") {
                    @Override
                    public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
                        PsiFile psiFile = psiElement.getContainingFile();
                        if (psiFile == null) {
                            return false;
                        }
                        return psiFile.getName().equals("project.properties");
                    }
                });
        extend(CompletionType.BASIC, cmdPropertyPlace, new CmdCompletionProvider());
    }

    private static class CmdCompletionProvider extends CompletionProvider {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {

            final int pos = parameters.getOffset() -1;
            PsiElement element = parameters.getOriginalPosition();
            PsiFile psiFile = element.getContainingFile();
            String fullText = psiFile.getText();
            String lineText = CompletionHelper.fullLine(fullText, pos);
            if (!lineText.contains("=")) {
                result.addElement(LookupElementBuilder.create("jeka.java.version=")
                        .withTailText("Java version for compiling build classes and default one to compile regular code (e.g. 8, 11, 14, 17,...)"));
                result.addElement(LookupElementBuilder.create("jeka.kotlin.version=")
                        .withTailText("Kotlin version for compiling build and regular code"));
                result.addElement(LookupElementBuilder.create("jeka.jdk.XX=")
                        .withTailText("Path of the JDK version XX (e.g. jeka.jdk.17=/path/to/jdk/17)"));
                result.stopHere();
                return;
            }
            result.stopHere();
        }
    }
}
