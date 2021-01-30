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

package dev.jeka.ide.intellij.panel.explorer.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import dev.jeka.ide.intellij.engine.CmdJekaDoer;

/**
 * @author Jerome Angibaud
 */
public class ShowRuntimeInformationAction extends AnAction {

    public static final ShowRuntimeInformationAction INSTANCE = new ShowRuntimeInformationAction();

    public ShowRuntimeInformationAction() {
        super("Show Jeka Runtime Information", "Show Jeka Runtime Information", AllIcons.FileTypes.Properties);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        RootAndJekaFolder rootAndJekaFolder = event.getData(RootAndJekaFolder.DATA_KEY);
        Module module = rootAndJekaFolder.getJekaFolder().getJekaModuleContainer().getModule();
        CmdJekaDoer.INSTANCE.showRuntimeInformation(module);
    }

    /*
    @Override
    public void update(AnActionEvent event) {
        RootAndJekaFolder rootAndJekaFolder = event.getData(RootAndJekaFolder.DATA_KEY);
        Module module = rootAndJekaFolder.getJekaFolder().getJekaModuleContainer().getModule();
        event.getPresentation().setVisible(module != null);
    }
    */




}
