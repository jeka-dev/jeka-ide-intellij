package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.properties.psi.impl.PropertyKeyImpl;
import com.intellij.lang.properties.psi.impl.PropertyValueImpl;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import dev.jeka.core.api.depmanagement.JkCoordinateSearch;
import dev.jeka.core.api.depmanagement.JkRepoSet;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.JdksHelper;
import dev.jeka.ide.intellij.common.ModuleHelper;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LocalPropertiesContributor extends CompletionContributor {

    LocalPropertiesContributor() {
        // see https://www.plugin-dev.com/intellij/custom-language/code-completion/


        // Add completion in local.properties files
        ElementPattern cmdPropertyPlace = PlatformPatterns.psiElement(PsiElement.class)
                .with(new PatternCondition<PsiElement>("") {
                    @Override
                    public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
                        PsiFile psiFile = psiElement.getContainingFile();
                        if (psiFile == null) {
                            return false;
                        }
                        return psiFile.getName().equals(JkConstants.PROPERTIES_FILE);
                    }
                });
        extend(CompletionType.BASIC, cmdPropertyPlace, new CmdCompletionProvider());
    }

    private static class CmdCompletionProvider extends CompletionProvider {

        private final Map<String, List<Entry>> keySuggests = new LinkedHashMap<>();

        private final Map<String, Function<Module, List<String>>> valueSuggests = new LinkedHashMap<>();

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {

            if (parameters.isAutoPopup()) {
                return;  // only perform auto-completion on explicit crt+space
            }

            // Psi element of the cursor. Can be propertyKey, equals symbol or propertyValue
            PropertyContext propertyContext = new PropertyContext(parameters);
            int index = 1000;
            init(propertyContext.module);
            if (propertyContext.isKey() && propertyContext.prefix.isEmpty()) {

                // Add suggest for toplevel keys as jeka.java.version
                for (Entry entry : keySuggests.getOrDefault("", new LinkedList<>())) {
                    CompletionHelper.addElement(result, index, LookupElementBuilder.create(entry.name)
                            .withTailText(entry.desc));
                    index--;
                }

                // Add suggest for first level keys as jeka.repos...
                for (String keyPrefix : keySuggests.keySet()) {
                    if (keyPrefix.isEmpty()) {
                        continue;
                    }
                    CompletionHelper.addElement(result, index, LookupElementBuilder.create(keyPrefix)
                            .withPresentableText(keyPrefix + "..."));
                    index--;
                }
            }

            if (propertyContext.isKey()) {

                // add key suggest as 'jeka.repos.download='
                for (String keyPrefix : keySuggests.keySet()) {
                    if (propertyContext.getItemFullText().trim().startsWith(keyPrefix)) {
                        for (Entry entry : keySuggests.getOrDefault(keyPrefix, new LinkedList<>())) {
                            CompletionHelper.addElement(result, index, LookupElementBuilder.create(entry.name)
                                    .withTailText(entry.desc));
                            index--;
                        }
                    }
                }

                // add key suggest for KBeans
                List<LookupElementBuilder> suggests = JekaCmdCompletionProvider.findSuggest(propertyContext.module,
                        propertyContext.prefix, false);
                for (int i = 0; i < suggests.size(); i++) {
                    CompletionHelper.addElement(result, suggests.size() - i, suggests.get(i));
                }
                result.stopHere();
                return;
            }

            // Suggest predefined values
            for (String key : valueSuggests.keySet()) {
                if (propertyContext.fullKey().equals(key)) {
                    List<String> suggests = valueSuggests.get(key).apply(propertyContext.module);
                    for (int i = 0; i < suggests.size(); i++) {
                        CompletionHelper.addElement(result, suggests.size() - i, LookupElementBuilder.create(suggests.get(i)));
                    }
                }
            }

            // suggest for -kb=
            if ("-kb=".equals(propertyContext.prefix)) {
                List<LookupElementBuilder> suggests = JekaCmdCompletionProvider.findKBeanSuggests(propertyContext.module,
                        result.getPrefixMatcher().getPrefix());
                for (int i = 0; i < suggests.size(); i++) {
                    CompletionHelper.addElement(result, suggests.size() - i, suggests.get(i));
                }
                result.stopHere();
                return;
            }

            // suggest for jeka.default.kbean
            if (!propertyContext.isKey() && JkConstants.DEFAULT_KBEAN_PROP.equals(propertyContext.fullKey())) {
                List<LookupElementBuilder> suggests = JekaCmdCompletionProvider.findKBeanSuggests(propertyContext.module,
                        "");
                for (int i = 0; i < suggests.size(); i++) {
                    CompletionHelper.addElement(result, suggests.size() - i, suggests.get(i));
                }
                result.stopHere();
                return;
            }

            // suggest for jeka.classpath.inject
            if (!propertyContext.isKey() && JkConstants.CLASSPATH_INJECT_PROP.equals(propertyContext.fullKey())) {
                String hint = propertyContext.prefix;
                List<LookupElementBuilder> lookups = CompletionHelper.findDependenciesVariants(propertyContext.module, hint, true);
                result.withPrefixMatcher(hint).addAllElements(lookups);
                result.stopHere();;
                return;
            }



            addValuesCompletions(parameters, propertyContext.lineText, result);
        }

        private void init(Module module) {
            suggestKey("", "jeka.java.version=", "Java version to compile build and regular (e.g. 8, 11, 14, 17,...)");
            suggestKey("", "jeka.kotlin.version=", "Kotlin version to compile build and regular code");
            suggestKey("", "jeka.classpath.inject=", "Space separated list of dependencies to add to classpath");
            suggestKey("", "jeka.default.kbean=", "KBean to use when omitting KBean prefixes");
            suggestKey("", "jeka.jdk.xx=", "Path of the JDK to use for version XX. Example : jeka.jdk.17=/software/jdks/corretto-17.0.5");
            suggestKey("jeka.cmd.", "_append=", "append the following text to all commands definined here. Typically used to add plugins in classpath or logging parameters");
            suggestKey("jeka.cmd.", "xxx=", "Define a command shortcut, callable using 'jeka :xxx'");
            suggestKey("jeka.repos.", "download=", "Comma separated repository urls (or repo reference) to download artefacts (default is Maven central)." );
            suggestKey("jeka.repos.", "download.username=", "Username credential to connect to download repo." );
            suggestKey("jeka.repos.", "download.password=", "Password credential to connect to download repo." );
            suggestKey("jeka.repos.", "download.headers.[httpHeaderName]=", "HTTP header to include in request towards the repo" );
            suggestKey("jeka.repos.", "publish=", "Comma separated repository urls for publish repos." );
            suggestKey("jeka.repos.", "publish.username=", "Username credential to connect to publish repo." );
            suggestKey("jeka.repos.", "publish.password=", "Password credential to connect to publish repo." );
            suggestKey("jeka.repos.", "publish.headers.[httpHeaderName]=", "HTTP header to include in request towards the repo" );
            suggestKey("jeka.repos.", "[repoName]=", "Comma separated of repository names to be used as a set for the named repo." );
            suggestKey("jeka.repos.", "[repoName].username=", "Username credential to connect to repo." );
            suggestKey("jeka.repos.", "[repoName].password=", "Password credential to connect to repo." );
            suggestKey("jeka.repos.", "[repoName].headers.[httpHeaderName]=", "HTTP header to include in request towards the repo." );

            suggestValues("jeka.java.version", "21", "17", "11", "8");
            suggestValuesWithVersions("jeka.kotlin.version", module, "org.jetbrains.kotlin:kotlin-stdlib");

            valueSuggests.put("intellij#jdkName", module1 -> JdksHelper.availableSdkNames());
        }

        private void suggestKey(String prefix, String name, String desc) {
            keySuggests.putIfAbsent(prefix, new LinkedList<>());
            keySuggests.get(prefix).add(new Entry(prefix + name, desc));
        }

        private void suggestValues(String key, String ... values) {
            valueSuggests.put(key , module1 -> Arrays.asList(values));
        }

        private void suggestValuesWithVersions(String key, Module module, String moduleId) {
            valueSuggests.put(key , module1 -> getSuggestVersionFor(module,moduleId));
        }

        private List<String> getSuggestVersionFor(Module module, String moduleId) {
            Path rootDir = ModuleHelper.getModuleDirPath(module);
            JkRepoSet repoSet = JkExternalToolApi.getDownloadRepos(rootDir);
            return JkCoordinateSearch.of(repoSet.getRepos().get(0))
                    .setGroupOrNameCriteria(moduleId + ":")
                    .search()
                    .stream()
                    .map(value -> JkUtilsString.substringAfterLast(value, ":"))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
        }

        @Value
        static class Entry {
            String name;
            String desc;
        }

        private void addValuesCompletions(@NotNull CompletionParameters parameters, String lineText,
                                      @NotNull CompletionResultSet result) {

            final int pos = parameters.getOffset() -1;
            PsiFile psiFile = parameters.getOriginalFile();
            String fullText = psiFile.getText();
            String lastCharOfPreviousLine = CompletionHelper.lastCharOfPreviousLine(fullText, pos);

            List<LookupElementBuilder> elements = new LinkedList<>();
            if (lineText.trim().startsWith("jeka.cmd.")) {
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
                CompletionHelper.addElement(elements, 9,LookupElementBuilder.create("@dev.jeka:protobuf-plugin")
                        .withIcon(AllIcons.Nodes.PpLibFolder)
                        .withTailText(" Add Protobuf plugin"));
                CompletionHelper.addElement(elements, 9,LookupElementBuilder.create("@dev.jeka:kotlin-plugin")
                        .withIcon(AllIcons.Nodes.PpLibFolder)
                        .withTailText(" Add Kotlin plugin"));
                String rawPrefix = CompletionHelper.prefix(fullText, pos);
                boolean breakingLine = "\\".equals(lastCharOfPreviousLine);
                String prefix = breakingLine ? rawPrefix : CompletionHelper.cleanedPrefix(lineText, rawPrefix);
                Module module =  ModuleUtil.findModuleForFile(parameters.getOriginalFile());
                elements.addAll( JekaCmdCompletionProvider.findSuggest(module, prefix, true));
                result.withPrefixMatcher(prefix).addAllElements(elements);

                // Add deps suggest if starting with @
                if (prefix.startsWith("@")) {
                    String hint = prefix.substring(1);
                    List<LookupElementBuilder> lookups = CompletionHelper.findDependenciesVariants(module, hint, true);
                    result.withPrefixMatcher(hint).addAllElements(lookups);
                    result.stopHere();;
                    return;
                }


            } else if (isStartingWithKBeanField(lineText)) {
                Module module =  ModuleUtil.findModuleForFile(parameters.getOriginalFile());
                String prefix = lineText.trim();
                List<LookupElementBuilder> lookupElements =
                        JekaCmdCompletionProvider.findSuggest(module, prefix, false);
                List<LookupElementBuilder> prioritizedLookupElements = CompletionHelper.prioritized(lookupElements, 1);
                elements.addAll(prioritizedLookupElements);
                result.withPrefixMatcher(prefix).addAllElements(elements);
            }
            result.stopHere();
        }

    }

    private static boolean isStartingWithKBeanField(String line) {
        return line.trim().endsWith("=") && line.contains("#");
    }

    private static class PropertyContext {

        final CompletionParameters parameters;

        private final PsiElement orighinalCursorPsiElement;

        final Module module;

        final String prefix;

        final String lineText;


        public PropertyContext(CompletionParameters parameters) {
            this.parameters = parameters;
            final int pos = parameters.getOffset() -1;
            orighinalCursorPsiElement = parameters.getOriginalPosition();
            PsiFile psiFile = parameters.getOriginalFile();
            String fullText = psiFile.getText();
            lineText = CompletionHelper.fullLine(fullText, pos);
            module = ModuleUtil.findModuleForFile(psiFile);
            String rawPrefix = CompletionHelper.prefix(fullText, pos);
            String lastCharOfPreviousLine = CompletionHelper.lastCharOfPreviousLine(fullText, pos);
            boolean breakingLine = "\\".equals(lastCharOfPreviousLine);
            prefix = breakingLine ? rawPrefix : CompletionHelper.cleanedPrefix(lineText, rawPrefix);
        }

        boolean isKey() {
            if (orighinalCursorPsiElement instanceof PropertyValueImpl) {
                return false;
            }
            return orighinalCursorPsiElement instanceof PropertyKeyImpl || lineText.isEmpty();
        }

        String getItemFullText() {
            if (orighinalCursorPsiElement == null) {
                return "";
            }
            return orighinalCursorPsiElement.getText();
        }

        // return the full key. If the cursor is in a value, it returns the matching key
        String fullKey() {
            if (isKey()) {
                return getItemFullText();
            }
            if ((orighinalCursorPsiElement instanceof PropertyValueImpl)) {
               return orighinalCursorPsiElement.getParent().getFirstChild().getText();
            }
            if (lineText.trim().startsWith("#")) {
                return "";
            }
            if (lineText.contains("=")) {
                return JkUtilsString.substringBeforeFirst(lineText, "=");
            }
            return lineText;
        }



    }

}
