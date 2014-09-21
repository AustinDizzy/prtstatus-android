package me.austindizzy.wvuprtstatus.app;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.text.format.DateUtils;
import android.util.Log;
import android.os.Handler;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Date;

/**
 * WVUPRTStatus by AustinDizzy <@AustinDizzy>
 *     7/23/2014.
 */

public class GcmIntentService extends IntentService {

    private Handler handler;
    private static final int notificationID = 1975;

    String prtMessage, notificationTitle, prtDate;
    int prtStatus;
    long longPRTDate;

    public GcmIntentService() {
        super("GcmMessageHandler");
        Log.i("GCM INTENT", "INVOKED");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        prtMessage = extras.getString("message");
        prtStatus = Integer.parseInt(extras.getString("status"));
        longPRTDate = Long.parseLong(extras.getString("timestamp")) * 1000;
        prtDate = DateUtils.getRelativeTimeSpanString(longPRTDate,
                new Date().getTime(), 0).toString();

        notificationTitle = "PRT Alert";
        showNotification();
        cacheToPrefs();

        Log.i("GCM", "Received : (" + messageType + ")  " + prtMessage);

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void cacheToPrefs(){
        SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(), MODE_PRIVATE);
        prefs.edit().putString("prtMessage", prtMessage)
                .putInt("prtStatus", prtStatus)
                .putString("prtDate",  prtDate)
                .apply();
    }

    private void showNotification(){

        Bitmap mBitmap;

        if (prtStatus != 1) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.color.FireBrick);
        } else {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.color.ForestGreen);
        }

        final NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender().setBackground(mBitmap);

        handler.post(new Runnable() {
            public void run() {
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                        0, resultIntent, 0);
                NotificationCompat.Builder notifBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(notificationTitle)
                        .setContentText(prtMessage)
                        .extend(wearableExtender)
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND |
                                Notification.DEFAULT_LIGHTS)
                        .setContentIntent(contentIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(notificationID, notifBuilder.build());
            }
        });
    }
}