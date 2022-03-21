package dev.jeka.ide.intellij.common.model;

import com.intellij.ui.CollectionListModel;
import dev.jeka.core.api.marshalling.xml.JkDomDocument;
import dev.jeka.core.api.marshalling.xml.JkDomElement;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.api.utils.JkUtilsString;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JekaTemplate {

    private static final String SPRINGBOOT_MODULE = "dev.jeka:springboot-plugin";

    public static final JekaTemplate BLANK = JekaTemplate.builder()
            .name("Blank")
            .commandArgs("")
            .description("Template for automation tasks that does not need to build projects.")
            .build();

    public static final JekaTemplate PROJECT = JekaTemplate.builder()
            .name("Java project")
            .commandArgs("project#")
            .description("Template for building Java projects.")
            .build();

    public static final JekaTemplate SPRINGBOOT = JekaTemplate.builder()
            .name("Springboot project")
            .commandArgs("@" + SPRINGBOOT_MODULE + " springboot#")
            .description("Template for building SpringBoot projects written in Java.")
            .build();

    public static final JekaTemplate PLUGIN = JekaTemplate.builder()
            .name("Plugin project")
            .commandArgs("project#scaffoldTemplate=PLUGIN")
            .description("Template for creating Jeka plugins.")
            .build();

    public static final List<JekaTemplate> BUILT_IN = JkUtilsIterable.listOf(BLANK, PROJECT, SPRINGBOOT, PLUGIN);

    private static final List<String> BUILT_IN_NAMES = BUILT_IN.stream().map(JekaTemplate::getName).collect(Collectors.toList());

    @EqualsAndHashCode.Include
    String name;

    String commandArgs;

    String description;

    @Override
    public String toString() {
        return name;
    }

    public static void resetBuiltin(List<JekaTemplate> jekaTemplates) {
        List<JekaTemplate> toDelete = jekaTemplates.stream()
                .filter(template -> BUILT_IN_NAMES.contains(template.getName()))
                .collect(Collectors.toList());
        toDelete.forEach(template -> jekaTemplates.remove(template));
        List<JekaTemplate> builtins = new LinkedList<>(BUILT_IN);
        Collections.reverse(builtins);
        for (JekaTemplate jekaTemplate : builtins) {
            jekaTemplates.add(0, jekaTemplate);
        }
    }

    public static void addOrReplace(CollectionListModel<JekaTemplate> jekaTemplates, JekaTemplate template) {
        int index = jekaTemplates.getElementIndex(template);
        if (index >= 0) {
            jekaTemplates.remove(index);
            jekaTemplates.add(index, template);
        } else {
            jekaTemplates.add(template);
        }
    }

    public static JekaTemplate duplicate(List<JekaTemplate> jekaTemplates, JekaTemplate template) {
        int index = jekaTemplates.indexOf(template);
        JekaTemplate newTemplate = JekaTemplate.builder()
                .name(suggestNewName(jekaTemplates, template.getName()))
                .commandArgs(template.getCommandArgs())
                .description(template.getDescription())
                .build();
        jekaTemplates.add(index + 1, newTemplate);
        return newTemplate;
    }

    public static String suggestNewName(List<JekaTemplate> templates, String original) {
        if (JkUtilsString.isBlank(original)) {
            original = "untitled";
        }
        if (!hasName(templates, original)) {
            return original;
        }
        NameAndNum nameAndNum = NameAndNum.of(original);
        String name = nameAndNum.name;
        for (int i = 1; i < 100; i++) {
           NameAndNum nameAndNum1 = new NameAndNum(name, i);
           String candidate = nameAndNum.toString();
           if (!hasName(templates, candidate)) {
               return candidate;
           }
        }
        throw new IllegalStateException("No suggest name found.");
    }

    private static boolean hasName(List<JekaTemplate> templates, String candidate) {
        return templates.stream().anyMatch(template -> template.getName().equals(candidate));
    }

    @Value
    private static class NameAndNum {
        String name;
        Integer num;

        static NameAndNum of(String candidate) {
            Integer num = extract(candidate);
            if (num == null) {
                return new NameAndNum(candidate, null);
            }
            String name = JkUtilsString.substringAfterLast(candidate, "(").trim();
            return new NameAndNum(name, num);
        }

        private static Integer extract(String candidate) {
            int openIndex = candidate.lastIndexOf('(');
            int closeIndex = candidate.lastIndexOf(')');
            if (openIndex < 0 || closeIndex < openIndex+2 ) {
                return null;
            }
            String between = candidate.substring(openIndex, closeIndex);
            try {
               return Integer.parseInt(between);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        public String toString() {
            return name + " (" + num + ")";
        }
    }

}
