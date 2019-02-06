package edu.neu.ccs.wellness.notifications;

import static edu.neu.ccs.wellness.notifications.NotificationType.NOTIFICATION_REGULAR;

/**
 * Created by hermansaksono on 2/5/19.
 */

public class RegularNotification implements WellnessNotification {
    private int day;
    private String title;
    private String text;
    private String type;
    private int notificationId;

    /* CONSTRUCTOR */
    public RegularNotification() {
        this.day = 0;
        this.title = "Read books and be a healthy family";
        this.text = "";
        this.type = NOTIFICATION_REGULAR;
        this.notificationId = 88;
    }

    public int getDay() {
        return this.day;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public int getNotificationId() {
        return this.notificationId;
    }
}
