package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.TextFieldCompletionProvider;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.ide.intellij.panel.explorer.tree.BeanNode;
import dev.jeka.ide.intellij.panel.explorer.tree.FieldNode;
import dev.jeka.ide.intellij.panel.explorer.tree.JekaToolWindowTreeService;
import dev.jeka.ide.intellij.panel.explorer.tree.MethodNode;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.stream.Collectors;

public class JekaCmdCompletionProvider extends TextFieldCompletionProvider {

    final JekaToolWindowTreeService treeService;

    @Setter
    private Module module;

    public JekaCmdCompletionProvider(Project project) {
        treeService = project.getService(JekaToolWindowTreeService.class);
    }

    @Override
    protected void addCompletionVariants(@NotNull String text, int offset, @NotNull String prefix,
                                         @NotNull CompletionResultSet result) {
        List<LookupElementBuilder> lookupElementBuilders = findFirstSuggest(prefix);
        int i= lookupElementBuilders.size();
        for (LookupElementBuilder element : lookupElementBuilders) {
            LookupElement prioritizedLookupElement = PrioritizedLookupElement.withPriority(element, i);
            result.addElement(prioritizedLookupElement);
            i--;
        }
        result.stopHere();

    }

    @Override
    public @Nullable String getPrefix(@NotNull String text, int offset) {
        int index = text.lastIndexOf(" ", offset - 1);
        return text.substring(index + 1, offset);
    }

    private List<LookupElementBuilder> findFirstSuggest(String prefix) {
        if (module == null) {
            return Collections.emptyList();
        }
        BeanComparator beanComparator = new BeanComparator();
        if (!prefix.contains("#")) {
            List<BeanNode> allBeans = treeService.getKbeans(module);
            Collections.sort(allBeans, beanComparator);
            if (allBeans.isEmpty()) {
                return Collections.emptyList();
            }
            List<LookupElementBuilder> result = new LinkedList<>();
            for (BeanNode beanNode : allBeans) {
                if (beanNode.isLocal()) {
                    result.addAll(findSuggestForBean(beanNode));
                } else {
                    result.add(LookupElementBuilder.create(beanNode.getName() + "#")
                            .withIcon(BeanNode.ICON));
                }
            }
            return result;
        };
        String beanName = JkUtilsString.substringBeforeFirst(prefix, "#");
        BeanNode bean = treeService.getKbeans(module).stream()
                .filter(beanNode -> beanNode.getName().equals(beanName))
                .findFirst().orElse(null);
        if (bean == null) {
            return Collections.emptyList();
        }
        return findSuggestForBean(bean);
    }

    private List<LookupElementBuilder> findSuggestForBean(BeanNode bean) {
        String beanName = bean.getName();
        List<LookupElementBuilder> result = new LinkedList<>();
        List<TreeNode> members = Collections.list(bean.children());
        Collections.sort(members, new MemberComparator());
        for (TreeNode member : members) {
            if (member instanceof FieldNode) {
                FieldNode fieldNode = (FieldNode) member;
                fieldNode.extend().forEach(subNode -> {
                    List<String> predefinedValues = subNode.getAcceptedValues().isEmpty() ? Collections.singletonList("")
                            : subNode.getAcceptedValues();
                    for (String value : predefinedValues) {
                        result.add(LookupElementBuilder.create(beanName + "#" + subNode.prefixedName() + "=" + value)
                                .withBoldness(bean.isLocal())
                                .withIcon(FieldNode.ICON));
                    }

                });
            } else if (member instanceof MethodNode) {
                MethodNode methodNode = (MethodNode) member;
                result.add(LookupElementBuilder.create(beanName + "#" + methodNode)
                        .withBoldness(bean.isLocal())
                        .withIcon(MethodNode.ICON)
                );
            }
        }
        return result;
    }

    private static class BeanComparator implements Comparator<BeanNode> {

        private static final List<String> UNPRIORIZEDS = JkUtilsIterable.listOf("nexus", "maven", "intellij",
                "eclipse", "git", "scaffold");

        @Override
        public int compare(BeanNode bean1, BeanNode bean2) {
            if (bean1.isLocal() && !bean2.isLocal()) {
                return -1;
            }
            if (!bean1.isLocal() && bean2.isLocal()) {
                return 1;
            }
            if (UNPRIORIZEDS.contains(bean1.getName())) {
                return 1;
            }
            if (UNPRIORIZEDS.contains(bean2.getName())) {
                return -1;
            }
            return 0;
        }
    }

    private static class MemberComparator implements Comparator<TreeNode> {

        @Override
        public int compare(TreeNode member1, TreeNode member2) {
            if (member1 instanceof MethodNode && member2 instanceof FieldNode) {
                return -1;
            }
            if (member1 instanceof MethodNode) {
                MethodNode methodNode1 = (MethodNode) member1;
                if (methodNode1.toString().equals("help")) {
                    return 1;
                }
            }
            return 0;
        }
    }
}
