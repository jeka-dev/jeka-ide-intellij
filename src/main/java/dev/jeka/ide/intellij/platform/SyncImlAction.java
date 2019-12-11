package dev.jeka.ide.intellij.platform;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import dev.jeka.ide.intellij.JekaDoer;


import java.nio.file.Path;
import java.nio.file.Paths;

public class SyncImlAction extends AnAction {

    private static final String JKCOMMANDS_NAME = "dev.jeka.core.tool.JkCommands";

    public SyncImlAction() {
        super("Synchronize iml from Jeka", "Synchronize iml" , AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        ModuleClass moduleClass = ModuleClass.ofMaybeCommandClass(event);
        String className = moduleClass.psiClass == null ? null : moduleClass.psiClass.getQualifiedName();
        VirtualFile virtualRoot = ModuleRootManager.getInstance(moduleClass.module).getContentRoots()[0];
        Path path = Paths.get(virtualRoot.getPath());
        JekaDoer jekaDoer = JekaDoer.getInstance();
        jekaDoer.generateIml(path, className);
        JkNotifications.info("Iml file for module " + moduleClass.module.getName() + " re-generated.");
        virtualRoot.getFileSystem().refresh(true);
    }

    @Override
    public void update(AnActionEvent event) {
        ModuleClass moduleClass = ModuleClass.ofMaybeCommandClass(event);
        if (moduleClass.psiClass != null) {
           event.getPresentation().setText("Synchronize " + moduleClass.module.getName() + " iml file using "
                   + moduleClass.psiClass.getName());
        } else {
            event.getPresentation().setText("Synchronize " + moduleClass.module.getName() + " iml file");
        }
    }

    private static boolean isExtendingJkCommands(PsiClass psiClass) {
        if (psiClass.getQualifiedName().equals(JKCOMMANDS_NAME)) {
            return true;
        }
        PsiClassType[] psiClassTypes = psiClass.getExtendsListTypes();
        for (PsiClassType psiClassType : psiClassTypes) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiClassType;
            PsiClass currentPsiClass = psiClassReferenceType.resolve();
            if (isExtendingJkCommands(currentPsiClass)) {
                return true;
            }
        }
        return false;
    }

    private static class ModuleClass {
        final Module module;
        final PsiClass psiClass;

        private ModuleClass(Module module, PsiClass psiClass) {
            this.module = module;
            this.psiClass = psiClass;
        }

        static ModuleClass ofMaybeCommandClass(AnActionEvent event) {
            VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
            Module module = ModuleUtil.findModuleForFile(virtualFile, event.getProject());
            PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                PsiClass psiClass = psiJavaFile.getClasses()[0];
                boolean isCommandsClass = isExtendingJkCommands(psiClass);
                if (isCommandsClass) {
                    return new ModuleClass(module, psiClass);
                }
            }
            return new ModuleClass(module, null);
        }
    }

}




