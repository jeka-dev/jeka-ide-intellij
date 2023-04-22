package dev.jeka.ide.intellij.panel.explorer.tree;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReference;
import com.intellij.ui.ColoredTreeCellRenderer;
import dev.jeka.core.api.utils.JkUtilsIterable;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.common.PsiMethodHelper;
import dev.jeka.ide.intellij.common.model.NavigableProxy;
import icons.JekaIcons;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BeanNode extends AbstractNode implements Comparable<BeanNode> {

    public static final List<String> UNPRIORIZEDS = JkUtilsIterable.listOf("nexus", "maven", "intellij",
            "eclipse", "git", "scaffold");

    public static final Icon ICON = JekaIcons.KBEAN;

    @Getter
    private final JavaClassReference psiClass;

    @Getter
    private final String name;

    @Getter
    private final String className;

    @Getter
    private final String tooltipText;

    @Getter
    private final String definition;

    @Getter
    private final boolean local;

    public BeanNode(Project project, PsiClass psiClass, boolean local) {
        super(project);
        this.psiClass = (JavaClassReference) psiClass.getReference();
        createFieldNodes(psiClass).forEach(this::add);
        createMethodNodes2(psiClass).forEach(this::add);
        createNestedBeanNodes(psiClass).forEach(this::add);
        name = JkExternalToolApi.getBeanName(psiClass.getQualifiedName());
        className = psiClass.getQualifiedName();
        String tooltipContent = "<b>" + psiClass.getQualifiedName() + "</b><";
        String doc = PsiClassHelper.getFormattedJkDoc(psiClass);
        if (!JkUtilsString.isBlank(doc)) {
            tooltipContent = tooltipContent + "<br/>" + doc;
        }
        definition = doc;
        tooltipText = tooltipContent;
        this.local = local;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void customizeCellRenderer(ColoredTreeCellRenderer coloredTreeCellRenderer) {
        coloredTreeCellRenderer.setIcon(ICON);
        coloredTreeCellRenderer.setToolTipText(tooltipText);
    }

    @Override
    public void fillPopupMenu(DefaultActionGroup group) {
        group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    private List<MethodNode> createMethodNodes2(PsiClass psiClass) {
        return Arrays.stream(psiClass.getAllMethods())
                .filter(PsiMethodHelper::isInstancePublicVoidNoArgsNotFromObject)
                .sorted( (m1,m2) -> m1.getContainingClass().isInheritor(m2.getContainingClass(), true)
                        ? 1
                        : 0)
                .map(psiMethod -> new MethodNode(project, psiMethod))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<FieldNode> createFieldNodes(PsiClass psiClass) {
        return FieldNode.createFieldNodes(project, psiClass);
    }

    private List<BeanNode> createNestedBeanNodes(PsiClass psiClass) {
        PsiField[] psiFields = psiClass.getAllFields();
        List<BeanNode> result = new LinkedList<>();
        for (PsiField psiField : psiFields) {
            if (psiField.getModifierList().hasExplicitModifier("private")) {
                continue;
            }
            PsiType psiType = psiField.getType();
            if (psiType instanceof PsiClassType) {
                PsiClassType classType = (PsiClassType) psiType;
                PsiClass childPsiClass = classType.resolve();
                if (PsiClassHelper.isExtendingJkBean(childPsiClass)) {
                    BeanNode beanNode = new BeanNode(project, childPsiClass, false);
                    result.add(beanNode);
                }
            }
        }
        return result;
    }

    @Override
    public int compareTo(@NotNull BeanNode o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeanNode beanNode = (BeanNode) o;
        return className.equals(beanNode.className);
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }
}
