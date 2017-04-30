package com.pdt.plume;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.pdt.plume.data.DbHelper;

import java.io.IOException;
import java.util.ArrayList;

/**
 * If you are familiar with Adapter of ListView,this is the same as mTasksAdapter
 * with few changes
 *
 */
public class TasksListProvider implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<Task> listItemList = new ArrayList<>();
    private Context context = null;
    private int appWidgetId;

    public TasksListProvider(Context context, Intent intent) throws IOException {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        populateListItem();
    }

    private void populateListItem() throws IOException {
        DbHelper dbHelper = new DbHelper(context);
        ArrayList<Task> tasks = dbHelper.getTaskDataArray();

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            String title = task.taskTitle;
            String description = task.taskDescription;
            String icon = task.taskIcon;

            // Apply the data changes to the UI
            Task listItem = new Task();
            listItem.taskTitle = title;
            listItem.taskDescription = description;
            listItem.taskIcon = icon;
            listItemList.add(listItem);
        }

        if (tasks.size() == 0) {
            Task plainText = new Task();
            plainText.taskTitle = context.getString(R.string.schedule_tasks_splash_no_tasks);
            listItemList.add(plainText);
        }

    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    *
    */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.list_item_task);

        Task listItem = listItemList.get(position);
        remoteView.setTextViewText(R.id.task_title, listItem.taskTitle);

        try {
            if (listItem.taskIcon != null)
                remoteView.setImageViewBitmap(R.id.task_icon,
                        MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(listItem.taskIcon)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}