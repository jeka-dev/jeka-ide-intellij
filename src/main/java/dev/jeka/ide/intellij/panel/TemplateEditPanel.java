package dev.jeka.ide.intellij.panel;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import dev.jeka.core.api.function.JkConsumers;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

class TemplateEditPanel {

    private final JBTextField nameTextField = new JBTextField();

    private final JBTextField cmdTextField = new ExpandableTextField();

    private final JBTextArea descTextarea = new JBTextArea();

    private final AtomicBoolean listenerOn = new AtomicBoolean(true);

    @Getter
    private JekaTemplate editedTemplate;

    @Getter
    private JkConsumers<JekaTemplate, Void> nameChangeListener = JkConsumers.of();

    @Getter
    private JPanel panel;

    TemplateEditPanel() {
        nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (listenerOn.get()) {
                    editedTemplate.setName(nameTextField.getText());
                    nameChangeListener.accept(editedTemplate);
                }
            }
        });
        cmdTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (listenerOn.get()) {
                    editedTemplate.setCommandArgs(cmdTextField.getText());
                }
            }
        });
        descTextarea.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (listenerOn.get()) {
                    editedTemplate.setDescription(descTextarea.getText());
                }
            }
        });
        descTextarea.setFont(cmdTextField.getFont());
        descTextarea.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        panel = panel();
    }

    void setEnabled(boolean enabled) {
        this.nameTextField.setEnabled(enabled);
        this.cmdTextField.setEnabled(enabled);
        this.descTextarea.setEnabled(enabled);
    }

    private JPanel panel() {
        JPanel cmdPanel = UI.PanelFactory.panel(cmdTextField)
                .withLabel("Cmd Arguments:")
                .moveLabelOnTop()
                .createPanel();
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("Name:", nameTextField, false)
                .addComponent(cmdPanel)
                .addLabeledComponentFillVertically("Description:", descTextarea)
                .getPanel();
    }

    public void fill(JekaTemplate template) {
        editedTemplate = template;
        listenerOn.set(false);
        nameTextField.setText(template.getName());
        cmdTextField.setText(template.getCommandArgs());
        descTextarea.setText(template.getDescription());
        listenerOn.set(true);

    }

    public JekaTemplate getTemplate() {
        return JekaTemplate.builder()
                .name(nameTextField.getText().trim())
                .commandArgs(cmdTextField.getText().trim())
                .description(descTextarea.getText().trim())
                .build();
    }

    private void updateName(DocumentEvent e) {
        this.editedTemplate.setName(nameTextField.getText());
    }

    private void updateCmd(DocumentEvent e) {
        this.editedTemplate.setCommandArgs(cmdTextField.getText());
    }




}
