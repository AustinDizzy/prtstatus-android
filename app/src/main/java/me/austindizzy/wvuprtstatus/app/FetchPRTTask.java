package me.austindizzy.wvuprtstatus.app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
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


/**
 * Created by Austin on 7/18/2014.
 */
public class FetchPRTTask extends AsyncTask<String, Void, String> {

    private Context mContext;

    public FetchPRTTask(Context context){
        mContext = context;
    }

    protected String doInBackground(String... url){

        InputStream is = null;
        String result = "";

        try {
            Log.i("HTTP", "Trying HTTP at " + url);
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url[0]);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch(Exception e) {
            Log.i("HTTP", "Caught " + e);
            return "null";
        }

        // Read response to string
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
            Log.i("JSON", "Exception is " + e);
            return "null";
        }
    }

    protected void onPostExecute(String result) {
        JSONObject prtObj = null;
        String prtMessage = "";
        int prtStatus;
        try {
            prtObj = new JSONObject(result);
            prtMessage = prtObj.get("message").toString();
            prtStatus = Integer.parseInt(prtObj.get("status").toString());
        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.toString());
        }
        Log.i("PRT TASK", prtMessage);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(mContext, prtMessage, duration);
        toast.show();
    }
}
