package edu.neu.ccs.wellness.notifications;

/**
 * Created by hermansaksono on 2/5/19.
 */

public interface WellnessNotification {

    String getType();

    String getTitle();

    String getText();

    int getNotificationId();
}
