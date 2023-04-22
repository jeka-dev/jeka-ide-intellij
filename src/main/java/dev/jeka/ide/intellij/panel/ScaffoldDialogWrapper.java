package dev.jeka.ide.intellij.panel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.ide.intellij.common.JekaWrapperInfo;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScaffoldDialogWrapper extends DialogWrapper {

    private VirtualFile moduleDir;

    private Project project;

    private ScaffoldFormPanel scaffoldFormPanel;

    private Module exisitingModule;

    public ScaffoldDialogWrapper(Project project, VirtualFile moduleDir, Module existingModule) {
        super(project, true);
        this.project = project;
        this.moduleDir = moduleDir;
        this.exisitingModule = existingModule;
        this.init();
        setTitle("Create Jeka files");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        boolean hasWrapperFiles = JekaWrapperInfo.hasWrapperShellFiles(moduleDir.toNioPath());
        scaffoldFormPanel = ScaffoldFormPanel.of(project, exisitingModule, !hasWrapperFiles, true);
        scaffoldFormPanel.getPanel().setPreferredSize(new Dimension(0, 400));
        return scaffoldFormPanel.getPanel();
    }

    private Path getDelegateModulePath(Module delegate) {
        Path thisModulePath = Paths.get(this.moduleDir.getPath()).toAbsolutePath();
        Path delegatePath = Paths.get(ModuleHelper.getModuleDir(delegate).getPath());
        return thisModulePath.relativize(delegatePath);
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            Module delegate = scaffoldFormPanel.isDelegatingJekaWrapper() ?
                    scaffoldFormPanel.getSelectedDelegateWrapperModule():
                    null;
            Path delegatePath = delegate == null ? null : getDelegateModulePath(delegate);
            FileDocumentManager.getInstance().saveAllDocuments();
            CmdJekaDoer jekaDoer = CmdJekaDoer.getInstance(project);
            String templateCmd = scaffoldFormPanel.getTemplateCmd();
            jekaDoer.scaffoldModule(
                    moduleDir.toNioPath(),
                    scaffoldFormPanel.isGeneratingStructure(),
                    scaffoldFormPanel.isCreatingWrapperFiles(),
                    delegatePath,
                    scaffoldFormPanel.getSelectedJekaVersion(),
                    exisitingModule,
                    templateCmd);
            ScaffoldDialogWrapper.this.close(0);
        });
    }

}
