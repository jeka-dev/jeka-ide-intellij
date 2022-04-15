package dev.jeka.ide.intellij.extension.autocompletion;

import com.intellij.openapi.module.Module;
import dev.jeka.core.api.depmanagement.JkRepoSet;
import dev.jeka.core.api.depmanagement.resolution.JkDependencyResolver;
import dev.jeka.core.tool.JkExternalToolApi;
import dev.jeka.ide.intellij.common.ModuleHelper;


import java.nio.file.Path;
import java.util.function.Consumer;

class DependencySearchProvider {

    void search(Module module, String searchString, Consumer<String> consumer) {
        Path dir = ModuleHelper.getModuleDirPath(module);
        JkRepoSet repoSet = JkExternalToolApi.getDownloadRepos(dir);
        JkDependencyResolver resolver = JkDependencyResolver.of().addRepos(repoSet);
    }



}
