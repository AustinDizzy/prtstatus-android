package me.austindizzy.wvuprtstatus.app;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Austin on 3/2/2018.
 */

@Database(entities = {PRTStatus.class, Weather.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase mDB;
    public abstract StatusDao statusDao();
    public abstract WeatherDao weatherDao();

    public static AppDatabase getAppDatabase(final Context context) {
        if (mDB == null) {
            RoomDatabase.Callback populateDB = new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    final String uri = String.format("%s?limit=10", context.getString(R.string.status_api));
                    final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, uri, null, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                int length = response.length();
                                for (int i = length - 1; i >= 0; i--) {
                                    PRTStatus status = new PRTStatus(response.getJSONObject(i));
                                    mDB.statusDao().insert(status);
                                }
                            } catch (JSONException error) {
                                // TODO: something with err
                            }
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(MainActivity.STATUS_UPDATE));
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: something with error
                        }
                    });
                    HTTPRequestQueue.getInstance(context).getRequestQueue().add(jsonArrayRequest);
                }
            };
            mDB = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "wvuprtstatus")
                    .addCallback(populateDB)
                    .allowMainThreadQueries()
                    .build();
        }
        return mDB;
    }

    public static void destoryInstance() {
        mDB = null;
    }
}
