package dev.jeka.ide.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PsiNavigateUtil;
import dev.jeka.ide.intellij.common.ClassUtils;
import dev.jeka.ide.intellij.common.Constants;
import org.jetbrains.annotations.NotNull;

// https://intellij-support.jetbrains.com/hc/en-us/community/posts/360004184479-How-to-open-editor-tab-with-code-
public class ShowCommandSetAction extends AnAction {

    public static final ShowCommandSetAction INSTANCE = new ShowCommandSetAction();

    private ShowCommandSetAction() {
        super("Goto Jeka CommandSet Source", "Goto Jeka CommandSet Source", Constants.JkIcons.JEKA_RUN);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile selectedFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (!selectedFile.isDirectory()) {
            return;
        }
        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile commandSetClass = findCommandSet(psiManager, selectedFile);
        if (commandSetClass == null) {
            return;
        }
        PsiFile psiFile = psiManager.findFile(commandSetClass);
        PsiNavigateUtil.navigate(psiFile, true);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile selectedFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (!selectedFile.isDirectory()) {
            event.getPresentation().setVisible(false);
        }
        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile commandSetClass = findCommandSet(psiManager, selectedFile);
        if (commandSetClass == null) {
            event.getPresentation().setVisible(false);
        } else {
            event.getPresentation().setText("Goto '" + commandSetClass.getName() + "'");
        }
    }

    private VirtualFile findCommandSet(PsiManager psiManager, VirtualFile moduleRoot) {
        VirtualFile jekaDir = moduleRoot.findChild("jeka");
        if (jekaDir == null) {
            return null;
        }
        VirtualFile defDir = jekaDir.findChild("def");
        if (defDir == null) {
            return null;
        }
        return find(psiManager, defDir);
    }

    private VirtualFile find(PsiManager psiManager, VirtualFile parent) {
        for (VirtualFile file : parent.getChildren()) {
            if (file.isDirectory()) {
                VirtualFile result = find(psiManager, file);
                if (result != null) {
                    return result;
                }
            } else {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null) {
                    if (psiFile instanceof PsiJavaFile) {
                        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                        PsiClass[] psiClasses = psiJavaFile.getClasses();
                        for (PsiClass psiClass: psiClasses) {
                            System.out.println("--" + psiClass.getName());
                            if (ClassUtils.isExtendingJkCommandSet(psiClass)) {
                                return file;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
