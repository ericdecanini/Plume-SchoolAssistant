package com.pdt.plume.data;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pdt.plume.Schedule;
import com.pdt.plume.Task;
import com.pdt.plume.Utility;
import com.pdt.plume.data.DbContract.ScheduleEntry;
import com.pdt.plume.data.DbContract.TasksEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


public class DbHelper extends SQLiteOpenHelper {
    private String LOG_TAG = DbHelper.class.getSimpleName();
    Utility utility = new Utility();

    private static final String DATABASE_NAME = "PlumeDb.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_SCHEDULE_TABLE = "CREATE TABLE " + ScheduleEntry.TABLE_NAME + " ("
                + ScheduleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ScheduleEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + ScheduleEntry.COLUMN_TEACHER + " TEXT NOT NULL, "
                + ScheduleEntry.COLUMN_ROOM + " TEXT NOT NULL, "
                + ScheduleEntry.COLUMN_OCCURRENCE + " TEXT NOT NULL, "
                + ScheduleEntry.COLUMN_TIMEIN + " REAL NOT NULL, "
                + ScheduleEntry.COLUMN_TIMEOUT + " REAL NOT NULL, "
                + ScheduleEntry.COLUMN_TIMEIN_ALT + " REAL NOT NULL, "
                + ScheduleEntry.COLUMN_TIMEOUT_ALT + " REAL NOT NULL, "
                + ScheduleEntry.COLUMN_PERIODS + " TEXT NOT NULL, "
                + ScheduleEntry.COLUMN_ICON + " TEXT NOT NULL "
                + " );";

        final String SQL_CREATE_TASKS_TABLE = "CREATE TABLE " + TasksEntry.TABLE_NAME + " ("
                + TasksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TasksEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_CLASS + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_TYPE + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_SHARER + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_ATTACHMENT + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_DUEDATE + " REAL NOT NULL, "
                + TasksEntry.COLUMN_REMINDER_DATE + " REAL NOT NULL, "
                + TasksEntry.COLUMN_REMINDER_TIME + " REAL NOT NULL, "
                + TasksEntry.COLUMN_ICON + " TEXT NOT NULL "
                + " );";

        db.execSQL(SQL_CREATE_SCHEDULE_TABLE);
        db.execSQL(SQL_CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS schedule");
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
    }

    /**
     * Schedule Database Functions
     * getCursor
     * getCurrentDayCursor
     * getCursorByTitle
     * getArray
     * getCurrentDayArray
     * InsertRow
     * UpdateRow
     * DeleteRow
     */

    public Cursor getAllScheduleData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(ScheduleEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                ScheduleEntry.COLUMN_TITLE + " ASC",
                null);
    }

