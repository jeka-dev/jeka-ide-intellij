package dev.jeka.ide.intellij.panel.explorer.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import dev.jeka.ide.intellij.common.PsiClassHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class JekaModule  {

    private final Module module;

    private final List<JekaCommandClass> commandClasses;

    static JekaModule fromModule(JekaFolder moduleFolder, Module module) {
        List<PsiClass> jekaPsilasses = PsiClassHelper.findJekaCommandClasses(module);
        List<JekaCommandClass> jekaCommandClasses = jekaPsilasses.stream()
                .map(psiClass -> JekaCommandClass.fromPsiClass(moduleFolder, psiClass))
                .collect(Collectors.toList());
        return new JekaModule(module, jekaCommandClasses);
    }

}

