package me.austindizzy.wvuprtstatus.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PRTMessagingService extends FirebaseMessagingService {

    final static String TAG = "PRT Status";
    final static String INTENT_TAG = "status update -" + TAG;
    private SharedPreferences prefs;

    @Override
    public void onMessageReceived(RemoteMessage msg) {
        AppDatabase db = AppDatabase.getAppDatabase(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        Map<String, String> data = msg.getData();
        Log.d(TAG, "New Message: " + data);

        Intent intent = new Intent(INTENT_TAG);
        intent.putExtra("status", Integer.parseInt(data.get("status")));
        intent.putExtra("timestamp", Long.parseLong(data.get("timestamp")));
        intent.putExtra("message", data.get("message"));
        intent.putExtra("bussesDispatched", data.get("bussesDispatched").equals("true"));
        intent.putExtra("stations", data.get("stations").split(","));

        PRTStatus status = new PRTStatus(intent.getExtras());
        broadcastManager.sendBroadcast(intent);
        if (shouldNotify(status)) {
            Bitmap mBitmap;
            if (status.getStatus() == 1) {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.color.ForestGreen);
            } else {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.color.FireBrick);
            }
            final NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().setBackground(mBitmap);
            String notificationMsg = "The PRT is " + (status.getStatus() == 1 ? "UP" : (status.IsClosed() ? "CLOSED" : "DOWN"));
            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, 0);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder = new NotificationCompat.Builder(this, TAG);
                NotificationChannel channel = new NotificationChannel(TAG, TAG, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(getString(R.string.notif_channel_desc));
                mNotificationManager.createNotificationChannel(channel);
            } else {
                 mBuilder = new NotificationCompat.Builder(this);
            }
            mBuilder = mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(notificationMsg)
                    .setContentText(status.getMessage())
                    .extend(wearableExtender)
                    .setWhen(status.getTimestamp() * 1000)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(Notification.CATEGORY_EVENT)
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                    .setContentIntent(contentIntent);
            mNotificationManager.notify(1975, mBuilder.build());
        }

        db.statusDao().insert(status);
    }

    private boolean shouldNotify(PRTStatus status) {
        if (!prefs.getBoolean("enable_notifs", true) || StatusApplication.isActivityVisible()) { return false; }

        boolean response;
        Set<String> defStations = new HashSet<>(Arrays.asList(getResources().getStringArray(R.array.pref_list_stations_titles)));
        Set<String> defNotifTypes = new HashSet<>(Arrays.asList(getResources().getStringArray(R.array.pref_list_notif_types)));

        Set<String> stations = prefs.getStringSet("stations", defStations);
        Set<String> notifTypesSet = prefs.getStringSet("notif_types", defNotifTypes);
        String[] notifTypes = notifTypesSet.toArray(new String[notifTypesSet.size()]);

        if (notifTypes.length < 3) {
            boolean notifFlag = false;
            for (int i = 0; i < notifTypes.length && !notifFlag; i++) {
                String notifType = notifTypes[i].toLowerCase();
                if (notifType.contains("Open") && status.getStatus() == 1) {
                    notifFlag = true;
                } else if (notifType.contains("Closed") && status.IsClosed()) {
                    notifFlag = true;
                } else if (notifType.contains("Disruption") && (status.IsDown() && !status.IsClosed())) {
                    notifFlag = true;
                }
            }
            response = notifFlag;
        } else {
            response = true;
        }
        if (status.getStations() != null && stations.size() != 5 && (!status.IsOpen() && !status.IsClosed())) {
            for(String s1 : status.getStations()) {
                response = stations.contains(s1) || s1.equals("All");
                if (response) { break; }
            }
        }
        return response;
    }
}
