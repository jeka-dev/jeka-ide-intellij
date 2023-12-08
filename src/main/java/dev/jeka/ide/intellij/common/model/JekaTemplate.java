package dev.jeka.ide.intellij.common.model;

import dev.jeka.core.api.file.JkPathFile;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.api.utils.JkUtilsString;
import lombok.*;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JekaTemplate {

    private static final String SPRINGBOOT_MODULE = "dev.jeka:springboot-plugin";

    private static final String CODELESS_MENTION = """
            You can configure the build by editing :
              - local.properties file
              - project-dependencies.txt file
            """;

    @EqualsAndHashCode.Include
    String name;

    String commandArgs;

    String description;

    @Setter(AccessLevel.NONE)
    boolean builtin;


    public static final JekaTemplate blank() {
        return JekaTemplate.builder()
                .name("Blank")
                .commandArgs("")
                .description("Template for automation tasks that does not need to build projects.")
                .builtin(true)
                .build();
    }

    public static final JekaTemplate projectPureApi() {
        return JekaTemplate.builder()
                .name("Java project - Pure API")
                .commandArgs("project#scaffold.template=PURE_API" )
                .description("""
                        Build Java Projects Using Basic API
                                                
                        Use simple APIs and skip the KBean mechanism. Simpler, but may require extra typing.
                                                
                        Recommended for beginners.
                        """)
                .builtin(true)
                .build();
    }

    public static final JekaTemplate projectKBean() {
        return JekaTemplate.builder()
                .name("Java project - KBean")
                .commandArgs("project#")
                .description("""
                       Build Java Projects Using KBean and Basic API.
                                                
                       Leverage of methods and configuration options provided out-of-the-box by KBean mechanism.
                       """)
                .builtin(true)
                .build();
    }

    public static JekaTemplate projectPropsOnly() {
        String propertyFileContent = """
                jeka.classpath.inject=dev.jeka:sonarqube-plugin dev.jeka:jacoco-plugin
                jeka.default.kbean=project
                
                jeka.cmd.build=#cleanPack
                jeka.cmd.build_quality=:build sonarqube#run jacoco# sonarqube#logOutput=true
                
                jeka.java.version=21
                jacoco#jacocoVersion=0.8.11
                sonarqube#scannerVersion=5.0.1.3006
                
                sonar.host.url=http://localhost:9000
                """;
        Path tempFile = JkPathFile.ofTemp("jeka-scaffold-props-", ".properties").write(propertyFileContent).get();
        return JekaTemplate.builder()
                .name("Java project - Properties only")
                .builtin(true)
                .commandArgs("project#scaffold.template=CODE_LESS project#scaffold.generateLocalLibsFolders=false scaffold#localPropsExtraContentPath="
                        + "\"" + tempFile.toAbsolutePath() + "\""
                )
                .description("""
                        Build Java Projects Using single Property File.
                                                
                        Use the KBean mechanism with just a simple property file.
                        No need for Java code to build projects, but you can still add code for extra settings if you want.
                                                  
                        """ + CODELESS_MENTION)
                .build();
    }

    public static JekaTemplate springbootKBean() {
        return JekaTemplate.builder()
                .name("Springboot project - KBean")
                .commandArgs("@" + SPRINGBOOT_MODULE + " springboot#")
                .description("Build SpringBoot projects using KBeans.")
                .builtin(true)
                .build();
    }

    public static JekaTemplate springbootPureApi() {
        return JekaTemplate.builder()
                .name("Springboot project - Pure API")
                .commandArgs("@" + SPRINGBOOT_MODULE + " springboot# springboot#scaffoldBuildKind=PURE_API")
                .description("""
                    Build SpringBoot projects using pure API.
                    
                    RRecommended for beginners.
                    """)
                .builtin(true)
                .build();
    }

    public static JekaTemplate springbootPropsOnly() {
        String propertyFileContent = """
                jeka.classpath.inject=dev.jeka:springboot-plugin dev.jeka:sonarqube-plugin dev.jeka:jacoco-plugin
                jeka.default.kbean=dev.jeka.plugins.springboot.SpringbootJkBean
                                                
                jeka.cmd.build=project#cleanPack
                jeka.cmd.build_quality=:build sonarqube#run jacoco# sonarqube#logOutput=true
                                                
                jeka.java.version=21
                springboot#springbootVersion=3.2.0
                jacoco#jacocoVersion=0.8.11
                sonarqube#scannerVersion=5.0.1.3006
                                                
                sonar.host.url=http://localhost:9000
                """;
        Path tempFile = JkPathFile.ofTemp("jeka-scaffold-props-", ".properties").write(propertyFileContent).get();
        return JekaTemplate.builder()
                .name("Spring-Boot project - Properties only")
                .builtin(true)
                .commandArgs(
                        "@" + SPRINGBOOT_MODULE + " springboot# " +
                                "project#scaffold.template=CODE_LESS " +
                                "project#scaffold.dependenciesTxt.compile=org.springframework.boot:spring-boot-starter-web " +
                                "project#scaffold.dependenciesTxt.test=org.springframework.boot:spring-boot-starter-test " +
                                "project#scaffold.generateLocalLibsFolders=false " +
                                "scaffold#localPropsExtraContentPath="
                                + "\"" + tempFile.toAbsolutePath() + "\" "
                )
                .description("""
                        Template for building Springboot projects with properties only.
                                            
                        """ + CODELESS_MENTION)
                .build();
    }

    public static JekaTemplate plugin() {
        return JekaTemplate.builder()
                .name("Plugin project")
                .commandArgs("project#scaffold.template=PLUGIN")
                .description("Template for creating Jeka plugins.")
                .builtin(true)
                .build();
    }

    public static final List<JekaTemplate> builtins() {
        return JkUtilsIterable.listOf(blank(), projectPureApi(), projectKBean(), projectPropsOnly(),
                springbootPureApi(), springbootKBean(), springbootPropsOnly(), plugin() );
    }

    public static final Optional<JekaTemplate> getBuiltin(String name) {
        return builtins().stream()
                .filter(jekaTemplate -> name.equals(jekaTemplate.name))
                .findFirst();
    }

    private static List<String> builtinNames() {
        return builtins().stream().map(JekaTemplate::getName).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return name;
    }

    public static void resetBuiltin(List<JekaTemplate> jekaTemplates) {
        List<String> builtinNames = builtinNames();
        List<JekaTemplate> toDelete = jekaTemplates.stream()
                .filter(template -> template.builtin)
                .collect(Collectors.toList());
        toDelete.forEach(template -> jekaTemplates.remove(template));
        List<JekaTemplate> builtins = new LinkedList<>(builtins());
        Collections.reverse(builtins);
        for (JekaTemplate jekaTemplate : builtins) {
            jekaTemplates.add(0, jekaTemplate);
        }
    }

    public static JekaTemplate duplicate(List<JekaTemplate> jekaTemplates, JekaTemplate template) {
        NameAndNum nameAndNum = suggestNewName(jekaTemplates, template.getName());
        JekaTemplate newTemplate = JekaTemplate.builder()
                .name(nameAndNum.name)
                .commandArgs(template.getCommandArgs())
                .description(template.getDescription())
                .build();
        int index = nameAndNum.num <= 0 ? indexOfName(jekaTemplates, template.name) + 1 : nameAndNum.num;
        jekaTemplates.add(index, newTemplate);
        return newTemplate;
    }

    public static String suggestNewName(List<JekaTemplate> templates) {
        return suggestNewName(templates, "untitled").name;
    }

    private static NameAndNum suggestNewName(List<JekaTemplate> templates, String original) {
        if (JkUtilsString.isBlank(original)) {
            original = "untitled";
        }
        if (indexOfName(templates, original) < 0) {
            return new NameAndNum(original, templates.size());
        }
        NameAndNum nameAndNum = NameAndNum.of(original);
        String name = nameAndNum.name;
        int lastIndexOfExistingName = -1;
        for (int i = 1; i < 100; i++) {
           NameAndNum nameAndNumI = new NameAndNum(name, i);
           String candidate = nameAndNumI.toString();
           int indexOfExistingName = indexOfName(templates, candidate);
           if (indexOfExistingName < 0) {
               return new NameAndNum(candidate, lastIndexOfExistingName + 1);
           } else {
               lastIndexOfExistingName = indexOfExistingName;
           }
        }
        throw new IllegalStateException("No suggest name found.");
    }

    private static int indexOfName(List<JekaTemplate> templates, String candidate) {
        for (int i=0; i < templates.size(); i++) {
            if (templates.get(i).name.equals(candidate)) {
                return i;
            }
        }
        return -1;
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
            String name = JkUtilsString.substringBeforeLast(candidate, "(").trim();
            return new NameAndNum(name, num);
        }

        private static Integer extract(String candidate) {
            int openIndex = candidate.lastIndexOf('(');
            int closeIndex = candidate.lastIndexOf(')');
            if (openIndex < 0 || closeIndex < openIndex+2 ) {
                return null;
            }
            String between = candidate.substring(openIndex + 1, closeIndex);
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
