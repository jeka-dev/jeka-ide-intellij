package dev.jeka.ide.intellij;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class ScaffoldDialogWrapper extends DialogWrapper {

    private VirtualFile moduleDir;

    private Project project;

    private FormPanel formPanel;

    private Module exisitingModule;

    protected ScaffoldDialogWrapper(Project project) {
        super(project, true);
        this.project = project;
        init();
        setTitle("Create Jeka files");
    }

    void setModuleDir(VirtualFile moduleDir, Module existingModule) {
        this.moduleDir = moduleDir;
        this.exisitingModule = existingModule;
        this.formPanel.updateModules(project, existingModule);
        this.formPanel.updateState();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        formPanel = new FormPanel(project);
        return formPanel;
    }

    private Path getDelegateModulePath(Module delegate) {
        Path thisModulePath = Paths.get(this.moduleDir.getPath()).toAbsolutePath();
        Path delegatePath = Paths.get(Utils.getModuleDir(delegate).getPath());
        return thisModulePath.relativize(delegatePath);
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            Module delegate = formPanel.delegatCheckBox.getState() ?
                    (Module) formPanel.moduleComboBox.getSelectedItem() :
                    null;
            Path delegatePath = delegate == null ? null : getDelegateModulePath(delegate);
            FileDocumentManager.getInstance().saveAllDocuments();
            CmdJekaDoer jekaDoer = CmdJekaDoer.INSTANCE;
            ScaffoldNature nature = (ScaffoldNature) formPanel.natureCombeBox.getSelectedItem();
            jekaDoer.scaffoldModule(project, moduleDir, formPanel.generateStructureCb.getState(),
                    formPanel.createWrapperFilesCb.getState(), delegatePath, exisitingModule, nature);
            ScaffoldDialogWrapper.this.close(0);
        });
    }

    private static class FormPanel extends JPanel {

        private Checkbox createWrapperFilesCb  = new Checkbox();

        private ComboBox<Module> moduleComboBox ;

        private JLabel delegateLabel = new JLabel("Delegate wrapper to");

        private Checkbox delegatCheckBox = new Checkbox();

        private Checkbox generateStructureCb = new Checkbox();

        private JLabel natureLabel = new JLabel("Build class");

        private ComboBox<ScaffoldNature> natureCombeBox;

        public FormPanel(Project project) {
            super(new GridLayout(5, 2));
            this.add(new JLabel("Generate structure and Build class"));
            generateStructureCb.setState(true);
            generateStructureCb.addItemListener(item -> updateState());
            this.add(generateStructureCb);
            this.add(natureLabel);
            natureCombeBox = new ComboBox<>();
            natureCombeBox.addItem(ScaffoldNature.SIMPLE);
            natureCombeBox.addItem(ScaffoldNature.JAVA);
            natureCombeBox.addItem(ScaffoldNature.SPRINGBOOT);
            natureCombeBox.setSelectedItem(ScaffoldNature.JAVA);
            this.add(natureCombeBox);
            createWrapperFilesCb.setState(true);
            createWrapperFilesCb.addItemListener(itemEvent -> {updateState();});
            this.add(new JLabel("Generate wrapper files"));
            createWrapperFilesCb.setState(true);
            this.add(createWrapperFilesCb);
            Module[] modules = ModuleManager.getInstance(project).getModules();
            this.add(delegateLabel);
            delegatCheckBox.addItemListener(item -> updateState());
            this.add(delegatCheckBox);
            this.add(new JLabel(""));
            moduleComboBox = new ComboBox<>(modules);
            this.add(moduleComboBox);
            updateState();
        }

        void updateModules(Project project, Module currentModule) {
            this.moduleComboBox.removeAllItems();
            Module[] modules = ModuleManager.getInstance(project).getModules();
            for (Module module : modules) {
                Path path = Paths.get(Utils.getModuleDir(module).getPath());
                Path wrapperProps = path.resolve("jeka/wrapper/jeka.properties");;
                if (Files.exists(wrapperProps) && !module.equals(currentModule)) {
                    moduleComboBox.addItem(module);
                }
            }

        }

        public boolean doesCreateWrapperFiles() {
            return createWrapperFilesCb.getState();
        }

        private void updateState() {
            delegateLabel.setEnabled(doesCreateWrapperFiles());
            delegatCheckBox.setEnabled(doesCreateWrapperFiles());
            moduleComboBox.setEnabled(doesCreateWrapperFiles() && delegatCheckBox.getState());
            if (!doesCreateWrapperFiles()) {
                delegatCheckBox.setState(false);
            }
            this.natureCombeBox.setEnabled(this.generateStructureCb.getState());
            this.natureLabel.setEnabled(this.generateStructureCb.getState());
            if (moduleComboBox.getItemCount() == 0) {
                delegateLabel.setEnabled(false);
                delegatCheckBox.setEnabled(false);
                delegatCheckBox.setState(false);
                moduleComboBox.setEnabled(false);
            } else {
                delegateLabel.setEnabled(true);
                delegatCheckBox.setEnabled(true);
                delegatCheckBox.setState(true);
                moduleComboBox.setEnabled(true);
            }
        }

    }

}
