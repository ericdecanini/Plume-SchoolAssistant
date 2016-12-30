package com.pdt.plume.data;


import android.provider.BaseColumns;

public class DbContract {

    public static final class ScheduleEntry implements BaseColumns{
        public static final String TABLE_NAME = "schedule";
        public static final String COLUMN_USER = "user";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TEACHER = "teacher";
        public static final String COLUMN_ROOM = "room";
        public static final String COLUMN_OCCURRENCE = "occurrence";
        public static final String COLUMN_TIMEIN = "timein";
        public static final String COLUMN_TIMEOUT = "timeout";
        public static final String COLUMN_TIMEIN_ALT = "timeinalt";
        public static final String COLUMN_TIMEOUT_ALT = "timeoutalt";
        public static final String COLUMN_PERIODS = "periods";
        public static final String COLUMN_PEERS = "peers";
        public static final String COLUMN_ICON = "icon";
    }

    public static final class TasksEntry implements BaseColumns{
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_USER = "user";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CLASS = "class";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_SHARER = "sharer";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_ATTACHMENT = "attachment";
        public static final String COLUMN_DUEDATE = "duedate";
        public static final String COLUMN_REMINDER_DATE = "reminderdate";
        public static final String COLUMN_REMINDER_TIME = "remindertime";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_PICTURE = "picture";
        public static final String COLUMN_COMPLETED = "completed";
    }

    public static final class NotesEntry implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_USER = "user";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_SCHEDULE_TITLE = "schedule_key";
    }

    public static final class PeersEntry implements BaseColumns {
        public static final String TABLE_NAME = "peers";
        public static final String COLUMN_USER = "user";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_FLAVOUR = "flavour";
        public static final String COLUMN_REQUEST_STATUS = "status";
    }

}
