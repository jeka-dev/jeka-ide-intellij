package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiTreeChangeEvent;
import dev.jeka.ide.intellij.common.Constants;
import dev.jeka.ide.intellij.common.ModuleHelper;


class EventFilter {

    private final Project project;

    private final static String DEF_DIR = Constants.JEKA_DIR_NAME + "/" + Constants.JEKA_DEF_DIR_NAME;

    public EventFilter(Project project) {
        this.project = project;
    }

    Module moduleFromChildIsAJekaJavaClassFile(PsiTreeChangeEvent event) {
        if (! (event.getChild() instanceof PsiJavaFile)) {
            return null;
        }
        PsiJavaFile psiFile = (PsiJavaFile) event.getElement();
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (!virtualFile.getPath().contains(DEF_DIR)) {
            return null;
        }
        Module module = ModuleUtil.findModuleForFile(psiFile);
        VirtualFile defDir = ModuleHelper.getModuleDir(module).findFileByRelativePath(DEF_DIR);
        if (virtualFile.getPath().startsWith(defDir.getPath())) {
            return module;
        }
        return null;
    }

    Module moduleFromChildIsIdentifierOfJkClassOrJkPlugin(PsiTreeChangeEvent event) {
        if (! (event.getChild() instanceof PsiJavaFile)) {
            return null;
        }
        PsiJavaFile psiFile = (PsiJavaFile) event.getElement();
        //PsiFile psiFile = event.getFile();
        if (!psiFile.getVirtualFile().getPath().contains(DEF_DIR)) {
            return null;
        }
        Module module = ModuleUtil.findModuleForFile(psiFile);
        VirtualFile defDir = ModuleHelper.getModuleDir(module).findFileByRelativePath(DEF_DIR);
        if (psiFile.getVirtualFile().getPath().startsWith(defDir.getPath())) {
            return module;
        }
        return null;
    }
}
