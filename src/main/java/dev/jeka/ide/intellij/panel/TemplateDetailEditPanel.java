package dev.jeka.ide.intellij.panel;

import com.google.common.escape.CharEscaper;
import com.google.common.escape.Escaper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
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

class TemplateDetailEditPanel {

    private final JBTextField nameTextField = new JBTextField();

    private final JBTextArea cmdTextArea = new JBTextArea();

    private final JBTextArea descTextarea = new JBTextArea();

    private final AtomicBoolean listenerOn = new AtomicBoolean(true);

    @Getter
    private JekaTemplate editedTemplate;

    @Getter
    private JkConsumers<JekaTemplate> nameChangeListener = JkConsumers.of();

    @Getter
    private JPanel panel;

    TemplateDetailEditPanel() {
        nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (listenerOn.get()) {
                    editedTemplate.setName(nameTextField.getText());
                    nameChangeListener.accept(editedTemplate);
                }
            }
        });
        cmdTextArea.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (listenerOn.get()) {
                    editedTemplate.setCommandArgs(cmdTextArea.getText());
                }
            }
        });
        descTextarea.setFont(Font.getFont("Arial"));
        descTextarea.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (listenerOn.get()) {
                    editedTemplate.setDescription(descTextarea.getText());
                }
            }
        });
        descTextarea.setFont(cmdTextArea.getFont());
        descTextarea.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        cmdTextArea.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        descTextarea.setLineWrap(true);
        descTextarea.setWrapStyleWord(true);
        cmdTextArea.setLineWrap(true);
        cmdTextArea.setWrapStyleWord(true);
        panel = panel();
    }

    void setEnabled(boolean enabled) {
        this.nameTextField.setEnabled(enabled);
        this.cmdTextArea.setEnabled(enabled);
        this.descTextarea.setEnabled(enabled);
    }

    private JPanel panel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("Name:", nameTextField, false)
                .addLabeledComponentFillVertically("Command:", cmdTextArea)
                .addLabeledComponentFillVertically("Description:", descTextarea)
                .getPanel();
    }

    public void fill(JekaTemplate template) {
        editedTemplate = template;
        listenerOn.set(false);
        nameTextField.setText(template.getName());
        cmdTextArea.setText(template.getCommandArgs());
        descTextarea.setText(template.getDescription());
        listenerOn.set(true);

    }

}
