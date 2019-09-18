package dev.jeka.ide.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import dev.jeka.ide.intellij.JekaDoer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScaffoldAction extends AnAction {

    public ScaffoldAction() {
        super("Add Jeka folder, scripts and classes");
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Path path = moduleRootPath(event);
        if (path == null) {
            return;
        }
        JekaDoer jekaDoer = new JekaDoer();
        jekaDoer.scaffoldModule(path);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return;
        }
        Module module = ModuleUtil.findModuleForFile(virtualFile, event.getProject());
        event.getPresentation().setText(" Add Jeka folder, scripts and classes to " + module.getName());
    }

    private static Path moduleRootPath(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return null;
        }
        Module module = ModuleUtil.findModuleForFile(virtualFile, event.getProject());
        if (module == null) {
            return null;
        }
        VirtualFile virtualRoot = ModuleRootManager.getInstance(module).getContentRoots()[0];
        return Paths.get(virtualRoot.getPath());
    }


}





