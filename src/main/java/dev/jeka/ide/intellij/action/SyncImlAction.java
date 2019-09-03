package dev.jeka.ide.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import dev.jeka.ide.intellij.JekaDoer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SyncImlAction extends AnAction {

    private static final String JKCOMMANDS_NAME = "dev.jeka.core.tool.JkCommands";

    public SyncImlAction() {
        super("Synchronize iml from Jeka");
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        System.out.println("----------------------------------------------------- launch jeka iml");
        PsiFile virtualFile = event.getData(CommonDataKeys.PSI_FILE);
        Module module = ModuleUtil.findModuleForFile(virtualFile);
        VirtualFile virtualRoot = ModuleRootManager.getInstance(module).getContentRoots()[0];
        Path path = Paths.get(virtualRoot.getPath());
        JekaDoer jekaDoer = new JekaDoer();
        jekaDoer.generateIml(path);
        System.out.println(".... done");
    }

    @Override
    public void update(AnActionEvent event) {
        PsiFile virtualFile = event.getData(CommonDataKeys.PSI_FILE);
        if (virtualFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) virtualFile;
            PsiClass psiClass = psiJavaFile.getClasses()[0];
            boolean isCommandsClass = extendingJkCommands(psiClass);
            if (isCommandsClass) {
                event.getPresentation().setEnabledAndVisible(isCommandsClass);
                return;
            }

        }
        event.getPresentation().setEnabledAndVisible(false);
    }

    private static boolean extendingJkCommands(PsiClass psiClass) {
        if (psiClass.getQualifiedName().equals(JKCOMMANDS_NAME)) {
            return true;
        }
        PsiClassType[] psiClassTypes = psiClass.getExtendsListTypes();
        for (PsiClassType psiClassType : psiClassTypes) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiClassType;
            PsiClass currentPsiClass = psiClassReferenceType.resolve();
            if (extendingJkCommands(currentPsiClass)) {
                return true;
            }
        }
        return false;
    }

}





