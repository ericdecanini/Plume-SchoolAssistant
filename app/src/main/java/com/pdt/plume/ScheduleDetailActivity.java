package com.pdt.plume;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

public class ScheduleDetailActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_detail);

        // Get references to the UI elements
        TextView titleTextview = (TextView) findViewById(R.id.schedule_detail_title);
        TextView teacherTextview = (TextView) findViewById(R.id.schedule_detail_teacher);
        TextView roomTextview = (TextView) findViewById(R.id.schedule_detail_room);

        // Set the attributes of the window
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        // Get the class's data based on the title and fill in the fields
        Intent intent = getIntent();
        String title = "";
        if (intent != null){
            title = intent.getStringExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE));
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor = dbHelper.getScheduleDataByTitle(title);

            if (cursor.moveToFirst()){
                titleTextview.setText(cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE)));
                teacherTextview.setText(cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TEACHER)));
                roomTextview.setText(cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ROOM)));
            }
        }
    }
}
