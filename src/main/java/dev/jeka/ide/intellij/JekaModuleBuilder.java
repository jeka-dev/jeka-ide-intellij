package dev.jeka.ide.intellij;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;

/// https://github.com/redline-smalltalk/intellij-redline-plugin/blob/master/src/st/redline/smalltalk/module/RsModuleBuilder.java
public class JekaModuleBuilder extends ModuleBuilder {

    @Override
    public ModuleType<JekaModuleBuilder> getModuleType() {
        return JekaModuleType.INSTANCE;
    }
}
