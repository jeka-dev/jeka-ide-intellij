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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import dev.jeka.ide.intellij.JekaDoer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SyncImlAction extends AnAction {

    public SyncImlAction() {
        super("Synchronize iml from Jeka");
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        PsiFile virtualFile = event.getData(CommonDataKeys.PSI_FILE);
        Module module = ModuleUtil.findModuleForFile(virtualFile);
        VirtualFile virtualRoot = ModuleRootManager.getInstance(module).getContentRoots()[0];
        Path path = Paths.get(virtualRoot.getPath());
        JekaDoer jekaDoer = new JekaDoer();
        jekaDoer.generateIml(path);
    }

    @Override
    public void update(AnActionEvent event) {
        PsiFile virtualFile = event.getData(CommonDataKeys.PSI_FILE);
        Module module = ModuleUtil.findModuleForFile(virtualFile);
        event.getPresentation().setEnabledAndVisible(module != null);
    }

}





