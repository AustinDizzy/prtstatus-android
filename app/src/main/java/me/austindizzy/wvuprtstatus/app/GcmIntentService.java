package me.austindizzy.wvuprtstatus.app;

import android.app.Application;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by Austin on 7/23/2014.
 */

public class GcmIntentService extends IntentService {

    private Handler handler;

    String mes;
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
        Log.i("GCM Extras", extras.toString());
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        mes = extras.getString("message");
        showToast();

        //TODO: Log data to SQLite database or something for cached responses and to be still able to update views on MainActivity create

        Log.i("GCM", "Received : (" + messageType + ")  " + extras.getString("title"));

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void showToast(){
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_LONG).show();

            }
        });
    }
}