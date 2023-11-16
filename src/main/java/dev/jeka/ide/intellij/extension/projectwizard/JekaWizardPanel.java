package dev.jeka.ide.intellij.extension.projectwizard;

import com.intellij.ide.util.projectWizard.ProjectWizardUtil;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.panel.ScaffoldFormPanel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

class JekaWizardPanel {

    private static final int MARGIN = 25;
    @Getter
    private WizardContext wizardContext;

    @Getter
    private ScaffoldFormPanel scaffoldPanel;

    @Getter
    private JPanel panel;

    private TextFieldWithBrowseButton locationTextField;

    private JBTextField nameTextField;

    private JBLabel fullPathLabel = new JBLabel();

    JekaWizardPanel(WizardContext wizardContext) {
        this.wizardContext = wizardContext;
        panel = panel();
    }

    private JPanel panel() {
        nameTextField = new ExtendableTextField();
        JBTextField locationPathText = new ExtendableTextField();
        locationTextField = new TextFieldWithBrowseButton(locationPathText);
        locationTextField.addBrowseFolderListener(
                "Module location",
                "Select a module location",
                wizardContext.getProject(),
                fileChooserDescriptor()
        );
        ComponentValidator nameValidator = new ComponentValidator(wizardContext.getDisposable())
                .withValidator(this::validateName)
                .installOn(nameTextField);
        ComponentValidator locationValidator = new ComponentValidator(locationTextField)
                .withValidator(this::validateLocation)
                .installOn(locationTextField);

        nameValidator.enableValidation();
        locationValidator.enableValidation();
        nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                nameValidator.revalidate();
                updateLabel();
            }
        });
        locationPathText.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                locationValidator.revalidate();
                updateLabel();
            }
        });

        String baseDir = wizardContext.getProjectFileDirectory();
        String name = wizardContext.getProject() == null
                ? ProjectWizardUtil.findNonExistingFileName(baseDir, "untitled", "")
                : ModuleHelper.findNonExistingModuleName(wizardContext.getProject(), "untitled");
        nameTextField.setText(name);

        locationTextField.setText(baseDir);
        this.scaffoldPanel = ScaffoldFormPanel.of(wizardContext.getProject(), null, true, false);

        Font font = fullPathLabel.getFont().deriveFont((float) fullPathLabel.getFont().getSize() - 1);
        fullPathLabel.setFontColor(UIUtil.FontColor.BRIGHTER);
        this.fullPathLabel.setFont(font);
        updateLabel();
        JPanel result = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Name:"), nameTextField)
                .addLabeledComponent(new JBLabel("Location:"), locationTextField, 15)
                .addLabeledComponent("", fullPathLabel)
                .addComponentFillVertically(this.scaffoldPanel.getPanel(), 20)
                .getPanel();
        result.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, 0, MARGIN));
        return result;
    }

    private static FileChooserDescriptor fileChooserDescriptor() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true,
                false, false, false, false) {
        };
        return fileChooserDescriptor;
    }

    String getLocation() {
        return Paths.get(locationTextField.getText()).resolve(this.nameTextField.getText()).toString();
    }

    String getName() {
        return nameTextField.getText().trim();
    }

    private String defaultName() {
        String base = "untitled";
        if (!nameExistForModule(base)) {
            return base;
        }
        for (int i=2;;i++) {
            String candidate = base + i;
            if (!nameExistForModule(candidate)) {
                return candidate;
            }
        }
    }

    private boolean nameExistForModule(String candidate) {
        if (wizardContext.getProject() == null) {
            return false;
        }
        Module[] modules = ModuleManager.getInstance(wizardContext.getProject()).getModules();
        return Arrays.stream(modules)
                .map(Module::getName)
                .anyMatch(candidate::equals);
    }

    String validate() {
        ValidationInfo validationInfo = validateName();
        if (validationInfo != null) {
            return validationInfo.message;
        }
        validationInfo = validateLocation();
        if (validationInfo != null) {
            return validationInfo.message;
        }
        return null;
    }

    private ValidationInfo validateName() {
        boolean isNameForModule = wizardContext.getProject() != null;
        String candidate = this.nameTextField.getText().trim();
        if (candidate.equals("")) {
            return new ValidationInfo("Field must be set").forComponent(nameTextField);
        }
        if (isNameForModule) {
            Optional<String> nameInalid = ModuleHelper.validateName(candidate);
            if (nameInalid.isPresent()) {
                return new ValidationInfo("Only Latin characters, digits, '_', '-' and '.' are allowed here.")
                        .forComponent(nameTextField);
            }
            if (nameExistForModule(candidate)) {
                return new ValidationInfo("Module with name '" + candidate + "' already exists.")
                        .forComponent(nameTextField);
            }
        } else {
            if (Files.exists(Paths.get(getLocation()))) {
                return new ValidationInfo("Project with name '" + candidate + "' already exists in "
                        + locationTextField.getText())
                        .forComponent(nameTextField);
            }
        }
        return null;
    }

    private ValidationInfo validateLocation() {
        String candidate = this.locationTextField.getText().trim();
        if (candidate.equals("")) {
            return new ValidationInfo("Field must be set").forComponent(locationTextField.getChildComponent());
        }
        return null;
    }

    private void updateLabel() {
        String prefix = wizardContext.getProject() == null ? "Project" : "Module";
        String text = prefix + " will be created in: " + getLocation();
        this.fullPathLabel.setText(text);
    }

}
