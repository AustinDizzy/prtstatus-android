package me.austindizzy.wvuprtstatus.app;

import android.app.Activity;
import android.content.Context;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    Activity MainContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainContext.getWindow().getDecorView().setBackgroundColor(MainContext.getResources().getColor(R.color.Gray));
        if(!isNetworkAvailable()) {
            TextView prtMessage = (TextView)MainContext.findViewById(R.id.prtMessage);
            prtMessage.setText(R.string.no_network);
        } else {
            new FetchPRTTask().execute("https://austindizzy.me/prt.json");
        }
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkConn = connectivityManager.getActiveNetworkInfo();

        return activeNetworkConn != null && activeNetworkConn.isConnectedOrConnecting();
    }

    private class FetchPRTTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... url){

            InputStream is = null;
            String result = "";

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
                String line = null;
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

            Toast toast = Toast.makeText(MainContext.getApplicationContext(), prtMessage, Toast.LENGTH_SHORT);
            toast.show();

            if (prtStatus != 1) {
                MainContext.getWindow().getDecorView().setBackgroundColor(MainContext.getResources().getColor(R.color.FireBrick));
            } else {
                MainContext.getWindow().getDecorView().setBackgroundColor(MainContext.getResources().getColor(R.color.ForestGreen));
            }
        }
    }

}
