package me.austindizzy.wvuprtstatus.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PRTMessagingService extends FirebaseMessagingService {

    final static String TAG = "PRTStatus";
    private SharedPreferences prefs;

    @Override
    public void onMessageReceived(RemoteMessage msg) {
        AppDatabase db = AppDatabase.getAppDatabase(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        Map<String, String> data = msg.getData();
        Log.d(TAG, "New Message: " + data);

        Intent update = new Intent(MainActivity.STATUS_UPDATE);

        for (String key : data.keySet()) {
            String val = data.get(key);
            switch (key) {
                case "status":
                    update.putExtra(key, Integer.parseInt(val));
                    break;
                case "timestamp":
                    update.putExtra(key, Long.parseLong(val));
                    break;
                case "bussesDispatched":
                    update.putExtra(key, val.equals("true"));
                    break;
                case "stations":
                    update.putExtra(key, val.split(","));
                    break;
                case "temperature":
                case "humidity":
                case "precip1hr":
                case "precipToday":
                case "windSpeed":
                case "visibility":
                case "feelsLike":
                    update.putExtra(key, Double.parseDouble(val));
                    break;
                case "message":
                case "weather":
                case "conditions":
                case "windDir":
                    update.putExtra(key, val);
                    break;
                default:
                    Log.i("PRTMsg", "untracked key \"" + key + "\"");
                    break;
            }
        }

        PRTStatus status = null;
        //Weather weather = null;
        if (update.getExtras() != null) {
            status = new PRTStatus(update.getExtras());
            // weather = new Weather(intent.getExtras());
        }

        broadcastManager.sendBroadcast(update);
        if (status != null && shouldNotify(status)) {
            sendNotification(status);
        }

        db.statusDao().insert(status);

        // don't store weather for now, until we have a real need for it
        //db.weatherDao().insert(weather);

        Intent widget = new Intent(this, StatusWidget.class);
        ComponentName component = new ComponentName(getApplication(), StatusWidget.class);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(component);

        widget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        widget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(widget);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = getString(R.string.notif_channel_name);
            String description = getString(R.string.notif_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(TAG, name, importance);
            channel.setDescription(description);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(PRTStatus status) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel();
        int color = status.IsOpen() ? R.color.ForestGreen : R.color.FireBrick;
        int colorRes = getResources().getColor(color);
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), color);
        String notificationMsg = "The PRT is " + (status.IsOpen() ? "UP" : (status.IsClosed() ? "CLOSED" : "DOWN"));

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().setBackground(mBitmap);
        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, 0);
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getApplicationContext());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, TAG)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(colorRes)
                .setContentTitle(notificationMsg)
                .extend(wearableExtender)
                .setWhen(status.getTimestamp() * 1000)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                .setLights(colorRes, 500, 500)
                .setContentIntent(contentIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            NotificationCompat.Style style = new NotificationCompat.BigTextStyle().bigText(status.getMessage());
            mBuilder.setStyle(style);
        } else {
            mBuilder = mBuilder.setContentText(status.getMessage());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) mBuilder.setChannelId(getString(R.string.notif_channel_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) mBuilder = mBuilder.setCategory(Notification.CATEGORY_EVENT);

        mNotificationManager.notify(1975, mBuilder.build());
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
