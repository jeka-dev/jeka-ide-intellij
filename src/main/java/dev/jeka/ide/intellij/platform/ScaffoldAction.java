/*
 * Copyright 2018-2019 original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.jeka.ide.intellij.platform;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.ide.intellij.JekaDoer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jerome Angibaud
 */
public class ScaffoldAction extends AnAction {

    public static final ScaffoldAction INSTANCE = new ScaffoldAction();

    private ScaffoldAction() {
        super("Generate Jeka files and folders", "Generate Jeka files and folders", AllIcons.Actions.Expandall);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return;
        }
        Module module = ModuleUtil.findModuleForFile(virtualFile, event.getProject());
        Path path = dirPath(virtualFile);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            JekaDoer jekaDoer = JekaDoer.getInstance();
            jekaDoer.scaffoldModule(module.getProject(), path);
            virtualFile.getFileSystem().refresh(true);
        });
    }

    @Override
    public void update(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        event.getPresentation().setVisible(virtualFile.isDirectory());

    }

    private static Path dirPath(VirtualFile virtualFile) {
        if (virtualFile.isDirectory()) {
            return Paths.get(virtualFile.getPath());
        }
        return  Paths.get(virtualFile.getParent().getPath());
    }

}

