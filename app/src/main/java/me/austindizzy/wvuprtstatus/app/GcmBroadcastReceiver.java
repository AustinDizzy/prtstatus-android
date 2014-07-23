package me.austindizzy.wvuprtstatus.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by Austin on 7/22/2014.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("GCM", "GOT SOMETHING");

        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());

        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
