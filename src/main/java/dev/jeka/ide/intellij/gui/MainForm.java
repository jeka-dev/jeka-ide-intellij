package dev.jeka.ide.intellij.gui;


import com.intellij.openapi.actionSystem.*;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import dev.jeka.ide.intellij.action.SyncImlAction;

import javax.swing.*;
import java.awt.*;

/**
 * Created by angibaudj on 24-07-17.
 */
public class MainForm {


    public JPanel panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayoutManager(2, 1,
                new Insets(0, 0, 0, 0), -1, -1));
        panel.add(new JLabel("toolbar"), toolbarGridConstraints());
        panel.add(new JLabel("center"), toolbarGridConstraints());
        return panel;
    }

    private ActionToolbar toolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        AnAction generateIml = new SyncImlAction();
        group.add(generateIml);
        group.addSeparator("hhhhhhh");
        return  ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true);
    }

    private GridConstraints toolbarGridConstraints() {
        return new GridConstraints(
                0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null);
    }



}
