package com.pdt.plume.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pdt.plume.R;
import com.pdt.plume.Schedule;
import com.pdt.plume.Task;
import com.pdt.plume.Utility;
import com.pdt.plume.data.DbContract.ScheduleEntry;
import com.pdt.plume.data.DbContract.TasksEntry;
import com.pdt.plume.data.DbContract.NotesEntry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;


public class DbHelper extends SQLiteOpenHelper {
    private String LOG_TAG = DbHelper.class.getSimpleName();
    Utility utility = new Utility();

    private static final String DATABASE_NAME = "PlumeDb.db";
    private static final int DATABASE_VERSION = 2;

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
                + TasksEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_ATTACHMENT + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_DUEDATE + " REAL NOT NULL, "
                + TasksEntry.COLUMN_REMINDER_DATE + " REAL NOT NULL, "
                + TasksEntry.COLUMN_REMINDER_TIME + " REAL NOT NULL, "
                + TasksEntry.COLUMN_ICON + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_PICTURE + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_COMPLETED + " INTEGER NOT NULL"
                + " );";

        final String SQL_CREATE_NOTES_TABLE = "CREATE TABLE " + NotesEntry.TABLE_NAME + " ("
                + NotesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NotesEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + NotesEntry.COLUMN_NOTE + " TEXT NOT NULL, "
                + NotesEntry.COLUMN_SCHEDULE_TITLE + " TEXT NOT NULL "
                + " );";

        db.execSQL(SQL_CREATE_SCHEDULE_TABLE);
        db.execSQL(SQL_CREATE_TASKS_TABLE);
        db.execSQL(SQL_CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS schedule");
        db.execSQL("DROP TABLE IF EXISTS tasks");
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
    }

