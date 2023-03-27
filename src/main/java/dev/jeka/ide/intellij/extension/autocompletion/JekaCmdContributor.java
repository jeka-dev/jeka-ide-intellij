package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class JekaCmdContributor extends CompletionContributor {

    JekaCmdContributor() {
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
                        return psiFile.getName().equals("cmd.properties");
                    }


                });
        extend(CompletionType.BASIC, cmdPropertyPlace, new CmdCompletionProvider());
    }

    private static class CmdCompletionProvider extends CompletionProvider {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {

            final int pos = parameters.getOffset() -1;
            PsiFile psiFile = parameters.getOriginalFile();
            String fullText = psiFile.getText();
            String lineText = CompletionHelper.fullLine(fullText, pos);
            String lastCharOfPrevoiusLine = CompletionHelper.lastCharOfPreviousLine(fullText, pos);
            List<LookupElementBuilder> elements = new LinkedList<>();
            if (lineText.trim().isEmpty()) {
                CompletionHelper.addElement(elements, 100, LookupElementBuilder.create("_append=")
                        .withTailText("append the following text to all commands definined here. Typically used to add plugins in classpath or logging parameters"));
            }
            CompletionHelper.addElement(elements, 10, LookupElementBuilder.create("@dev.jeka:springboot-plugin")
                    .withIcon(AllIcons.Nodes.PpLibFolder)
                    .withTailText(" Add Springboot plugin"));
            CompletionHelper.addElement(elements, 10,LookupElementBuilder.create("@dev.jeka:sonarqube-plugin")
                    .withIcon(AllIcons.Nodes.PpLibFolder)
                    .withTailText(" Add sonarQube plugin"));
            CompletionHelper.addElement(elements, 10,LookupElementBuilder.create("@dev.jeka:jacoco-plugin")
                    .withIcon(AllIcons.Nodes.PpLibFolder)
                    .withTailText(" Add Jacoco plugin"));
            CompletionHelper.addElement(elements, 9,LookupElementBuilder.create("@dev.jeka:nodejs-plugin")
                    .withIcon(AllIcons.Nodes.PpLibFolder)
                    .withTailText(" Add NodeJS plugin"));
            CompletionHelper.addElement(elements, 10, LookupElementBuilder.create("@dev.jeka:kotlin-plugin")
                    .withIcon(AllIcons.Nodes.PpLibFolder)
                    .withTailText(" Add Kotlin plugin"));
            CompletionHelper.addElement(elements, 8, LookupElementBuilder.create("@dev.jeka:protobuf-plugin")
                    .withIcon(AllIcons.Nodes.PpLibFolder)
                    .withTailText(" Add Protobuf plugin"));


            String rawPrefix = CompletionHelper.prefix(fullText, pos);
            boolean breakingLine = "\\".equals(lastCharOfPrevoiusLine);
            String prefix = breakingLine ? rawPrefix : CompletionHelper.cleanedPrefix(lineText, rawPrefix);
            Module module =  ModuleUtil.findModuleForFile(parameters.getOriginalFile());
            elements.addAll( JekaCmdCompletionProvider.findSuggest(module, prefix, true));
            result.withPrefixMatcher(prefix).addAllElements(elements);
            result.stopHere();
        }


    }
}
