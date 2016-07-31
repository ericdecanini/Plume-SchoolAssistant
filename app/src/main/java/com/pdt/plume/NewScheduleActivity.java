package com.pdt.plume;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pdt.plume.data.DbHelper;

public class NewScheduleActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule);

        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_done:
                Intent intent = new Intent(this, MainActivity.class);
                if (insertScheduleData()){
                    startActivity(intent);
                } else finish();

                break;
        }
        return true;
    }

    private boolean insertScheduleData(){
//        Bundle bundle = new Bundle();
        EditText fieldTitle = (EditText) findViewById(R.id.field_new_schedule_title);
        String title = fieldTitle.getText().toString();

        EditText fieldTeacher = (EditText) findViewById(R.id.field_new_schedule_teacher);
        String teacher = fieldTeacher.getText().toString();

        EditText fieldRoom = (EditText) findViewById(R.id.field_new_schedule_room);
        String room = fieldRoom.getText().toString();

        String occurrence = "";

        TextView fieldTimein = (TextView) findViewById(R.id.field_new_schedule_timein);
        int timein = 1300;

        TextView fieldTimeout = (TextView) findViewById(R.id.field_new_schedule_timeout);
        int timeout = 1400;

        int icon = R.drawable.placeholder_sixtyfour;

        DbHelper dbHelper = new DbHelper(this);
        if (dbHelper.insertSchedule(title, teacher, room, occurrence, timein, timeout, icon)){
            return true;
        } else Toast.makeText(NewScheduleActivity.this, "Error creating new schedule", Toast.LENGTH_SHORT).show();
        return false;
    }
}
