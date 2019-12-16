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

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.components.ProjectComponent;

import java.io.File;
import java.util.Set;

/**
 * @author Jerome Angibaud
 */
public class JekaProjectComponent implements ProjectComponent {

  private static final String JEKA_USER_HOME = "JEKA_USER_HOME";

  @Override
  public void projectOpened() {

    // Instantiate jeka group action
    DefaultActionGroup jekaGroup = new ProjectPopupJekaActionGroup();

    // Register jeka group action
    ActionManager actionManager = ActionManager.getInstance();
    actionManager.registerAction(ProjectPopupJekaActionGroup.class.getName(), jekaGroup);

    // Add Jeka group to popup menu
    DefaultActionGroup projectPopupGroup = (DefaultActionGroup) actionManager.getAction("ProjectViewPopupMenu");
    Constraints menuLocation = new Constraints(Anchor.BEFORE, "Maven.GlobalProjectMenu");
    //Constraints menuLocation = new Constraints(Anchor.BEFORE, "MakeModule");
    projectPopupGroup.addAction(jekaGroup, menuLocation);
    projectPopupGroup.addAction(Separator.getInstance(), menuLocation);

    // Add classpath variable
    PathMacros pathMacros = PathMacros.getInstance();
    Set<String> macros = pathMacros.getAllMacroNames();
    if (!macros.contains(JEKA_USER_HOME)) {
      String value = System.getProperty("user.home") + File.separator + ".jeka";
      pathMacros.setMacro(JEKA_USER_HOME, value);
    }
  }

}
