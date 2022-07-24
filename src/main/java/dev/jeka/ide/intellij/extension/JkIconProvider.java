package dev.jeka.ide.intellij.extension;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IconProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import icons.JekaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry;

import javax.swing.*;
import java.util.List;

public class JkIconProvider extends IconProvider {

    @Nullable
    @Override
    public Icon getIcon(@NotNull PsiElement element, int flags) {
        if (element instanceof PsiDirectory) {
            PsiDirectory psiDirectory = (PsiDirectory) element;
            if (psiDirectory.getName().equals("jeka")) {
                VirtualFile dir = psiDirectory.getVirtualFile();
                if (ModuleHelper.isExistingModuleRoot(element.getProject(), dir.getParent())) {
                    return AllIcons.Nodes.ConfigFolder;
                }
            }
            return null;
        }
        if (!element.getContainingFile().getVirtualFile().toString().contains(JkConstants.DEF_DIR)) {
            return null;
        }
        if (element instanceof KtClass) {
            KtClass ktClass = (KtClass) element;
            List<KtSuperTypeListEntry> superTypeListEntries = ktClass.getSuperTypeListEntries();
            for(KtSuperTypeListEntry ktSuperTypeListEntry : superTypeListEntries) {
                if (ktSuperTypeListEntry.getTypeReference().getText().equals("JkBean")) {
                    return JekaIcons.KBEAN;
                }
            }

        }
        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            if (PsiClassHelper.isExtendingJkBean(psiClass)) {
                return JekaIcons.KBEAN;
            }
        }
        return null;
    }
}
