package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.intellij.util.lang.JavaVersion;
import dev.jeka.core.api.depmanagement.JkCoordinateSearch;
import dev.jeka.core.api.depmanagement.JkRepoSet;
import dev.jeka.core.api.java.JkJavaVersion;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.ModuleHelper;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
            PsiFile psiFile = parameters.getOriginalFile();
            String fullText = psiFile.getText();
            String lineText = CompletionHelper.fullLine(fullText, pos);
            Module module = ModuleUtil.findModuleForFile(psiFile);
            String rawPrefix = CompletionHelper.prefix(fullText, pos);
            String lastCharOfPrevoiusLine = CompletionHelper.lastCharOfPreviousLine(fullText, pos);
            boolean breakingLine = "\\".equals(lastCharOfPrevoiusLine);
            String prefix = breakingLine ? rawPrefix : CompletionHelper.cleanedPrefix(lineText, rawPrefix);
            if (!lineText.contains("=")) {
                CompletionHelper.addElement(result, 1010, LookupElementBuilder.create("jeka.java.version=")
                        .withTailText("Java version for compiling build classes and default one to compile regular code (e.g. 8, 11, 14, 17,...)"));
                CompletionHelper.addElement(result, 1000, LookupElementBuilder.create("jeka.kotlin.version=")
                        .withTailText("Kotlin version for compiling build and regular code"));
                List<LookupElementBuilder> suggests = JekaCmdCompletionProvider.findSuggest(module, prefix, false);
                for (int i = 0; i < suggests.size(); i++) {
                    CompletionHelper.addElement(result, suggests.size() - i, suggests.get(i));
                }
                result.stopHere();
                return;
            }
            if (lineText.trim().equals("jeka.java.version=")) {
                List<String> versions = JkUtilsIterable.listOf("17", "11", "8", "19", "18");
                for (int i = 0; i < versions.size(); i++) {
                    CompletionHelper.addElement(result, versions.size() - i, LookupElementBuilder.create(versions.get(i)));
                }
            } else if (lineText.trim().equals("jeka.kotlin.version=")) {
                Path rootDir = ModuleHelper.getModuleDirPath(module);
                JkRepoSet repoSet = JkExternalToolApi.getDownloadRepos(rootDir);
                List<String> versions = JkCoordinateSearch.of(repoSet.getRepos().get(0))
                        .setGroupOrNameCriteria("org.jetbrains.kotlin:kotlin-stdlib:")
                        .search()
                        .stream()
                        .map(value -> JkUtilsString.substringAfterLast(value, ":"))
                        .collect(Collectors.toList());
                for (int i = 0; i < versions.size(); i++) {
                    CompletionHelper.addElement(result, versions.size() - i, LookupElementBuilder.create(versions.get(i)));
                }
            } else if (lineText.trim().equals("springboot#springbootVersion=")) {
                Path rootDir = ModuleHelper.getModuleDirPath(module);
                JkRepoSet repoSet = JkExternalToolApi.getDownloadRepos(rootDir);
                List<String> versions = JkCoordinateSearch.of(repoSet.getRepos().get(0))
                        .setGroupOrNameCriteria("org.springframework.boot:spring-boot-starter-parent:")
                        .search()
                        .stream()
                        .map(value -> JkUtilsString.substringAfterLast(value, ":"))
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());
                for (int i = 0; i < versions.size(); i++) {
                    CompletionHelper.addElement(result, versions.size() - i, LookupElementBuilder.create(versions.get(i)));
                }
            }
            result.stopHere();
        }
    }
}
