package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import dev.jeka.core.api.depmanagement.JkModuleSearch;
import dev.jeka.core.api.depmanagement.JkRepoSet;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.ModuleHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CompletionHelper {
    static String prefix(String fullText, int pos) {
        String separators = " \n";
        return prefix(fullText, pos, separators);
    }

    static String fullLine(String fullText, int pos) {
        String separators = "\n";
        return prefix(fullText, pos, separators);
    }

    private static String prefix(String fullText, int pos, String separators) {
        int i = pos;
        for (; i >= 0; i--) {
            char c = fullText.charAt(i);
            if (separators.contains(Character.toString(c))) {
                break;
            }
        }
        return fullText.substring(Math.max(0, i), pos + 1).trim();
    }

    static List<LookupElement> findDependeciesVariants(CompletionParameters parameters, String item)  {
        Module module = ModuleUtil.findModuleForFile(parameters.getOriginalFile());
        Path rootDir = ModuleHelper.getModuleDirPath(module);
        JkRepoSet repoSet = JkExternalToolApi.getDownloadRepos(rootDir);
        List<String> suggests = JkModuleSearch.of(repoSet.getRepos().get(0))
                .setGroupOrNameCriteria(item)
                .search();
        List<String> container = new ArrayList<>(suggests);
        Collections.reverse(container);
        List<LookupElement> result = new LinkedList<>();
        for (int i=0; i < container.size(); i++ ) {
            LookupElementBuilder lookupElementBuilder = LookupElementBuilder
                    .create(container.get(i))
                    .withIcon(AllIcons.Nodes.PpLibFolder);
            LookupElement prioritizedLookupElement = PrioritizedLookupElement.withExplicitProximity(
                    lookupElementBuilder, i);
            result.add(prioritizedLookupElement);
        }
        return result;
    }

    static void addElements(List<? extends LookupElement> result, List<? extends LookupElement> elements, int priority) {
        elements.forEach(lookupElement -> addElement(result, priority, lookupElement));
    }

    static void addElement(CompletionResultSet resultSet, int priority, LookupElement lookupElement) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, priority));
    }

    static void addElement(List result, int priority, LookupElement lookupElement) {
        result.add(PrioritizedLookupElement.withPriority(lookupElement, priority));
    }
}