    public void deleteTaskTable() {
        SQLiteDatabase db = getWritableDatabase();
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
                null,
                ScheduleEntry.COLUMN_TITLE + " ASC");
    }

    public Cursor getCurrentDayScheduleDataFromSQLite(Context context, int year, int month, int day) {
        SQLiteDatabase db = this.getReadableDatabase();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String weekNumber = preferences.getString(context.getString(R.string.KEY_WEEK_NUMBER), "0");
        Cursor cursor;
        // Start by querying the whole table
        if (weekNumber.equals("0"))
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

        // Next, match each class against their occurrences
        ArrayList<String> returningRowIDs = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                String title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));
                String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                String periods = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS));
                if (utility.occurrenceMatchesCurrentDay(context, occurrence, periods, weekNumber, dayOfWeek)) {
                    returningRowIDs.add(String.valueOf(cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID))));
                }
            }
        }
        cursor.close();

        // If no classes were matched, return null
        if (returningRowIDs.size() == 0) {
            return null;
        }

        // Every item will be an argument for another cursor to search
        String[] selectionArgs = returningRowIDs.toArray(new String[0]);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < selectionArgs.length; i++) {
            if (i != 0)
                builder.append(" OR ");
            builder.append(ScheduleEntry._ID);
            builder.append("=?");
        }
        String selection = builder.toString();

        // Query the new table with the new arguments
        Cursor currentDayCursor;
        if (weekNumber.equals("0"))
            currentDayCursor = db.query(ScheduleEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    ScheduleEntry.COLUMN_TIMEIN + ", " + ScheduleEntry.COLUMN_PERIODS + " DESC");
        else
            currentDayCursor = db.query(ScheduleEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    ScheduleEntry.COLUMN_TIMEIN_ALT + ", " + ScheduleEntry.COLUMN_PERIODS + " DESC");
        return currentDayCursor;
    }

    public ArrayList<Schedule> getCurrentDayScheduleArray(Context context, @Nullable Integer year,
                                                          @Nullable Integer month, @Nullable Integer day) throws IOException {
        // Temporarily set 12 hours to false to correct sorting
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean twentyFourHours = preferences.getBoolean(context.getString(R.string.KEY_SETTINGS_CLOCK_FORMAT), true);
        if (!twentyFourHours) {
            preferences.edit().putBoolean(context.getString(R.string.KEY_SETTINGS_CLOCK_FORMAT), true).apply();
        }

        Calendar c = Calendar.getInstance();
        if (year == null)
            year = c.get(Calendar.YEAR);
        if (month == null)
            month = c.get(Calendar.MONTH);
        if (day == null)
            day = c.get(Calendar.DAY_OF_MONTH);

        int forerunnerTime = preferences.getInt(context.getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), 0);
        String weekNumber = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.KEY_WEEK_NUMBER), "0");

        // Query the cursor
        Cursor cursor = getCurrentDayScheduleDataFromSQLite(context, year, month, day);
        ArrayList<Schedule> arrayList = new ArrayList<>();

        // If no classes were matched, return a blank array list
        if (cursor == null)
            return arrayList;

        // Decide whether to add in time/period items and basic or alternate items
        for (int i = 0; i < cursor.getCount(); i++) {
            // Check for week 1 or week 2 and add into the array list based on that
            if (cursor.moveToPosition(i)) {
                String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                String title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));

                // Week 1 / Same week items
                if (weekNumber.equals("0") || occurrence.split(":")[1].equals("0")) {
                    // Get the variables to check from the database
                    long timeInValue = cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN));
                    String periods = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS));

                    // Add the time based list item and schedule the notification
                    if (timeInValue > -1) {
                        arrayList.add(new Schedule(
                                context,
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                utility.millisToHourTime(context, cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN))),
                                utility.millisToHourTime(context, cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT))),
                                ""));
                        // Schedule the notification
                        int ID = cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID));
                        String icon = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON));

                        Calendar timeInCalendar = Calendar.getInstance();
                        timeInCalendar.setTimeInMillis(timeInValue);
                        c.set(Calendar.HOUR, timeInCalendar.get(Calendar.HOUR));
                        c.set(Calendar.MINUTE, timeInCalendar.get(Calendar.MINUTE) - forerunnerTime);

                        Calendar current = Calendar.getInstance();
                        if (c.getTimeInMillis() > current.getTimeInMillis())
                            c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
                    }

                    // Add the period/block based list item
                    else if (!periods.equals("-1")) {
                        ArrayList<String> periodList = utility.createSetPeriodsArrayList(periods, "0", occurrence.split(":")[1]);
                        for (int ii = 0; ii < periodList.size(); ii++) {
                            arrayList.add(new Schedule(
                                    context,
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                    "",
                                    "",
                                    periodList.get(ii)));
                        }
                    }
                }

                // Week 2: Use alternate data
                else {
                    // Get the variables to check from the database
                    long timeInValue = cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT));
                    String periods = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS));

                    // Add the time based list item
                    if (timeInValue > -1) {
                        arrayList.add(new Schedule(
                                context,
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                utility.millisToHourTime(context, cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT))),
                                utility.millisToHourTime(context, cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT))),
                                ""));

                        // Schedule the notification
                        int ID = cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID));
                        String icon = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON));
                        c.setTimeInMillis(timeInValue);
                        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) - forerunnerTime);
                    }

                    // Add the period/block based list item
                    else {
                        ArrayList<String> periodList = utility.createSetPeriodsArrayList(periods, "1", occurrence.split(":")[1]);
                        for (int ii = 0; ii < periodList.size(); ii++) {
                            arrayList.add(new Schedule(
                                    context,
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                    "",
                                    "",
                                    periodList.get(ii)));
                        }
                    }
                }
            }
        }

        cursor.close();

        return arrayList;
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
                ScheduleEntry.COLUMN_TITLE + " ASC");

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
                            ""));

                    usedTitles.add(title);
                }
            }
        }
        return arrayList;
    }

    public ArrayList<Bundle> getAllClassesBundleArray() {
        Cursor cursor = getAllScheduleData();
        ArrayList<Bundle> arrayList = new ArrayList<>();
        ArrayList<String> usedTitles = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                String title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));
                if (!usedTitles.contains(title)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", title);
                    bundle.putString("teacher", cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)));
                    bundle.putString("room", cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)));
                    bundle.putString("occurrence", cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE)));
                    bundle.putLong("timein", cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN)));
                    bundle.putLong("timeout", cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT)));
                    bundle.putLong("timeinalt", cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT)));
                    bundle.putLong("timeoutalt", cursor.getLong(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT)));
                    bundle.putString("periods", cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS)));
                    bundle.putString("icon", cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)));


                    arrayList.add(bundle);

                    usedTitles.add(title);
                }
            }
        }
        return arrayList;
    }

    public boolean insertSchedule(String title, String teacher, String room, String occurrence,
                                  long timein, long timeout, long timeinalt, long timeoutalt,
                                  String periods, String icon) {
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

    public boolean updateScheduleItem(Integer id, String title, String teacher, String room, String occurrence,
                                      int timein, int timeout, int timeinalt, int timeoutalt,
                                      String periods, String icon, int notes_id) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

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

        // Insert to the Cloud
        if (firebaseUser != null) {
            String uID = firebaseUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference classRef = ref.child("users").child(uID).child("classes").child(title);
            classRef.setValue(title);
            classRef.child("teacher").setValue(teacher);
            classRef.child("room").setValue(room);
            classRef.child("occurrence").setValue(occurrence);
            classRef.child("timein").setValue(timein);
            classRef.child("timeout").setValue(timeout);
            classRef.child("timeinalt").setValue(timeinalt);
            classRef.child("timeoutalt").setValue(timeoutalt);
            classRef.child("periods").setValue(periods);
            classRef.child("icon").setValue(icon);
        }
        return true;
    }

    public Integer deleteScheduleItem(Integer id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(ScheduleEntry.TABLE_NAME,
                null,
                ScheduleEntry._ID + "=?",
                new String[]{id.toString()},
                null,
                null,
                null);
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));

        // First, delete the cloud database data
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String uID = firebaseUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("users").child(uID).child("classes").child(title).removeValue();
        }


        return db.delete(ScheduleEntry.TABLE_NAME, "_ID = ?", new String[]{Integer.toString(id)});
    }

    public Integer deleteScheduleItemByTitle(String title) {
        // First, delete the cloud database data
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String uID = firebaseUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("users").child(uID).child("classes").child(title).removeValue();
        }

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
                TasksEntry.COLUMN_DUEDATE + " DESC");
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

    public Cursor getTaskDataByClass(String classTitle) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TasksEntry.TABLE_NAME,
                null,
                TasksEntry.COLUMN_CLASS + "=?",
                new String[]{classTitle},
                null,
                null,
                null);
    }

    public ArrayList<Task> getTaskDataArray() {
        Cursor cursor = getTaskData();
        ArrayList<Task> arrayList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                arrayList.add(i, new Task(null,
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ICON)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE)),
                        "",
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_CLASS)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_DUEDATE)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE)),
                        null
                ));
            }
        }
        return arrayList;
    }

    public Cursor getUncompletedTaskData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TasksEntry.TABLE_NAME,
                null,
                TasksEntry.COLUMN_COMPLETED + "=?",
                new String[]{"0"},
                null,
                null,
                null);
    }

    public ArrayList<Task> getUncompletedTaskArray() {
        Cursor cursor = getUncompletedTaskData();
        ArrayList<Task> arrayList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                arrayList.add(i, new Task(null,
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ICON)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE)),
                        "",
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_CLASS)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_DUEDATE)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE)),
                        null
                ));
            }
        }
        return arrayList;
    }

    public Cursor getCompletedTaskData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TasksEntry.TABLE_NAME,
                null,
                TasksEntry.COLUMN_COMPLETED + "=?",
                new String[]{"1"},
                null,
                null,
                null);
    }

    public ArrayList<Task> getCompletedTaskArray() {
        Cursor cursor = getCompletedTaskData();
        ArrayList<Task> arrayList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                arrayList.add(i, new Task(null,
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ICON)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE)),
                        "",
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_CLASS)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_DUEDATE)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE)),
                        null
                ));
            }
        }
        return arrayList;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public long insertTask(Context context, String title, String classTitle, String type,
                              String description, String attachment,
                              float dueDate, float reminderdate, float remindertime,
                              String icon, ArrayList<Uri> picture, boolean completed) {
            StringBuilder pictureString = new StringBuilder();
            for (int i = 0; i < picture.size(); i++) {
                Uri imageUri = picture.get(i);
                InputStream inputStream = null;
                try {
                    inputStream = context.getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String filename = imageUri.getLastPathSegment();
                byte[] data = new byte[0];
                try {
                    data = getBytes(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File file = new File(context.getFilesDir(), filename);
                FileOutputStream outputStream;
                try {
                    outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(data);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                pictureString.append(Uri.fromFile(file).toString());
                if (i != picture.size())
                    pictureString.append("#seperate#");
            }

            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(TasksEntry.COLUMN_TITLE, title);
            contentValues.put(TasksEntry.COLUMN_CLASS, classTitle);
            contentValues.put(TasksEntry.COLUMN_TYPE, type);
            contentValues.put(TasksEntry.COLUMN_DESCRIPTION, description);
            contentValues.put(TasksEntry.COLUMN_ATTACHMENT, attachment);
            contentValues.put(TasksEntry.COLUMN_DUEDATE, dueDate);
            contentValues.put(TasksEntry.COLUMN_REMINDER_DATE, reminderdate);
            contentValues.put(TasksEntry.COLUMN_REMINDER_TIME, remindertime);
            contentValues.put(TasksEntry.COLUMN_ICON, icon);
            contentValues.put(TasksEntry.COLUMN_PICTURE, pictureString.toString());
            contentValues.put(TasksEntry.COLUMN_COMPLETED, completed);
            return db.insert(TasksEntry.TABLE_NAME, null, contentValues);
    }

    public boolean updateTaskItem(Context context, Integer id, String title, String classTitle, String type,
                                  String description, String attachment,
                                  float dueDate, float reminderdate, float remindertime,
                                  String icon, ArrayList<Uri> picture, boolean completed) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        StringBuilder pictureString = new StringBuilder();
        for (int i = 0; i < picture.size(); i++) {
            Uri imageUri = picture.get(i);
            if (imageUri.toString().length() > 2) {
                InputStream inputStream = null;
                try {
                    inputStream = context.getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String filename = imageUri.getLastPathSegment();
                byte[] data = new byte[0];
                try {
                    data = getBytes(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File file = new File(context.getFilesDir(), filename);
                FileOutputStream outputStream;
                try {
                    outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(data);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                pictureString.append(Uri.fromFile(file).toString());
                if (i != picture.size())
                    pictureString.append("#seperate#");
            }
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TasksEntry.COLUMN_TITLE, title);
        contentValues.put(TasksEntry.COLUMN_CLASS, classTitle);
        contentValues.put(TasksEntry.COLUMN_TYPE, type);
        contentValues.put(TasksEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(TasksEntry.COLUMN_ATTACHMENT, attachment);
        contentValues.put(TasksEntry.COLUMN_DUEDATE, dueDate);
        contentValues.put(TasksEntry.COLUMN_REMINDER_DATE, reminderdate);
        contentValues.put(TasksEntry.COLUMN_REMINDER_TIME, remindertime);
        contentValues.put(TasksEntry.COLUMN_ICON, icon);
        contentValues.put(TasksEntry.COLUMN_PICTURE, pictureString.toString());
        contentValues.put(TasksEntry.COLUMN_COMPLETED, completed);
        db.update(TasksEntry.TABLE_NAME, contentValues, "_ID = ?", new String[]{Integer.toString(id)});

        // Insert to the Cloud
        if (firebaseUser != null) {
            String uID = firebaseUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference classRef = ref.child("users").child(uID).child("tasks").child(title);
            classRef.child("class").setValue(classTitle);
            classRef.child("type").setValue(type);
            classRef.child("description").setValue(description);
            classRef.child("attachment").setValue(attachment);
            classRef.child("duedate").setValue(dueDate);
            classRef.child("reminderdate").setValue(reminderdate);
            classRef.child("remindertime").setValue(remindertime);
            classRef.child("icon").setValue(icon);
            classRef.child("picture").setValue(picture);
            classRef.child("completed").setValue(completed);
        }

        return true;
    }

    public Integer deleteTaskItem(Integer id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TasksEntry.TABLE_NAME,
                null,
                TasksEntry._ID + "=?",
                new String[]{id.toString()},
                null,
                null,
                null);

        if (cursor.getCount() == 0)
            return -1;

        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE));

        // First, delete the cloud database data
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String uID = firebaseUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("users").child(uID).child("tasks").child(title).removeValue();
        }

        return db.delete(TasksEntry.TABLE_NAME, "_ID = ?", new String[]{Integer.toString(id)});
    }


    /**
     *
     * NOTES TABLE FUNCTIONS
     *
     */

    public Cursor getNoteByScheduleTitle(String schedule_title) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(NotesEntry.TABLE_NAME,
                null,
                NotesEntry.COLUMN_SCHEDULE_TITLE + "=?",
                new String[]{schedule_title},
                null,
                null,
                null);
    }

    public Cursor getNoteByID(int _ID) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(NotesEntry.TABLE_NAME,
                null,
                NotesEntry._ID + "=?",
                new String[]{Integer.toString(_ID)},
                null,
                null,
                null);
    }

    public Cursor getAllNoteData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(NotesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public ArrayList<String> getNoteDataArray() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(NotesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                arrayList.add(cursor.getString(cursor.getColumnIndex(NotesEntry.COLUMN_TITLE)));
            }
        }
        return arrayList;
    }

    public int insertNoteItem(String title, String note, String schedule_title) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Insert to the Cloud
        if (firebaseUser != null) {
            String uID = firebaseUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference classRef = ref.child("users").child(uID).child("notes").child(title);
            classRef.child("message").setValue(note);
            classRef.child("scheduletitle").setValue(schedule_title);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotesEntry.COLUMN_TITLE, title);
        contentValues.put(NotesEntry.COLUMN_NOTE, note);
        contentValues.put(NotesEntry.COLUMN_SCHEDULE_TITLE, schedule_title);
        return (int) db.insert(NotesEntry.TABLE_NAME, null, contentValues);
    }

    public int updateNoteItem(int _ID, String title, String note, String schedule_title) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Insert to the Cloud
        if (firebaseUser != null) {
            String uID = firebaseUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference classRef = ref.child("users").child(uID).child("notes").child(title);
            classRef.child("message").setValue(note);
            classRef.child("scheduletitle").setValue(schedule_title);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotesEntry.COLUMN_TITLE, title);
        contentValues.put(NotesEntry.COLUMN_NOTE, note);
        contentValues.put(NotesEntry.COLUMN_SCHEDULE_TITLE, schedule_title);
        return db.update(NotesEntry.TABLE_NAME, contentValues, NotesEntry._ID + "=?", new String[]{Integer.toString(_ID)});
    }

    public Integer deleteNoteItem(Integer id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(NotesEntry.TABLE_NAME,
                null,
                NotesEntry._ID + "=?",
                new String[]{id.toString()},
                null,
                null,
                null);
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(NotesEntry.COLUMN_TITLE));

        // First, delete the cloud database data
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String uID = firebaseUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("users").child(uID).child("notes").child(title).removeValue();
        }

        return db.delete(NotesEntry.TABLE_NAME, "_ID = ?", new String[]{Integer.toString(id)});
    }

}
