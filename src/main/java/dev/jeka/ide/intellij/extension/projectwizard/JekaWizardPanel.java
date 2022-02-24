package dev.jeka.ide.intellij.extension.projectwizard;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import dev.jeka.ide.intellij.panel.ScaffoldFormPanel;
import lombok.Getter;

import javax.swing.*;
import java.nio.file.Paths;

class JekaWizardPanel {

    private JBTextField distributionPathText = new ExtendableTextField();

    private JBTextField nameTextField = new JBTextField("untitled");

    private Project project;

    @Getter
    private ScaffoldFormPanel scaffoldPanel;

    @Getter
    private JPanel panel;

    private TextFieldWithBrowseButton locationTextField;

    JekaWizardPanel(Project project) {
        this.project = project;
        panel = panel();

    }

    private JPanel panel() {
        locationTextField = new TextFieldWithBrowseButton(distributionPathText);
        locationTextField.setText(Paths.get(project.getBasePath()).toString());
        locationTextField.addBrowseFolderListener(
                "Module location",
                "Select a module location",
                project,
                fileChooserDescriptor()
        );
        this.scaffoldPanel = ScaffoldFormPanel.of(project, null, true, false);
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Name:"), nameTextField)
                .addLabeledComponent(new JBLabel("Location:"), locationTextField)
                .addComponent(this.scaffoldPanel.getPanel(), 20)
                .addComponentFillVertically(new JPanel(), 0)  // to make component have their preffered size.
                .getPanel();
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
}
