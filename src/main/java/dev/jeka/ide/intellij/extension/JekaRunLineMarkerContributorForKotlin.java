package dev.jeka.ide.intellij.extension;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.extension.action.JekaRunMethodAction;
import dev.jeka.ide.intellij.extension.action.JekaRunMethodParamAction;
import icons.JekaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import java.util.function.Function;

public class JekaRunLineMarkerContributorForKotlin extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement psiEl) {
        if (psiEl.textMatches("fun") && psiEl.getParent() instanceof KtNamedFunction) {
            KtNamedFunction ktNamedFunction = (KtNamedFunction) psiEl.getParent();
            if (!ktNamedFunction.getValueParameters().isEmpty()) {
                return null;
            }
            if (ktNamedFunction.getChildren()[0].getText().equals("private")) {
                return null;
            }
            KtFile ktFile = ktNamedFunction.getContainingKtFile();
            PsiClass[] psiClasses = ktFile.getClasses();
            if (psiClasses.length == 0) {
                return null;
            }
            PsiClass psiClass = psiClasses[0];
            if (!PsiClassHelper.isExtendingJkBean(psiClass)) {
                return null;
            }

            final AnAction[] actions = new AnAction[] {
                    JekaRunMethodAction.RUN_JEKA_INSTANCE,
                    JekaRunMethodAction.DEBUG_JEKA_INSTANCE,
                    JekaRunMethodParamAction.RUN_JEKA_INSTANCE,
                    JekaRunMethodParamAction.DEBUG_JEKA_INSTANCE
            };
            Function<PsiElement, String> tooltipProvider = el -> "Run '" + ktNamedFunction.getName() + "' as Jeka command";
            return new Info(JekaIcons.COMMAND, actions, tooltipProvider);
        }
        return null;
    }



}
