package me.austindizzy.wvuprtstatus.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter;
    private AppDatabase db;
    private List<PRTStatus> updates;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();
        db = AppDatabase.getAppDatabase(context);

        initToolbar(context);
        initStatus(context);
        updates = db.statusDao().getRecent(System.currentTimeMillis() / 1000);

        MobileAds.initialize(this, getString(R.string.modpub_app_id));

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StatusAdapter(updates);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(statusReceiver,
                new IntentFilter(PRTMessagingService.INTENT_TAG));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(statusReceiver);
        AppDatabase.destoryInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initStatus(final Context context) {
        String uri = "https://prtstat.us/api/status";
        PRTStatus lastStatus = db.statusDao().getLast();
        if (lastStatus == null) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    PRTStatus status = new PRTStatus(response);
                    db.statusDao().insert(status);
                    updateStatus(context, status);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // error
                    Log.d("Reg Error Response", error.getMessage());
                }
            });
            HTTPRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
        } else {
            updateStatus(context, lastStatus);
        }
    }

    private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                PRTStatus status = new PRTStatus(intent.getExtras());
                updates.add(status);
                adapter.notifyDataSetChanged();
                updateStatus(context, status);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void updateStatus(Context context, PRTStatus status) {
        TextView statusText = findViewById(R.id.status_text);
        statusText.setText(status.getMessage());

        int statusColor = ContextCompat.getColor(context, status.IsDown() || status.IsClosed() ? R.color.FireBrick : R.color.ForestGreen);
        findViewById(R.id.app_bar).setBackgroundColor(statusColor);
        findViewById(R.id.status_parent).setBackgroundColor(statusColor);

        Drawable src = ContextCompat.getDrawable(context, status.IsDown() || status.IsClosed() ? R.drawable.down : R.drawable.running);
        ((ImageView) findViewById(R.id.status_icon)).setImageDrawable(src);

        CharSequence since = DateUtils.getRelativeTimeSpanString(status.getTimestamp() * 1000, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        String lastUpdatedMsg = getString(R.string.last_updated);
        ((TextView)findViewById(R.id.status_updated)).setText(String.format(lastUpdatedMsg, since));
    }

    private void initToolbar(Context context) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        final LinearLayout statusCont = findViewById(R.id.status_parent);

        float heightDp =  getResources().getDisplayMetrics().heightPixels * 0.8f;
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        lp.height = (int)heightDp;

        appBarLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.ForestGreen));
        appBarLayout.setLayoutParams(lp);
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout1, int verticalOffset) {
                float scrollPct = Math.abs(verticalOffset / (float) appBarLayout1.getTotalScrollRange());
                statusCont.setAlpha(1.0f - scrollPct);
            }
        });
        setSupportActionBar(toolbar);
    }
}
