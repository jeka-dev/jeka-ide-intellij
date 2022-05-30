package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import dev.jeka.core.api.utils.JkUtilsIO;
import dev.jeka.core.api.utils.JkUtilsSystem;
import dev.jeka.ide.intellij.common.JekaDistributions;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.common.model.JekaTemplate;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class ScaffoldFormPanel {

    private static final String WRAPPER_DOC_URL = "https://jeka-dev.github.io/jeka/reference-guide/execution-engine-files/#jeka-wrapper";

    private final Project project;

    // All modules available in project
    private final Module[] modules;

    private final boolean wrapperSelected;

    private final Module currentModule;

    private final boolean showCreateStructure;

    private JCheckBox generateStructureCheckBox;

    private WrapperPanel wrapperPanel;

    private TemplatesPanel templatesPanel;

    @Getter
    private JPanel panel;

    public ScaffoldFormPanel(Project project, Module[] allModules, @Nullable Module currentModule, boolean wrapperSelected,
                             boolean showCreateStructure) {
        this.modules = allModules;
        this.wrapperSelected = wrapperSelected;
        this.currentModule = currentModule;
        this.showCreateStructure = showCreateStructure;
        this.project = project;
        this.panel = panel();
    }

    public static ScaffoldFormPanel of(@Nullable Project project, @Nullable Module currentModule, boolean wrapperSelected,
                                       boolean showCreateStructure) {
        Module[] modules = project != null ? ModuleManager.getInstance(project).getModules() : new Module[0];
        return new ScaffoldFormPanel(project, modules, currentModule, wrapperSelected, showCreateStructure);
    }

    private JPanel panel() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder();
        List<Module> modules = effectiveModules();
        wrapperPanel = new WrapperPanel(modules, wrapperSelected);
        formBuilder.addComponent(wrapperPanel.getPanel());
        formBuilder.addVerticalGap(15);
        templatesPanel = new TemplatesPanel(project);
        JComponent templatesComponent = templatesPanel.getComponent();
        templatesComponent.setPreferredSize(new Dimension(700, 300));
        if (showCreateStructure) {
            generateStructureCheckBox = new JCheckBox();
            generateStructureCheckBox.addItemListener(item ->
                    templatesPanel.setEnabled(generateStructureCheckBox.isSelected()));
            generateStructureCheckBox.setSelected(true);
            formBuilder.addLabeledComponent("Generate structure and Build class", generateStructureCheckBox);
            formBuilder.addComponentFillVertically( templatesComponent, 5);
        } else {
            JPanel templateLabel = UI.PanelFactory.panel(new JLabel("Template"))
                    .withTooltip("<b>Template</b><br/>Pre-configured command-line arguments to generate a customised Jeka project.<br/><br/>" +
                            "The special things generated here can be added later by using <i>scaffold...</i> from project contextual menu or manually.<br/>"
                            )
                    .createPanel();
            formBuilder.addComponent(templateLabel);
            formBuilder.addComponentFillVertically(templatesComponent, 5);
        }

        //formBuilder.addComponentFillVertically(new JPanel(), 0);
        JPanel panel = formBuilder.getPanel();
        panel.setMinimumSize(new Dimension(600, 0));
        return panel;
    }

    private java.util.List<Module> effectiveModules() {
        List result = new LinkedList<>();
        for (Module module : modules) {
            Path path = ModuleHelper.getModuleDirPath(module);
            Path wrapperProps = path.resolve("jeka/wrapper/wrapper.properties");

            Path jekaw = JkUtilsSystem.IS_WINDOWS ? path.resolve("jekaw.bat") : path.resolve("jekaw");
            if (Files.exists(wrapperProps) && !module.equals(currentModule) && Files.exists(jekaw)) {
                result.add(module);
            }
        }
        return result;
    }

    public boolean isCreatingWrapperFiles() {
        return this.wrapperPanel.createWrapperFilesCb.isSelected();
    }

    public boolean isGeneratingStructure() {
        if (generateStructureCheckBox == null) {
            return true;
        }
        return generateStructureCheckBox.isSelected();
    }

    // TODO remove
    public boolean isDelegatingJekaWrapper() {
        return this.wrapperPanel.getDelegateModule() != null;
    }

    public Module getSelectedDelegateWrapperModule() {
        return this.wrapperPanel.getDelegateModule();
    }

    public String getSelectedJekaVersion() {
        return this.wrapperPanel.getJekaVersion();
    }

    public JekaTemplate getTemplate() {
        return templatesPanel.getSelectedTemplate();
    }

    public String getTemplateCmd() {
        return templatesPanel.getTemplateCmd();
    }

    static class WrapperPanel {

        private final List<Module> modules;

        private final boolean initialValue;

        private JPanel modulePanel;

        private JCheckBox createWrapperFilesCb;

        @Getter
        private ComboBox<Object> moduleComboBox;

        private ComboBox<String> versionsComboBox;

        @Getter
        private JPanel panel;

        public WrapperPanel(List<Module> modules, boolean initialValue) {
            this.modules = modules;
            this.initialValue = initialValue;
            init();
        }

        private void init() {
            createWrapperFilesCb  = new JCheckBox();
            createWrapperFilesCb.setSelected(initialValue);
            createWrapperFilesCb.addItemListener(e -> update());
            JPanel createWrapperPanel =  UI.PanelFactory.panel(createWrapperFilesCb)
                    .withTooltip("<b>Jeka Wrapper</b><br/>Generate jekaw.bat and jekaw shell scripts in the project dir.<br/>" +
                            "These scripts download and install a specified version of Jeka, instead of using the default one.<br/>" +
                            "Using wrapper ensures that Jeka runs the same version on any machine, making it portable.<br/>" +
                            "This is the recommended way of using Jeka.")
                    .withTooltipLink("See official documentation", () -> browseTo(WRAPPER_DOC_URL))
                    .createPanel();

            moduleComboBox = new ComboBox();
            moduleComboBox.addItem("Do not delegate");
            modules.forEach(moduleComboBox::addItem);
            if (!modules.isEmpty()) {
                moduleComboBox.setItem(modules.get(0));
            }
            moduleComboBox.setEnabled(true);
            moduleComboBox.addItemListener(e -> update());
            modulePanel = UI.PanelFactory.panel(moduleComboBox)
                    .withLabel("Delegate to:")
                    .withTooltip("<b>Wrapper Delegation</b><br/>Use the wrapper installed on another module instead of installing one, specifically for this module.<br/>" +
                            "When using multi-module projects, this is the recommended way of using wrapper as the Jeka version " +
                            "is defined in a single place for all modules.")
                    .createPanel();

            versionsComboBox = new ComboBox<>();

            JPanel subPanel = FormBuilder.createFormBuilder()
                    .addComponent(modulePanel)
                    .addLabeledComponent("Jeka version:", versionsComboBox)
                    .getPanel();

            panel = FormBuilder.createFormBuilder()
                        .addLabeledComponent("Create wrapper files:", createWrapperPanel)
                        .addLabeledComponent("", subPanel)
                        .getPanel();

            // Only use it when > 3 elements : https://jetbrains.design/intellij/controls/group_header/
            //panel.setBorder(IdeBorderFactory.createTitledBorder("Jeka Wrapper"));
            panel.setAlignmentX(0);
            update();
        }

        private void update() {
            boolean modulesBoxEnabled = !modules.isEmpty() && createWrapperFilesCb.isSelected();
            moduleComboBox.setEnabled(modulesBoxEnabled);
            boolean versionEnabled = getDelegateModule() == null && createWrapperFilesCb.isSelected();
            if (versionEnabled) {
                versionsComboBox.removeAllItems();
                fillVersions();
            } else if (!versionEnabled && versionsComboBox.isEnabled()) {
                versionsComboBox.removeAllItems();
            }
            versionsComboBox.setEnabled(versionEnabled);
        }

        public Module getDelegateModule() {
            if (!moduleComboBox.isEnabled() || !moduleComboBox.isVisible()) {
                return null;
            }
            Object result = moduleComboBox.getItem();
            return (result instanceof Module) ? (Module) result : null;
        }

        public String getJekaVersion() {
            if (!this.versionsComboBox.isEnabled()) {
                return null;
            }
            return this.versionsComboBox.getItem();
        }

        private void fillVersions() {
            List<String> versions = JekaDistributions.searchVersionsSortedByDesc();
            for (String version : versions) {
                versionsComboBox.addItem(version);
            }
        }

    }

    private static void browseTo(String url) {
        try {
            Desktop.getDesktop().browse(JkUtilsIO.toUri(url));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }



}
