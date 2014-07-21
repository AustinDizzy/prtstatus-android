package me.austindizzy.wvuprtstatus.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("PRT", "CREATED");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("OPTIONS", "CREATED");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
           Log.i("Selected", " OptionsItem");
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Context context = getApplicationContext();
            new FetchPRTTask(context).execute("http://prtstatus.sitespace.wvu.edu/cache.php?json=true");
            getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.FireBrick));
        }
        return super.onOptionsItemSelected(item);
    }
}
