package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import dev.jeka.ide.intellij.common.JkPluginHelper;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JekaModule  {

    @Getter
    private final Module module;

    private final JekaFolder moduleFolder;

    private List<JekaCommandClass> cachedCommandClasses;

    private Collection<PsiClass> cachedPluginClasses;

    static JekaModule fromModule(JekaFolder moduleFolder, Module module) {
        JekaModule result = new JekaModule(module, moduleFolder);
        result.getCommandClasses();
        return  result;
    }

    List<JekaCommandClass> getCommandClasses() {
        if (cachedCommandClasses != null) {
            return cachedCommandClasses;
        }
        List<PsiClass> jekaPsilasses = PsiClassHelper.findJekaCommandClasses(module);
        cachedCommandClasses = jekaPsilasses.stream()
                .map(psiClass -> JekaCommandClass.fromPsiClass(moduleFolder, psiClass))
                .collect(Collectors.toList());
        return cachedCommandClasses;
    }

    Collection<PsiClass> getCachedPluginClasses() {
        if (cachedPluginClasses != null) {
            return cachedPluginClasses;
        }
        cachedPluginClasses = JkPluginHelper.getPluginClasses(module);
        return cachedPluginClasses;
    }

    void refresh() {
        cachedPluginClasses = null;
        cachedCommandClasses = null;
    }

}

