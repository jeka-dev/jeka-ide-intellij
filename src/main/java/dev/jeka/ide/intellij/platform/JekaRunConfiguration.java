/*
 * Copyright 2018-2019 original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.jeka.ide.intellij.platform;

import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationCommandLineState;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.junit.RefactoringListeners;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.execution.util.ProgramParametersUtil;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.util.PathUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jerome Angibaud
 */
public class JekaRunConfiguration
        extends
        ModuleBasedConfiguration<JavaRunConfigurationModule, Element>
        implements
        CommonJavaRunConfigurationParameters,
        ConfigurationWithCommandLineShortener,
        SingleClassConfiguration,
        RefactoringListenerProvider,
        InputRedirectAware {

    public JekaRunConfiguration(String name, @NotNull Project project, @NotNull JekaRunConfigurationType configurationType) {
        this(name, project, configurationType.getConfigurationFactories()[0]);
    }

    public JekaRunConfiguration(final String name, @NotNull Project project) {
        this(name, project, JekaRunConfigurationType.INSTANCE.getConfigurationFactories()[0]);
    }

    protected JekaRunConfiguration(String name, @NotNull Project project, @NotNull ConfigurationFactory factory) {
        super(name, new JavaRunConfigurationModule(project, true), factory);
    }

    // backward compatibility (if 3rd-party plugin extends ApplicationConfigurationType but uses own factory without options class)
    @Override
    @NotNull
    protected final Class<? extends JekaRunConfigurationOptions> getDefaultOptionsClass() {
        return JekaRunConfigurationOptions.class;
    }

    /**
     * Because we have to keep backward compatibility, never use `getOptions()` to get or set values - use only designated getters/setters.
     */
    @NotNull
    @Override
    protected JekaRunConfigurationOptions getOptions() {
        return (JekaRunConfigurationOptions) super.getOptions();
    }

    @Override
    public void setMainClass(@NotNull PsiClass psiClass) {
        final Module originalModule = getConfigurationModule().getModule();
        setMainClassName(JavaExecutionUtil.getRuntimeQualifiedName(psiClass));
        setModule(JavaExecutionUtil.findModule(psiClass));
        restoreOriginalModule(originalModule);
    }

    @Override
    public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
        final JavaCommandLineState state = new JavaApplicationCommandLineState(this, env);
        JavaRunConfigurationModule module = getConfigurationModule();
        state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject(), module.getSearchScope()));
        return state;
    }

    @Override
    @NotNull
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<JekaRunConfiguration> group = new SettingsEditorGroup<>();
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), new JekaRunConfigurable(getProject()));
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<>());
        return group;
    }

    @Override
    public RefactoringElementListener getRefactoringElementListener(final PsiElement element) {
        final RefactoringElementListener listener = RefactoringListeners.
                getClassOrPackageListener(element, new RefactoringListeners.SingleClassConfigurationAccessor(this));
        return RunConfigurationExtension.wrapRefactoringElementListener(element, this, listener);
    }

    @Override
    @Nullable
    public PsiClass getMainClass() {
        return getConfigurationModule().findClass(getMainClassName());
    }

    @Nullable
    public String getMainClassName() {
        return getOptions().getMainClassName();
    }


    @Override
    @Nullable
    public String suggestedName() {
        if (getMainClassName() == null) {
            return null;
        }
        return JavaExecutionUtil.getPresentableClassName(getMainClassName());
    }

    @Override
    public String getActionName() {
        if (getMainClassName() == null) {
            return null;
        }
        return ProgramRunnerUtil.shortenName(JavaExecutionUtil.getShortClassName(getMainClassName()), 6) + ".main()";
    }

    @Override
    public void setMainClassName(@Nullable String qualifiedName) {
        getOptions().setMainClassName(qualifiedName);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        JavaParametersUtil.checkAlternativeJRE(this);
        final JavaRunConfigurationModule configurationModule = getConfigurationModule();
        final PsiClass psiClass = configurationModule.checkModuleAndClassName(getMainClassName(), ExecutionBundle.message("no.main.class.specified.error.text"));
        if (!PsiMethodUtil.hasMainMethod(psiClass)) {
            throw new RuntimeConfigurationWarning(ExecutionBundle.message("main.method.not.found.in.class.error.message", getMainClassName()));
        }
        ProgramParametersUtil.checkWorkingDirectoryExist(this, getProject(), configurationModule.getModule());
        JavaRunConfigurationExtensionManager.checkConfigurationIsValid(this);
    }

    @Override
    public void setVMParameters(@Nullable String value) {
        getOptions().setVmParameters(value);
    }

    @Override
    public String getVMParameters() {
        return getOptions().getVmParameters();
    }

    @Override
    public void setProgramParameters(@Nullable String value) {
        getOptions().setProgramParameters(value);
    }

    @Override
    public String getProgramParameters() {
        return getOptions().getProgramParameters();
    }

    @Override
    public void setWorkingDirectory(@Nullable String value) {
        String normalizedValue = StringUtil.isEmptyOrSpaces(value) ? null : value.trim();
        String independentValue = PathUtil.toSystemIndependentName(normalizedValue);
        getOptions().setWorkingDirectory(Comparing.equal(independentValue, getProject().getBasePath()) ? null : independentValue);
    }

    @Override
    public String getWorkingDirectory() {
        return getOptions().getWorkingDirectory();
    }

    @Override
    public void setPassParentEnvs(boolean value) {
        getOptions().setPassParentEnv(value);
    }

    @Override
    @NotNull
    public Map<String, String> getEnvs() {
        return getOptions().getEnv();
    }

    @Override
    public void setEnvs(@NotNull Map<String, String> envs) {
        getOptions().setEnv(envs);
    }

    @Override
    public boolean isPassParentEnvs() {
        return getOptions().isPassParentEnv();
    }

    @Override
    @Nullable
    public String getRunClass() {
        return getMainClassName();
    }

    @Override
    @Nullable
    public String getPackage() {
        return null;
    }

    @Override
    public boolean isAlternativeJrePathEnabled() {
        return getOptions().isAlternativeJrePathEnabled();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setAlternativeJrePathEnabled(boolean enabled) {
        boolean changed = isAlternativeJrePathEnabled() != enabled;
        getOptions().setAlternativeJrePathEnabled(enabled);
        onAlternativeJreChanged(changed, getProject());
    }

    @Nullable
    @Override
    public String getAlternativeJrePath() {
        return getOptions().getAlternativeJrePath();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setAlternativeJrePath(@Nullable String path) {
        boolean changed = !Objects.equals(getAlternativeJrePath(), path);
        getOptions().setAlternativeJrePath(path);
        onAlternativeJreChanged(changed, getProject());
    }

    public static void onAlternativeJreChanged(boolean changed, Project project) {
        if (changed) {
            AlternativeSdkRootsProvider.reindexIfNeeded(project);
        }
    }

    public boolean isProvidedScopeIncluded() {
        return getOptions().isIncludeProvidedScope();
    }

    public void setIncludeProvidedScope(boolean value) {
        getOptions().setIncludeProvidedScope(value);
    }

    @Override
    public Collection<Module> getValidModules() {
        return JavaRunConfigurationModule.getModulesForClass(getProject(), getMainClassName());
    }

    @Override
    public void readExternal(@NotNull final Element element) {
        super.readExternal(element);
        JavaRunConfigurationExtensionManager.getInstance().readExternal(this, element);
    }

    @Override
    public void setOptionsFromConfigurationFile(@NotNull BaseState state) {
        super.setOptionsFromConfigurationFile(state);
    }

    @Override
    public void writeExternal(@NotNull Element element) {
        super.writeExternal(element);

        JavaRunConfigurationExtensionManager.getInstance().writeExternal(this, element);
    }

    @Nullable
    @Override
    public ShortenCommandLine getShortenCommandLine() {
        return getOptions().getShortenClasspath();
    }

    @Override
    public void setShortenCommandLine(@Nullable ShortenCommandLine mode) {
        getOptions().setShortenClasspath(mode);
    }

    @NotNull
    @Override
    public InputRedirectOptions getInputRedirectOptions() {
        return getOptions().getRedirectOptions();
    }

    public boolean isSwingInspectorEnabled() {
        return getOptions().isSwingInspectorEnabled();
    }

    public void setSwingInspectorEnabled(boolean value) {
        getOptions().setSwingInspectorEnabled(value);
    }

    public static class JavaApplicationCommandLineState<T extends JekaRunConfiguration> extends ApplicationCommandLineState<T> {
        public JavaApplicationCommandLineState(@NotNull final T configuration, final ExecutionEnvironment environment) {
            super(configuration, environment);
        }

        @Override
        protected boolean isProvidedScopeIncluded() {
            return myConfiguration.isProvidedScopeIncluded();
        }
    }

}

