package edu.neu.ccs.wellness.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by hermansaksono on 2/5/19.
 */

public class RegularNotificationManager {
    private String channelId;

    /* CONSTRUCTORS */
    public RegularNotificationManager(String channelId) {
        this.channelId = channelId;
    }

    /**
     * Create the NotificationChannel, but only on API 26+ because the NotificationChannel class
     * is new and not in the support library
     * @param channelId Channel's ID
     * @param name Channel's Name
     * @param description Channel's Description
     * @param context
     */
    public static void createNotificationChannel(
            String channelId, String name, String description, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context
                    .getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Show a regular notification.
     * @param notificationId
     * @param title
     * @param text
     * @param iconResourceId
     * @param intent
     * @param context
     */
    public void showNotification(
            int notificationId, String title, String text, int iconResourceId,
            Intent intent, Context context) {
        NotificationCompat.Builder builder = makeNotificationBuilder(
                title, text, iconResourceId, intent, context);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Generate notifications texts using the data in Firebase. Then show the notification.
     * @param day The n-th day of notification text. Will not show notification if there's
     *            no matching day
     * @param iconResourceId
     * @param intent
     * @param context
     */
    public void generateAndShowARegularNotification(
            int day, final int iconResourceId, final Intent intent, final Context context) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showNotificationFromDataSnapshot(dataSnapshot, iconResourceId, intent, context);
                Log.d("SWELL", "Showing this notification: " + dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(
                        "SWELL",
                        "Error when retrieving notification: " + databaseError.toString());
            }
        };
        NotificationRepository.generateARegularNotification(day, listener);
    }

    private void showNotificationFromDataSnapshot(
            DataSnapshot dataSnapshot, int iconResourceId, Intent intent, Context context) {
        for (DataSnapshot data : dataSnapshot.getChildren()) {
            showNotification(
                    data.getValue(RegularNotification.class),
                    iconResourceId,
                    intent,
                    context);
        }
    }

    private void showNotification(
            WellnessNotification notification, int iconResourceId, Intent intent, Context context) {
        NotificationCompat.Builder builder = makeNotificationBuilder(
                notification.getTitle(), notification.getText(),
                iconResourceId, intent, context);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notification.getNotificationId(), builder.build());
    }

    private NotificationCompat.Builder makeNotificationBuilder(
            String title, String text, int iconResource, final Intent intent, Context context) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(iconResource)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (title != null || !title.isEmpty()) {
            builder.setContentTitle(title);
        }

        if (text != null || !text.isEmpty()) {
            builder.setContentText(text);
        }

        return builder;
    }
}
