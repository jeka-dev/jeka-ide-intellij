package dev.jeka.ide.intellij.extension;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.lang.properties.psi.impl.PropertyKeyImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.ide.intellij.extension.action.JekaRunCmdAction;
import dev.jeka.ide.intellij.extension.action.JekaRunCmdParamAction;
import icons.JekaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class JekaRunLineMarkerContributorForProperties extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement psiEl) {
        PsiFile psiFile = psiEl.getContainingFile();
        if (!psiFile.getName().equals(JkConstants.PROPERTIES_FILE)
                || !psiFile.getParent().getName().equals(JkConstants.JEKA_DIR)) {
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
        if (!keyName.startsWith(JkConstants.CMD_PROP_PREFIX) || keyName.startsWith(JkConstants.CMD_APPEND_PROP)) {
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
