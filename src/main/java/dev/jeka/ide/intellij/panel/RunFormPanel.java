package dev.jeka.ide.intellij.panel;

import com.google.common.base.Strings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.GroupHeaderSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.ide.intellij.panel.explorer.tree.BeanNode;
import dev.jeka.ide.intellij.panel.explorer.tree.JekaToolWindowTreeService;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RunFormPanel {

    @Getter
    private JPanel panel;

    private OptionPanel optionPanel;

    private BehaviorPanel behaviorPanel;

    private final Module module;

    private final JBTextField cmdTextField = new ExpandableTextField();

    private volatile boolean updateCmd = true;

    public RunFormPanel(Module module, String originalCommand) {
        this.module = module;
        this.panel = panel();
        cmdTextField.setText(originalCommand);
        cmdTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                syncOptionsWithCmdLine();
            }
        });
    }

    public void syncOptionsWithCmdLine() {
        String txt = cmdTextField.getText();
        optionPanel.sync(txt);
        behaviorPanel.sync(txt);
    }

    public String getCmd() {
        return cmdTextField.getText();
    }

    public void setCmd(String cmd) {
        cmdTextField.setText(cmd);
    }

    public void setModule(Module module) {
        behaviorPanel.setKBeanCombo(module);
    }

    private JPanel panel() {
        JPanel cmdPanel = UI.PanelFactory.panel(cmdTextField)
                .withLabel("Cmd Arguments:")
                .moveLabelOnTop()
                .createPanel();
        this.optionPanel = new OptionPanel();
        this.behaviorPanel = new BehaviorPanel();

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

        private JBCheckBox livCB = new JBCheckBox("log.ivy.verbose (-liv)");

        private JBCheckBox ldCB = new JBCheckBox("log.duration (-ld)");

        OptionPanel() {
            Border border = BorderFactory.createEmptyBorder(0,20,0,0);
            setBorder(border);
            GridLayout layout = new GridLayout(0,3);
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
                if (!updateCmd) {
                    return;
                }
                String selectedValue = ls.getItem();
                selectedValue = selectedValue.equals("default") ? null : selectedValue;
                adaptText("ls", selectedValue);
            });

            this.add(new JPanel());
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
                if (updateCmd) {
                    Object value = cb.isSelected() ? true : false;
                    adaptText(keyword, value);
                }
            });
        }

        private void sync(String cmd) {
            updateCmd = false;
            List<String> items = Arrays.asList(JkUtilsString.translateCommandline(cmd));
            RunFormPanel.sync(items, lvCb, "lv", "log.verbose");
            RunFormPanel.sync(items, lsuCb, "lsu", "log.setup");
            RunFormPanel.sync(items, lstCb, "lst", "log.verstacktrace");
            RunFormPanel.sync(items, lriCb, "lri", "log.runtime.information");
            RunFormPanel.sync(items, livCB, "liv", "log.ivy.verbose");
            RunFormPanel.sync(items, ldCB, "ld", "log.duration");
            RunFormPanel.sync(items, ls, "ls", "log.style");
            updateCmd = true;
        }
    }

    private class BehaviorPanel extends JBPanel {

        private ComboBox<BeanNode> kb;

        private JBCheckBox cwCb = new JBCheckBox("clean.work (-cw)");

        private JBCheckBox dciCb = new JBCheckBox("def.compile.ignore-failure (-dci)");

        BehaviorPanel() {
            Border border = BorderFactory.createEmptyBorder(0,20,0,0);
            //Border border = BorderFactory.createLineBorder(Color.BLUE);
            //setBorder(border);
            GridLayout layout = new GridLayout(2,1);
            layout.setVgap(0);
            layout.setHgap(20);
            this.setLayout(layout);

            // -kb
            kb = new ComboBox<>();
            ListCellRenderer cellRenderer = new KbCellRenderer();
            kb.setRenderer(cellRenderer);
            setKBeanCombo(module);
            kb.setName("log.style (-ls)");
            kb.setMaximumRowCount(20);
            kb.setMinimumAndPreferredWidth(300);
            JPanel lsPanel = UI.PanelFactory.panel(kb)
                    .withLabel("kbean (-kb)")
                    .withTooltip("The first KBean to be instantiated.<br/>" +
                            "By default this is the first one found in 'def' folder. <br/>Methods and properties not prefixed with bean name will apply on this bean.")
                    .createPanel();
            lsPanel.setBorder(border);
            this.add(lsPanel);
            kb.addItemListener(event -> {
                if (!updateCmd) {
                    return;
                }
                BeanNode selectedValue = kb.getItem();
                if (selectedValue != null) {
                    adaptText("kb", selectedValue.getName());
                } else {
                    adaptText("kb", null);
                }
            });

            JBPanel cbPanel = new JBPanel();
            cbPanel.setBorder(border);
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            flowLayout.setHgap(0);
            cbPanel.setLayout(flowLayout);

            // -wc
            listen(cwCb, "cw");
            cbPanel.add(itemPanel(cwCb, "Delete Jeka caches before running."));
            cbPanel.add(new JBLabel("      "));

            // -dci
            listen(dciCb, "dci");
            cbPanel.add(itemPanel(dciCb, "Ignore compilation failure on 'def' folder. It can be useful if you want to execute a KBean which is already on classpath."));

            this.add(cbPanel);
        }

        private void setKBeanCombo(Module module) {
            if (module != null) {
                updateKbeans(module);
            }
        }

        private void updateKbeans(Module module) {
            BeanNode currentValue = kb.getItem();
            kb.removeAllItems();
            List<BeanNode> valueList = new LinkedList<>();

            List<BeanNode> kbeans = module.getProject().getService(JekaToolWindowTreeService.class).getKbeans(module);
            valueList.add(null);
            valueList.addAll(kbeans);
            valueList.forEach(item -> kb.addItem(item));
            if (valueList.contains(currentValue)) {
                kb.setItem(currentValue);
            }
            kb.addNotify();
        }

        private JPanel itemPanel(JComponent cmp, String tooltipText) {
            return UI.PanelFactory.panel(cmp)
                    .withTooltip(tooltipText)
                    .moveCommentRight()
                    .createPanel();
        }

        private void listen(JBCheckBox cb, String keyword) {
            cb.addItemListener(item -> {
                if (updateCmd) {
                    Object value = cb.isSelected() ? true : false;
                    adaptText(keyword, value);
                }
            });
        }

        private void sync(String cmd) {
            updateCmd = false;
            List<String> items = Arrays.asList(JkUtilsString.translateCommandline(cmd));
            RunFormPanel.sync(items, cwCb, "cw", "clean.work");
            RunFormPanel.sync(items, dciCb, "dci", "def.compile.ignore-failure");
            RunFormPanel.sync(items, kb, "kb", "kbean");
            updateCmd = true;
        }

    }

    private static void sync(List<String> items, JBCheckBox checkBox, String ... optionNames) {
        boolean select = false;
        for (String optionName : optionNames) {
            for (String item : items) {
                if (item.equals("-" + optionName) || item.equals("-" + optionName + "=true")) {
                    select = true;
                }
                if (item.equals("-" + optionName + "=false")) {
                    select = false;
                }
            }
        }
        checkBox.setSelected(select);
    }

    private static void sync(List<String> items, ComboBox<?> comboBox, String ... optionNames) {
        if (comboBox.getItemCount() == 0) {
            return;
        }
        for (String optionName : optionNames) {
            for (String item : items) {
                String prefix = "-" + optionName + "=";
                if (item.startsWith(prefix)) {
                    String suffix = item.substring(prefix.length());
                    for (int i=0; i < comboBox.getItemCount(); i++) {
                        Object comboItem = comboBox.getItemAt(i);
                        String stringItem = comboItem == null ? null : comboItem.toString();
                        if (Objects.equals(stringItem, suffix)) {
                            comboBox.setSelectedIndex(i);
                            return;
                        }
                    }
                }
            }
        }
        comboBox.setSelectedIndex(0);
    }

    private static class KbCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel original = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            original.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            BeanNode beanNode = (BeanNode) value;
            if (beanNode == null) {
                original.setText("default");
                return original;
            }
            String name = Strings.padEnd(beanNode.getName(), 18, ' ');
            original.setText(name + "      " + beanNode.getClassName());
            return original;
        }
    }

}
