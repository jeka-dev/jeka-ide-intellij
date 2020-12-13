package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class JekaModule  {

    @Getter
    private final Module module;

    private final JekaFolder moduleFolder;

    private List<JekaCommandClass> cachedCommandClasses;

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

}

