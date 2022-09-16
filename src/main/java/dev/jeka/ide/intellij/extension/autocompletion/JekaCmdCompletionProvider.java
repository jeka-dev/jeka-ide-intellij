package dev.jeka.ide.intellij.extension.autocompletion;

import com.google.common.base.Strings;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.util.TextFieldCompletionProvider;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.ide.intellij.panel.explorer.tree.BeanNode;
import dev.jeka.ide.intellij.panel.explorer.tree.FieldNode;
import dev.jeka.ide.intellij.panel.explorer.tree.JekaToolWindowTreeService;
import dev.jeka.ide.intellij.panel.explorer.tree.MethodNode;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class JekaCmdCompletionProvider extends TextFieldCompletionProvider {

    @Setter
    private Module module;

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
        return findSuggest(module, prefix);
    }

    static List<LookupElementBuilder> findSuggest(Module module, String prefix) {
        if (module == null) {
            return Collections.emptyList();
        }
        final JekaToolWindowTreeService treeService = module.getProject().getService(JekaToolWindowTreeService.class);
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
                    result.addAll(findSuggestForBean(beanNode, prefix));
                } else {
                    CompletionHelper.addElement(result, 10, LookupElementBuilder.create(beanNode.getName() + "#")
                            .withTailText(" " + Strings.nullToEmpty(beanNode.getDefinition()))
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
        return findSuggestForBean(bean, prefix);
    }

    private static List<LookupElementBuilder> findSuggestForBean(BeanNode bean, String prefix) {
        String beanName = bean.getName();
        List<LookupElementBuilder> result = new LinkedList<>();
        List<TreeNode> members = Collections.list(bean.children());
        Collections.sort(members, new MemberComparator());
        for (TreeNode member : members) {
            if (member instanceof FieldNode) {
                FieldNode fieldNode = (FieldNode) member;
                List<LookupElementBuilder> fieldElements = createFieldElements(fieldNode, prefix);
                CompletionHelper.addElements(result, fieldElements, 20);
            } else if (member instanceof MethodNode) {
                MethodNode methodNode = (MethodNode) member;
                CompletionHelper.addElement(result, 30, LookupElementBuilder.create(beanName + "#" + methodNode)
                        .withBoldness(bean.isLocal())
                        .withPresentableText(methodNode.toString())
                        .withTailText(" " + Strings.nullToEmpty(methodNode.getTooltipText()))
                        .withIcon(MethodNode.ICON)
                );
            }
        }
        return result;
    }

    private static List<LookupElementBuilder> createFieldElements(FieldNode fieldNode, String prefix) {
        String fieldPrefix = JkUtilsString.substringAfterLast(prefix, "#");
        if (fieldNode.isLeaf()) {
            return fieldElements(fieldNode, prefix);
        }
        List<LookupElementBuilder> result = fieldNode.extend().stream()
                .filter(subNode -> fieldPrefix.startsWith(subNode.getCloserParentOfType(FieldNode.class).prefixedName()) )
                .flatMap(subnode -> fieldElements(subnode, prefix).stream())
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            return Collections.singletonList(fieldElement(fieldNode, "."));
        }
        return result;
    }

    private static List<LookupElementBuilder> fieldElements(FieldNode fieldNode, String prefix) {
        List<String> predefinedValues = fieldNode.getAcceptedValues();
        if (!predefinedValues.isEmpty() && prefix.contains("=")) {
            return predefinedValues.stream()
                    .map(value -> fieldElement(fieldNode, "=" + value))
                    .collect(Collectors.toList());
        }
        return Collections.singletonList(fieldElement(fieldNode, "="));
    }

    private static LookupElementBuilder fieldElement(FieldNode fieldNode, String suffix) {
        BeanNode beanNode = fieldNode.getCloserParentOfType(BeanNode.class);
        boolean showValue = suffix.contains("=") && !suffix.endsWith("=");
        String presentableName =  showValue ?
                JkUtilsString.substringAfterLast(suffix, "=")
                : fieldNode + suffix;
        Icon icon = showValue ? AllIcons.Nodes.Enum : FieldNode.ICON;
        String tailText = showValue ? "" : " " + Strings.nullToEmpty(fieldNode.getTooltipText());
        return  LookupElementBuilder.create(beanNode.getName() + "#" + fieldNode.prefixedName() + suffix )
                .withBoldness(beanNode.isLocal())
                .withPresentableText(presentableName)
                .withTailText(tailText)
                .withIcon(icon);
    }

    private static class BeanComparator implements Comparator<BeanNode> {

        @Override
        public int compare(BeanNode bean1, BeanNode bean2) {
            if (bean1.isLocal() && !bean2.isLocal()) {
                return -1;
            }
            if (!bean1.isLocal() && bean2.isLocal()) {
                return 1;
            }
            if (BeanNode.UNPRIORIZEDS.contains(bean1.getName())) {
                return 1;
            }
            if (BeanNode.UNPRIORIZEDS.contains(bean2.getName())) {
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
