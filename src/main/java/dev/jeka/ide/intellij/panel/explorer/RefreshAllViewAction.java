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

package dev.jeka.ide.intellij.panel.explorer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import dev.jeka.ide.intellij.panel.explorer.model.JekaFolderNode;
import dev.jeka.ide.intellij.panel.explorer.model.JekaRootManager;
import lombok.Value;

/**
 * @author Jerome Angibaud
 */
class RefreshAllViewAction extends AnAction {

    private final JekaRootManager rootManager;

    RefreshAllViewAction(JekaRootManager rootManager) {
        super("Force Refresh View", "Force Refresh View On All Modules", AllIcons.Actions.ChangeView);
        this.rootManager = rootManager;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        rootManager.refreshAllModules();
    }

    @Value
    public static class RootAndJekaFolder {
        JekaRootManager rootManager;
        JekaFolderNode jekaFolder;
    }

}
