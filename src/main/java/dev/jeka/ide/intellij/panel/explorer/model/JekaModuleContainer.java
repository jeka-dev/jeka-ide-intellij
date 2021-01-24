package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import dev.jeka.ide.intellij.common.JkPluginHelper;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JekaModuleContainer {

    @Getter
    private final Module module;

    private final JekaFolderNode moduleFolder;

    private List<JekaCommandClassNode> cachedCommandClasses;

    private Collection<PsiClass> cachedPluginClasses;

    static JekaModuleContainer fromModule(JekaFolderNode moduleFolder, Module module) {
        JekaModuleContainer result = new JekaModuleContainer(module, moduleFolder);
        result.getCommandClasses();
        return  result;
    }

    List<JekaCommandClassNode> getCommandClasses() {
        if (cachedCommandClasses != null) {
            return cachedCommandClasses;
        }
        List<PsiClass> jekaPsilasses = PsiClassHelper.findJekaCommandClasses(module);
        cachedCommandClasses = jekaPsilasses.stream()
                .map(psiClass -> JekaCommandClassNode.fromPsiClass(moduleFolder, psiClass))
                .collect(Collectors.toList());
        return cachedCommandClasses;
    }

    Collection<PsiClass> getAllPluginClasses() {
        if (cachedPluginClasses != null) {
            return cachedPluginClasses;
        }
        cachedPluginClasses = JkPluginHelper.getPluginClasses(module, new HashMap<>());
        return cachedPluginClasses;
    }

    void refresh() {
        cachedPluginClasses = null;
        cachedCommandClasses = null;
    }

}

