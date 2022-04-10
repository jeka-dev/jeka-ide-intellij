package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.GroupHeaderSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.ide.intellij.panel.explorer.tree.JekaToolWindowTreeService;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RunFormPanel {

    @Getter
    private JPanel panel;

    private final Module module;

    private final JBTextField cmdTextField = new ExpandableTextField();

    RunFormPanel(Module module, String originalCommand) {
        this.module = module;
        this.panel = panel();
        cmdTextField.setText(originalCommand);
    }

    public String getCmd() {
        return cmdTextField.getText();
    }

    private JPanel panel() {
        JPanel cmdPanel = UI.PanelFactory.panel(cmdTextField)
                .withLabel("Cmd Arguments:")
                .moveLabelOnTop()
                .createPanel();
        JPanel optionPanel = new OptionPanel();
        JPanel behaviorPanel = new BehaviorPanel();

        JPanel result = FormBuilder.createFormBuilder()
                //.addLabeledComponent("Name:", nameTextField, false)
                .addComponent(cmdPanel)
                .addLabeledComponent("Log Options", new GroupHeaderSeparator(new Insets(0,0,20,0)))
                .addComponent(optionPanel, 10)
                .addLabeledComponent("Behavior Options", new GroupHeaderSeparator(new Insets(0,0,20,0)))
                .addComponent(behaviorPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        result.setMaximumSize(new Dimension(600, 0));
        return result;
    }

    void adaptText(String keyword, Object value) {
        List<String> tokens = Arrays.stream(cmdTextField.getText().split(" "))
                .map(String::trim)
                .collect(Collectors.toList());
        final List<String> result = new LinkedList<>();
        boolean found = false;
        for (String token : tokens) {
            String extractedOption = extractStandardOption(token);
            if (extractedOption == null || !keyword.equals(extractedOption)) {
                result.add(token);
            } else {
                found = true;
                result.add(toTokenValue(keyword, value));
            }
        }
        if (!found) {
            result.add(toTokenValue(keyword, value));
        }
        String text = String.join(" ", result);
        cmdTextField.setText(text);
    }

    private static String toTokenValue(String keyword, Object value) {
        if (Boolean.TRUE.equals(value)) {
            return "-" + keyword;
        }
        if (value == null || Boolean.FALSE.equals(value)) {
            return "";
        } else {
            return "-" + keyword + "=" + value;
        }
    }

    private String extractStandardOption(String token) {
        if (token.startsWith("-D")) {
            return null;
        }
        if (!token.startsWith("-")) {
            return null;
        }
        if (!token.contains("=")) {
            return token.substring(1);
        }
        return JkUtilsString.substringBeforeFirst(token.substring(1), "=");
    }

    private class OptionPanel extends JBPanel {

        private ComboBox<String> ls;

        private JBCheckBox lvCb = new JBCheckBox("log.verbose (-lv)");

        private JBCheckBox lsuCb = new JBCheckBox("log.setup (-lsu)");

        private JBCheckBox lstCb = new JBCheckBox("log.stacktrace (-lst)");

        private JBCheckBox lriCb = new JBCheckBox("log.runtime.info (-lri)");

        private JBCheckBox livCB = new JBCheckBox("log.ivy.setup (-liv)");

        private JBCheckBox ldCB = new JBCheckBox("log.duration (-ld)");

        OptionPanel() {
            Border border = BorderFactory.createEmptyBorder(0,20,0,0);
            setBorder(border);
            GridLayout layout = new GridLayout(0,2);
            layout.setVgap(0);
            layout.setHgap(20);
            this.setLayout(layout);

            // -ls
            String[] values = new String[] {"default", "INDENT", "BRACE", "DEBUG"};
            ls = new ComboBox<>(values);
            ls.setName("log.style (-ls)");
            ls.setMaximumRowCount(20);
            JPanel lsPanel = UI.PanelFactory.panel(ls)
                    .withLabel("log.style (-ls)")
                    .withTooltip("How logs will be rendered.<ul>" +
                            "<li>INDENT : Just indent when changing nested level.</li>" +
                            "<li>BRACE : Wrap each nested level in '{}' and mention duration for each block.</li>" +
                            "<li>DEBUG : Mention class name and line number where the log has been emitted..</li>" +
                            "</ul>")
                    .createPanel();
            this.add(lsPanel);
            ls.addItemListener(event -> {
                String selectedValue = ls.getItem();
                selectedValue = selectedValue.equals("default") ? null : selectedValue;
                adaptText("ls", selectedValue);
            });

            this.add(new JPanel());

            // -lv
            listen(lvCb, "lv");
            this.add(itemPanel(lvCb, "Display trace level events."));

            // -lsu
            listen(lsuCb, "lsu");
            this.add(itemPanel(lsuCb, "Display information when Jeka is " +
                    "setting up build classes and instances. It can be combined with -lv for more details."));

            // -lst
            listen(lstCb, "lst");
            this.add(itemPanel(lstCb, "Display complete stacktrace on failure. This is not needed if -lv is checked."));

            // -lri
            listen(lriCb, "lri");
            this.add(itemPanel(lriCb, "Display Jeka runtime information at start of execution."));

            // -liv
            listen(livCB, "liv");
            this.add(itemPanel(livCB, "Display ivy logs with TRACE or DEBUG level."));

            // -ld
            listen(ldCB, "ld");
            this.add(itemPanel(ldCB, "Display total execution time at the end of the execution."));
        }

        private JPanel itemPanel(JComponent cmp, String tooltipText) {
            return UI.PanelFactory.panel(cmp)
                    .withTooltip(tooltipText)
                    .moveCommentRight()
                    .createPanel();
        }

        private void listen(JBCheckBox cb, String keyword) {
            cb.addItemListener(item -> {
                Object value = cb.isSelected() ? true : false;
                adaptText(keyword, value);
            });
        }

    }

    private class BehaviorPanel extends JBPanel {

        private ComboBox<String> kb;

        private JBCheckBox wcCb = new JBCheckBox("work.clean (-wc)");

        private JBCheckBox dciCb = new JBCheckBox("def.compile.ignore-failure (-dci)");

        BehaviorPanel() {
            Border border = BorderFactory.createEmptyBorder(0,20,0,0);
            setBorder(border);
            GridLayout layout = new GridLayout(0,2);
            layout.setVgap(0);
            layout.setHgap(20);
            this.setLayout(layout);

            // -kb
            List<String> valueList = new LinkedList<>();
            valueList.add("default");
            List<String> kbeans = module.getProject().getService(JekaToolWindowTreeService.class).getKbeans(module);
            valueList.addAll(kbeans);
            String[] values = valueList.toArray(new String[0]);
            kb = new ComboBox<>(values);
            kb.setName("log.style (-ls)");
            kb.setMaximumRowCount(20);
            JPanel lsPanel = UI.PanelFactory.panel(kb)
                    .withLabel("kbean (-kb)")
                    .withTooltip("The first KBean to be instantiated.<br/>" +
                            "By default this is the first one found in 'def' folder.")
                    .createPanel();
            this.add(lsPanel);
            kb.addItemListener(event -> {
                String selectedValue = kb.getItem();
                selectedValue = selectedValue.equals("default") ? null : selectedValue;
                adaptText("kb", selectedValue);
            });

            this.add(new JPanel());

            // -wc
            listen(wcCb, "wc");
            this.add(itemPanel(wcCb, "Delete Jeka caches before running."));

            // -dci
            listen(dciCb, "dci");
            this.add(itemPanel(dciCb, "Ignore compilation failure on 'def' folder. It can be useful if you want execute a KBean which is not on 'def' folder."));
        }

        private JPanel itemPanel(JComponent cmp, String tooltipText) {
            return UI.PanelFactory.panel(cmp)
                    .withTooltip(tooltipText)
                    .moveCommentRight()
                    .createPanel();
        }

        private void listen(JBCheckBox cb, String keyword) {
            cb.addItemListener(item -> {
                Object value = cb.isSelected() ? true : false;
                adaptText(keyword, value);
            });
        }

    }


}
