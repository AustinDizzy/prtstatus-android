package me.austindizzy.wvuprtstatus.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * WVUPRTStatus by AustinDizzy <@AustinDizzy>
 *     7/23/2014.
 */

public class MainActivity extends ActionBarActivity {

    Activity MainContext = this;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "195209769133";

    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "0.1";

    String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainContext.getWindow().getDecorView().setBackgroundColor(MainContext.getResources().getColor(R.color.Gray));

        if(!checkPlayServices()) {
            Toast toast = Toast.makeText(MainContext.getApplicationContext(), "Go get Google Play Services", Toast.LENGTH_LONG);
            toast.show();
            System.exit(1);
        } else {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = getRegistrationId(MainContext.getApplicationContext());

            if(regId.isEmpty()) {
                registerInBackground();
            }
        }

        /**
        if(!isNetworkAvailable()) {
            TextView prtMessage = (TextView)MainContext.findViewById(R.id.prtMessage);
            prtMessage.setText(R.string.no_network);
        } else {
            new FetchPRTTask().execute("https://austindizzy.me/prt.json");
        }
        **/
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
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

            Toast toast = Toast.makeText(MainContext.getApplicationContext(), "Settings Clicked", Toast.LENGTH_SHORT);
            toast.show();
        } else if (id == R.id.action_refresh) {
            if(!isNetworkAvailable()) {
                Toast toast = Toast.makeText(MainContext.getApplicationContext(), R.string.no_network, Toast.LENGTH_LONG);
                toast.show();
            } else {
                new FetchPRTTask().execute("https://austindizzy.me/prt.json");
            }
        } else if (id == R.id.action_about) {
            //TODO: An about page or something.
        }
        return super.onOptionsItemSelected(item);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
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

    private SharedPreferences getGCMPreferences(Context context){
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
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
                        gcm = GoogleCloudMessaging.getInstance(MainContext.getApplicationContext());
                    }
                    regId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID =" + regId;

                    //sendRegistrationIdToBackend();

                    storeRegistrationId(MainContext.getApplicationContext(), regId);
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

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("storeRegID", "Saving regId on app version " + appVersion);
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

    //TODO: Clean out AsyncTask and update new GCM handler to parse/update view properly
    private class FetchPRTTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... url){

            InputStream is;
            String result;

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url[0]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            } catch(Exception e) {
                Log.i("HTTP", "Caught " + e);
                return null;
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                return result;
            } catch(Exception e) {
                Log.i("JSON", "Caught " + e);
                return null;
            }
        }

        protected void onPostExecute(String result) {
            JSONObject prtObj = null;

            try {
                prtObj = new JSONObject(result);
            } catch (JSONException e) {
                Log.i("JSON EXCEPTION", e.toString());
            }

            parseStatus(prtObj);
        }

        public void parseStatus(JSONObject prtObj) {
            try {

                String prtMessage = prtObj.get("message").toString();
                int prtStatus = Integer.parseInt(prtObj.get("status").toString());
                long longPRTDate = Long.parseLong(prtObj.get("timestamp").toString()) * 1000;
                long currentTime = new Date().getTime();
                String prtDate = DateUtils.getRelativeTimeSpanString(longPRTDate, currentTime, 0).toString();

                updateStatus(prtMessage, prtDate, prtStatus);
            } catch (JSONException e) {
                Log.i("PARSE EXCEPTION", e.toString() + prtObj.toString());
            }

        }

        public void updateStatus(String prtMessage, String prtDate, int prtStatus){

            TextView prtMessageView = (TextView)MainContext.findViewById(R.id.prtMessage);
            TextView prtUpdatedView = (TextView)MainContext.findViewById(R.id.updatedTime);

            prtMessageView.setText(prtMessage);
            prtUpdatedView.setText("Updated " + prtDate);

            if (prtStatus != 1)
                MainContext.getWindow().getDecorView().setBackgroundColor(MainContext.getResources().getColor(R.color.FireBrick));
            else
                MainContext.getWindow().getDecorView().setBackgroundColor(MainContext.getResources().getColor(R.color.ForestGreen));
        }
    }

}
