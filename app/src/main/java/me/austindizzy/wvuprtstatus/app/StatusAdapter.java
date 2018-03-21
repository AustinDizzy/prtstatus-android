package me.austindizzy.wvuprtstatus.app;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    List<PRTStatus> updates;

    public StatusAdapter(List<PRTStatus> updates) {
        this.updates = updates;
    }

    @Override
    public StatusAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = viewType == 0 ? R.layout.status_list_header : R.layout.status_list;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StatusAdapter.ViewHolder holder, int position) {
        if (position == 0) { return; }
        int truePosition = updates.size() - position - 1;
        PRTStatus status = updates.get(truePosition);

        holder.statusText.setText(status.getMessage());
        Context context = holder.statusText.getContext();

        CharSequence when;
        long now = System.currentTimeMillis();
        long timestamp = status.getTimestamp() * 1000;
        if (now - timestamp > (60*60*24*7)*1000) {
            when = DateFormat.format("h:mma, M/d/y", timestamp);
        } else {
            when = DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.SECOND_IN_MILLIS);
        }
        holder.whenText.setText(when);

        int color = ContextCompat.getColor(context, status.IsDown() || status.IsClosed() ? R.color.FireBrick : R.color.ForestGreen);
        ((CardView) holder.itemView).setCardBackgroundColor(color);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return updates.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder {
        public TextView statusText;
        public TextView whenText;
        public ViewHolder(View itemView) {
            super(itemView);
            statusText = itemView.findViewById(R.id.status_text_list);
            whenText = itemView.findViewById(R.id.status_when_list);
        }
    }
}
