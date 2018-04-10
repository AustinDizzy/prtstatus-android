package me.austindizzy.wvuprtstatus.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class StatusWidget extends AppWidgetProvider {

    static RemoteViews genAppWidgetViews(Context context, PRTStatus status) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.status_widget);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.status_parent, pendingIntent);
        views.setInt(R.id.status_parent, "setBackgroundResource", status.IsOpen() ? R.drawable.card_green : R.drawable.card_red);
        views.setTextViewText(R.id.status_text, status.getMessage());
        return views;
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, PRTStatus status) {
        RemoteViews views = genAppWidgetViews(context, status);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, views.getLayoutId());
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        PRTStatus status = AppDatabase.getAppDatabase(context).statusDao().getLast();
        if (status != null) updateAppWidget(context, appWidgetManager, appWidgetId, status);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // First widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Last widget is disabled
    }
}

