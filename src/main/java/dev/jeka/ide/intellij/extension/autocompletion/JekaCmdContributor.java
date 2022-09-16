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
import dev.jeka.core.api.utils.JkUtilsString;
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
            String rawPrefix = CompletionHelper.prefix(fullText, pos);
            String prefix = cleanedPrefix(lineText, rawPrefix);
            Module module =  ModuleUtil.findModuleForFile(parameters.getOriginalFile());
            elements.addAll( JekaCmdCompletionProvider.findSuggest(module, prefix));
            result.withPrefixMatcher(prefix).addAllElements(elements);
            result.stopHere();
        }

        private String cleanedPrefix(String fullLine, String prefix) {
            String propName = JkUtilsString.substringBeforeFirst(fullLine, "=");
            if (propName.isEmpty()) {
                return prefix;
            }
            boolean prefixStartsWithPropName =  prefix.startsWith(propName + "=");
            return  prefixStartsWithPropName ? JkUtilsString.substringAfterLast(prefix, propName + "=") : prefix;
        }
    }
}
