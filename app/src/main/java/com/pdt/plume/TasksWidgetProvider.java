package com.pdt.plume;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.pdt.plume.services.TasksWidgetService;


public class TasksWidgetProvider extends AppWidgetProvider {

    /**
     * this method is called every 30 mins as specified on widgetinfo.xml
     * this method is also called on every phone reboot
     **/

    @Override
    public void onUpdate(Context context, AppWidgetManager
            appWidgetManager,int[] appWidgetIds) {

/*int[] appWidgetIds holds ids of multiple instance
 * of your widget
 * meaning you are placing more than one widgets on
 * your homescreen*/
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; ++i) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(context.getString(R.string.INTENT_FLAG_RETURN_TO_TASKS), context.getString(R.string.INTENT_FLAG_RETURN_TO_TASKS));
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
                context.getPackageName(),R.layout.widget_tasks);

        //RemoteViews Service needed to provide mScheduleAdapter for ListView
        Intent svcIntent = new Intent(context, TasksWidgetService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(
                svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting mScheduleAdapter to listview of the widget
        remoteViews.setRemoteAdapter(appWidgetId, R.id.tasks_list,
                svcIntent);
        //setting an empty view in case of no data
        remoteViews.setEmptyView(R.id.tasks_list, R.id.tasks_list);
        return remoteViews;
    }

}

