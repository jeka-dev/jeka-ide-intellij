package dev.jeka.ide.intellij.platform;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;

class JkNotifications {

    private static final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("", NotificationDisplayType.NONE, true);

    static void info(String message) {
        Notifications.Bus.notify(NOTIFICATION_GROUP.createNotification().setTitle("Jeka").setContent(message));
    }

}
