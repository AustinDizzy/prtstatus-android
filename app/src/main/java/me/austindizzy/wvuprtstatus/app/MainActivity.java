package me.austindizzy.wvuprtstatus.app;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public static final String PROPERTY_APP_VERSION = "appVersion";

    String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainContext.getWindow().getDecorView().setBackgroundColor(MainContext.getResources().getColor(R.color.Gray));

        if(!checkPlayServices()) {
            //checkPlayServices() displays "Get Play Services" prompt.
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
        } else if (id == R.id.action_about) {
            LayoutInflater inflater = LayoutInflater.from(MainContext);
            View layout = inflater.inflate(R.layout.about_dialog, null);
            AlertDialog.Builder aboutDialog = new AlertDialog.Builder(MainContext);
            aboutDialog.setTitle("About");
            aboutDialog.setIcon(android.R.drawable.ic_menu_info_details);
            aboutDialog.setPositiveButton("Okay", null);

            AlertDialog displayAbout = aboutDialog.create();
            displayAbout.setView(layout, 0, 5, 0, 0);
            displayAbout.show();
            TextView aboutMessage = (TextView) layout.findViewById(R.id.about_text);
            String mess = getString(R.string.about_dialog);
            aboutMessage.setText(mess.replace("{versionID}", String.valueOf(getAppVersion(MainContext)) + ".0"));
            aboutMessage.setMovementMethod(LinkMovementMethod.getInstance());
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
                    msg = "Successfully registered with GCM. ID = " + regId;

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("https://austindizzy.me/prt/_sandbox/user");
                    List <NameValuePair> postData = new ArrayList<NameValuePair>();
                    postData.add(new BasicNameValuePair("regID", regId));
                    httpPost.setEntity(new UrlEncodedFormEntity(postData, HTTP.UTF_8));
                    try {
                        HttpResponse httpResponse = httpClient.execute(httpPost);
                    } catch (ClientProtocolException e) {
                        Log.i("ClientProtocolException", e.toString());
                    } catch (IOException e) {
                        Log.i("IOException", e.toString());
                    }

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
