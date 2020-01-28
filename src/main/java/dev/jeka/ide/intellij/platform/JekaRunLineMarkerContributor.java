package dev.jeka.ide.intellij.platform;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.util.containers.ContainerUtil;
import dev.jeka.ide.intellij.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;


public class JekaRunLineMarkerContributor extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement psiEl) {
      ///  if (Registry.is("ide.jvm.run.marker")) return null;
        if (!isIdentifier(psiEl)) {
            return null;
        }
        PsiElement psiParentEl = psiEl.getParent();
        if (! (psiParentEl instanceof PsiMethod)) {
            return null;
        }
        PsiMethod psiMethod = (PsiMethod) psiParentEl;
        PsiClass psiClass = psiMethod.getContainingClass();
        if (!Utils.isExtendingJkCommands(psiClass)) {
            return null;
        }
        if (psiMethod.hasParameters() || psiMethod.isConstructor()
                || psiMethod.getModifierList().hasExplicitModifier("static")
                || !psiMethod.getModifierList().hasExplicitModifier("public")) {
            return null;
        }
        final AnAction[] actions = ExecutorAction.getActions();
        Function<PsiElement, String> tooltipProvider = el -> "Run '" + el.getText() + "' as Jeka command";
        return new Info(JkIcons.JEKA_RUN, actions, tooltipProvider);
    }

    protected boolean isIdentifier(PsiElement e) {
        return e instanceof PsiIdentifier;
    }
}
