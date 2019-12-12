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

import com.intellij.application.options.ModuleDescriptionsComboBox;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.configurations.ConfigurationUtil;
import com.intellij.execution.ui.*;
import com.intellij.execution.util.JreVersionDetector;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.JavaCodeFragment;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.EditorTextFieldWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * @author Jerome Angibaud
 */
public class JekaRunConfigurable extends SettingsEditor<JekaRunConfiguration> implements PanelWithAnchor {

  private final Project myProject;
  private final ConfigurationModuleSelector myModuleSelector;
  private final JreVersionDetector myVersionDetector;

  private JPanel myWholePanel;
  private LabeledComponent<EditorTextFieldWithBrowseButton> myMainClass;
  private CommonJavaParametersPanel myCommonProgramParameters;
  private LabeledComponent<ModuleDescriptionsComboBox> myModule;
  private LabeledComponent<JBCheckBox> myIncludeProvidedDeps;
  private JrePathEditor myJrePathEditor;
  private LabeledComponent<ShortenCommandLineModeCombo> myShortenClasspathModeCombo;
  private JCheckBox myShowSwingInspectorCheckbox;
  private JComponent myAnchor;

  public JekaRunConfigurable(Project project) {
    myProject = project;
    try {
      $$$setupUI$$$();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    myModuleSelector = new ConfigurationModuleSelector(project, myModule.getComponent());
    myVersionDetector = new JreVersionDetector();

    myJrePathEditor.setDefaultJreSelector(DefaultJreSelector.fromSourceRootsDependencies(myModule.getComponent(), getMainClassField()));
    myCommonProgramParameters.setModuleContext(myModuleSelector.getModule());
    ProgramParametersConfigurator.addMacroSupport(myCommonProgramParameters.getProgramParametersComponent().getComponent().getEditorField());
    myModule.getComponent().addActionListener(e -> myCommonProgramParameters.setModuleContext(myModuleSelector.getModule()));
    new ClassBrowser.AppClassBrowser<EditorTextField>(project, myModuleSelector).setField(getMainClassField());
    myShortenClasspathModeCombo.setComponent(new ShortenCommandLineModeCombo(myProject, myJrePathEditor, myModule.getComponent()));
    myIncludeProvidedDeps.setComponent(new JBCheckBox(ExecutionBundle.message("application.configuration.include.provided.scope")));

    myAnchor = UIUtil.mergeComponentsWithAnchor(myMainClass, myCommonProgramParameters, myJrePathEditor, myModule,
            myShortenClasspathModeCombo, myIncludeProvidedDeps);
  }

  @Override
  public void applyEditorTo(@NotNull JekaRunConfiguration configuration) throws ConfigurationException {
    if (configuration == null) {
      $$$reportNull$$$0(0);
    }
    myCommonProgramParameters.applyTo(configuration);
    myModuleSelector.applyTo(configuration);

    String className = getMainClassField().getText();
    PsiClass aClass = myModuleSelector.findClass(className);
    configuration.setMainClassName(aClass != null ? JavaExecutionUtil.getRuntimeQualifiedName(aClass) : className);
    configuration.setAlternativeJrePath(myJrePathEditor.getJrePathOrName());
    configuration.setAlternativeJrePathEnabled(myJrePathEditor.isAlternativeJreSelected());
    configuration.setSwingInspectorEnabled(isJre50Configured(configuration) && myShowSwingInspectorCheckbox.isSelected());
    configuration.setShortenCommandLine(myShortenClasspathModeCombo.getComponent().getSelectedItem());
    configuration.setIncludeProvidedScope(myIncludeProvidedDeps.getComponent().isSelected());

    updateShowSwingInspector(configuration);
  }

  @Override
  public void resetEditorFrom(@NotNull JekaRunConfiguration configuration) {
    if (configuration == null) {
      $$$reportNull$$$0(1);
    }
    myCommonProgramParameters.reset(configuration);
    myModuleSelector.reset(configuration);
    getMainClassField().setText(configuration.getMainClassName() != null ? configuration.getMainClassName().replaceAll("\\$", "\\.") : "");
    myJrePathEditor.setPathOrName(configuration.getAlternativeJrePath(), configuration.isAlternativeJrePathEnabled());
    myShortenClasspathModeCombo.getComponent().setSelectedItem(configuration.getShortenCommandLine());
    myIncludeProvidedDeps.getComponent().setSelected(configuration.isProvidedScopeIncluded());

    updateShowSwingInspector(configuration);
  }

  private boolean isJre50Configured(JekaRunConfiguration configuration) {
    return myVersionDetector.isJre50Configured(configuration) || myVersionDetector.isModuleJre50Configured(configuration);
  }

  private void updateShowSwingInspector(JekaRunConfiguration configuration) {
    if (isJre50Configured(configuration)) {
      myShowSwingInspectorCheckbox.setEnabled(true);
      myShowSwingInspectorCheckbox.setSelected(configuration.isSwingInspectorEnabled());
      myShowSwingInspectorCheckbox.setText(ExecutionBundle.message("show.swing.inspector"));
    } else {
      myShowSwingInspectorCheckbox.setEnabled(false);
      myShowSwingInspectorCheckbox.setSelected(false);
      myShowSwingInspectorCheckbox.setText(ExecutionBundle.message("show.swing.inspector.disabled"));
    }
  }

  public EditorTextFieldWithBrowseButton getMainClassField() {
    return myMainClass.getComponent();
  }

  public CommonJavaParametersPanel getCommonProgramParameters() {
    return myCommonProgramParameters;
  }

  @NotNull
  @Override
  public JComponent createEditor() {
    final JPanel myWholePanel = this.myWholePanel;
    if (myWholePanel == null) {
      $$$reportNull$$$0(2);
    }
    return myWholePanel;
  }

  private void createUIComponents() {
    myMainClass = new LabeledComponent<>();
    myMainClass.setComponent(new EditorTextFieldWithBrowseButton(myProject, true, (declaration, place) -> {
      if (declaration instanceof PsiClass) {
        PsiClass aClass = (PsiClass) declaration;
        if (ConfigurationUtil.MAIN_CLASS.value(aClass) && PsiMethodUtil.findMainMethod(aClass) != null ||
                place.getParent() != null && myModuleSelector.findClass(((PsiClass) declaration).getQualifiedName()) != null) {
          return JavaCodeFragment.VisibilityChecker.Visibility.VISIBLE;
        }
      }
      return JavaCodeFragment.VisibilityChecker.Visibility.NOT_VISIBLE;
    }));
    myShortenClasspathModeCombo = new LabeledComponent<>();
  }

  @Override
  public JComponent getAnchor() {
    return myAnchor;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    myAnchor = anchor;
    myMainClass.setAnchor(anchor);
    myCommonProgramParameters.setAnchor(anchor);
    myJrePathEditor.setAnchor(anchor);
    myModule.setAnchor(anchor);
    myShortenClasspathModeCombo.setAnchor(anchor);
  }

  private static /* synthetic */ void $$$reportNull$$$0(final int n) {
    String format = null;
    switch (n) {
      default: {
        format = "Argument for @NotNull parameter '%s' of %s.%s must not be null";
        break;
      }
      case 2: {
        format = "@NotNull method %s.%s must not return null";
        break;
      }
    }
    int n2 = 0;
    switch (n) {
      default: {
        n2 = 3;
        break;
      }
      case 2: {
        n2 = 2;
        break;
      }
    }
    final Object[] args = new Object[n2];
    switch (n) {
      default: {
        args[0] = "configuration";
        break;
      }
      case 2: {
        args[0] = "com/intellij/execution/application/ApplicationConfigurable";
        break;
      }
    }
    switch (n) {
      default: {
        args[1] = "com/intellij/execution/application/ApplicationConfigurable";
        break;
      }
      case 2: {
        args[1] = "createEditor";
        break;
      }
    }
    switch (n) {
      default: {
        args[2] = "applyEditorTo";
        break;
      }
      case 1: {
        args[2] = "resetEditorFrom";
        break;
      }
      case 2: {
        break;
      }
    }
    final String format2 = String.format(format, args);
    RuntimeException ex = null;
    switch (n) {
      default: {
        ex = new IllegalArgumentException(format2);
        break;
      }
      case 2: {
        ex = new IllegalStateException(format2);
        break;
      }
    }
    throw ex;
  }

  private /* synthetic */ void $$$setupUI$$$() throws Exception {
    this.createUIComponents();
    myWholePanel = new JPanel();
    myWholePanel.setLayout(new GridLayoutManager(8, 1,
            new Insets(0, 0, 0, 0),
            -1, -1, false, false));
    myWholePanel.setMinimumSize(new Dimension(-1, -1));
    myWholePanel.setPreferredSize(new Dimension(-1, -1));
    myWholePanel.add((Component) (this.myCommonProgramParameters = new CommonJavaParametersPanel()),
            new GridConstraints(1, 0, 1, 1, 0, 1, 3,
                    0, (Dimension) null, (Dimension) null, (Dimension) null));
    final LabeledComponent<EditorTextFieldWithBrowseButton> myMainClass = this.myMainClass;
    myMainClass.setLabelLocation("West");
    myMainClass.setText(ResourceBundle.getBundle("messages/ExecutionBundle").getString("application.configuration.main.class.label"));
    myWholePanel.add((Component) myMainClass, new GridConstraints
            (0, 0, 1, 1, 0, 1, 3, 3,
                    (Dimension) null, new Dimension(-1, 20), (Dimension) null));
    final LabeledComponent labeledComponent = new LabeledComponent();
    (this.myModule = (LabeledComponent<ModuleDescriptionsComboBox>) labeledComponent)
            .setComponentClass("com.intellij.application.options.ModuleDescriptionsComboBox");
    labeledComponent.setLabelLocation("West");
    labeledComponent.setText(ResourceBundle.getBundle("messages/ExecutionBundle")
            .getString("application.configuration.use.classpath.and.jdk.of.module.label"));
    myWholePanel.add((Component) labeledComponent, new GridConstraints(2, 0, 1, 1,
            0, 1, 3, 0, (Dimension) null, (Dimension) null, (Dimension) null));
    myWholePanel.add((Component) (this.myJrePathEditor = new JrePathEditor()),
            new GridConstraints(4, 0, 1, 1, 0, 1, 3,
                    3, (Dimension) null, (Dimension) null, (Dimension) null));
    final JCheckBox checkBox = new JCheckBox();
    this.$$$loadButtonText$$$(this.myShowSwingInspectorCheckbox = checkBox,
            ResourceBundle.getBundle("messages/ExecutionBundle").getString("show.swing.inspector"));
    myWholePanel.add(checkBox, new GridConstraints(6, 0, 1, 1, 8, 0,
            3, 0, (Dimension) null, (Dimension) null, (Dimension) null));
    myWholePanel.add((Component) new Spacer(), new GridConstraints(7, 0, 1, 1,
            0, 2, 1, 6, (Dimension) null, (Dimension) null, (Dimension) null));
    final LabeledComponent<ShortenCommandLineModeCombo> myShortenClasspathModeCombo = this.myShortenClasspathModeCombo;
    myShortenClasspathModeCombo.setLabelLocation("West");
    myShortenClasspathModeCombo.setText(ResourceBundle.getBundle("messages/ExecutionBundle")
            .getString("application.configuration.shorten.command.line.label"));
    myWholePanel.add((Component) myShortenClasspathModeCombo, new GridConstraints(5, 0, 1,
            1, 0, 1, 3, 0, (Dimension) null, (Dimension) null, (Dimension) null));
    final LabeledComponent labeledComponent2 = new LabeledComponent();
    (this.myIncludeProvidedDeps = (LabeledComponent<JBCheckBox>) labeledComponent2).setLabelLocation("West");
    labeledComponent2.setText("");
    myWholePanel.add((Component) labeledComponent2, new GridConstraints(3, 0, 1, 1,
            8, 0, 3, 0, (Dimension) null, (Dimension) null, (Dimension) null));
  }

  private /* synthetic */ void $$$loadButtonText$$$(final AbstractButton abstractButton, final String s) {
    final StringBuffer sb = new StringBuffer();
    int n = 0;
    char char1 = '\0';
    int length = -1;
    for (int i = 0; i < s.length(); ++i) {
      if (s.charAt(i) == '&') {
        if (++i == s.length()) {
          break;
        }
        if (n == 0 && s.charAt(i) != '&') {
          n = 1;
          char1 = s.charAt(i);
          length = sb.length();
        }
      }
      sb.append(s.charAt(i));
    }
    abstractButton.setText(sb.toString());
    if (n != 0) {
      abstractButton.setMnemonic(char1);
      abstractButton.setDisplayedMnemonicIndex(length);
    }
  }
}
