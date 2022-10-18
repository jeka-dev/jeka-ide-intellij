package dev.jeka.ide.intellij.extension;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.lang.properties.psi.impl.PropertyKeyImpl;
import com.intellij.lang.properties.psi.impl.PropertyValueImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.*;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.extension.action.JekaRunCmdAction;
import dev.jeka.ide.intellij.extension.action.JekaRunCmdParamAction;
import dev.jeka.ide.intellij.extension.action.JekaRunMethodAction;
import dev.jeka.ide.intellij.extension.action.JekaRunMethodParamAction;
import icons.JekaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class JekaRunLineMarkerContributorForCmdProperties extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement psiEl) {
        PsiFile psiFile = psiEl.getContainingFile();
        if (!psiFile.getName().equals("cmd.properties") || !psiFile.getParent().getName().equals("jeka")) {
            return null;
        }
        if (! (psiEl instanceof PropertyKeyImpl)) {
            return null;
        }
        PropertyKeyImpl propertyKey = (PropertyKeyImpl) psiEl;
        String keyName = propertyKey.getText();
        if (keyName == null) {
            return null;
        }
        if (keyName.startsWith("_")) {
            return null;
        }
        final AnAction[] actions = new AnAction[] {
                JekaRunCmdAction.RUN_JEKA_INSTANCE,
                JekaRunCmdAction.DEBUG_JEKA_INSTANCE,
                JekaRunCmdParamAction.RUN_JEKA_INSTANCE,
                JekaRunCmdParamAction.DEBUG_JEKA_INSTANCE,
        };
        Function<PsiElement, String> tooltipProvider = el -> "Run '" + el.getText() + "' as Jeka command";
        return new Info(JekaIcons.CMD, actions, tooltipProvider);
    }



}
