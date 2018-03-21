package me.austindizzy.wvuprtstatus.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    private List<PRTStatus> updates;
    private SharedPreferences prefs;

    private static final int AD_TYPE = 0;
    private static final int HEADER_TYPE = 1;
    private static final int UPDATE_TYPE = 2;


    public StatusAdapter(List<PRTStatus> updates) {
        this.updates = updates;
        this.updates.add(null);
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
            case UPDATE_TYPE: layout = R.layout.status_list;
                break;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StatusAdapter.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case AD_TYPE:
                if (prefs.getBoolean("enable_ads", false)) {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    if (!BuildConfig.DEBUG) {
                        holder.bannerAd.setAdUnitId(holder.context.getString(R.string.modpub_main_ad_unit_id));
                    }
                    holder.bannerAd.setVisibility(View.VISIBLE);
                    holder.bannerAd.loadAd(adRequest);
                }
                return;
            case HEADER_TYPE:
                return;
            case UPDATE_TYPE:
                int truePosition = updates.size() - position - 1;
                PRTStatus status = updates.get(truePosition);
                int color = ContextCompat.getColor(holder.context,
                        status.IsDown() || status.IsClosed() ? R.color.FireBrick : R.color.ForestGreen);

                holder.statusText.setText(status.getMessage());
                holder.whenText.setText(Converters.timestampToWhen(status.getTimestamp()));
                ((CardView) holder.itemView).setCardBackgroundColor(color);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return AD_TYPE;
        } else if (position == 1) {
            return HEADER_TYPE;
        }
        return UPDATE_TYPE;
    }

    @Override
    public int getItemCount() {
        return this.updates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView statusText;
        public TextView whenText;
        public AdView bannerAd;
        public Context context;
        public ViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            statusText = itemView.findViewById(R.id.status_text_list);
            whenText = itemView.findViewById(R.id.status_when_list);
            bannerAd = itemView.findViewById(R.id.main_ad);
        }
    }
}
