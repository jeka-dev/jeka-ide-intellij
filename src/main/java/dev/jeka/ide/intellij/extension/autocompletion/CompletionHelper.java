package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import dev.jeka.core.api.depmanagement.JkCoordinateSearch;
import dev.jeka.core.api.depmanagement.JkRepoSet;
import dev.jeka.core.api.depmanagement.JkVersion;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.ModuleHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

    static String lastCharOfPreviousLine(String fullText, int pos) {
        int i = pos;
        for (; i >= 0; i--) {
            char c = fullText.charAt(i);
            if ("\n".contains(Character.toString(c))) {
                break;
            }
        }
        if (i <= 0) {
            return "";
        }
        return fullText.substring(i-1, i);
    }

    static List<LookupElementBuilder> findVersions(Module module, String item)  {
        Path rootDir = ModuleHelper.getModuleDirPath(module);
        JkRepoSet repoSet = JkExternalToolApi.getDownloadRepos(rootDir);
        final List<String> suggests;
        suggests = JkCoordinateSearch.of(repoSet.getRepos().get(0))
                .setGroupOrNameCriteria(item)
                .search();
        List<String> container = suggests.stream()
                .sorted(JkVersion.VERSION_COMPARATOR.reversed())
                .toList();
        List<LookupElementBuilder> result = new LinkedList<>();
        for (String fullResult : container ) {
            String version = JkUtilsString.substringAfterLast(fullResult, ":");
            LookupElementBuilder lookupElementBuilder = LookupElementBuilder
                    .create(version)
                    .withIcon(AllIcons.Nodes.PpLibFolder);
            result.add(lookupElementBuilder);
        }
        return result;
    }

    static List<LookupElementBuilder> findDependenciesVariants(Module module, String item, boolean includeDevJeka)  {
        Path rootDir = ModuleHelper.getModuleDirPath(module);
        JkRepoSet repoSet = JkExternalToolApi.getDownloadRepos(rootDir);
        final List<String> suggests;
        if (JkUtilsString.isBlank(item)) {
            suggests = popularGroups(includeDevJeka);
        } else {
            suggests = JkCoordinateSearch.of(repoSet.getRepos().get(0))
                    .setGroupOrNameCriteria(item)
                    .search();
        }
        List<LookupElementBuilder> result = new LinkedList<>();
        for (String suggest : suggests) {
            LookupElementBuilder lookupElementBuilder = LookupElementBuilder
                    .create(suggest)
                    .withIcon(AllIcons.Nodes.PpLibFolder);
            result.add(lookupElementBuilder);
        }
        return result;
    }

    static void addElements(List<? extends LookupElement> result, List<? extends LookupElement> elements, int priority) {
        for (int i = 0; i < elements.size(); i++) {
            double finePriority = ((999d - i));
            double priorityDetail = priority + finePriority; // higher priority for first elements
            addElement(result, priorityDetail, elements.get(i));
        }
    }

    static List<LookupElementBuilder> prioritized(List<? extends LookupElement> elements, int prirority) {
        List<LookupElementBuilder> result = new LinkedList<>();
        addElements(result, elements, prirority);
        return result;
    }

    static void addElement(CompletionResultSet resultSet, int priority, LookupElement lookupElement) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, priority));
    }

    static void addElement(List result, double priority, LookupElement lookupElement) {
        result.add(PrioritizedLookupElement.withPriority(lookupElement, priority));
    }


    static String cleanedPrefix(String fullLine, String prefix) {
        String propName = JkUtilsString.substringBeforeFirst(fullLine, "=");
        if (propName.isEmpty()) {
            return prefix;
        }
        boolean prefixStartsWithPropName =  prefix.startsWith(propName + "=") && !prefix.startsWith(" ");
        return  prefixStartsWithPropName ? JkUtilsString.substringAfterLast(prefix, propName + "=") : prefix;
    }

    private static List<String> popularGroups(boolean includeDevJeka) {
        List<String> result = JkUtilsIterable.listOf("org.slf4j:", "com.google.guava:", "org.mockito:", "commons-io:",
                "ch.qos.logback:", "org.apache.commons:", "com.fasterxml.jackson.core:", "org.jetbrain.kotlin:",
                "com.google.code.gson:", "log4j:", "org.projectlombok:", "javax.servlet:", "org.assertj:",
                "commons-lang:", "org.springframework:", "commons-codec:", "org.junit.jupiter:", "commons-logging:",
                "org.springframework.boot:", "com.h2database:", "org.junit:").stream().sorted().toList();
        if (includeDevJeka) {
            List<String> res = new LinkedList<>();
            res.add("dev.jeka:");
            res.addAll(result);
            result = res;
        }
        return result;
    }
}
