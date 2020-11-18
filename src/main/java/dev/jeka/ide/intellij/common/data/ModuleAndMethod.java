package dev.jeka.ide.intellij.common.data;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiMethod;
import lombok.Value;
import sun.security.pkcs11.Secmod;

@Value
public class ModuleAndMethod {

    public static final DataKey<ModuleAndMethod> KEY = DataKey.create("dev.jeka.moduleAndMethod");

    Module module;

    PsiMethod method;


}
