package dev.jeka.ide.intellij.common;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import dev.jeka.core.tool.JkDoc;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PsiClassHelper {

    private static final String JKBEAN_CLASS_NAME = "dev.jeka.core.tool.JkBean";

    public static boolean isExtendingJkBean(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        if (JKBEAN_CLASS_NAME.equals(psiClass.getQualifiedName())) {
            return true;
        }
        PsiClassType[] psiClassTypes = psiClass.getExtendsListTypes();
        for (PsiClassType psiClassType : psiClassTypes) {
            if (psiClassType == null) {
                return false;
            }
            PsiClass currentPsiClass = psiClassType.resolve();
            if (isExtendingJkBean(currentPsiClass)) {
                return true;
            }
        }
        return false;
    }

    public static PsiClass getPsiClass(Project project, String className) {
        return JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
    }

    public static List<PsiClass> findLocalBeanClasses(Module module) {
        VirtualFile rootDir = ModuleHelper.getModuleDir(module);
        if (rootDir == null) {
            return Collections.emptyList();
        }
        VirtualFile jekaDefFolder = rootDir.findFileByRelativePath(Constants.JEKA_DIR_NAME + "/" + Constants.JEKA_DEF_DIR_NAME);
        if (jekaDefFolder == null) {
            return Collections.emptyList();
        }
        PsiManager psiManager = PsiManager.getInstance(module.getProject());
        PsiDirectory jekaDir = psiManager.findDirectory(jekaDefFolder);
        return findKBeanClasses(jekaDir);
    }

    private static List<PsiClass> findKBeanClasses(PsiDirectory dir) {
        List<PsiClass> result = new LinkedList<>();
        for (PsiFile psiFile : dir.getFiles()) {
            if (psiFile instanceof PsiClassOwner) {
                PsiClassOwner psiJavaFile = (PsiClassOwner) psiFile;
                for (PsiClass psiClass : psiJavaFile.getClasses()) {
                    if (PsiClassHelper.isExtendingJkBean(psiClass)) {
                        result.add(psiClass);
                    }
                }
            }
        }
        for (PsiDirectory subDir : dir.getSubdirectories()) {
            result.addAll(findKBeanClasses(subDir));
        }
        return result;
    }

    public static String getFormattedJkDoc(PsiJvmModifiersOwner psiClass) {
        String doc = getJkDoc(psiClass);
        if (doc == null) {
            return null;
        }
        return doc.replace("\\n", "<br/>").replace("\n", "<br/>");
    }

    public static String getJkDoc(PsiJvmModifiersOwner psiClass) {
        PsiAnnotation annotation = psiClass.getAnnotation(JkDoc.class.getName());
        if (annotation == null) {
            return null;
        }
        PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
        if (value == null || value.getText() == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (PsiElement psiElement : value.getChildren()) {
            String item = psiElement.getText();
            if (item.startsWith("\"")) {
                item = item.substring(1, item.length() -1);
            }
            stringBuilder.append(item).append("\n");
        }
        String text = stringBuilder.length() == 0 ? "" : stringBuilder.substring(0, stringBuilder.length() -1);
        return text;
    }

}
