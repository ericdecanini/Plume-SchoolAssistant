package com.pdt.plume.data;


import android.provider.BaseColumns;

public class DbContract {

    public static final class ScheduleEntry implements BaseColumns{
        public static final String TABLE_NAME = "schedule";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TEACHER = "teacher";
        public static final String COLUMN_ROOM = "room";
        public static final String COLUMN_OCCURRENCE = "occurrence";
        public static final String COLUMN_TIMEIN = "timein";
        public static final String COLUMN_TIMEOUT = "timeout";
        public static final String COLUMN_TIMEIN_ALT = "timeinalt";
        public static final String COLUMN_TIMEOUT_ALT = "timeoutalt";
        public static final String COLUMN_ICON = "icon";
    }

    public static final class TasksEntry implements BaseColumns{
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SHARER = "sharer";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_ATTACHMENT = "attachment";
        public static final String COLUMN_DUEDATE = "duedate";
        public static final String COLUMN_ALARMTIME = "alarmtime";
        public static final String COLUMN_ICON = "icon";
    }

}
