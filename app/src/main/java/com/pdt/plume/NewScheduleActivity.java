package com.pdt.plume;

import android.content.Intent;
import android.database.Cursor;
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

    EditText fieldTitle;
    EditText fieldTeacher;
    EditText fieldRoom;
    TextView fieldTimein;
    TextView fieldTimeout;

    boolean FLAG_EDIT = false;
    int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule);

        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0f);

        fieldTitle = (EditText) findViewById(R.id.field_new_schedule_title);
        fieldTeacher = (EditText) findViewById(R.id.field_new_schedule_teacher);
        fieldRoom = (EditText) findViewById(R.id.field_new_schedule_room);
        fieldTimein = (TextView) findViewById(R.id.field_new_schedule_timein);
        fieldTimeout = (TextView) findViewById(R.id.field_new_schedule_timeout);

        Intent intent = getIntent();
        if (intent != null){
            Bundle extras = intent.getExtras();
            if (extras != null){
                editId = extras.getInt(getResources().getString(R.string.SCHEDULE_EXTRA_ID));
                String title = extras.getString(getResources().getString(R.string.SCHEDULE_EXTRA_TITLE));
                String teacher = extras.getString(getResources().getString(R.string.SCHEDULE_EXTRA_TEACHER));
                String room = extras.getString(getResources().getString(R.string.SCHEDULE_EXTRA_ROOM));
                float timeIn = extras.getFloat(getResources().getString(R.string.SCHEDULE_EXTRA_TIMEIN));
                float timeOut = extras.getFloat(getResources().getString(R.string.SCHEDULE_EXTRA_TIMEOUT));
                FLAG_EDIT = extras.getBoolean(getResources().getString(R.string.SCHEDULE_FLAG_EDIT));

                fieldTitle.setText(title);
                fieldTeacher.setText(teacher);
                fieldRoom.setText(room);
                fieldTimein.setText("" + timeIn);
                fieldTimeout.setText("" + timeOut);
            }
        }
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
        String title = fieldTitle.getText().toString();
        String teacher = fieldTeacher.getText().toString();
        String room = fieldRoom.getText().toString();
        String occurrence = "";
        int timein = 1300;
        int timeout = 1400;
        int icon = R.drawable.placeholder_sixtyfour;

        DbHelper dbHelper = new DbHelper(this);
        if (dbHelper.updateScheduleItem(editId, title, teacher, room, occurrence, timein, timeout, icon)){
            return true;
        } else Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();

        if (dbHelper.insertSchedule(title, teacher, room, occurrence, timein, timeout, icon)){
            return true;
        } else Toast.makeText(NewScheduleActivity.this, "Error creating new schedule", Toast.LENGTH_SHORT).show();
        return false;
    }
}
