package com.pdt.plume;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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
        ClassTimeThreeFragmentTime.onTimeSelectedListener,
        ClassTimeThreeFragmentTime.onDaysSelectedListener,
        ClassTimeThreeFragmentPeriod.onDaysSelectedListener,
        ClassTimeThreeFragmentBlock.onDaysSelectedListener,
        ClassTimeThreeFragmentTime.onBasisTextviewSelectedListener,
        ClassTimeThreeFragmentPeriod.onBasisTextviewSelectedListener,
        ClassTimeThreeFragmentBlock.onBasisTextviewSelectedListener,
        ClassTimeThreeFragmentTime.onWeektypeTextviewSelectedListener,
        ClassTimeThreeFragmentPeriod.onWeektypeTextviewSelectedListener {

    // Constantly Used Variables
    String LOG_TAG = NewScheduleActivity.class.getSimpleName();
    Utility utility = new Utility();

    // UI Elements
    EditText fieldTitle;
    EditText fieldTeacher;
    EditText fieldRoom;
    ImageView fieldIcon;
    ListView classTimeList;
    TextView fieldAddClassTime;

    // UI Data
    String scheduleTitle;
    String scheduleTeacher;
    String scheduleRoom;
    ArrayList<OccurrenceTimePeriod> occurrenceTimePeriodList;
    ArrayList<String> occurrenceList;
    ArrayList<Integer> timeInList;
    ArrayList<Integer> timeOutList;
    ArrayList<Integer> timeInAltList;
    ArrayList<Integer> timeOutAltList;
    ArrayList<String> periodsList;
    OccurrenceTimePeriodAdapter classTimeAdapter;
    int scheduleIconResourceId = -1;

    // Intent Data
    boolean FLAG_EDIT = false;
    public static boolean isEdited;
    int editId = -1;

    // Interface Data
    String basis = "-1";
    String weekType = "-1";
    String classDays;
    int timeSelectedResourceId = -1;
    int previousTimeInSeconds;
    int previousTimeOutSeconds;
    int previousTimeInAltSeconds;
    int previousTimeOutAltSeconds;
    int[] previousButtonsChecked;
    public static int resourceId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule);

        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0f);

        // Get references to the UI elements
        fieldTitle = (EditText) findViewById(R.id.field_new_schedule_title);
        fieldTeacher = (EditText) findViewById(R.id.field_new_schedule_teacher);
        fieldRoom = (EditText) findViewById(R.id.field_new_schedule_room);
        fieldAddClassTime = (TextView) findViewById(R.id.field_new_schedule_add_class_time);
        fieldIcon = (ImageView) findViewById(R.id.new_schedule_icon);
        classTimeList = (ListView) findViewById(R.id.field_new_schedule_class_time_list);

        // Initialise the Array Lists
        occurrenceTimePeriodList = new ArrayList<>();
        occurrenceList = new ArrayList<>();
        timeInList = new ArrayList<>();
        timeOutList = new ArrayList<>();
        timeInAltList = new ArrayList<>();
        timeOutAltList = new ArrayList<>();
        periodsList = new ArrayList<>();

        // Set the OnClickListener for the UI elements
        fieldAddClassTime.setOnClickListener(addClassTime());

        // Check if the activity was started by an intent from an edit action
        Intent intent = getIntent();
        // If the intent is not null the activity should have been started from an edit action
        if (intent != null) {
            Bundle extras = intent.getExtras();
            // Get the title and edit flag sent through the intent
            if (extras != null) {
                scheduleTitle = extras.getString(getString(R.string.SCHEDULE_EXTRA_TITLE));
                editId = extras.getInt(getResources().getString(R.string.SCHEDULE_EXTRA_ID));
                FLAG_EDIT = extras.getBoolean(getResources().getString(R.string.SCHEDULE_FLAG_EDIT));
            }
        }

        // isEdited is a constant used in the TimePickerFragment to set the default selected time
        // upon creation of the dialog to be the previously selected time if the activity has been
        // launched through an edit action
        isEdited = FLAG_EDIT;

        // Get schedule data in database based on the schedule title to auto-fill the fields in the UI element
        if (isEdited) {
            // The cursor should only contain schedule data of the item's title, so multiple rows would only include different instances of occurrence
            Cursor cursor = new DbHelper(this).getScheduleDataArrayByTitle(scheduleTitle);
            if (cursor.moveToFirst()) {
                scheduleTeacher = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER));
                scheduleRoom = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM));
                scheduleIconResourceId = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON));
                // Get database values to put in activity Array Lists
                for (int i = 0; i < cursor.getCount(); i++) {
                    occurrenceTimePeriodList.add(new OccurrenceTimePeriod(
                            this,
                            utility.secondsToTime(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN))),
                            utility.secondsToTime(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT))),
                            utility.secondsToTime(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT))),
                            utility.secondsToTime(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT))),
                            cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS)),
                            cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE))));
                    timeInList.add(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN)));
                    timeOutList.add(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT)));
                    timeInAltList.add(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT)));
                    timeOutAltList.add(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT)));
                    if (!cursor.moveToNext())
                        cursor.moveToFirst();
                }

                // Auto-fill the fields with the previously inserted data
                fieldTitle.setText(scheduleTitle);
                fieldTeacher.setText(scheduleTeacher);
                fieldRoom.setText(scheduleRoom);
                fieldIcon.setImageResource(scheduleIconResourceId);
            }
        }

        // Initialise the adapter for the addClassTime UI. If the activity was launched through edit, occurrenceTimePeriodList
        // will have been previously populated and therefore the list view will contain the class time list items
        classTimeAdapter = new OccurrenceTimePeriodAdapter(this, R.layout.list_item_occurrence_time_period, occurrenceTimePeriodList);
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
            // Without this, the up button will not do anything and return the error 'Cancelling event due to no window focus'
            case android.R.id.home:
                finish();
                break;

            // Validate input fields then
            // Insert inputted data into the database and terminate the activity
            case R.id.action_done:
                // Check if the title field is empty, disallow insertion of it is
                if ((fieldTitle.getText().toString().equals(""))){
                    Toast.makeText(NewScheduleActivity.this, getString(R.string.new_schedule_toast_error_title_not_found),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                // If all fields are valid, perform database insertion
                else if (insertScheduleDataIntoDatabase()) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else finish();
                break;
        }
        return true;
    }

    private boolean insertScheduleDataIntoDatabase() {
        // Store data from UI input fields to variables to prepare them for insertion into the database
        String title = fieldTitle.getText().toString();
        String teacher = fieldTeacher.getText().toString();
        String room = fieldRoom.getText().toString();
        // Prepare a default icon to insert if no other icon was set
        if (scheduleIconResourceId == -1)
            scheduleIconResourceId = R.drawable.placeholder_sixtyfour;

        DbHelper dbHelper = new DbHelper(this);
        // If the activity was started by an edit action, update the database row, else, insert a new row
        if (FLAG_EDIT) {
            // Delete the previous all instances of the schedule (based on the title)
            Cursor cursor = dbHelper.getScheduleDataArrayByTitle(scheduleTitle);
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    int rowId = cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID));
                    dbHelper.deleteScheduleItem(rowId);
                }
            }

            // Insert a row for each occurrence item
            for (int i = 0; i < occurrenceTimePeriodList.size(); i++) {
                // Initialise occurrence, time, and period strings
                String occurrence = occurrenceList.get(i);
                int timeIn = -1;
                int timeOut = -1;
                int timeInAlt = -1;
                int timeOutAlt = -1;
                String periods = "-1";
                // Get time and period data from Array Lists. Class items that do not utilise
                // the variables are inserted as -1.
                // Variables include: timeIn, timeOut, timeInAlt, timeOutAlt, periods
                try {
                    timeIn = timeInList.get(i);
                    timeOut = timeOutList.get(i);
                    timeInAlt = timeInAltList.get(i);
                    timeOutAlt = timeOutAltList.get(i);
                    periods = periodsList.get(i);
                } catch (IndexOutOfBoundsException exception) {
                    Log.e(LOG_TAG, "occurrenceTimePeriodList size is larger than timeInList and timeOutList");
                }
                // Database insert function
                if (dbHelper.insertSchedule(title, teacher, room, occurrence, timeIn, timeOut, timeInAlt, timeOutAlt, periods, scheduleIconResourceId)) {
                    if (i == occurrenceTimePeriodList.size() - 1)
                        return true;
                } else
                    Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();
            }
        }
        // If the activity was not started by an edit action, insert a new row into the database
        else {
            // Insert a row for each occurrence item
            for (int i = 0; i < occurrenceTimePeriodList.size(); i++) {
                // Initialise occurrence, time, and period strings
                String occurrence = occurrenceList.get(i);
                int timeIn = -1;
                int timeOut = -1;
                int timeInAlt = -1;
                int timeOutAlt = -1;
                String periods = "-1";
                // Get time and period data from Array Lists. Class items that do not utilise
                // the variables are inserted as -1.
                // Variables include: timeIn, timeOut, timeInAlt, timeOutAlt, periods
                try {
                    timeIn = timeInList.get(i);
                    timeOut = timeOutList.get(i);
                    timeInAlt = timeInAltList.get(i);
                    timeOutAlt = timeOutAltList.get(i);
                    periods = periodsList.get(i);
                } catch (IndexOutOfBoundsException exception) {
                    Log.e(LOG_TAG, "occurrenceTimePeriodList size is larger than timeInList and timeOutList");
                }

                // Database insert function
                if (dbHelper.insertSchedule(title, teacher, room, occurrence, timeIn, timeOut, timeInAlt, timeOutAlt, periods, scheduleIconResourceId)) {
                    if (i == occurrenceTimePeriodList.size() - 1)
                        return true;
                } else
                    Toast.makeText(NewScheduleActivity.this, "Error creating new schedule", Toast.LENGTH_SHORT).show();
            }
        }

        // If data insertion functions were not executed, return false by default
        return false;
    }

    private View.OnClickListener addClassTime() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if any SharedPreferences for the basis or weekType was previously stored
                // If there are, jump to ClassTimeThreeFragment (Time/Period/Block Selection)
                // If there are none, start from ClassTimeOneFragment (Basis Selection)

                // Get the stored preference
                SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                basis = preferences.getString(getString(R.string.SCHEDULE_PREFERENCE_BASIS_KEY), "-1");
                weekType = preferences.getString(getString(R.string.SCHEDULE_PREFERENCE_WEEKTYPE_KEY), "-1");

                // Check if preferences were not stored
                if (basis.equals("-1") || weekType.equals("-1")){
                    // Check if other dialogs are present and remove them if so
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Show the dialog
                    DialogFragment fragment = ClassTimeOneFragment.newInstance(0);
                    fragment.show(getSupportFragmentManager(), "dialog");
                }
                // If stored preferences were found, launch ClassTimeThreeFragment with arguments basis and weekType
                else {
                    // Create arguments bundle
                    Bundle args = new Bundle();
                    args.putString("basis", basis);
                    args.putString("weekType", weekType);

                    // Start necessary ClassTimeThreeFragment based on basis
                    switch (basis){
                        case "0":
                            // Check if other dialogs are present and remove them if so
                            FragmentTransaction ftTime = getFragmentManager().beginTransaction();
                            Fragment prevTime = getFragmentManager().findFragmentByTag("dialog");
                            if (prevTime != null) {
                                ftTime.remove(prevTime);
                            }
                            ftTime.addToBackStack(null);

                            // Show the dialog
                            DialogFragment fragmentTime = ClassTimeThreeFragmentTime.newInstance(0);
                            fragmentTime.setArguments(args);
                            fragmentTime.show(getSupportFragmentManager(), "dialog");
                            break;

                        case "1":
                            // Check if other dialogs are present and remove them if so
                            FragmentTransaction ftPeriod = getFragmentManager().beginTransaction();
                            Fragment prevPeriod = getFragmentManager().findFragmentByTag("dialog");
                            if (prevPeriod != null) {
                                ftPeriod.remove(prevPeriod);
                            }
                            ftPeriod.addToBackStack(null);

                            // Show the dialog
                            DialogFragment fragmentPeriod = ClassTimeThreeFragmentPeriod.newInstance(0);
                            fragmentPeriod.setArguments(args);
                            fragmentPeriod.show(getSupportFragmentManager(), "dialog");
                            break;

                        case "2":
                            // Check if other dialogs are present and remove them if so
                            FragmentTransaction ftBlock = getFragmentManager().beginTransaction();
                            Fragment prevBlock = getFragmentManager().findFragmentByTag("dialog");
                            if (prevBlock != null) {
                                ftBlock.remove(prevBlock);
                            }
                            ftBlock.addToBackStack(null);

                            // Show the dialog
                            DialogFragment fragmentBlock = ClassTimeThreeFragmentBlock.newInstance(0);
                            fragmentBlock.setArguments(args);
                            fragmentBlock.show(getSupportFragmentManager(), "dialog");
                            break;
                    }
                }}
        };
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Interface launched by TimePickerDialog to restart ClassTimeThreeFragmentTime with data from the dialog

        // Create the arguments bundle for the fragment
        Bundle args = new Bundle();
        int resourceId = timeSelectedResourceId;
        args.putString("basis", basis);
        args.putString("weekType", weekType);
        args.putInt("resourceId", resourceId);
        args.putInt("hourOfDay", hourOfDay);
        args.putInt("minute", minute);
        args.putInt("timeInSeconds", previousTimeInSeconds);
        args.putInt("timeOutSeconds", previousTimeOutSeconds);
        args.putInt("timeInAltSeconds", previousTimeInAltSeconds);
        args.putInt("timeOutAltSeconds", previousTimeOutAltSeconds);
        args.putIntArray("buttonsChecked", previousButtonsChecked);

        // Launch the fragment
        // Check if other dialogs are present and remove them if so
        android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
        transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
        transactionWeekType.addToBackStack(null).commit();

        // Show the dialog
        DialogFragment fragment = ClassTimeThreeFragmentTime.newInstance(0);
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onTimeSelected(int resourceId, int previousTimeInSeconds, int previousTimeOutSeconds,
                               int previousTimeInAltSeconds, int previousTimeOutAltSeconds, int[] buttonsChecked) {
        // Interface from ClassTimeThreeFragmentTime to save fragment data when the TimePickerDialog is opened
        // This creates the illusion that the fragment was never restarted and creates a smooth user experience
        timeSelectedResourceId = resourceId;
        this.previousTimeInSeconds = previousTimeInSeconds;
        this.previousTimeOutSeconds = previousTimeOutSeconds;
        this.previousTimeInAltSeconds = previousTimeInAltSeconds;
        this.previousTimeOutAltSeconds = previousTimeOutAltSeconds;
        previousButtonsChecked = buttonsChecked;
    }

    @Override
    public void onBasisSelected(String basis) {
        // Interface launched by ClassTimeOneFragment when basis is selected
        // Selected basis is stored in this activity
        // If TimeBased or PeriodBased was selected, launch ClassTimeTwoFragment (WeekType Selection)
        // If BlockBased was selected, launch classTimeThreeFragment (Block Selection)
        this.basis = basis;

        // If TimeBased/PeriodBased is selected, launch ClassTimeTwoFragment (WeekType Selection)
        // else blockBased is selected, launch ClassTimeTwoFragment (WeekType Selection)
        if (!basis.equals("2")) {
            weekType = "-1";
            // Check if other dialogs are present and remove them if so
            android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
            transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
            transactionWeekType.addToBackStack(null).commit();

            // Show the dialog
            DialogFragment fragment = ClassTimeTwoFragment.newInstance(0);
            fragment.show(getSupportFragmentManager(), "dialog");
        }
        else{
            // Check if other dialogs are present and remove them if so
            android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
            transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
            transactionWeekType.addToBackStack(null).commit();

            // Show the dialog
            DialogFragment fragment = ClassTimeThreeFragmentBlock.newInstance(0);
            fragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public void onWeekTypeSelected(String weekType) {
        // Interface launched by ClassTimeTwoFragment when WeekType is selected
        // Store the selected WeekType in this activity
        // Launch the corresponding ClassTimeThreeFragment based on stored basis
        this.weekType = weekType;

        // Create the arguments bundle to include in the fragment
        Bundle args = new Bundle();
        args.putString("basis", basis);
        args.putString("weekType", weekType);

        // If basis is TimeBased, launch ClassTimeThreeFragmentTime
        // Else if basis is PeriodBased, launch ClassTimeThreeFragmentPeriod
        if (basis.equals("0")) {// Check if other dialogs are present and remove them if so
            android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
            transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
            transactionWeekType.addToBackStack(null).commit();

            // Show the dialog
            DialogFragment fragment = ClassTimeThreeFragmentTime.newInstance(0);
            fragment.show(getSupportFragmentManager(), "dialog");
        }
        else if (basis.equals("1")) {
            // Check if other dialogs are present and remove them if so
            android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
            transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
            transactionWeekType.addToBackStack(null).commit();

            // Show the dialog
            DialogFragment fragment = ClassTimeThreeFragmentPeriod.newInstance(0);
            fragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds,
                               int timeInAltSeconds, int timeOutAltSeconds, String periods) {
        // Interface launched by ClassTimeThreeFragment in the final stage of adding a new class time
        // The fragment is removed (identified by its tag "TAG")
        // Occurrence, timeIn, timeOut, timeInAlt, timeOutAlt, and periodsList are all added to their respective Array Lists. These will later be added to the database
        // A new item in the occurrenceTimePeriodList is also added. This is the visual list view from the NewScheduleActivity.
        // SharedPreferences "basis" and "weekType" will also be updated
        this.classDays = classDays;

        // Remove the fragment
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("dialog"))
                .commit();

        // Add values into Array Lists to be inserted into the database
        occurrenceList.add(processOccurrenceString(basis, weekType, classDays));
        timeInList.add(timeInSeconds);
        timeOutList.add(timeOutSeconds);
        timeInAltList.add(timeInAltSeconds);
        timeOutAltList.add(timeOutAltSeconds);
        periodsList.add(periods);

        // Add an item into the visual list view
        occurrenceTimePeriodList.add(new OccurrenceTimePeriod(
                this,
                utility.secondsToTime(timeInSeconds) + "",
                utility.secondsToTime(timeOutSeconds) + "",
                utility.secondsToTime(timeInAltSeconds) + "",
                utility.secondsToTime(timeOutAltSeconds) + "",
                periods,
                processOccurrenceString(basis, weekType, classDays)));
        classTimeAdapter.notifyDataSetChanged();

        // Store the shared preferences for "basis" and "weekType"
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.SCHEDULE_PREFERENCE_BASIS_KEY), basis)
                .putString(getString(R.string.SCHEDULE_PREFERENCE_WEEKTYPE_KEY), weekType)
                .apply();
    }

    @Override
    public void onBasisTextviewSelected() {
        // Interface launched from ClassTime[Two/Three]Fragment to restart the basis selection
        // Check if other dialogs are present and remove them if so
        android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
        transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
        transactionWeekType.addToBackStack(null).commit();

        // Show the dialog
        DialogFragment fragment = ClassTimeOneFragment.newInstance(0);
        fragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onWeektypeTextViewSelectedListener(String basis) {
        // Interface launched from ClassTimeThreeFragment to restart the weekType selection
        // It takes in the string basis and sets the basis to the received value
        this.basis = basis;
        android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
        transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
        transactionWeekType.addToBackStack(null).commit();

        // Show the dialog
        DialogFragment fragment = ClassTimeTwoFragment.newInstance(0);
        fragment.show(getSupportFragmentManager(), "dialog");
        // Check if other dialogs are present and remove them if so

    }

    private String processOccurrenceString(String basis, String weekType, String classDays) {
        // Helper method to create the computer-readable occurrence string
        return basis + ":" + weekType + ":" + classDays;
    }


}