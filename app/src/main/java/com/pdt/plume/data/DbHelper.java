package com.pdt.plume.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.R;
import com.pdt.plume.Schedule;
import com.pdt.plume.Task;
import com.pdt.plume.Utility;
import com.pdt.plume.data.DbContract.ScheduleEntry;
import com.pdt.plume.data.DbContract.TasksEntry;
import com.pdt.plume.data.DbContract.NotesEntry;
import com.pdt.plume.data.DbContract.PeersEntry;

import java.io.IOException;
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

    public Cursor getCurrentDayScheduleDataFromSQLite(Context context) {
        SQLiteDatabase db = this.getReadableDatabase();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String weekNumber = preferences.getString(context.getString(R.string.KEY_WEEK_NUMBER), "0");
        Cursor cursor;
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

        ArrayList<String> returningRowIDs = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                String title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));
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

    public ArrayList<Schedule> getCurrentDayScheduleArray(Context context) throws IOException {
        // Get the calendar data for the week number
        Calendar c = Calendar.getInstance();
        String weekNumber = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.KEY_WEEK_NUMBER), "0");

        // Get data from SQLite
        // Query the cursor, calendar, initialise the Array List
        // and get the preference for the week number
        Cursor cursor = getCurrentDayScheduleDataFromSQLite(context);
        ArrayList<Schedule> arrayList = new ArrayList<>();

        // Run through the cursor's items
        // 1ST CHECK = WEEK NUMBER
        // 2ND CHECK = CLASS TYPE
        for (int i = 0; i < cursor.getCount(); i++) {
            // Check for week 1 or week 2 and add into the array list based on that
            if (cursor.moveToPosition(i)) {
                String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                String title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));

                // Week 1 / Same week items
                if (weekNumber.equals("0") || occurrence.split(":")[1].equals("0")) {
                    // Get the variables to check from the database
                    String timeIn = utility.millisToHourTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN)));
                    String periods = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS));

                    // Add the time based list item
                    if (!timeIn.equals("")) {
                        arrayList.add(new Schedule(
                                context,
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                utility.millisToHourTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN))),
                                utility.millisToHourTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT))),
                                ""
                        ));
                    }
                    // Add the period/block based list item
                    else if (!periods.equals("-1")) {
                        ArrayList<String> periodList = utility.createSetPeriodsArrayList(periods, "0");
                        for (int ii = 0; ii < periodList.size(); ii++) {
                            arrayList.add(new Schedule(
                                    context,
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                    utility.millisToHourTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN))),
                                    utility.millisToHourTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT))),
                                    periodList.get(ii)));
                        }
                    }
                }

                // Week 2: Use alternate data
                else {
                    // Get the variables to check from the database
                    String timeIn = utility.millisToHourTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT)));
                    String periods = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS));

                    // Add the time based list item
                    if (!timeIn.equals("")) {
                        // Changed from arrayList.add(i, new Schedule);
                        arrayList.add(new Schedule(
                                context,
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                utility.millisToHourTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT))),
                                utility.millisToHourTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT))),
                                ""
                        ));
                    }
                    // Add the period/block based list item
                    else {
                        ArrayList<String> periodList = utility.createSetPeriodsArrayList(periods, "1");
                        for (int ii = 0; ii < periodList.size(); ii++) {
                            arrayList.add(new Schedule(
                                    context,
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM)),
                                    utility.millisToHourTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT))),
                                    utility.millisToHourTime(cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT))),
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
                            cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS))
                    ));

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
                    bundle.putInt("timein", cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN)));
                    bundle.putInt("timeout", cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT)));
                    bundle.putInt("timeinalt", cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT)));
                    bundle.putInt("timeoutalt", cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT)));
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
                                  int timein, int timeout, int timeinalt, int timeoutalt,
                                  String periods, String icon) {
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
        db.insert(ScheduleEntry.TABLE_NAME, null, contentValues);

        // Insert to the Cloud
        if (firebaseUser != null) {
            String uID = firebaseUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference classRef = ref.child("users").child(uID).child("classes").child(title);
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
                arrayList.add(i, new Task(
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ICON)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE)),
                        "",
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_DUEDATE)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE))
                ));
            }
        }
        return arrayList;
    }

    public ArrayList<Task> getTaskDataArrayFromFirebase() {
        final ArrayList<Task> arrayList = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();

        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("tasks");
        final long[] snapshotCount = new long[1];
        final int[] taskCount = {0};
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 snapshotCount[0] = dataSnapshot.getChildrenCount();
                for (DataSnapshot taskSnapshot: dataSnapshot.getChildren()) {
                    String iconUri = taskSnapshot.child("icon").getValue(String.class);
                    String title = taskSnapshot.child("title").getValue(String.class);
                    String description = taskSnapshot.child("description").getValue(String.class);
                    String sharer = taskSnapshot.child("sharer").getValue(String.class);
                    float duedate = taskSnapshot.child("duedate").getValue(float.class);
                    arrayList.add(new Task(iconUri, title, sharer, description, "", duedate, -1));
                    taskCount[0]++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        while (taskCount[0] < snapshotCount[0]) {
            // Do nothing
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
                arrayList.add(i, new Task(
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ICON)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE)),
                        "",
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_DUEDATE)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE))
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
                arrayList.add(i, new Task(
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ICON)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE)),
                        "",
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_DUEDATE)),
                        cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE))
                ));
            }
        }
        return arrayList;
    }

    public boolean insertTask(String title, String classTitle, String type,
                              String description, String attachment,
                              float dueDate, float reminderdate, float remindertime,
                              String icon, String picture, boolean completed) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

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
        contentValues.put(TasksEntry.COLUMN_PICTURE, picture);
        contentValues.put(TasksEntry.COLUMN_COMPLETED, completed);
        db.insert(TasksEntry.TABLE_NAME, null, contentValues);

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

    public boolean updateTaskItem(Integer id, String title, String classTitle, String type,
                                  String description, String attachment,
                                  float dueDate, float reminderdate, float remindertime,
                                  String icon, String picture, boolean completed) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

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
        contentValues.put(TasksEntry.COLUMN_PICTURE, picture);
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

    /**
     * Peers Database Functions
     */

    public Cursor getPeersData(Context context) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();
        SQLiteDatabase db = getWritableDatabase();

        return db.query(
                PeersEntry.TABLE_NAME,
                null,
                PeersEntry.COLUMN_REQUEST_STATUS + "=?",
                new String[]{"0", userId},
                null,
                null,
                null
        );
    }

    public Cursor getPeerByUid(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(PeersEntry.TABLE_NAME,
                null,
                PeersEntry.COLUMN_UID + "=?",
                new String[]{uid},
                null,
                null,
                null);
    }

    ArrayList<String> userIdList = new ArrayList<>();
    ArrayList<String> userNameList = new ArrayList<>();
    ArrayList<String> userFlavourList = new ArrayList<>();
    ArrayList<String> userIconList = new ArrayList<>();

    public void updateRequestsInDb() {
        // Cleanup previous uses of the method
        userIdList.clear();
        userNameList.clear();
        userFlavourList.clear();
        userIconList.clear();

        // Initialise Firebase
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        // Get a reference to the requests database and create the array lists for the name and icon
        if (firebaseUser != null) {
            final DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(firebaseUser.getUid()).child("requests").getRef();

            // Add the values to the array lists
            requestsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                        final String userIDItem = userSnapshot.getKey();
                        userIdList.add(userIDItem);
                        final int snapshotChildrenCount = (int) dataSnapshot.getChildrenCount();
                        userSnapshot.getRef().child("nickname").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                userNameList.add(dataSnapshot.getValue(String.class));
                                if (userNameList.size() == snapshotChildrenCount
                                        && userIconList.size() == snapshotChildrenCount
                                        && userFlavourList.size() == snapshotChildrenCount)
                                    insertRequestsDataToDb();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        userSnapshot.getRef().child("icon").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                userIconList.add(dataSnapshot.getValue(String.class));
                                if (userNameList.size() == snapshotChildrenCount
                                        && userIconList.size() == snapshotChildrenCount
                                        && userFlavourList.size() == snapshotChildrenCount)
                                    insertRequestsDataToDb();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        userSnapshot.getRef().child("flavour").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                userFlavourList.add(dataSnapshot.getValue(String.class));
                                if (userNameList.size() == snapshotChildrenCount
                                        && userIconList.size() == snapshotChildrenCount
                                        && userFlavourList.size() == snapshotChildrenCount)
                                    insertRequestsDataToDb();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private long insertRequestsDataToDb() {
        // This method is fired by the previous method after all the data is collected
        // Start by deleting all the previous data
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();
        SQLiteDatabase db = getWritableDatabase();
        db.delete(PeersEntry.TABLE_NAME, PeersEntry.COLUMN_REQUEST_STATUS + "=?", new String[]{"1"});

        // Run an insert for each row to be inserted
        for (int i = 0; i < userIdList.size(); i++) {
            // Create the ContentValues for the data to be inserted
            ContentValues contentValues = new ContentValues();
            contentValues.put(PeersEntry.COLUMN_UID, userIdList.get(i));
            contentValues.put(PeersEntry.COLUMN_NAME, userNameList.get(i));
            contentValues.put(PeersEntry.COLUMN_FLAVOUR, userFlavourList.get(i));
            contentValues.put(PeersEntry.COLUMN_ICON, userIconList.get(i));
            contentValues.put(PeersEntry.COLUMN_REQUEST_STATUS, "1");

            // Insert the data into the database
            return db.insert(PeersEntry.TABLE_NAME, null, contentValues);
        }
        // If no data was inserted, return -1
        return -1;
    }

    public int insertPeer(String id, String icon, String name, String flavour) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Insert to the Cloud
        if (firebaseUser != null) {
            String uID = firebaseUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference classRef = ref.child("users").child(uID).child("peers").child(id);
            classRef.child("name").setValue(name);
            classRef.child("icon").setValue(icon);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PeersEntry.COLUMN_UID, id);
        contentValues.put(PeersEntry.COLUMN_NAME, name);
        contentValues.put(PeersEntry.COLUMN_ICON, icon);
        contentValues.put(PeersEntry.COLUMN_FLAVOUR, flavour);
        contentValues.put(PeersEntry.COLUMN_REQUEST_STATUS, 0);
        return (int) db.insert(PeersEntry.TABLE_NAME, null, contentValues);
    }

}
