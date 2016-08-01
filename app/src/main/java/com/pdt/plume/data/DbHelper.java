package com.pdt.plume.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pdt.plume.Schedule;
import com.pdt.plume.data.DbContract.ScheduleEntry;
import com.pdt.plume.data.DbContract.TasksEntry;

import java.util.ArrayList;


public class DbHelper extends SQLiteOpenHelper {
    private String LOG_TAG = DbHelper.class.getSimpleName();

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
                + ScheduleEntry.COLUMN_OCCURENCE + " TEXT NOT NULL, "
                + ScheduleEntry.COLUMN_TIMEIN + " REAL NOT NULL, "
                + ScheduleEntry.COLUMN_TIMEOUT + " REAL NOT NULL, "
                + ScheduleEntry.COLUMN_ICON + " INTEGER NOT NULL "
                + " );";

        final String SQL_CREATE_TASKS_TABLE = "CREATE TABLE " + TasksEntry.TABLE_NAME + " ("
                + TasksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TasksEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, "
                + TasksEntry.COLUMN_FILE + " BLOB NOT NULL, "
                + TasksEntry.COLUMN_DUEDATE + " REAL NOT NULL, "
                + TasksEntry.COLUMN_ALARMTIME + " REAL NOT NULL, "
                + TasksEntry.COLUMN_ICON + " INTEGER NOT NULL "
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

    public boolean insertSchedule(String title, String teacher, String room, String occurrence, int timein, int timeout, int icon){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ScheduleEntry.COLUMN_TITLE, title);
        contentValues.put(ScheduleEntry.COLUMN_TEACHER, teacher);
        contentValues.put(ScheduleEntry.COLUMN_ROOM, room);
        contentValues.put(ScheduleEntry.COLUMN_OCCURENCE, occurrence);
        contentValues.put(ScheduleEntry.COLUMN_TIMEIN, timein);
        contentValues.put(ScheduleEntry.COLUMN_TIMEOUT, timeout);
        contentValues.put(ScheduleEntry.COLUMN_ICON, icon);
        db.insert(ScheduleEntry.TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getAllScheduleData(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(ScheduleEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, ScheduleEntry.TABLE_NAME);
    }

    public boolean updateScheduleItem(Integer id, String title, String teacher, String room, String occurrence, int timein, int timeout, int icon){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ScheduleEntry.COLUMN_TITLE, title);
        contentValues.put(ScheduleEntry.COLUMN_TEACHER, teacher);
        contentValues.put(ScheduleEntry.COLUMN_ROOM, room);
        contentValues.put(ScheduleEntry.COLUMN_OCCURENCE, occurrence);
        contentValues.put(ScheduleEntry.COLUMN_TIMEIN, timein);
        contentValues.put(ScheduleEntry.COLUMN_TIMEOUT, timeout);
        contentValues.put(ScheduleEntry.COLUMN_ICON, icon);
        db.update(ScheduleEntry.TABLE_NAME, contentValues, "id = ?", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteScheduleItem(Integer id){
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(ScheduleEntry.TABLE_NAME, "_ID = ?", new String[]{Integer.toString(id)});
    }

    public ArrayList<Schedule> getCurrentDayScheduleArray(){
        Cursor cursor = getCurrentDayScheduleData();
        ArrayList<Schedule> arrayList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++){
            if (cursor.moveToPosition(i)){
                arrayList.add(i, new Schedule(
                        cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON)),
                        cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TEACHER)),
                        cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ROOM)),
                        cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN)),
                        cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEOUT))
                ));
            }
        }
        return arrayList;
    }

    public Cursor getCurrentDayScheduleData(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DbContract.ScheduleEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        return cursor;
    }

}
