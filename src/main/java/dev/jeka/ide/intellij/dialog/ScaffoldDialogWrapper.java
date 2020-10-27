package dev.jeka.ide.intellij.dialog;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.ide.intellij.common.ModuleUtils;
import dev.jeka.ide.intellij.engine.ScaffoldNature;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;
import dev.jeka.ide.intellij.panel.ScaffoldFormPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScaffoldDialogWrapper extends DialogWrapper {

    private VirtualFile moduleDir;

    private Project project;

    private ScaffoldFormPanel formPanel;

    private Module exisitingModule;

    public ScaffoldDialogWrapper(Project project) {
        super(project, true);
        this.project = project;
        init();
        setTitle("Create Jeka files");
    }

    public void setModuleDir(VirtualFile moduleDir, Module existingModule) {
        this.moduleDir = moduleDir;
        this.exisitingModule = existingModule;
        this.formPanel.updateModules(project, existingModule);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        formPanel = new ScaffoldFormPanel(ModuleManager.getInstance(project).getModules());
        return formPanel;
    }

    private Path getDelegateModulePath(Module delegate) {
        Path thisModulePath = Paths.get(this.moduleDir.getPath()).toAbsolutePath();
        Path delegatePath = Paths.get(ModuleUtils.getModuleDir(delegate).getPath());
        return thisModulePath.relativize(delegatePath);
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            Module delegate = formPanel.isDelegatingJekaWrapper() ?
                    formPanel.getSelectedDelegateWrapperModule():
                    null;
            Path delegatePath = delegate == null ? null : getDelegateModulePath(delegate);
            FileDocumentManager.getInstance().saveAllDocuments();
            CmdJekaDoer jekaDoer = CmdJekaDoer.INSTANCE;
            ScaffoldNature nature = formPanel.getScaffoldNature();
            jekaDoer.scaffoldModule(project, moduleDir, formPanel.isGeneratingStructure(),
                    formPanel.isCreatingWrapperFiles(), delegatePath, exisitingModule, nature);
            ScaffoldDialogWrapper.this.close(0);
        });
    }

}
