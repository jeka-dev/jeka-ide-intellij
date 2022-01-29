package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JekaModuleContainer {

    @Getter
    private final Module module;

    private final JekaFolderNode moduleFolder;

    private List<JekaBeanNode> cachedBeanNodes;

    static JekaModuleContainer fromModule(JekaFolderNode moduleFolder, Module module) {
        JekaModuleContainer result = new JekaModuleContainer(module, moduleFolder);
        result.getBeanNodes();
        return  result;
    }

    List<JekaBeanNode> getBeanNodes() {
        if (cachedBeanNodes != null) {
            return cachedBeanNodes;
        }
        List<PsiClass> jekaPsilasses = PsiClassHelper.findJekaCommandClasses(module);
        cachedBeanNodes = jekaPsilasses.stream()
                .map(psiClass -> new JekaBeanNode(moduleFolder, psiClass))
                .collect(Collectors.toList());
        return cachedBeanNodes;
    }

    void refresh() {
        cachedBeanNodes = null;
    }

}

