package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.common.PsiMethodHelper;
import lombok.Getter;

import java.util.*;

@Getter
public class JekaBeanNode implements JekaModelNode {

    @Getter
    private final JekaModelNode parent;

    @Getter
    private final PsiClass kbeanPsiClass;

    private List<JekaBeanNode> cachedBeans;

    JekaBeanNode(JekaModelNode parent, PsiClass psiClass) {
        this.kbeanPsiClass = psiClass;
        this.parent = parent;
    }

    public String getName() {
        return getKbeanPsiClass().getName();
    }

    @Override
    public NodeInfo getNodeInfo() {
        return NodeInfo.simple(this, AllIcons.Nodes.Class,
                kbeanPsiClass::getName, this::getParent, this::getChildren);
    }

    public Module getModule() {
        JekaFolderNode jekaFolder = (JekaFolderNode) this.getParent();
        return jekaFolder.getModule();
    }


    private List<JekaModelNode> getChildren() {
        List<JekaModelNode> result = children();
        return result;
    }

    private List<JekaModelNode> children() {
        PsiMethod[] methods;
        try {
            methods = kbeanPsiClass.getAllMethods();
        } catch (PsiInvalidElementAccessException e) {
            System.out.println("issue with getAllMethods on class " + kbeanPsiClass.getName());
            return Collections.emptyList();
        }
        List<JekaModelNode> result = new LinkedList<>();
        result.addAll(kbeans());
        result.add(new JekaFieldSetNode(this));
        Set<String> methodNames = new HashSet<>();
        for (PsiMethod method : methods) {
            if (PsiMethodHelper.isInstancePublicVoidNoArgsNotFromObject(method)) {
                String methodName = method.getName();
                result.add(new JekaMethodNode(this, methodName, method));
                methodNames.add(methodName);
            }
        }
        return result;
    }

    public List<JekaBeanNode> kbeans() {
        if (cachedBeans != null) {
            return cachedBeans;
        }
        PsiField[] psiFields = kbeanPsiClass.getAllFields();
        List<JekaBeanNode> result = new LinkedList<>();
        for (PsiField psiField : psiFields) {
            PsiType psiType = psiField.getType();
            if (psiType instanceof PsiClassType) {
                PsiClassType classType = (PsiClassType) psiType;
                PsiClass psiClass = classType.resolve();
                if (PsiClassHelper.isExtendingJkBean(psiClass)) {
                    JekaBeanNode plugin = new JekaBeanNode(this, psiClass);
                    result.add(plugin);
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
