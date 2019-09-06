package dev.jeka.ide.intellij;

import com.intellij.openapi.components.ProjectComponent;

public class JekaProjectComponent implements ProjectComponent {

    @Override
    public void projectOpened() {
        System.out.println("------------------------------------- project opened");
    }
}
