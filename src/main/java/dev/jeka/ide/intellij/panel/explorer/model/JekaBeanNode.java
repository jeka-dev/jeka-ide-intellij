package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import dev.jeka.core.tool.JkDoc;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.common.PsiMethodHelper;
import icons.JekaIcons;
import lombok.Getter;

import java.util.*;

public class JekaBeanNode extends JekaAbstractModelNode {

    @Getter
    private final PsiClass kbeanPsiClass;

    private List<JekaBeanNode> cachedBeans;

    JekaBeanNode(JekaAbstractModelNode parent, PsiClass psiClass) {
        super(parent);
        this.kbeanPsiClass = psiClass;
    }

    @Override
    protected NodeDescriptor<? extends JekaAbstractModelNode> makeNodeDescriptor() {
        NodeDescriptor<? extends JekaAbstractModelNode> result = basicNodeDescriptor(JekaIcons.KBEAN, getName());
        String doc = getDoc();
        if (doc != null) {

        }
        return result;
    }

    @Override
    public List<JekaAbstractModelNode> getChildren() {
        if (!kbeanPsiClass.isValid()) {
            return Collections.emptyList();
        }
        PsiMethod[] methods;
        try {
            methods = kbeanPsiClass.getAllMethods();
        } catch (PsiInvalidElementAccessException e) {
            System.out.println("issue with getAllMethods on class " + kbeanPsiClass.getName());
            return Collections.emptyList();
        }
        List<JekaAbstractModelNode> result = new LinkedList<>();
        result.addAll(kbeans());
        JekaFieldSetNode jekaFieldSetNode = new JekaFieldSetNode(this);
        if (!jekaFieldSetNode.getChildren().isEmpty()) {
            result.add(new JekaFieldSetNode(this));
        }
        Set<String> methodNames = new HashSet<>();
        for (PsiMethod method : methods) {
            if (PsiMethodHelper.isInstancePublicVoidNoArgsNotFromObject(method)) {
                String methodName = method.getName();
                result.add(new JekaMethodNode(this, method));
                methodNames.add(methodName);
            }
        }
        return result;
    }

    public String getName() {
        return JkExternalToolApi.getBeanName(kbeanPsiClass.getQualifiedName());
    }

    private String getDoc() {
        PsiModifierList psiModifierList = kbeanPsiClass.getModifierList();
        if (psiModifierList == null) {
            return null;
        }
        PsiAnnotation[] annotations = psiModifierList.getAnnotations();
        PsiAnnotation jkDoc = Arrays.stream(annotations)
                .filter(annotation ->annotation.hasQualifiedName(JkDoc.class.getName()))
                .findFirst().orElse(null);
        if (jkDoc != null) {
            return jkDoc.getText();
        }
        return null;
    }

    public Module getModule() {
        JekaAbstractModelNode parent = this.getParent();
        while (!(parent instanceof JekaFolderNode)) {
            parent = parent.getParent();
        }
        JekaFolderNode jekaFolder = (JekaFolderNode) parent;
        return jekaFolder.getModule();
    }

    private List<JekaBeanNode> kbeans() {
        if (cachedBeans != null) {
            return cachedBeans;
        }
        PsiField[] psiFields = kbeanPsiClass.getAllFields();
        List<JekaBeanNode> result = new LinkedList<>();
        for (PsiField psiField : psiFields) {
            if (psiField.getModifierList().hasExplicitModifier("private")) {
                continue;
            }
            PsiType psiType = psiField.getType();
            if (psiType instanceof PsiClassType) {
                PsiClassType classType = (PsiClassType) psiType;
                PsiClass psiClass = classType.resolve();
                if (PsiClassHelper.isExtendingJkBean(psiClass)) {
                    JekaBeanNode kbean = new JekaBeanNode(this, psiClass);
                    result.add(kbean);
                }
            }
        }
        cachedBeans = Collections.unmodifiableList(result);
        return result;
    }

    List<JekaBeanNode> pluginsRecursive() {
        List<JekaBeanNode> result = new LinkedList<>();
        kbeans().forEach(pluginNode -> result.addAll(pluginNode.pluginsRecursive()));
        return result;
    }

    void refresh() {
        cachedBeans = null;
    }

}