    public Cursor getCurrentDayScheduleData(Context context) {
        SQLiteDatabase db = this.getReadableDatabase();
        SharedPreferences preferences = ((Activity) context).getPreferences(Context.MODE_PRIVATE);
        int weekNumber = preferences.getInt("weekNumber", 0);
        Cursor cursor;
        if (weekNumber == 0)
            cursor = db.query(DbContract.ScheduleEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    ScheduleEntry.COLUMN_TIMEIN);
        else cursor = db.query(DbContract.ScheduleEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                ScheduleEntry.COLUMN_TIMEIN_ALT);

        ArrayList<String> returningRowIDs = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                String periods = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS));
                if (utility.occurrenceMatchesCurrentDay(context, occurrence, periods, weekNumber, dayOfWeek)) {
                    returningRowIDs.add("" + cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID)));
                }
            }
        }

        cursor.close();
        String[] selectionArgs = returningRowIDs.toArray(new String[0]);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < selectionArgs.length; i++) {
            if (i != 0)
                builder.append(" OR ");
            builder.append(ScheduleEntry._ID);
            builder.append("=?");
        }

        String selection = builder.toString();

        Cursor currentDayCursor = db.query(ScheduleEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
        return currentDayCursor;
    }

    public Cursor getScheduleDataByTitle(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ScheduleEntry.TABLE_NAME,
                null,
                ScheduleEntry.COLUMN_TITLE + "=?",
                new String[]{title},
                null,
                null,
                null);
        return cursor;
    }

    public Cursor getAllClassesData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = getAllScheduleData();
        ArrayList<String> IDs = new ArrayList<>();
        ArrayList<String> usedTitles = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                String title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));
                if (!usedTitles.contains(title)) {
                    IDs.add("" + cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID)));
                    usedTitles.add(title);
                }
            }
        }

        cursor.close();

        String[] selectionArgs = IDs.toArray(new String[0]);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < selectionArgs.length; i++) {
            if (i != 0)
                builder.append(" OR ");
            builder.append(ScheduleEntry._ID);
            builder.append("=?");
        }

        String selection = builder.toString();

        Cursor classesCursor = db.query(ScheduleEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);

        return classesCursor;
    }

    public ArrayList<Schedule> getAllClassesArray(Context context) {
        Cursor cursor = getAllScheduleData();
        ArrayList<Schedule> arrayList = new ArrayList<>();
        ArrayList<String> usedTitles = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                String title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));
                if (!usedTitles.contains(title)) {
                    arrayList.add(new Schedule(
                            context,
                            cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON)),
                            cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TEACHER)),
                            cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ROOM)),
                            " ",
                            " ",
                            cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS))
                    ));

                    usedTitles.add(title);
                }
            }
        }
        return arrayList;
    }

    public ArrayList<Schedule> getCurrentDayScheduleArray(Context context) {
        // Query the cursor, calendar, initialise the Array List
        // and get the preference for the week number
        Cursor cursor = getCurrentDayScheduleData(context);
        Calendar c = Calendar.getInstance();
        ArrayList<Schedule> arrayList = new ArrayList<>();
        int weekNumber = ((Activity) context).getPreferences(Context.MODE_PRIVATE)
                .getInt("weekNumber", 0);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        // Check for week 1 or week 2 and add into the array list based on that
        if (weekNumber == 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    // Get the variables to check from the database
                    String timeIn = utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN)));
                    String periods = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS));

                    // Add the time based list item
                    if (!timeIn.equals("")) {
                        String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                        if (utility.occurrenceMatchesCurrentDay(context, occurrence, periods, weekNumber, dayOfWeek))
                            arrayList.add(new Schedule(
                                    context,
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                    utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN))),
                                    utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT))),
                                    ""
                            ));
                    }
                    // Add the period/block based list item
                    else if (!periods.equals("-1")) {
                        ArrayList<String> periodList = utility.createSetPeriodsArrayList(periods, 0);
                        String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                        if (utility.occurrenceMatchesCurrentDay(context, occurrence, periods, weekNumber, dayOfWeek))
                            for (int ii = 0; ii < periodList.size() - 1; ii++) {
                                arrayList.add(new Schedule(
                                        context,
                                        cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                        cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                        cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                        cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                        utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN))),
                                        utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT))),
                                        periodList.get(ii)));
                            }
                    }
                }
            }
        }
        // Week 2: Use alternate data
        else {
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    // Get the variables to check from the database
                    String timeIn = utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT)));
                    String periods = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS));

                    // Add the time based list item
                    if (!timeIn.equals("")) {
                        String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                        if (utility.occurrenceMatchesCurrentDay(context, occurrence, periods, weekNumber, dayOfWeek))
                            // Changed from arrayList.add(i, new Schedule);
                            arrayList.add(new Schedule(
                                    context,
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                    utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT))),
                                    utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT))),
                                    ""
                            ));
                    }
                    // Add the period/block based list item
                    else {
                        String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                        if (utility.occurrenceMatchesCurrentDay(context, occurrence, periods, weekNumber, dayOfWeek)) {
                            ArrayList<String> periodList = utility.createSetPeriodsArrayList(periods, 1);
                            for (int ii = 0; ii < periodList.size(); ii++) {
                                arrayList.add(new Schedule(
                                        context,
                                        cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                        cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                        cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                        cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                        utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT))),
                                        utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT))),
                                        periodList.get(ii)));
                            }
                        }
                    }

                }
            }
        }

        cursor.close();

        return arrayList;
    }

    public boolean insertSchedule(String title, String teacher, String room, String occurrence, int timein, int timeout, int timeinalt, int timeoutalt, String periods, String icon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ScheduleEntry.COLUMN_TITLE, title);
        contentValues.put(ScheduleEntry.COLUMN_TEACHER, teacher);
        contentValues.put(ScheduleEntry.COLUMN_ROOM, room);
        contentValues.put(ScheduleEntry.COLUMN_OCCURRENCE, occurrence);
        contentValues.put(ScheduleEntry.COLUMN_TIMEIN, timein);
        contentValues.put(ScheduleEntry.COLUMN_TIMEOUT, timeout);
        contentValues.put(ScheduleEntry.COLUMN_TIMEIN_ALT, timeinalt);
        contentValues.put(ScheduleEntry.COLUMN_TIMEOUT_ALT, timeoutalt);
        contentValues.put(ScheduleEntry.COLUMN_PERIODS, periods);
        contentValues.put(ScheduleEntry.COLUMN_ICON, icon);
        db.insert(ScheduleEntry.TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean updateScheduleItem(Integer id, String title, String teacher, String room, String occurrence, int timein, int timeout, int timeinalt, int timeoutalt, String periods, String icon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ScheduleEntry.COLUMN_TITLE, title);
        contentValues.put(ScheduleEntry.COLUMN_TEACHER, teacher);
        contentValues.put(ScheduleEntry.COLUMN_ROOM, room);
        contentValues.put(ScheduleEntry.COLUMN_OCCURRENCE, occurrence);
        contentValues.put(ScheduleEntry.COLUMN_TIMEIN, timein);
        contentValues.put(ScheduleEntry.COLUMN_TIMEOUT, timeout);
        contentValues.put(ScheduleEntry.COLUMN_TIMEIN_ALT, timeinalt);
        contentValues.put(ScheduleEntry.COLUMN_TIMEOUT_ALT, timeoutalt);
        contentValues.put(ScheduleEntry.COLUMN_PERIODS, periods);
        contentValues.put(ScheduleEntry.COLUMN_ICON, icon);
        db.update(ScheduleEntry.TABLE_NAME, contentValues, "_ID = ?", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteScheduleItem(Integer id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(ScheduleEntry.TABLE_NAME, "_ID = ?", new String[]{Integer.toString(id)});
    }

    public Integer deleteScheduleItemByTitle(String title) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(ScheduleEntry.TABLE_NAME, ScheduleEntry.COLUMN_TITLE + " = ?", new String[]{title});
    }

    /**
     * Task Database Functions
     * getCursor
     * getArray
     * InsertRow
     * UpdateRow
     * DeleteRow
     */

    public Cursor getTaskData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TasksEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public Cursor getTaskById(int _ID) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TasksEntry.TABLE_NAME,
                null,
                TasksEntry._ID + "=?",
                new String[]{Integer.toString(_ID)},
                null,
                null,
                null);
    }

    public ArrayList<Task> getTaskDataArray() {
        Cursor cursor = getTaskData();
        ArrayList<Task> arrayList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                arrayList.add(i, new Task(
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ICON)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_SHARER)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_DUEDATE)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE))
                ));
            }
        }
        return arrayList;
    }

    public boolean insertTask(String title, String classTitle, String type, String sharer, String description, String attachment, float dueDate, float reminderdate, float remindertime, String icon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TasksEntry.COLUMN_TITLE, title);
        contentValues.put(TasksEntry.COLUMN_CLASS, classTitle);
        contentValues.put(TasksEntry.COLUMN_TYPE, type);
        contentValues.put(TasksEntry.COLUMN_SHARER, sharer);
        contentValues.put(TasksEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(TasksEntry.COLUMN_ATTACHMENT, attachment);
        contentValues.put(TasksEntry.COLUMN_DUEDATE, dueDate);
        contentValues.put(TasksEntry.COLUMN_REMINDER_DATE, reminderdate);
        contentValues.put(TasksEntry.COLUMN_REMINDER_TIME, remindertime);
        contentValues.put(TasksEntry.COLUMN_ICON, icon);
        db.insert(TasksEntry.TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean updateTaskItem(Integer id, String title, String classTitle, String type, String sharer, String description, String attachment, float dueDate, float reminderdate, float remindertime, String icon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TasksEntry.COLUMN_TITLE, title);
        contentValues.put(TasksEntry.COLUMN_CLASS, classTitle);
        contentValues.put(TasksEntry.COLUMN_TYPE, type);
        contentValues.put(TasksEntry.COLUMN_SHARER, sharer);
        contentValues.put(TasksEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(TasksEntry.COLUMN_ATTACHMENT, attachment);
        contentValues.put(TasksEntry.COLUMN_DUEDATE, dueDate);
        contentValues.put(TasksEntry.COLUMN_REMINDER_DATE, reminderdate);
        contentValues.put(TasksEntry.COLUMN_REMINDER_TIME, remindertime);
        contentValues.put(TasksEntry.COLUMN_ICON, icon);
        db.update(TasksEntry.TABLE_NAME, contentValues, "_ID = ?", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteTaskItem(Integer id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TasksEntry.TABLE_NAME, "_ID = ?", new String[]{Integer.toString(id)});
    }

}
