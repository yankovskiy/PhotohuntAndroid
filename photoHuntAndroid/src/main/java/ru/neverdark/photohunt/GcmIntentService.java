package ru.neverdark.photohunt;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;

public class GcmIntentService extends IntentService {
    public static final String OPEN_PROFILE_ACTION = "ru.neverdark.photohunt.openprofile";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            Log.message(extras.toString());

            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String userId = extras.getString("message");
                String authedUserId = Settings.getUserId(getApplicationContext());
                String collapseKey = extras.getString("collapse_key");

                if (userId.equals(authedUserId) && collapseKey.equals("message")) {
                    sendNotification();
                } else {
                    Log.message("Bad login: " + userId);
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification() {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent openProfile = new Intent(this, MainActivity.class);
        openProfile.setAction(OPEN_PROFILE_ACTION);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                openProfile, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(getSmallIcon())
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                        .setContentTitle(getString(R.string.app_name))
                        .setAutoCancel(true)
                        .setContentText(getString(R.string.new_message));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private int getSmallIcon() {
        boolean useSilhouette = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        return useSilhouette ? R.drawable.notify_icon_alpha_25dp : R.drawable.notify_icon_noalpha_25dp;
    }

}
