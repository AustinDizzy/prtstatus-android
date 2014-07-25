package me.austindizzy.wvuprtstatus.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * WVUPRTStatus by AustinDizzy <@AustinDizzy>
 *     7/23/2014.
 */

public class MainActivity extends ActionBarActivity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "195209769133";

    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";

    String regId;
    private Handler UIHandler;
    private final int scheduleInterval = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.Gray));

        if(!checkPlayServices()) {
            //checkPlayServices() displays "Get Play Services" prompt.
        } else {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = getRegistrationId(getApplicationContext());

            if(regId.isEmpty()) {
                registerInBackground();
            }
        }

        if(!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.no_network, Toast.LENGTH_SHORT);
            toast.show();
        }

        UIHandler = new Handler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        UIHandler.removeCallbacks(UIUpdateScheduler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        UIUpdateScheduler.run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //TODO: Maybe settings, maybe not.

            Toast toast = Toast.makeText(getApplicationContext(),
                    "Settings Coming Soon", Toast.LENGTH_SHORT);
            toast.show();
        } else if (id == R.id.action_about) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View layout = inflater.inflate(R.layout.about_dialog, null);
            AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);
            aboutDialog.setTitle("About");
            aboutDialog.setIcon(android.R.drawable.ic_menu_info_details);
            aboutDialog.setPositiveButton("Okay", null);

            AlertDialog displayAbout = aboutDialog.create();
            displayAbout.setView(layout, 0, 5, 0, 0);
            displayAbout.show();
            TextView aboutMessage = (TextView) layout.findViewById(R.id.about_text);
            String mess = getString(R.string.about_dialog);
            aboutMessage.setText(mess.replace("{versionID}", String.valueOf(getAppVersion(this))));
            aboutMessage.setMovementMethod(LinkMovementMethod.getInstance());
        }
        return super.onOptionsItemSelected(item);
    }

    Runnable UIUpdateScheduler = new Runnable() {
        @Override
        public void run() {
            updateStatus();
            UIHandler.postDelayed(UIUpdateScheduler, scheduleInterval);
        }
    };

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getStoredPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("Registration", "Registration not found.");
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("VERSION", "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getStoredPreferences(){
        return getSharedPreferences(MainActivity.class.getSimpleName(), MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e){
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params){
                String msg;
                try {
                    if(gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regId = gcm.register(SENDER_ID);
                    msg = "Successfully registered with GCM. ID = " + regId;

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("https://austindizzy.me/prt/_sandbox/user");
                    List <NameValuePair> postData = new ArrayList<NameValuePair>();
                    postData.add(new BasicNameValuePair("regID", regId));
                    httpPost.setEntity(new UrlEncodedFormEntity(postData, HTTP.UTF_8));
                    try {
                        httpClient.execute(httpPost);
                    } catch (ClientProtocolException e) {
                        Log.i("ClientProtocolException", e.toString());
                    } catch (IOException e) {
                        Log.i("IOException", e.toString());
                    }

                    storeRegistrationId(getApplicationContext(), regId);
                } catch (IOException ex){
                    msg = "Error: " + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i("REGISTER", msg);
            }
        }.execute(null, null, null);
    }

    public void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getStoredPreferences();
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkConn = connectivityManager.getActiveNetworkInfo();
        return activeNetworkConn != null && activeNetworkConn.isConnectedOrConnecting();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("PLAY_SERVICES", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void updateStatus(){
        prefs = getStoredPreferences();
        String prtMessage = prefs.getString("prtMessage", "Unknown");
        int prtStatus = prefs.getInt("prtStatus", 0);
        String prtDate = prefs.getString("prtDate", "never");

        TextView prtMessageView = (TextView)this.findViewById(R.id.prtMessage);
        TextView prtUpdatedView = (TextView)this.findViewById(R.id.updatedTime);
        ImageView statusIcon = (ImageView)this.findViewById(R.id.statusIcon);

        prtMessageView.setText(prtMessage);
        prtUpdatedView.setText("Updated " + prtDate);

        if (prtStatus == 1) {
            getWindow().getDecorView().setBackgroundColor(getResources()
                    .getColor(R.color.ForestGreen));
            statusIcon.setImageResource(R.drawable.running);
        } else if (prtStatus == 0) {
            getWindow().getDecorView().setBackgroundColor(getResources()
                    .getColor(R.color.Gray));
            statusIcon.setImageResource(R.drawable.unknown);
        } else {
            getWindow().getDecorView().setBackgroundColor(getResources()
                    .getColor(R.color.FireBrick));
            statusIcon.setImageResource(R.drawable.down);
        }
    }

}
