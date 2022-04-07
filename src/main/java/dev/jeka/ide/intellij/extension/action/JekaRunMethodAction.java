package dev.jeka.ide.intellij.extension.action;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.engine.ConfigurationRunner;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtNamedFunction;

public class JekaRunMethodAction extends AnAction {

    public static final JekaRunMethodAction RUN_JEKA_INSTANCE = new JekaRunMethodAction(false);

    public static final JekaRunMethodAction DEBUG_JEKA_INSTANCE = new JekaRunMethodAction(true);

    private final boolean debug;

    private JekaRunMethodAction(boolean debug) {
        super((debug ? "Debug" : "Run") +   " method",
                (debug ? "Debug" : "Run") +   " method",
                debug ? AllIcons.Actions.StartDebugger : AllIcons.RunConfigurations.TestState.Run);
        this.debug = debug;
    }

    static String configurationName(Module module, String simpleClassName, String methodName) {
        return module.getName() + " [jeka " + simpleClassName + "#" + methodName + "]";
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        CallContext callContext = getCallContext(event);
        Module module = callContext.getModule();
        String configurationName = configurationName(module, callContext.getSimpleClassName(),
                callContext.getMethodName());
        ConfigurationRunner.run(callContext.getModule(), configurationName, callContext.cmd(), debug);
    }

    static CallContext getCallContext(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        PsiLocation<PsiElement> location = (PsiLocation<PsiElement>) dataContext.getData(Location.DATA_KEY);
        final String methodName;
        final String simpleClassName;
        final Module module;
        if (location != null) {
            if (location.getPsiElement() instanceof PsiIdentifier) {
                PsiMethod psiMethod = (PsiMethod) location.getPsiElement().getParent();
                methodName = psiMethod.getName();
                simpleClassName =  psiMethod.getContainingClass().getName();

                // Handle kotlin
            } else if (location.getPsiElement().getParent() instanceof KtNamedFunction) {
                KtNamedFunction ktNamedFunction = (KtNamedFunction) location.getPsiElement().getParent();
                methodName = ktNamedFunction.getName();
                simpleClassName = ktNamedFunction.getContainingKtFile().getClasses()[0].getName();
            } else {
                throw new IllegalStateException("Type of action data " + location.getPsiElement() + " not handled.");
            }
            module = ModuleHelper.getModule(event);
        } else {
            MethodInfo methodInfo = MethodInfo.KEY.getData(dataContext);
            if (methodInfo == null) {
                throw new IllegalStateException("Can not find reference to Psi method");
            }
            methodName = methodInfo.getMethodName();
            simpleClassName = methodInfo.getBeanClass().getName();
            module = methodInfo.getModule();;
        }
        return new CallContext(module, simpleClassName, methodName);
    }

    @Value
    public static class MethodInfo {

        public static final DataKey<MethodInfo> KEY = DataKey.create(MethodInfo.class.getName());

        Module module;

        PsiClass beanClass;

        String beanName;

        String methodName;

    }

    @Value
    static class CallContext {

        Module module;

        String simpleClassName;

        String methodName;

        String cmd() {
            return simpleClassName + "#" + methodName;
        }

    }
}
