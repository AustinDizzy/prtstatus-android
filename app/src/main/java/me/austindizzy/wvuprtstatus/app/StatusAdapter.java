package me.austindizzy.wvuprtstatus.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    private List<PRTStatus> updates;
    private SharedPreferences prefs;

    private static final int AD_TYPE = 0;
    private static final int WEATHER_TYPE = 1;
    private static final int HEADER_TYPE = 2;
    private static final int UPDATE_TYPE = 3;


    public StatusAdapter(List<PRTStatus> updates) {
        if (updates != null && updates.size() > 0) updates.remove(0);
        this.updates = updates;
    }

    @Override
    public StatusAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = 0;
        if (prefs == null) prefs = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
        switch (viewType) {
            case AD_TYPE:
                layout = prefs.getBoolean("enable_ads", false) ? R.layout.main_adview : R.layout.empty;
                break;
            case HEADER_TYPE: layout = R.layout.status_list_header;
                break;
            case WEATHER_TYPE: layout = R.layout.main_weather;
                break;
            case UPDATE_TYPE: layout = R.layout.status_list;
                break;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        if (viewType == WEATHER_TYPE) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    final String href = context.getString(R.string.weather_uri);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(href));
                    context.startActivity(intent);
                }
            });
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StatusAdapter.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (prefs == null) prefs = PreferenceManager.getDefaultSharedPreferences(holder.context);
        switch (type) {
            case AD_TYPE:
                if (prefs.getBoolean("enable_ads", false) && holder.bannerAd != null) {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    holder.bannerAd.loadAd(adRequest);
                }
                break;
            case WEATHER_TYPE:
                fetchWeather(holder);
                break;
            case UPDATE_TYPE:
                PRTStatus status = updates.get(position - 3);
                setStatus(holder, status);
                break;
            case HEADER_TYPE:
            default:
                break;
        }
    }

    public void setData(List<PRTStatus> list) {
        if (list != null && list.size() > 0) list.remove(0);
        this.updates = list;
        notifyDataSetChanged();
    }

    private void setWeather(ViewHolder holder, Weather weather) {
        if (weather == null) { return; }
        String w = weather.getWeather().replace("chance", "");
        int icon = R.drawable.clear_day;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean isDay = (hour > 6 && hour < 18);

        switch (w) {
            case "flurries":
            case "snow":
                icon = isDay ? R.drawable.snow_day : R.drawable.snow_night;
                break;
            case "sleet":
                icon = isDay ? R.drawable.sleet_day : R.drawable.sleet_night;
                break;
            case "fog":
            case "hazy":
                icon = R.drawable.fog;
                break;
            case "clear":
            case "sunny":
                icon = isDay ? R.drawable.clear_day : R.drawable.clear_night;
                break;
            case "tstorms":
                icon = isDay ? R.drawable.tstorms_day : R.drawable.tstorms_night;
                break;
            case "rain":
                icon = isDay ? R.drawable.rain_day : R.drawable.rain_night;
                break;
            case "cloudy":
            case "partlycloudy":
            case "partlysunny":
                icon = isDay ? R.drawable.cloudy_part_day : R.drawable.cloudy_part_night;
                break;
            case "mostlycloudy":
            case "mostlysunny":
                icon = isDay ? R.drawable.cloudy_full_day : R.drawable.cloudy_full_night;
                break;
        }

        holder.conditionsText.setText(weather.toString());
        holder.temperatureText.setText(weather.getTemperatureString());
        holder.weatherIcon.setImageResource(icon);
    }

    private void setStatus(ViewHolder holder, PRTStatus status) {
        if (status == null) { return; }
        int color = ContextCompat.getColor(holder.context,
                status.IsDown() || status.IsClosed() ? R.color.FireBrick : R.color.ForestGreen);

        holder.statusText.setText(status.getMessage());
        holder.whenText.setText(Converters.timestampToWhen(status.getTimestamp()));
        ((CardView) holder.itemView).setCardBackgroundColor(color);
    }

    private void fetchWeather(final ViewHolder holder) {
        final String weatherAPI = holder.context.getString(R.string.weather_api);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, weatherAPI, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Weather w;
                try {
                    w = new Weather(response);
                } catch (JSONException err) {
                    // TODO: something with error
                    w = AppDatabase.getAppDatabase(holder.context).weatherDao().getLast();
                }
                if (w != null) setWeather(holder, w);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: something with error
            }
        });
        HTTPRequestQueue.getInstance(holder.context).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public int getItemViewType(int position) {
        switch(position) {
            case 0:
                return WEATHER_TYPE;
            case 1:
                return AD_TYPE;
            case 2:
                return HEADER_TYPE;
            default:
                return UPDATE_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return this.updates.size() + UPDATE_TYPE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView statusText;
        private TextView whenText;
        private AdView bannerAd;
        public Context context;
        private TextView temperatureText;
        private TextView conditionsText;
        private ImageView weatherIcon;
        private ViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            statusText = itemView.findViewById(R.id.status_text_list);
            bannerAd = itemView.findViewById(R.id.main_ad);
            whenText = itemView.findViewById(R.id.status_when_list);
            temperatureText = itemView.findViewById(R.id.tempText);
            conditionsText = itemView.findViewById(R.id.conditionsText);
            weatherIcon = itemView.findViewById(R.id.weatherIcon);
        }
    }
}
