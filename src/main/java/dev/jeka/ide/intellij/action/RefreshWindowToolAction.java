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

package dev.jeka.ide.intellij.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import dev.jeka.ide.intellij.common.FileHelper;
import dev.jeka.ide.intellij.common.ModuleHelper;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;
import dev.jeka.ide.intellij.extension.JekaToolWindowFactory;
import dev.jeka.ide.intellij.panel.explorer.JekaExplorerPanel;
import dev.jeka.ide.intellij.panel.explorer.model.JekaRootManager;


/**
 * @author Jerome Angibaud
 */
public class RefreshWindowToolAction extends AnAction {

    public static final RefreshWindowToolAction INSTANCE = new RefreshWindowToolAction();

    private RefreshWindowToolAction() {
        super("Refresh view", "Synchronize all iml files in aka modules", AllIcons.Actions.SyncPanels);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        JekaRootManager jekaRootManager = project.getService(JekaRootManager.class);
        if (!jekaRootManager.isInitialised()) {
            return;
        }
        if (jekaRootManager != null) {
            DumbService.getInstance(project).smartInvokeLater(jekaRootManager::init);
        }
    }

    private JekaExplorerPanel jekaExplorerPanel(ToolWindow toolWindow) {
        return (JekaExplorerPanel) toolWindow.getContentManager().getContent(0).getComponent();
    }

}