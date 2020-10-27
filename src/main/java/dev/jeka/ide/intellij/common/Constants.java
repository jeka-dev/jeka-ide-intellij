package dev.jeka.ide.intellij.common;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class Constants {

    public static final String JEKA_USER_HOME = "JEKA_USER_HOME";

    public static final String JEKA_HOME = "JEKA_HOME";

    /**
     * @author Jerome Angibaud
     */
    public interface JkIcons {

        Icon JEKA_GROUP_ACTION = IconLoader.getIcon("/icons/knight-color-naked.svg");

        Icon JEKA_GREY_NAKED = IconLoader.getIcon("/icons/knight-grey-naked.svg");

        Icon JEKA_GREYLIGHT_NAKED = IconLoader.getIcon("/icons/knight-greylight-naked.svg");

        Icon JEKA_GREY_NAKED_13 = IconLoader.getIcon("/icons/knight-grey-naked-13.png");

        Icon JEKA_RUN = IconLoader.getIcon("/icons/knight-run.svg");

    }
}
