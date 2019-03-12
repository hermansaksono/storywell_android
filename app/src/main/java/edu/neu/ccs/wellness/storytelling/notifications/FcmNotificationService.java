package edu.neu.ccs.wellness.storytelling.notifications;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import edu.neu.ccs.wellness.notifications.RegularNotificationManager;
import edu.neu.ccs.wellness.storytelling.HomeActivity;
import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSetting;
import edu.neu.ccs.wellness.storytelling.settings.SynchronizedSettingRepository;

/**
 * Created by hermansaksono on 2/9/19.
 */

public class FcmNotificationService extends FirebaseMessagingService {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            Log.d("SWELL", "Message Notification: " + notification);

            RegularNotificationManager notificationManager =
                    new RegularNotificationManager(
                            getString(R.string.notification_default_channel_id));

            remoteMessage.getMessageId();

            notificationManager.showNotification(
                    Constants.FCM_NOTIFICATION_ID,
                    notification.getTitle(), notification.getBody(),
                    Constants.DEFAULT_NOTIFICATION_ICON_RESID,
                    getRetrievingActivityIntent(getApplicationContext()), getApplicationContext());
        }
    }

    private static Intent getRetrievingActivityIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d("SWELL", "Refreshed token: " + token);

        if(SynchronizedSettingRepository.isAuth()) {
            saveFCMTokenToSynchronizedSetting(token, this);
        }
    }

    private static void saveFCMTokenToSynchronizedSetting(String token, Context context) {
        DatabaseReference ref = SynchronizedSettingRepository.getDefaultFirebaseRepository(context);
        ref.child(SynchronizedSetting.KEY_FCM_TOKEN).setValue(token);
    }

    /**
     * Retrieve FCM token and save it under {@link SynchronizedSetting}
     */
    public static void initializeFCM(Context context) {
        getFCMToken(context);
    }

    private static void getFCMToken(final Context context) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Swell", "FCM getInstanceId failed", task.getException());
                            return;
                        } else {
                            String token = task.getResult().getToken();
                            saveFCMToken(token, context);
                        }
                    }
                });
    }

    private static void saveFCMToken(String token, Context context) {
        DatabaseReference ref = SynchronizedSettingRepository.getDefaultFirebaseRepository(context);
        ref.child(SynchronizedSetting.KEY_FCM_TOKEN).setValue(token);
    }
}
