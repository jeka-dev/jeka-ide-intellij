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
import dev.jeka.ide.intellij.panel.ScaffoldFormPanel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

class JekaWizardPanel {

    private static final int MARGIN = 25;

    @Getter
    private WizardContext wizardContext;

    @Getter
    private ScaffoldFormPanel scaffoldPanel;

    @Getter
    private JPanel panel;

    private TextFieldWithBrowseButton locationTextField;

    JekaWizardPanel(@Nullable WizardContext wizardContext) {
        this.wizardContext = wizardContext;
        panel = panel();
    }

    private JPanel panel() {
        JBTextField nameLabel = new JBTextField();
        nameLabel.setEditable(false);
        JBTextField locationPathText = new ExtendableTextField();
        locationTextField = new TextFieldWithBrowseButton(locationPathText);
        if (wizardContext.getProject() != null) {
            locationTextField.setText(Paths.get(wizardContext.getProject().getBasePath()).resolve(defaultName()).toString());
        } else {
            String baseDir = wizardContext.getProjectFileDirectory();
            String name = ProjectWizardUtil.findNonExistingFileName(baseDir, "untitled", "");
            locationTextField.setText(baseDir + File.separator + name);
        }
        locationTextField.addBrowseFolderListener(
                "Module location",
                "Select a module location",
                wizardContext.getProject(),
                fileChooserDescriptor()
        );
        locationPathText.getDocument().addDocumentListener(new DocumentAdapter() {
               @Override
               protected void textChanged(@NotNull DocumentEvent e) {
                   nameLabel.setText(getName());
               }
        });
        nameLabel.setText(getName());
        ComponentValidator componentValidator = new ComponentValidator(locationTextField).withValidator(this::validateLocation)
                .installOn(locationTextField);
        componentValidator.enableValidation();
        locationPathText.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                ComponentValidator.getInstance(locationTextField).ifPresent(v -> v.revalidate());
            }
        });
        this.scaffoldPanel = ScaffoldFormPanel.of(wizardContext.getProject(), null, true, false);

        JPanel result = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Name:"), nameLabel)
                .addLabeledComponent(new JBLabel("Location:"), locationTextField, 15)
                .addComponentFillVertically(this.scaffoldPanel.getPanel(), 20)
                .getPanel();
        result.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
        return result;
    }

    private static FileChooserDescriptor fileChooserDescriptor() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true,
                false, false, false, false) {
        };
        return fileChooserDescriptor;
    }

    String getLocation() {
        return this.locationTextField.getText();
    }

    String getName() {
        return Paths.get(locationTextField.getText().trim()).getFileName().toString();
    }

    private String defaultName() {
        String base = "untitled";
        if (!nameExist(base)) {
            return base;
        }
        for (int i=2;;i++) {
            String candidate = base + i;
            if (!nameExist(candidate)) {
                return candidate;
            }
        }
    }

    private boolean nameExist(String candidate) {
        Module[] modules = ModuleManager.getInstance(wizardContext.getProject()).getModules();
        return Arrays.stream(modules)
                .map(Module::getName)
                .anyMatch(candidate::equals);
    }

    private String validateModuleName() {
        if (wizardContext.getProject() == null) {
            return null;
        }
        String candidate = getName();
        if (nameExist(candidate)) {
            return "Module with name '" + candidate + "' already exists.";
        }
        return null;
    }

    String validate() {
        if (wizardContext.getProject() != null) {
            return validateModuleName();
        }

        return null;
    }

    private ValidationInfo validateLocation() {
        String nameValidation = validateModuleName();
        if (nameValidation != null) {
            return new ValidationInfo(nameValidation).forComponent(locationTextField);
        }
        return null;
    }

}
