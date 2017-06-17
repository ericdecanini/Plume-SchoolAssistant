package com.pdt.plume;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Log;

public class Schedule {
    String LOG_TAG = Schedule.class.getSimpleName();

    public String scheduleIcon;
    public String scheduleLesson;
    public String scheduleTeacher;
    public String scheduleRoom;
    public String scheduleTimeIn;
    public String scheduleTimeOut;

    public Object extra;

    public Schedule(){
        super();
    }

    // The global variables are accessed by the mTasksAdapter
    // Set them using the input parameters
    public Schedule(Context context, String icon, String lesson, String teacher, String room,
                    String timeIn, String timeOut, String period) {
        super();
        this.scheduleIcon = icon;
        this.scheduleLesson = lesson;
        this.scheduleTeacher = teacher;
        this.scheduleRoom = room;

        // Find out the basis of the item based on the timeIn and period Strings
        // and set the in and out strings based on that
        // Time based
        if ((!timeIn.equals("")) && period.equals("0:0:0:0:0:0:0:0") || period.equals("") || period.equals(" ")){
            this.scheduleTimeIn = timeIn;
            this.scheduleTimeOut = timeOut;
        }
        // Period/Block based
        else {
            this.scheduleTimeIn = period;
            this.scheduleTimeOut = context.getString(R.string.class_time_list_header_substring_period);
        }

    }

    public void addExtra(Object extra) {
        this.extra = extra;
    }

}