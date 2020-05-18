package dev.jeka.ide.intellij.common;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import dev.jeka.ide.intellij.Utils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScaffoldFormPanel extends JPanel {

    private Checkbox createWrapperFilesCb  = new Checkbox();

    private ComboBox<Module> moduleComboBox ;

    private JLabel delegateLabel = new JLabel("Delegate Jeka wrapper to");

    private Checkbox delegateCheckBox = new Checkbox();

    private Checkbox generateStructureCb = new Checkbox();

    private JLabel natureLabel = new JLabel("Build class");

    private ComboBox<ScaffoldNature> natureCombeBox;

    public ScaffoldFormPanel(Module[] allModules) {
        super(new GridLayout(5, 2));
        this.add(new JLabel("Generate structure and Build class"));
        generateStructureCb.setState(true);
        generateStructureCb.addItemListener(item -> update());
        this.add(generateStructureCb);
        this.add(natureLabel);
        natureCombeBox = new ComboBox<>();
        natureCombeBox.addItem(ScaffoldNature.SIMPLE);
        natureCombeBox.addItem(ScaffoldNature.JAVA);
        natureCombeBox.addItem(ScaffoldNature.SPRINGBOOT);
        natureCombeBox.setSelectedItem(ScaffoldNature.JAVA);
        this.add(natureCombeBox);
        createWrapperFilesCb.setState(true);
        this.add(new JLabel("Generate Jeka wrapper files"));
        createWrapperFilesCb.setState(true);
        createWrapperFilesCb.addItemListener(itemEvent -> update());
        this.add(createWrapperFilesCb);
        delegateLabel.setEnabled(true);
        this.add(delegateLabel);
        delegateCheckBox.setEnabled(true);
        delegateCheckBox.setState(true);
        delegateCheckBox.addItemListener(item -> update());
        this.add(delegateCheckBox);
        this.add(new JLabel(""));
        moduleComboBox = new ComboBox<>(allModules);
        moduleComboBox.setEnabled(true);
        this.add(moduleComboBox);
        update();
    }

    public void updateModules(Project project, Module currentModule) {
        this.moduleComboBox.removeAllItems();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            if (module.getModuleFile() == null) {  // Sometimes IntelliJ return erased Module (so without module file)
                continue;
            }
            Path path = Paths.get(Utils.getModuleDir(module).getPath());
            Path wrapperProps = path.resolve("jeka/wrapper/jeka.properties");;
            if (Files.exists(wrapperProps) && !module.equals(currentModule)) {
                moduleComboBox.addItem(module);
            }
        }
        update();
    }

    public boolean isCreatingWrapperFiles() {
        return createWrapperFilesCb.getState();
    }

    public boolean isGeneratingStructure() {
        return generateStructureCb.getState();
    }

    public boolean isDelegatingJekaWrapper() {
        return delegateCheckBox.getState();
    }

    public Module getSelectedDelegateWrapperModule() {
        return (Module) moduleComboBox.getSelectedItem();
    }

    public ScaffoldNature getScaffoldNature() {
        return (ScaffoldNature) natureCombeBox.getSelectedItem();
    }

    public void update() {
        delegateLabel.setEnabled(isCreatingWrapperFiles());
        delegateCheckBox.setEnabled(isCreatingWrapperFiles());
        moduleComboBox.setEnabled(isCreatingWrapperFiles() && delegateCheckBox.getState());
        if (!isCreatingWrapperFiles()) {
            delegateCheckBox.setState(false);
        }
        this.natureCombeBox.setEnabled(this.generateStructureCb.getState());
        this.natureLabel.setEnabled(this.generateStructureCb.getState());
        if (moduleComboBox.getItemCount() == 0) {
            delegateLabel.setEnabled(false);
            delegateCheckBox.setEnabled(false);
            delegateCheckBox.setState(false);
            moduleComboBox.setEnabled(false);
        }
    }

}
