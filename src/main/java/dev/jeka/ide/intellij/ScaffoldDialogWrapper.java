package dev.jeka.ide.intellij;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.ide.intellij.common.ScaffoldFormPanel;
import dev.jeka.ide.intellij.common.ScaffoldNature;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

class ScaffoldDialogWrapper extends DialogWrapper {

    private VirtualFile moduleDir;

    private Project project;

    private ScaffoldFormPanel formPanel;

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
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        formPanel = new ScaffoldFormPanel(ModuleManager.getInstance(project).getModules());
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
            Module delegate = formPanel.isDelegatingJekaWrapper() ?
                    formPanel.getSelectedDelegateWrapperModule():
                    null;
            Path delegatePath = delegate == null ? null : getDelegateModulePath(delegate);
            FileDocumentManager.getInstance().saveAllDocuments();
            CmdJekaDoer jekaDoer = CmdJekaDoer.INSTANCE;
            ScaffoldNature nature = (ScaffoldNature) formPanel.getScaffoldNature();
            jekaDoer.scaffoldModule(project, moduleDir, formPanel.isGeneratingStructure(),
                    formPanel.isCreatingWrapperFiles(), delegatePath, exisitingModule, nature);
            ScaffoldDialogWrapper.this.close(0);
        });
    }

}
