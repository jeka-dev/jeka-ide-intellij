package dev.jeka.ide.intellij.dialog;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.ide.intellij.common.FileHelper;
import dev.jeka.ide.intellij.common.JekaWrapperInfo;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;
import dev.jeka.ide.intellij.engine.ScaffoldNature;
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

    public ScaffoldDialogWrapper(Project project, VirtualFile moduleDir, Module existingModule) {
        super(project, true);
        this.project = project;
        this.moduleDir = moduleDir;
        this.exisitingModule = existingModule;
        init();
        this.formPanel.updateModules(project, existingModule);
        setTitle("Create Jeka files");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        boolean hasWrapperFiles = JekaWrapperInfo.hasWrapperShellFiles(moduleDir.toNioPath());
        formPanel = new ScaffoldFormPanel(ModuleManager.getInstance(project).getModules(), !hasWrapperFiles);
        return formPanel;
    }

    private Path getDelegateModulePath(Module delegate) {
        Path thisModulePath = Paths.get(this.moduleDir.getPath()).toAbsolutePath();
        Path delegatePath = Paths.get(ModuleHelper.getModuleDir(delegate).getPath());
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
