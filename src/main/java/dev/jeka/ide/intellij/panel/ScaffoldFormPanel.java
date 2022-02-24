package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.FormBuilder;
import dev.jeka.ide.intellij.common.JekaDistributions;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.engine.ScaffoldNature;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ScaffoldFormPanel {

    // All modules available in project
    private final Module[] modules;

    private final boolean wrapperSelected;

    private final Module currentModule;

    private final boolean showCreateStructure;

    private JCheckBox generateStructureCheckBox;

    private ComboBox<ScaffoldNature> natureComboBox;

    private WrapperPanel wrapperPanel;

    @Getter
    private JPanel panel;

    public ScaffoldFormPanel(Module[] allModules, @Nullable Module currentModule, boolean wrapperSelected,
                             boolean showCreateStructure) {
        this.modules = allModules;
        this.wrapperSelected = wrapperSelected;
        this.currentModule = currentModule;
        this.showCreateStructure = showCreateStructure;
        this.panel = panel();
    }

    public static ScaffoldFormPanel of(@Nullable Project project, @Nullable Module currentModule, boolean wrapperSelected,
                                       boolean showCreateStructure) {
        Module[] modules = project != null ? ModuleManager.getInstance(project).getModules() : new Module[0];
        return new ScaffoldFormPanel(modules, currentModule, wrapperSelected, showCreateStructure);
    }

    private JPanel panel() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder();

        generateStructureCheckBox = new JCheckBox();
        generateStructureCheckBox.setSelected(true);
        if (showCreateStructure) {
            generateStructureCheckBox.addItemListener(item -> update());
            formBuilder.addLabeledComponent("Generate structure and Build class", generateStructureCheckBox);
        }

        natureComboBox = new ComboBox<>();
        natureComboBox.addItem(ScaffoldNature.SIMPLE);
        natureComboBox.addItem(ScaffoldNature.JAVA_PROJECT);
        natureComboBox.addItem(ScaffoldNature.SPRINGBOOT);
        natureComboBox.addItem(ScaffoldNature.JEKA_PLUGIN);
        natureComboBox.setSelectedItem(ScaffoldNature.JAVA_PROJECT);
        formBuilder.addLabeledComponent("Build class:", natureComboBox);

        List<Module> modules = effectiveModules();
        wrapperPanel = new WrapperPanel(modules, wrapperSelected);
        formBuilder.addLabeledComponent("Generate Jeka wrapper files:", wrapperPanel.getPanel());
        formBuilder.addComponentFillVertically(new JPanel(), 0);
        update();
        return formBuilder.getPanel();
    }

    private java.util.List<Module> effectiveModules() {
        List result = new LinkedList<>();
        for (Module module : modules) {
            Path path = Paths.get(ModuleHelper.getModuleDir(module).getPath());
            Path wrapperProps = path.resolve("jeka/wrapper/wrapper.properties");;
            if (Files.exists(wrapperProps) && !module.equals(currentModule)) {
                result.add(module);
            }
        }
        return result;
    }

    public boolean isCreatingWrapperFiles() {
        return this.wrapperPanel.createWrapperFilesCb.isSelected();
    }

    public boolean isGeneratingStructure() {
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

    public ScaffoldNature getScaffoldNature() {
        return (ScaffoldNature) natureComboBox.getSelectedItem();
    }

    private void update() {
        this.natureComboBox.setEnabled(this.generateStructureCheckBox.isSelected());
    }

    private static class WrapperPanel {

        private final List<Module> modules;

        private final boolean initialValue;

        private JLabel delegateToLabel = new JLabel("  Delegate to:  ");

        private JCheckBox createWrapperFilesCb;

        private ComboBox<Object> moduleComboBox ;

        private JLabel versionLabel = new JLabel("  Jeka version:  ");

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

            moduleComboBox = new ComboBox();
            moduleComboBox.addItem("Do not delegate");
            modules.forEach(moduleComboBox::addItem);
            if (!modules.isEmpty()) {
                moduleComboBox.setItem(modules.get(0));
            }
            moduleComboBox.setEnabled(true);
            moduleComboBox.addItemListener(e -> update());

            versionsComboBox = versionsCombo();

            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            flowLayout.setVgap(0);
            flowLayout.setHgap(0);
            flowLayout.setAlignOnBaseline(true);

            panel = new JPanel();
            panel.setLayout(flowLayout);
            panel.add(this.createWrapperFilesCb);
            panel.add(delegateToLabel);
            panel.add(moduleComboBox);
            panel.add(versionLabel);
            panel.add(versionsComboBox);

            update();
        }

        private void update() {
            boolean enabled = !modules.isEmpty() && createWrapperFilesCb.isSelected();
            delegateToLabel.setEnabled(enabled);
            moduleComboBox.setEnabled(enabled);
            boolean versionEnabled = moduleComboBox.isEnabled() && getDelegateModule() == null;
            versionLabel.setEnabled(versionEnabled);
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

        private ComboBox<String> versionsCombo() {
            ComboBox<String> comboBox = new ComboBox<>();
            List<String> versions = JekaDistributions.searchVersionsSortedByDesc();
            for (String version : versions) {
                comboBox.addItem(version);
            }
            return comboBox;
        }

    }

}
