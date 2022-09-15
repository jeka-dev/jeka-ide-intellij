package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

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
            if (lineText.startsWith(("_append="))) {
                result.addElement(LookupElementBuilder.create("@dev.jeka:springboot-plugin")
                        .withIcon(AllIcons.Nodes.PpLibFolder)
                        .withTailText(" Add Springboot plugin"));
                result.addElement(LookupElementBuilder.create("@dev.jeka:sonarqube-plugin")
                        .withIcon(AllIcons.Nodes.PpLibFolder)
                        .withTailText(" Add sonarQube plugin"));
                result.addElement(LookupElementBuilder.create("@dev.jeka:jacoco-plugin")
                        .withIcon(AllIcons.Nodes.PpLibFolder)
                        .withTailText(" Add Jacoco plugin"));
            }
            if (!lineText.contains("=")) {
                result.addElement(LookupElementBuilder.create("_append=")
                        .withTailText("append the following text to all commands definined here. Typically used to add plugins in classpath or logging parameters"));
                result.stopHere();
                return;
            }
            String prefix = CompletionHelper.prefix(fullText, pos);
            Module module =  ModuleUtil.findModuleForFile(parameters.getOriginalFile());
            List<LookupElementBuilder> lookupElementBuilders = JekaCmdCompletionProvider.findSuggest(module, prefix);
            result.withPrefixMatcher(prefix).addAllElements(lookupElementBuilders);
            result.stopHere();
        }
    }
}
