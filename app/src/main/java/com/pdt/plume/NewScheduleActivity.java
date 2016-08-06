package com.pdt.plume;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class NewScheduleActivity extends AppCompatActivity
        implements TimePickerDialog.OnTimeSetListener,
        ClassTimeOneFragment.onBasisSelectedListener,
        ClassTimeTwoFragment.onWeekTypeSelectedListener,
        ClassTimeThreeFragment.onDaysSelectedListener {

    String LOG_TAG = NewScheduleActivity.class.getSimpleName();

    EditText fieldTitle;
    EditText fieldTeacher;
    EditText fieldRoom;
    LinearLayout fieldTimeIn;
    LinearLayout fieldTimeOut;
    TextView fieldValueTimeIn;
    TextView fieldValueTimeOut;
    public static int timeInHour;
    int timeInMinute;
    public static int timeOutHour;
    int timeOutMinute;

    TextView fieldOccurrence;

    boolean FLAG_EDIT = false;
    public static boolean isEdited;
    int editId = -1;
    public static int resourceId = -1;

    String basis;
    String weekType;
    ArrayList<String> classDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule);

        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0f);

        fieldTitle = (EditText) findViewById(R.id.field_new_schedule_title);
        fieldTeacher = (EditText) findViewById(R.id.field_new_schedule_teacher);
        fieldRoom = (EditText) findViewById(R.id.field_new_schedule_room);
        fieldTimeIn = (LinearLayout) findViewById(R.id.field_new_schedule_timein);
        fieldTimeOut = (LinearLayout) findViewById(R.id.field_new_schedule_timeout);
        fieldValueTimeIn = (TextView) findViewById(R.id.fieldvalue_new_schedule_timein);
        fieldValueTimeOut = (TextView) findViewById(R.id.fieldvalue_new_schedule_timeout);
        fieldOccurrence = (TextView) findViewById(R.id.field_new_schedule_add_class_time);

        fieldTimeIn.setOnClickListener(showTimePickerDialog());
        fieldTimeOut.setOnClickListener(showTimePickerDialog());
        Calendar c = Calendar.getInstance();

        fieldOccurrence.setOnClickListener(addClassTime());


        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
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
                fieldValueTimeIn.setText("" + timeIn);
                fieldValueTimeOut.setText("" + timeOut);

                timeInHour = (int) timeIn;
                timeOutHour = (int) timeOut;

            }
        }
        isEdited = FLAG_EDIT;

        if (!isEdited) {
            timeInHour = c.get(Calendar.HOUR_OF_DAY) + 1;
            timeOutHour = c.get(Calendar.HOUR_OF_DAY) + 2;
            String timeInDefault = timeInHour + ":00";
            String timeOutDefault = timeOutHour + ":00";
            fieldValueTimeIn.setText(timeInDefault);
            fieldValueTimeOut.setText(timeOutDefault);

            classDays = new ArrayList<>();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_done:
                Intent intent = new Intent(this, MainActivity.class);
                if (insertScheduleData()) {
                    startActivity(intent);
                    finish();
                } else finish();

                break;
        }
        return true;
    }

    private View.OnClickListener showTimePickerDialog() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resourceId = v.getId();
                DialogFragment timePickerFragment = new TimePickerFragment();
                if (resourceId != -1)
                    timePickerFragment.show(getSupportFragmentManager(), "time picker");
            }
        };
    }

    private View.OnClickListener addClassTime() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClassTimeOne();
            }

            private void setClassTimeOne() {
                ClassTimeOneFragment fragment = new ClassTimeOneFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, fragment)
                        .commit();
            }
        };
    }


    private boolean insertScheduleData() {
        String title = fieldTitle.getText().toString();
        String teacher = fieldTeacher.getText().toString();
        String room = fieldRoom.getText().toString();
        String occurrence = "";
        int timein = timeInHour + timeInMinute;
        int timeout = timeOutHour + timeOutMinute;
        int icon = R.drawable.placeholder_sixtyfour;

        DbHelper dbHelper = new DbHelper(this);
        if (FLAG_EDIT) {
            if (dbHelper.updateScheduleItem(editId, title, teacher, room, occurrence, timein, timeout, icon)) {
                return true;
            } else
                Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();
        } else {
            if (dbHelper.insertSchedule(title, teacher, room, occurrence, timein, timeout, icon)) {
                return true;
            } else
                Toast.makeText(NewScheduleActivity.this, "Error creating new schedule", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String timeString = hourOfDay + " " + minute;
        switch (resourceId) {
            case R.id.field_new_schedule_timein:
                timeInHour = hourOfDay;
                timeInMinute = minute;
                fieldValueTimeIn.setText(timeString);
                break;
            case R.id.field_new_schedule_timeout:
                timeOutHour = hourOfDay;
                timeOutMinute = minute;
                fieldValueTimeOut.setText(timeString);
                break;
        }

    }

    @Override
    public void onBasisSelected(String basis) {
        this.basis = basis;
        Log.v(LOG_TAG, "Basis: " + this.basis);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ClassTimeTwoFragment())
                .commit();
    }

    @Override
    public void onWeekTypeSelected(String weekType) {
        this.weekType = weekType;
        Log.v(LOG_TAG, "Week Type: " + this.weekType);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ClassTimeThreeFragment())
                .commit();
    }

    @Override
    public void onDaysSelectedListener(String weekType) {

    }

    private void processOccurenceString(String basis, String weekType, ArrayList<String> classDays){

    }
}

