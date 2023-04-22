package dev.jeka.ide.intellij.common.model;

import com.intellij.pom.Navigatable;
import lombok.RequiredArgsConstructor;

/**
 * Avoid com.intellij.diagnostic.PluginException: PSI element is provided on EDT by dev.jeka.ide.intellij.panel.explorer.JekaExplorerPanel.getData("Navigatable"). Please move that to a BGT data provider using PlatformCoreDataKeys.BGT_DATA_PROVIDER [Plugin: dev.jeka.ide.intellij]
 */
@RequiredArgsConstructor
public class NavigableProxy implements Navigatable {

    private final Navigatable proxied;

    @Override
    public void navigate(boolean requestFocus) {
        proxied.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return proxied.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return proxied.canNavigateToSource();
    }

}
