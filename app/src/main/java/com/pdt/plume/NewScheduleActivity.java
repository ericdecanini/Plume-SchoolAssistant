package com.pdt.plume;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.ScheduleEntry;

import java.util.ArrayList;
import java.util.Calendar;

public class NewScheduleActivity extends AppCompatActivity
        implements TimePickerDialog.OnTimeSetListener,
        ClassTimeOneFragment.onBasisSelectedListener,
        ClassTimeTwoFragment.onWeekTypeSelectedListener,
        ClassTimeThreeFragment.onDaysSelectedListener {

    String LOG_TAG = NewScheduleActivity.class.getSimpleName();
    Utility utlility = new Utility();

    String scheduleTitle;
    String scheduleTeacher;
    String scheduleRoom;
    ArrayList<String> occurrenceList;
    int timeInSeconds;
    int timeOutSeconds;
    int scheduleIconResource = -1;

    EditText fieldTitle;
    EditText fieldTeacher;
    EditText fieldRoom;
    LinearLayout fieldTimeIn;
    LinearLayout fieldTimeOut;
    TextView fieldValueTimeIn;
    TextView fieldValueTimeOut;
    public static int timeInHour;
    public static int timeOutHour;
    ImageView fieldIcon;

    ListView classTimeList;
    TextView fieldAddClassTime;
    ArrayAdapter<String> classTimeAdapter;

    boolean FLAG_EDIT = false;
    public static boolean isEdited;
    int editId = -1;
    public static int resourceId = -1;

    String basis;
    String weekType;
    String classDays;


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
        fieldAddClassTime = (TextView) findViewById(R.id.field_new_schedule_add_class_time);
        fieldIcon = (ImageView) findViewById(R.id.new_schedule_icon);
        classTimeList = (ListView) findViewById(R.id.field_new_schedule_class_time_list);
        occurrenceList = new ArrayList<>();

        fieldTimeIn.setOnClickListener(showTimePickerDialog());
        fieldTimeOut.setOnClickListener(showTimePickerDialog());
        Calendar c = Calendar.getInstance();

        fieldAddClassTime.setOnClickListener(addClassTime());



        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                scheduleTitle = extras.getString(getString(R.string.SCHEDULE_EXTRA_TITLE));
                editId = extras.getInt(getResources().getString(R.string.SCHEDULE_EXTRA_ID));
                FLAG_EDIT = extras.getBoolean(getResources().getString(R.string.SCHEDULE_FLAG_EDIT));

                timeInHour = timeInSeconds;
                timeOutHour = timeOutSeconds;
            }
        }
        isEdited = FLAG_EDIT;

        if (isEdited) {
            Cursor cursor = new DbHelper(this).getScheduleDataArrayByTitle(scheduleTitle);
            if (cursor.moveToFirst()) {

                scheduleTeacher = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER));
                scheduleRoom = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM));
                for (int i = 0; i < cursor.getCount(); i++) {
                    occurrenceList.add(cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE)));
                    if (!cursor.moveToNext())
                        cursor.moveToFirst();
                }
                timeInSeconds = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN));
                timeOutSeconds = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT));
                scheduleIconResource = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON));

                fieldTitle.setText(scheduleTitle);
                fieldTeacher.setText(scheduleTeacher);
                fieldRoom.setText(scheduleRoom);
                fieldValueTimeIn.setText(utlility.secondsToTime(timeInSeconds));
                fieldValueTimeOut.setText(utlility.secondsToTime(timeOutSeconds));
                fieldIcon.setImageResource(scheduleIconResource);
            }
        } else {
            timeInHour = c.get(Calendar.HOUR_OF_DAY) + 1;
            timeOutHour = c.get(Calendar.HOUR_OF_DAY) + 2;
            timeInSeconds = utlility.timeToSeconds(timeInHour, 0);
            timeOutSeconds = utlility.timeToSeconds(timeOutHour, 0);
            String timeInDefault = timeInHour + ":00";
            String timeOutDefault = timeOutHour + ":00";
            fieldValueTimeIn.setText(timeInDefault);
            fieldValueTimeOut.setText(timeOutDefault);
        }

        classTimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, occurrenceList);
        classTimeList.setAdapter(classTimeAdapter);
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



    private boolean insertScheduleData() {
        String title = fieldTitle.getText().toString();
        String teacher = fieldTeacher.getText().toString();
        String room = fieldRoom.getText().toString();
        //Default icon if no other resource was set
        if (scheduleIconResource == -1)
            scheduleIconResource = R.drawable.placeholder_sixtyfour;

        DbHelper dbHelper = new DbHelper(this);
        if (FLAG_EDIT) {
            Cursor cursor = dbHelper.getScheduleDataArrayByTitle(scheduleTitle);
            for (int i = 0; i < cursor.getCount(); i++){
                if (cursor.moveToPosition(i)){
                    int rowId = cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID));
                    dbHelper.deleteScheduleItem(rowId);
                }
            }
            for (int i = 0; i < occurrenceList.size(); i++) {
                String occurrence = occurrenceList.get(i);
                if (dbHelper.insertSchedule(title, teacher, room, occurrence, timeInSeconds, timeOutSeconds, scheduleIconResource)) {
                    if (i == occurrenceList.size() - 1)
                        return true;
                } else
                    Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();
            }
        } else {
            for (int i = 0; i < occurrenceList.size(); i++) {
                String occurrence = occurrenceList.get(i);
                if (dbHelper.insertSchedule(title, teacher, room, occurrence, timeInSeconds, timeOutSeconds, scheduleIconResource)) {
                    if (i == occurrenceList.size() - 1)
                        return true;
                } else
                    Toast.makeText(NewScheduleActivity.this, "Error creating new schedule", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String timeString;
        if (minute < 10)
            timeString = hourOfDay + ":0" + minute;
        else
            timeString = hourOfDay + ":" + minute;
        switch (resourceId) {
            case R.id.field_new_schedule_timein:
                timeInHour = hourOfDay;
                timeInSeconds = utlility.timeToSeconds(hourOfDay, minute);
                fieldValueTimeIn.setText(timeString);
                break;
            case R.id.field_new_schedule_timeout:
                timeOutHour = hourOfDay;
                timeOutSeconds = utlility.timeToSeconds(hourOfDay, minute);
                fieldValueTimeOut.setText(timeString);
                break;
        }

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

    @Override
    public void onBasisSelected(String basis) {
        this.basis = basis;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ClassTimeTwoFragment())
                .commit();
    }

    @Override
    public void onWeekTypeSelected(String weekType) {
        this.weekType = weekType;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ClassTimeThreeFragment(), "TAG")
                .commit();
    }

    @Override
    public void onDaysSelected(String classDays) {
        this.classDays = classDays;
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("TAG"))
                .commit();
        occurrenceList.add(processOccurrenceString(basis, weekType, classDays));
        Log.v(LOG_TAG, "Occurrence: " + processOccurrenceString(basis, weekType, classDays));
        classTimeAdapter.notifyDataSetChanged();
    }

    private String processOccurrenceString(String basis, String weekType, String classDays) {
        return basis + ":" + weekType + ":" + classDays;
    }
}

