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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.ScheduleEntry;

import java.util.ArrayList;

public class NewScheduleActivity extends AppCompatActivity
        implements TimePickerDialog.OnTimeSetListener,
        ClassTimeOneFragment.onBasisSelectedListener,
        ClassTimeTwoFragment.onWeekTypeSelectedListener,
        ClassTimeThreeFragment.onDaysSelectedListener,
        ClassTimeThreeFragment.onTimeSelectedListener {

    String LOG_TAG = NewScheduleActivity.class.getSimpleName();
    Utility utility = new Utility();

    String scheduleTitle;
    String scheduleTeacher;
    String scheduleRoom;
    ArrayList<String> occurrenceList;
    ArrayList<Integer> timeInList;
    ArrayList<Integer> timeOutList;
    //    int timeInSeconds;
//    int timeOutSeconds;
    int scheduleIconResource = -1;

    EditText fieldTitle;
    EditText fieldTeacher;
    EditText fieldRoom;
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
    int timeSelectedResourceId = -1;
    int previousTimeInSeconds;
    int previousTimeOutSeconds;
    int[] previousButtonsChecked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule);

        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0f);

        fieldTitle = (EditText) findViewById(R.id.field_new_schedule_title);
        fieldTeacher = (EditText) findViewById(R.id.field_new_schedule_teacher);
        fieldRoom = (EditText) findViewById(R.id.field_new_schedule_room);
        fieldAddClassTime = (TextView) findViewById(R.id.field_new_schedule_add_class_time);
        fieldIcon = (ImageView) findViewById(R.id.new_schedule_icon);
        classTimeList = (ListView) findViewById(R.id.field_new_schedule_class_time_list);
        occurrenceList = new ArrayList<>();
        timeInList = new ArrayList<>();
        timeOutList = new ArrayList<>();


        fieldAddClassTime.setOnClickListener(addClassTime());


        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                scheduleTitle = extras.getString(getString(R.string.SCHEDULE_EXTRA_TITLE));
                editId = extras.getInt(getResources().getString(R.string.SCHEDULE_EXTRA_ID));
                FLAG_EDIT = extras.getBoolean(getResources().getString(R.string.SCHEDULE_FLAG_EDIT));

//                timeInHour = timeInSeconds;
//                timeOutHour = timeOutSeconds;
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
                    timeInList.add(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN)));
                    timeOutList.add(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT)));
                    if (!cursor.moveToNext())
                        cursor.moveToFirst();
                }
                scheduleIconResource = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON));

                fieldTitle.setText(scheduleTitle);
                fieldTeacher.setText(scheduleTeacher);
                fieldRoom.setText(scheduleRoom);
                fieldIcon.setImageResource(scheduleIconResource);
            }
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
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    int rowId = cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID));
                    dbHelper.deleteScheduleItem(rowId);
                }
            }
            for (int i = 0; i < occurrenceList.size(); i++) {
                String occurrence = occurrenceList.get(i);
                int timeIn = -1;
                int timeOut = -1;
                try {
                    timeIn = timeInList.get(i);
                    timeOut = timeOutList.get(i);
                } catch (IndexOutOfBoundsException exception) {
                    Log.e(LOG_TAG, "occurrenceList size is larger than timeInList and timeOutList");
                }
                if (dbHelper.insertSchedule(title, teacher, room, occurrence, timeIn, timeOut, scheduleIconResource)) {
                    if (i == occurrenceList.size() - 1)
                        return true;
                } else
                    Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();
            }
        } else {
            for (int i = 0; i < occurrenceList.size(); i++) {
                String occurrence = occurrenceList.get(i);
                int timeIn = -1;
                int timeOut = -1;
                try {
                    timeIn = timeInList.get(i);
                    timeOut = timeOutList.get(i);
                } catch (IndexOutOfBoundsException exception) {
                    Log.e(LOG_TAG, "occurrenceList size is larger than timeInList and timeOutList");
                }
                if (dbHelper.insertSchedule(title, teacher, room, occurrence, timeIn, timeOut, scheduleIconResource)) {
                    if (i == occurrenceList.size() - 1)
                        return true;
                } else
                    Toast.makeText(NewScheduleActivity.this, "Error creating new schedule", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
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
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Bundle args = new Bundle();
        int resourceId = timeSelectedResourceId;
        args.putString("basis", basis);
        args.putString("weekType", weekType);
        args.putInt("resourceId", resourceId);
        args.putInt("hourOfDay", hourOfDay);
        args.putInt("minute", minute);
        args.putInt("timeInSeconds", previousTimeInSeconds);
        args.putInt("timeOutSeconds", previousTimeOutSeconds);
        args.putIntArray("buttonsChecked", previousButtonsChecked);
        ClassTimeThreeFragment fragment = new ClassTimeThreeFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, "TAG")
                .commit();
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
        Bundle args = new Bundle();
        args.putString("basis", basis);
        args.putString("weekType", weekType);
        ClassTimeThreeFragment fragment = new ClassTimeThreeFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, "TAG")
                .commit();
    }

    @Override
    public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds) {
        this.classDays = classDays;
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("TAG"))
                .commit();
        occurrenceList.add(processOccurrenceString(basis, weekType, classDays));
        timeInList.add(timeInSeconds);
        timeOutList.add(timeOutSeconds);
        classTimeAdapter.notifyDataSetChanged();
    }

    private String processOccurrenceString(String basis, String weekType, String classDays) {
        return basis + ":" + weekType + ":" + classDays;
    }

    @Override
    public void onTimeSelected(int resourceId, int previousTimeInSeconds, int previousTimeOutSeconds, int[] buttonsChecked) {
        timeSelectedResourceId = resourceId;
        this.previousTimeInSeconds = previousTimeInSeconds;
        this.previousTimeOutSeconds = previousTimeOutSeconds;
        previousButtonsChecked = buttonsChecked;
    }
}