package com.pdt.plume.services;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

import com.pdt.plume.ScheduleListProvider;

import java.io.IOException;

public class TasksWidgetService extends RemoteViewsService {
/*
* So pretty simple just defining the Adapter of the listview
* here Adapter is ListProvider
* */

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        try {
            return (new ScheduleListProvider(this.getApplicationContext(), intent));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}