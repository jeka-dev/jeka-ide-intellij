package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Jerome Angibaud
 */
public interface JekaIcons {

    //Icon JEKA_GROUP_ACTION = IconLoader.getIcon("/icons/knight-color-naked.svg", JekaIcons.class);
    Icon JEKA_GROUP_ACTION = IconLoader.getIcon("/icons/knight2-color.svg", JekaIcons.class);

    //Icon JEKA_GREY = IconLoader.getIcon("/icons/knight-grey-naked.svg", JekaIcons.class);
    Icon JEKA_GREY = IconLoader.getIcon("/icons/knight2-grey.svg", JekaIcons.class);

    Icon KBEAN = IconLoader.getIcon("/icons/beanInfrastructure.svg", JekaIcons.class);

    Icon COMMAND = IconLoader.getIcon("/icons/method.svg", JekaIcons.class);

    Icon CMD = IconLoader.getIcon("/icons/cmd.svg", JekaIcons.class);

    Icon BEAN_BOX = IconLoader.getIcon("/icons/managedBean.svg", JekaIcons.class);

}
