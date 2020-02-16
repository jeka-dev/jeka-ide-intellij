package dev.jeka.ide.intellij;

import com.intellij.openapi.application.ApplicationManager;
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

    protected ScaffoldDialogWrapper(Project project) {
        super(project, true);
        this.project = project;
        init();
        setTitle("Create Jeka files");
    }

    void setModuleDir(VirtualFile moduleDir) {
        this.moduleDir = moduleDir;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        formPanel = new FormPanel(project);
        formPanel.updateModules(project);
        return formPanel;
    }

    private Path getDelegateModulePath(Module delegate) {
        Path thisModulePath = Paths.get(this.moduleDir.getPath()).toAbsolutePath();
        Path delegatePath = Utils.getModuleDir(delegate);
        return thisModulePath.relativize(delegatePath);
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            CmdJekaDoer jekaDoer = CmdJekaDoer.INSTANCE;
            Module delegate = formPanel.delegatCheckBox.getState() ?
                    (Module) formPanel.moduleComboBox.getSelectedItem() :
                    null;
            Path delegatePath = delegate == null ? null : getDelegateModulePath(delegate);
            jekaDoer.scaffoldModule(project, moduleDir, formPanel.generateStructureCb.getState(),
                    formPanel.createWrapperFilesCb.getState(), delegatePath);
            ScaffoldDialogWrapper.this.close(0);
        });
    }

    private static class FormPanel extends JPanel {

        private Checkbox createWrapperFilesCb  = new Checkbox();

        private ComboBox<Module> moduleComboBox ;

        private JLabel delegateLabel = new JLabel("Delegate wrapper to");

        private Checkbox delegatCheckBox = new Checkbox();

        private Checkbox generateStructureCb = new Checkbox();

        public FormPanel(Project project) {
            super(new GridLayout(4, 2));
            this.add(new JLabel("Generate structure and Build class"));
            this.add(generateStructureCb);
            createWrapperFilesCb.setState(true);
            createWrapperFilesCb.addItemListener(itemEvent -> {updateState();});
            this.add(new JLabel("Generate wrapper files"));
            createWrapperFilesCb.setState(true);
            this.add(createWrapperFilesCb);
            this.add(delegateLabel);
            delegatCheckBox.addItemListener(item -> updateState());
            this.add(delegatCheckBox);
            this.add(new JLabel(""));
            Module[] modules = ModuleManager.getInstance(project).getModules();
            moduleComboBox = new ComboBox<>(modules);
            this.add(moduleComboBox);
            updateState();
        }

        void updateModules(Project project) {
            this.moduleComboBox.removeAllItems();
            for (Module module : ModuleManager.getInstance(project).getModules()) {
                Path path = Utils.getModuleDir(module);
                Path wrapperProps = path.resolve("jeka/wrapper/jeka.properties");
                if (Files.exists(wrapperProps)) {
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
        }

    }

}
