package dev.jeka.ide.intellij.common.data;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import lombok.Value;

@Value
public class CommandInfo {

    public static final DataKey<CommandInfo> KEY = DataKey.create("dev.jeka.moduleAndMethod");

    Module module;

    PsiClass commandClass;

    String pluginName;

    String methodName;


}
