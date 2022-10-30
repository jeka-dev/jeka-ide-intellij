package dev.jeka.ide.intellij.extension.action;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import dev.jeka.ide.intellij.engine.ConfigurationRunner;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import java.util.List;

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

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ApplicationManager.getApplication().runReadAction(() -> {
            CallContext callContext = getCallContext(event);
            ConfigurationRunner.run(callContext.getModule(), null, callContext.cmd(), debug);
        });
    }

    static CallContext getCallContext(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        PsiLocation<PsiElement> location = (PsiLocation<PsiElement>) dataContext.getData(Location.DATA_KEY);
        final String methodName;
        final String className;
        final Module module;
        if (location != null) {
            if (location.getPsiElement() instanceof PsiIdentifier) {
                PsiMethod psiMethod = (PsiMethod) location.getPsiElement().getParent();
                methodName = psiMethod.getName();
                className =  psiMethod.getContainingClass().getName();

                // Handle kotlin
            } else if (location.getPsiElement().getParent() instanceof KtNamedFunction) {
                KtNamedFunction ktNamedFunction = (KtNamedFunction) location.getPsiElement().getParent();
                methodName = ktNamedFunction.getName();
                className = ktNamedFunction.getContainingKtFile().getClasses()[0].getName();
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
            className = methodInfo.qualifiedClassName;
            module = methodInfo.getModule();;
        }
        boolean multiModule = ModuleManager.getInstance(module.getProject()).getModules().length > 1;
        List<PsiClass> psiClasses = PsiClassHelper.findLocalBeanClasses(module);
        return new CallContext(module, className, methodName, multiModule, psiClasses.size() > 1);
    }

    @Value
    public static class MethodInfo {

        public static final DataKey<MethodInfo> KEY = DataKey.create(MethodInfo.class.getName());

        Module module;

        String qualifiedClassName;

        String beanName;

        String methodName;

    }

    @Value
    static class CallContext {

        Module module;

        String className;

        String methodName;

        boolean multiModule;

        boolean multiLocalBeans;

        String cmd() {
            return  JkExternalToolApi.getBeanName(className) + "#" + methodName;
        }

        String toConfigName() {
            String beanName = JkExternalToolApi.getBeanName(className);
            String prefix = multiModule ? "[" + module.getName() + "] " : "";
            String suffix = multiLocalBeans ? beanName + "#" : "";
            return prefix + suffix + methodName;
        }
    }
}
