package com.pdt.plume;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.pdt.plume.services.ScheduleWidgetService;


public class ScheduleWidgetProvider extends AppWidgetProvider {

    /**
     * this method is called every 30 mins as specified on widgetinfo.xml
     * this method is also called on every phone reboot
     **/

    @Override
    public void onUpdate(Context context, AppWidgetManager
            appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i = 0; i < N; ++i) {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews remoteViews = updateWidgetListView(context, appWidgetIds[i]);
            remoteViews.setOnClickPendingIntent(R.id.master_layout, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private RemoteViews updateWidgetListView(Context context,
                                             int appWidgetId) {

        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(), R.layout.widget_schedule);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, ScheduleWidgetService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(
                svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(appWidgetId, R.id.schedule_list,
                svcIntent);
        //setting an empty view in case of no data
        remoteViews.setEmptyView(R.id.schedule_list, R.id.schedule_list);
        return remoteViews;
    }

}

