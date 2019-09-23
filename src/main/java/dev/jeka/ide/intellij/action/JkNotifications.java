package dev.jeka.ide.intellij.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;

class JkNotifications {

    private static final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("", NotificationDisplayType.TOOL_WINDOW, true);

    static void info(String message) {
        Notifications.Bus.notify(NOTIFICATION_GROUP.createNotification().setTitle("Jeka").setContent(message));
    }

}
