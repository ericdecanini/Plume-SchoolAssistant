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
public class ScheduleListProvider implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<Schedule> listItemList = new ArrayList<>();
    private Context context = null;
    private int appWidgetId;

    public ScheduleListProvider(Context context, Intent intent) throws IOException {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        populateListItem();
    }

    private void populateListItem() throws IOException {
        DbHelper dbHelper = new DbHelper(context);
        ArrayList<Schedule> schedules = dbHelper.getCurrentDayScheduleArray(context);

        for (int i = 0; i < schedules.size(); i++) {
            Schedule schedule = schedules.get(i);
            String title = schedule.scheduleLesson;
            String teacher = schedule.scheduleTeacher;
            String room = schedule.scheduleRoom;
            String timeIn = schedule.scheduleTimeIn;
            String timeOut = schedule.scheduleTimeOut;
            String icon = schedule.scheduleIcon;

            // Apply the data changes to the UI
            Schedule listItem = new Schedule();
            listItem.scheduleLesson = title;
            listItem.scheduleTeacher = teacher;
            listItem.scheduleRoom = room;
            listItem.scheduleTimeIn = timeIn;
            listItem.scheduleTimeOut = timeOut;
            listItem.scheduleIcon = icon;
            listItemList.add(listItem);
        }

        if (schedules.size() == 0) {
            Schedule plainText = new Schedule();
            plainText.scheduleLesson = context.getString(R.string.schedule_fragment_splash_no_classes);
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
final RemoteViews remoteView = new RemoteViews(
        context.getPackageName(), R.layout.list_item_schedule);
        Schedule listItem = listItemList.get(position);
        remoteView.setTextViewText(R.id.schedule_lesson, listItem.scheduleLesson);
        remoteView.setTextViewText(R.id.schedule_teacher, listItem.scheduleTeacher);
        remoteView.setTextViewText(R.id.schedule_room, listItem.scheduleRoom);
        remoteView.setTextViewText(R.id.schedule_time_in, context.getString(R.string.format_time,
                listItem.scheduleTimeIn, listItem.scheduleTimeOut));
        try {
        remoteView.setImageViewBitmap(R.id.schedule_icon,
        MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(listItem.scheduleIcon)));
        } catch (IOException e) {
        e.printStackTrace();
        }
        remoteView.setTextColor(R.id.schedule_teacher, Color.parseColor("#000000"));
        remoteView.setTextColor(R.id.schedule_room, Color.parseColor("#000000"));
        remoteView.setTextColor(R.id.schedule_time_in, Color.parseColor("#000000"));

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