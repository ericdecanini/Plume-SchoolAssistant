package com.pdt.plume;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.ScheduleEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.pdt.plume.NewTaskActivity.REQUEST_NOTIFICATION_ALARM;
import static com.pdt.plume.NewTaskActivity.REQUEST_NOTIFICATION_INTENT;

public class NewScheduleActivity extends AppCompatActivity
        implements TimePickerDialog.OnTimeSetListener,
        IconDialogFragment.iconDialogListener,
        AddClassTimeOneFragment.onBasisSelectedListener,
        AddClassTimeTwoFragment.onBasisTextviewSelectedListener,
        AddClassTimeTwoFragment.onWeekTypeSelectedListener,
        AddClassTimeThreeFragmentTime.onTimeSelectedListener,
        AddClassTimeThreeFragmentTime.onDaysSelectedListener,
        AddClassTimeThreeFragmentPeriod.onDaysSelectedListener,
        AddClassTimeThreeFragmentBlock.onDaysSelectedListener,
        AddClassTimeThreeFragmentTime.onBasisTextviewSelectedListener,
        AddClassTimeThreeFragmentPeriod.onBasisTextviewSelectedListener,
        AddClassTimeThreeFragmentBlock.onBasisTextviewSelectedListener,
        AddClassTimeThreeFragmentTime.onWeektypeTextviewSelectedListener,
        AddClassTimeThreeFragmentPeriod.onWeektypeTextviewSelectedListener {

    // Constantly Used Variables
    String LOG_TAG = NewScheduleActivity.class.getSimpleName();
    Utility utility = new Utility();

    // CAM Variables
    private Menu mActionMenu;
    private int mOptionMenuCount;
    List<Integer> CAMselectedItemsList = new ArrayList<>();

    // UI Elements
    AutoCompleteTextView fieldTitle;
    EditText fieldTeacher;
    EditText fieldRoom;
    ImageView fieldIcon;
    ListView classTimeList;
    TextView fieldAddClassTime;
    ImageView fieldAddClassTimeIcon;

    // UI Data
    String scheduleIconUriString;
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
    int scheduleIconResourceId = R.drawable.art_class_64dp;

    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    private Integer[] mThumbIds = {
            R.drawable.art_arts_64dp,
            R.drawable.art_biology_64dp,
            R.drawable.art_business_64dp,
            R.drawable.art_chemistry_64dp,
            R.drawable.art_childdevelopment_64dp,
            R.drawable.art_class_64dp,
            R.drawable.art_computing_64dp,
            R.drawable.art_cooking_64dp,
            R.drawable.art_creativestudies_64dp,
            R.drawable.art_drama_64dp,
            R.drawable.art_engineering_64dp,
            R.drawable.art_english_64dp,
            R.drawable.art_french_64dp,
            R.drawable.art_geography_64dp,
            R.drawable.art_graphics_64dp,
            R.drawable.art_hospitality_64dp,
            R.drawable.art_ict_64dp,
            R.drawable.art_maths_64dp,
            R.drawable.art_media_64dp,
            R.drawable.art_music_64dp,
            R.drawable.art_pe_64dp,
            R.drawable.art_physics_64dp,
            R.drawable.art_psychology_64dp,
            R.drawable.art_re_64dp,
            R.drawable.art_science_64dp,
            R.drawable.art_spanish_64dp,
            R.drawable.art_task_64dp,
            R.drawable.art_woodwork_64dp
    };

    // Intent Data
    boolean INTENT_FLAG_EDIT = false;
    public static boolean isEdited;
    int editId = -1;
    int REQUEST_IMAGE_GET = 2;
    boolean STARTED_BY_NEWTASKACTIVITY = false;

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

    // Flags
    boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule);

        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0f);
        isTablet = getResources().getBoolean(R.bool.isTablet);

        // Get references to the UI elements
        fieldTitle = (AutoCompleteTextView) findViewById(R.id.field_new_schedule_title);
        fieldTeacher = (EditText) findViewById(R.id.field_new_schedule_teacher);
        fieldRoom = (EditText) findViewById(R.id.field_new_schedule_room);
        fieldAddClassTime = (TextView) findViewById(R.id.field_new_schedule_add_class_time);
        fieldAddClassTimeIcon = (ImageView) findViewById(R.id.field_new_schedule_add_class_time_icon);
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
        fieldIcon.setOnClickListener(showIconDialog());
        fieldAddClassTime.setOnClickListener(addClassTime());

        // Set the adapter for the title auto-complete text view
        String[] subjects = getResources().getStringArray(R.array.subjects);
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, subjects);
        fieldTitle.setAdapter(autoCompleteAdapter);

        // Check if the activity was started by an intent from an edit action
        Intent intent = getIntent();
        // If the intent is not null the activity should have been started from an edit action
        if (intent != null) {
            Bundle extras = intent.getExtras();
            // Get the title and edit flag sent through the intent
            if (extras != null) {
                scheduleTitle = extras.getString(getString(R.string.SCHEDULE_EXTRA_TITLE));
                INTENT_FLAG_EDIT = extras.getBoolean(getResources().getString(R.string.SCHEDULE_FLAG_EDIT));
                STARTED_BY_NEWTASKACTIVITY = extras.getBoolean("STARTED_BY_NEWTASKACTIVITY", false);
            }
        }

        // isEdited is a constant used in the TimePickerFragmentSchedule to set the default selected time
        // upon creation of the dialog to be the previously selected time if the activity has been
        // launched through an edit action
        isEdited = INTENT_FLAG_EDIT;

        // Get schedule data in database based on the schedule title to auto-fill the fields in the UI element
        if (isEdited) {
            // The cursor should only contain schedule data of the item's title, so multiple rows would only include different instances of occurrence
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor;
            cursor = dbHelper.getScheduleDataByTitle(scheduleTitle);
            if (cursor.moveToFirst()) {
                scheduleTeacher = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER));
                scheduleRoom = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM));
                scheduleIconUriString = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON));
                // Get database values to put in activity Array Lists
                for (int i = 0; i < cursor.getCount(); i++) {
                    String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                    if (!occurrence.equals("-1")){
                        occurrenceTimePeriodList.add(new OccurrenceTimePeriod(
                                this,
                                utility.millisToHourTime(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN))),
                                utility.millisToHourTime(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT))),
                                utility.millisToHourTime(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT))),
                                utility.millisToHourTime(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT))),
                                cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS)),
                                occurrence));
                        occurrenceList.add(occurrence);
                        periodsList.add(cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS)));
                        timeInList.add(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN)));
                        timeOutList.add(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT)));
                        timeInAltList.add(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT)));
                        timeOutAltList.add(cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT)));
                    }

                    if (!cursor.moveToNext())
                        cursor.moveToFirst();
                }

                // Auto-fill the fields with the previously inserted data
                fieldTitle.setText(scheduleTitle);
                fieldTeacher.setText(scheduleTeacher);
                fieldRoom.setText(scheduleRoom);
                Bitmap setImageBitmap = null;
                try {
                    setImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(scheduleIconUriString));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fieldIcon.setImageBitmap(setImageBitmap);
            }
            cursor.close();
        } else {
            // Set any default data
            Resources resources = getResources();
            int resId = R.drawable.art_class_64dp;
            Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId)
                    + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
            scheduleIconUriString = drawableUri.toString();
        }

        // Initialise the adapter and listener for the addClassTime UI. If the activity was launched through edit, occurrenceTimePeriodList
        // will have been previously populated and therefore the list view will contain the class time list items
        classTimeAdapter = new OccurrenceTimePeriodAdapter(this, R.layout.list_item_occurrence_time_period, occurrenceTimePeriodList);
        classTimeList.setAdapter(classTimeAdapter);
        classTimeList.setOnItemClickListener(OccurrenceTimePeriodListener());
        classTimeList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        classTimeList.setMultiChoiceModeListener(new ModeCallback());
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
                    Toast.makeText(NewScheduleActivity.this, getString(R.string.new_schedule_toast_validation_title_not_found),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                // If all fields are valid, perform database insertion
                else if (insertScheduleDataIntoDatabase()) {
                    if (!STARTED_BY_NEWTASKACTIVITY) {
                        Intent intent = new Intent(this, MainActivity.class);
                        Toast.makeText(NewScheduleActivity.this, scheduleTitle + " " + getString(R.string.new_schedule_toast_class_inserted), Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                    }
                    finish();
                }
                else finish();
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor  = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent));
        fieldTitle.setBackgroundColor(mPrimaryColor);
        fieldAddClassTime.setTextColor(mPrimaryColor);
        fieldAddClassTimeIcon.setColorFilter(mPrimaryColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fieldTeacher.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            fieldRoom.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            Uri fullPhotoUri = data.getData();
            Bitmap setImageBitmap = null;

            try {
                setImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fullPhotoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            fieldIcon.setImageBitmap(setImageBitmap);
        }
    }

    private boolean insertScheduleDataIntoDatabase() {
        // Store data from UI input fields to variables to prepare them for insertion into the database
        String title = fieldTitle.getText().toString();
        scheduleTitle = title;
        String teacher = fieldTeacher.getText().toString();
        String room = fieldRoom.getText().toString();
        // Prepare a default icon to insert if no other icon was set
        if (scheduleIconResourceId == -1)
            scheduleIconResourceId = R.drawable.art_class_64dp;
        int notes_id = -1;

        DbHelper dbHelper = new DbHelper(this);
        // If the activity was started by an edit action, update the database row, else, insert a new row
        if (INTENT_FLAG_EDIT) {
            // Delete the previous all instances of the schedule (based on the title)
            Cursor cursor = dbHelper.getScheduleDataByTitle(scheduleTitle);
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    int rowId = cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID));
                    dbHelper.deleteScheduleItem(rowId);
                }
            }

            // Insert a row for each occurrence item. If there is no occurrence item
            // Insert a single row
            if (occurrenceTimePeriodList.size() != 0)
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

                    // Database insert function performed as update
                    if (dbHelper.insertSchedule(title, teacher, room, occurrence,
                            timeIn, timeOut, timeInAlt, timeOutAlt,
                            periods, scheduleIconUriString, notes_id)) {
                        if (i == occurrenceTimePeriodList.size() - 1)
                            return true;
                    }
                    else Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();
                }
            // Single row edit, no occurrence
            else {
                // Database insert function without any occurrences
                if (dbHelper.insertSchedule(title, teacher, room, "-1",
                        -1, -1, -1, -1,
                        "-1", scheduleIconUriString, notes_id)) {
                    return true;
                }
                else Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();
            }

        }
        // If the activity was not started by an edit action, insert a new row into the database
        else {
            // Insert a row for each occurrence item
            if (occurrenceTimePeriodList.size() != 0)
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

                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent intent = new Intent(this, MuteAlarmReceiver.class);
                    intent.putExtra("UNMUTE_TIME", timeOut);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Log.v(LOG_TAG, "Class notification for " + title + " set for " + timeIn);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeIn, AlarmManager.INTERVAL_DAY, pendingIntent);

                    // Database insert function
                    if (dbHelper.insertSchedule(title, teacher, room, occurrence,
                            timeIn, timeOut, timeInAlt, timeOutAlt,
                            periods, scheduleIconUriString, notes_id)) {
                        if (i == occurrenceTimePeriodList.size() - 1)
                            return true;
                    } else
                        Toast.makeText(NewScheduleActivity.this, "Error creating new schedule", Toast.LENGTH_SHORT).show();
                }
            else {
                // Database insert function without any occurrences
                if (dbHelper.insertSchedule(title, teacher, room, "-1",
                        -1, -1, -1, -1,
                        "-1", scheduleIconUriString, notes_id)) {
                    Log.v(LOG_TAG, "Inserting single schedule returned true");
                    return true;
                }
                else Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();
            }
        }

        // If data insertion functions were not executed, return false by default
        return false;
    }

    public void Remind(Date dateTime, String title, String message, int ID) {
        scheduleNotification(dateTime, ID, title, message);
    }

    private void scheduleNotification(final Date dateTime, final int ID, final String title, final String message) {

        final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Bitmap largeIcon = null;
        try {
            largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(scheduleIconUriString));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setBackground(largeIcon);

        Intent contentIntent = new Intent(this, ScheduleDetailActivity.class);
        contentIntent.putExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE), title);
        final PendingIntent contentPendingIntent = PendingIntent.getBroadcast(this, REQUEST_NOTIFICATION_INTENT, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Palette.generateAsync(largeIcon, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                builder
                        .setContentIntent(contentPendingIntent)
                        .setSmallIcon(R.drawable.ic_assignment)
                        .setColor(palette.getVibrantColor(mPrimaryColor))
                        .setContentTitle(title)
                        .setContentText(message)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .extend(wearableExtender)
                        .setDefaults(Notification.DEFAULT_ALL);

                Notification notification = builder.build();

                Intent notificationIntent = new Intent(NewScheduleActivity.this, TaskNotificationPublisher.class);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(NewScheduleActivity.this, REQUEST_NOTIFICATION_ALARM, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC, dateTime.getTime(), AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        });
    }

    private void editClassTimeItem(int position){
        // Get the data of the selected row through the Array Lists
        // and put it in a bundle
        Bundle args = new Bundle();
        String occurrence = occurrenceList.get(position);
        args.putString("occurrence", occurrence);
        basis = occurrence.split(":")[0];
        weekType = occurrence.split(":")[1];
        args.putString("weekType", occurrence.split(":")[1]);
        args.putString("period", periodsList.get(position));
        args.putInt("rowId", position);
        args.putInt("timeInSeconds", timeInList.get(position));
        args.putInt("timeOutSeconds", timeOutList.get(position));
        args.putInt("timeInAltSeconds", timeInAltList.get(position));
        args.putInt("timeOutAltSeconds", timeOutAltList.get(position));

        // Check if the activity is in Contextual Action Mode
        // and emulate a back press to destroy it if it is
        if (CAMselectedItemsList != null && CAMselectedItemsList.size() > 0){
            CAMselectedItemsList.clear();
            NewScheduleActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            NewScheduleActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        // Create a new fragment based on the basis and launch it
        switch (occurrence.split(":")[0]){
            case "0":
                // Check if other dialogs are present and remove them if so
                FragmentTransaction ftTime = getFragmentManager().beginTransaction();
                Fragment prevTime = getFragmentManager().findFragmentByTag("dialog");
                if (prevTime != null) {
                    ftTime.remove(prevTime);
                }
                ftTime.addToBackStack(null);

                // Show the dialog
                DialogFragment fragmentTime = AddClassTimeThreeFragmentTime.newInstance(0);
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
                DialogFragment fragmentPeriod = AddClassTimeThreeFragmentPeriod.newInstance(0);
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
                DialogFragment fragmentBlock = AddClassTimeThreeFragmentBlock.newInstance(0);
                fragmentBlock.setArguments(args);
                fragmentBlock.show(getSupportFragmentManager(), "dialog");

                break;
        }
    }

    private View.OnClickListener addClassTime() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if any SharedPreferences for the basis or weekType was previously stored
                // If there are, jump to ClassTimeThreeFragment (Time/Period/Block Selection)
                // If there are none, start from AddClassTimeOneFragment (Basis Selection)

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
                    DialogFragment fragment = AddClassTimeOneFragment.newInstance(0);
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
                            DialogFragment fragmentTime = AddClassTimeThreeFragmentTime.newInstance(0);
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
                            DialogFragment fragmentPeriod = AddClassTimeThreeFragmentPeriod.newInstance(0);
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
                            DialogFragment fragmentBlock = AddClassTimeThreeFragmentBlock.newInstance(0);
                            fragmentBlock.setArguments(args);
                            fragmentBlock.show(getSupportFragmentManager(), "dialog");
                            break;
                    }
                }}
        };
    }

    private AdapterView.OnItemClickListener OccurrenceTimePeriodListener(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editClassTimeItem(position);    }
        };
    }

    // Interfaces and Override Methods
    @Override
    public void onBasisSelected(String basis) {
        // Interface launched by AddClassTimeOneFragment when basis is selected
        // Selected basis is stored in this activity
        // If TimeBased or PeriodBased was selected, launch AddClassTimeTwoFragment (WeekType Selection)
        // If BlockBased was selected, launch classTimeThreeFragment (Block Selection)
        this.basis = basis;

        // If TimeBased/PeriodBased is selected, launch AddClassTimeTwoFragment (WeekType Selection)
        // else blockBased is selected, launch AddClassTimeTwoFragment (WeekType Selection)
        if (!basis.equals("2")) {
            weekType = "-1";
            // Check if other dialogs are present and remove them if so
            android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
            transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
            transactionWeekType.addToBackStack(null).commit();

            // Create the args
            Bundle args = new Bundle();
            args.putString("basis", basis);

            // Show the dialog
            DialogFragment fragment = AddClassTimeTwoFragment.newInstance(0);
            fragment.setArguments(args);
            fragment.show(getSupportFragmentManager(), "dialog");
        }
        else{
            // Check if other dialogs are present and remove them if so
            android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
            transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
            transactionWeekType.addToBackStack(null).commit();

            // Show the dialog
            DialogFragment fragment = AddClassTimeThreeFragmentBlock.newInstance(0);
            fragment.show(getSupportFragmentManager(), "dialog");
        }
    }
    @Override
    public void onWeekTypeSelected(String weekType) {
        // Interface launched by AddClassTimeTwoFragment when WeekType is selected
        // Store the selected WeekType in this activity
        // Launch the corresponding ClassTimeThreeFragment based on stored basis
        this.weekType = weekType;

        // Create the arguments bundle to include in the fragment
        Bundle args = new Bundle();
        args.putString("basis", basis);
        args.putString("weekType", weekType);

        // If basis is TimeBased, launch AddClassTimeThreeFragmentTime
        // Else if basis is PeriodBased, launch AddClassTimeThreeFragmentPeriod
        if (basis.equals("0")) {// Check if other dialogs are present and remove them if so
            android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
            transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
            transactionWeekType.addToBackStack(null).commit();

            // Show the dialog
            DialogFragment fragment = AddClassTimeThreeFragmentTime.newInstance(0);
            fragment.setArguments(args);
            fragment.show(getSupportFragmentManager(), "dialog");
        }
        else if (basis.equals("1")) {
            // Check if other dialogs are present and remove them if so
            android.support.v4.app.FragmentTransaction transactionWeekType = getSupportFragmentManager().beginTransaction();
            transactionWeekType.remove(getSupportFragmentManager().findFragmentByTag("dialog"));
            transactionWeekType.addToBackStack(null).commit();

            // Show the dialog
            DialogFragment fragment = AddClassTimeThreeFragmentPeriod.newInstance(0);
            fragment.setArguments(args);
            fragment.show(getSupportFragmentManager(), "dialog");
        }
    }
    @Override
    public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds,
                               int timeInAltSeconds, int timeOutAltSeconds, String periods,
                               boolean FLAG_EDIT, int rowId) {
        // Interface launched by ClassTimeThreeFragment in the final stage of adding a new class time
        // The fragment is removed (identified by its tag "TAG")
        // Occurrence, timeIn, timeOut, timeInAlt, timeOutAlt, and periodsList are all added to their respective Array Lists. These will later be added to the database
        // A new item in the occurrenceTimePeriodList is also added. This is the visual list view from the NewScheduleActivity.
        // SharedPreferences "basis" and "weekType" will also be updated

        // Set the global variable for the selected days
        this.classDays = classDays;

        // Remove the fragment
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("dialog"))
                .commit();

        // Check the interface for the edit flag and choose
        // to either update or insert
        // If the interface contains an edit flag, update the array list items
        if (FLAG_EDIT) {
            Toast.makeText(this, "Updating row " + rowId, Toast.LENGTH_SHORT).show();
            // Create temporary array lists to hold the list's data as it is cleared for bulk re-inserting
            ArrayList<String> previousStringObjects = new ArrayList<>();
            ArrayList<Integer> previousIntObjects = new ArrayList<>();

            // Update occurrenceList
            previousStringObjects.addAll(occurrenceList);
            occurrenceList.clear();
            occurrenceList.addAll(utility.updateStringArrayListItemAtPosition(
                    previousStringObjects, rowId, processOccurrenceString(basis, weekType, classDays)));
            previousStringObjects.clear();
            // Update periodList
            previousStringObjects.addAll(periodsList);
            periodsList.clear();
            periodsList.addAll(utility.updateStringArrayListItemAtPosition(previousStringObjects, rowId, periods));
            previousStringObjects.clear();
            // Update timeInList
            previousIntObjects.addAll(timeInList);
            timeInList.clear();
            timeInList.addAll(utility.updateIntegerArrayListItemAtPosition(previousIntObjects, rowId, timeInSeconds));
            previousIntObjects.clear();
            // Update timeOutList
            previousIntObjects.addAll(timeOutList);
            timeOutList.clear();
            timeOutList.addAll(utility.updateIntegerArrayListItemAtPosition(previousIntObjects, rowId, timeInSeconds));
            previousIntObjects.clear();
            // Update timeInAltList
            previousIntObjects.addAll(timeInAltList);
            timeInAltList.clear();
            timeInAltList.addAll(utility.updateIntegerArrayListItemAtPosition(previousIntObjects, rowId, timeInSeconds));
            previousIntObjects.clear();
            // Update timeOutAltList
            previousIntObjects.addAll(timeOutAltList);
            timeOutAltList.clear();
            timeOutAltList.addAll(utility.updateIntegerArrayListItemAtPosition(previousIntObjects, rowId, timeInSeconds));
            previousIntObjects.clear();

            // Update the item in the visual list view
            ArrayList<OccurrenceTimePeriod> previousOccurrenceTimePeriodObjects = new ArrayList<>();
            previousOccurrenceTimePeriodObjects.addAll(occurrenceTimePeriodList);
            occurrenceTimePeriodList.clear();
            occurrenceTimePeriodList.addAll(utility.updateOccurrenceTimePeriodArrayListItemAtPosition(
                    previousOccurrenceTimePeriodObjects, rowId, new OccurrenceTimePeriod(
                            this,
                            utility.millisToHourTime(timeInSeconds) + "",
                            utility.millisToHourTime(timeOutSeconds) + "",
                            utility.millisToHourTime(timeInAltSeconds) + "",
                            utility.millisToHourTime(timeOutAltSeconds) + "",
                            periods,
                            processOccurrenceString(basis, weekType, classDays))));
            previousOccurrenceTimePeriodObjects.clear();
        }

        // If the interface does not contain an edit flag
        // Add values into Array Lists to be inserted into the database
        else{
            Toast.makeText(this, "Inserting new row", Toast.LENGTH_SHORT).show();
            occurrenceList.add(processOccurrenceString(basis, weekType, classDays));
            timeInList.add(timeInSeconds);
            timeOutList.add(timeOutSeconds);
            timeInAltList.add(timeInAltSeconds);
            timeOutAltList.add(timeOutAltSeconds);
            periodsList.add(periods);

            // Add an item into the visual list view
            occurrenceTimePeriodList.add(new OccurrenceTimePeriod(
                    this,
                    utility.millisToHourTime(timeInSeconds) + "",
                    utility.millisToHourTime(timeOutSeconds) + "",
                    utility.millisToHourTime(timeInAltSeconds) + "",
                    utility.millisToHourTime(timeOutAltSeconds) + "",
                    periods,
                    processOccurrenceString(basis, weekType, classDays)));
        }

        // Update the list view's UI to correspond with the data
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
        DialogFragment fragment = AddClassTimeOneFragment.newInstance(0);
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

        // Create the args
        Bundle args = new Bundle();
        args.putString("basis", basis);

        // Show the dialog
        DialogFragment fragment = AddClassTimeTwoFragment.newInstance(0);
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), "dialog");
        // Check if other dialogs are present and remove them if so

    }
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Interface launched by TimePickerDialog to restart AddClassTimeThreeFragmentTime with data from the dialog

        // Create the arguments bundle for the fragment
        Bundle args = new Bundle();
        int resourceId = timeSelectedResourceId;
        args.putString("basis", basis);
        args.putString("weekType", weekType);
        args.putString("classDays", classDays);
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
        DialogFragment fragment = AddClassTimeThreeFragmentTime.newInstance(0);
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), "dialog");
    }
    @Override
    public void onTimeSelected(int resourceId, String classDays, int previousTimeInSeconds, int previousTimeOutSeconds,
                               int previousTimeInAltSeconds, int previousTimeOutAltSeconds, int[] buttonsChecked) {
        // Interface from AddClassTimeThreeFragmentTime to save fragment data when the TimePickerDialog is opened
        // This creates the illusion that the fragment was never restarted and creates a smooth user experience
        timeSelectedResourceId = resourceId;
        this.classDays = classDays;
        this.previousTimeInSeconds = previousTimeInSeconds;
        this.previousTimeOutSeconds = previousTimeOutSeconds;
        this.previousTimeInAltSeconds = previousTimeInAltSeconds;
        this.previousTimeOutAltSeconds = previousTimeOutAltSeconds;
        previousButtonsChecked = buttonsChecked;
    }

    private String processOccurrenceString(String basis, String weekType, String classDays) {
        // Helper method to create the computer-readable occurrence string
        return basis + ":" + weekType + ":" + classDays;
    }

    @Override
    public void OnIconListItemSelected(int item) {
        switch (item){
            case 0:
                showBuiltInIconsDialog();
                break;
            case 1:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(intent, REQUEST_IMAGE_GET);
                break;
        }
    }

    private View.OnClickListener showIconDialog() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new IconDialogFragment();
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        };
    }

    private void showBuiltInIconsDialog() {
        // Prepare grid view
        GridView gridView = new GridView(this);
        final AlertDialog dialog;

        int[] builtinIcons = getResources().getIntArray(R.array.builtin_icons);
        List<Integer>  mList = new ArrayList<>();
        for (int i = 1; i < builtinIcons.length; i++) {
            mList.add(builtinIcons[i]);
        }

        gridView.setAdapter(new BuiltInIconsAdapter(this));
        gridView.setNumColumns(4);
        gridView.setPadding(0, 16, 0, 16);
        gridView.setGravity(Gravity.CENTER);
        // Set grid view to alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(gridView);
        builder.setTitle(getString(R.string.new_schedule_icon_builtin_title));
        dialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int resId = mThumbIds[position];
                fieldIcon.setImageResource(resId);
                Resources resources = getResources();
                Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId)
                        + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
                scheduleIconUriString = drawableUri.toString();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // Subclass for the Contextual Action Mode
    private class ModeCallback implements ListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            // Get the number of list items selected
            // and set the window subtitle based on that
            final int checkedCount = classTimeList.getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                case 1:
                    mOptionMenuCount = 0;
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mOptionMenuCount = 1;
                    mode.setSubtitle("" + checkedCount + " items selected");
                    break;
            }

            // If the clicked item became selected, add it to
            // an array list of selected items
            if (checked) {
                CAMselectedItemsList.add(position);
            }

            // If the clicked item became deselected, get its item id
            // and remove it from the array list
            else {
                int itemId = -1;
                // Scan through the array list until the
                // item's value matches its position
                // When it does, set the itemId to the matched position
                // and then remove the item in that array list
                // matching that position
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    if (position == CAMselectedItemsList.get(i)) {
                        itemId = i;
                    }
                }
                if (itemId != -1)
                    CAMselectedItemsList.remove(itemId);
            }

            // Invalidating the Action Mode calls onPrepareActionMode
            // which will show or hide the edit menu action based on
            // the number of items selected
            mode.invalidate();
        }
        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            // Inflate the action menu and set the global menu variable
            MenuInflater inflater = NewScheduleActivity.this.getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode_single, menu);
            mActionMenu = menu;

            // Set the title of the contextual action bar
            mode.setTitle(NewScheduleActivity.this.getString(R.string.select_items));

            // Set the colour of the contextual action bar
            ColorDrawable colorDrawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                colorDrawable = new ColorDrawable(getColor(R.color.gray_500));
                NewScheduleActivity.this.getWindow().setStatusBarColor(getResources().getColor(R.color.gray_700));
                findViewById(R.id.field_new_schedule_title).setBackgroundColor(getColor(R.color.gray_500));
            } else {
                colorDrawable = new ColorDrawable(getResources().getColor(R.color.gray_500));
                findViewById(R.id.field_new_schedule_title).setBackgroundColor(getResources().getColor(R.color.gray_500));
            }
            NewScheduleActivity.this.getSupportActionBar().setBackgroundDrawable(colorDrawable);

            return true;
        }
        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            // Checks the count of items selected.
            // If it is one, show the edit menu action.
            // If it is more than one, hide the edit menu action.
            MenuItem menuItem = mActionMenu.findItem(R.id.action_edit);
            if (mOptionMenuCount == 0)
                menuItem.setVisible(true);
            else
                menuItem.setVisible(false);
            return true;
        }
        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteSelectedItems();
                    break;

                case R.id.action_edit:
                    if (CAMselectedItemsList.size() == 1)
                        editClassTimeItem(CAMselectedItemsList.get(0));
                    else Log.w(LOG_TAG, "Cancelled edit action due to more than one item selected");
                    break;
            }
            return true;
        }
        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            // Clear the array list of selected items and revert the window colour back to normal
            CAMselectedItemsList.clear();

            // Set back the colour of the action bar to normal
            ColorDrawable colorDrawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                colorDrawable = new ColorDrawable(getColor(R.color.colorPrimary));
                NewScheduleActivity.this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                findViewById(R.id.field_new_schedule_title).setBackgroundColor(getColor(R.color.colorPrimary));
            } else {
                colorDrawable = new ColorDrawable(getResources().getColor(R.color.gray_500));
                findViewById(R.id.field_new_schedule_title).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
            NewScheduleActivity.this.getSupportActionBar().setBackgroundDrawable(colorDrawable);
        }

        private void deleteSelectedItems() {
            // Delete all the selected items based on the itemIDs
            // Stored in the array list
            for(int i = 0; i < CAMselectedItemsList.size(); i++) {
                occurrenceList.remove((int)CAMselectedItemsList.get(i));
                periodsList.remove((int)CAMselectedItemsList.get(i));
                timeInList.remove((int)CAMselectedItemsList.get(i));
                timeOutList.remove((int)CAMselectedItemsList.get(i));
                timeInAltList.remove((int)CAMselectedItemsList.get(i));
                timeOutAltList.remove((int)CAMselectedItemsList.get(i));
                occurrenceTimePeriodList.remove((int)CAMselectedItemsList.get(i));
            }

            // Notify the adapter of the changes
            classTimeAdapter.notifyDataSetChanged();

            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            NewScheduleActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            NewScheduleActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }
    }

}