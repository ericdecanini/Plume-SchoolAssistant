package com.pdt.plume;

import android.*;
import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NewTaskActivity extends AppCompatActivity
        implements
        IconDialogFragment.iconDialogListener {
    // Constantly used variables
    String LOG_TAG = NewTaskActivity.class.getSimpleName();
    Utility utility = new Utility();

    // UI Elements
    EditText fieldTitle;
    CheckBox fieldShared;
    EditText fieldDescription;
    ImageView fieldIcon;

    LinearLayout fieldClassDropdown;
    TextView fieldClassTextview;
    LinearLayout fieldTypeDropdown;
    TextView fieldTypeTextview;

    TextView fieldTakePhoto;
    ImageView fieldPhotoSlot;
    LinearLayout fieldDueDate;
    TextView fieldDueDateTextView;
    TextView fieldAttachFile;
    LinearLayout fieldSetReminderDate;
    TextView fieldSetReminderDateTextview;
    LinearLayout fieldSetReminderTime;
    TextView fieldSetReminderTimeTextview;

    // UI Data
    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    String iconUriString;
    ArrayList<String> classTitleArray = new ArrayList<>();
    ArrayList<String> classTypeArray = new ArrayList<>();
    String classTitle = "None";
    String classType = "None";
    float dueDateMillis;

    long reminderDateMillis;
    float reminderTimeSeconds;

    static int REQUEST_NOTIFICATION_ALARM = 40;
    static int REQUEST_NOTIFICATION_INTENT = 41;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 42;

    String attachedFileUriString = "";

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
    boolean FLAG_EDIT = false;
    int editId = -1;
    static final int REQUEST_FILE_GET = 1;
    static final int REQUEST_IMAGE_GET_ICON = 2;
    static final int REQUEST_IMAGE_CAPTURE = 3;
    static final int REQUEST_IMAGE_GET_PHOTO = 4;
    Uri mCurrentPhotoPath;
    String mCurrentPhotoPathString;
    URI previousPhotoPath;
    boolean LAUNCHED_NEW_CLASS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        // Get references to the UI elements
        fieldTitle = (EditText) findViewById(R.id.field_new_task_title);
        fieldIcon = (ImageView) findViewById(R.id.field_new_task_icon);
        fieldClassDropdown = (LinearLayout) findViewById(R.id.field_class_dropdown);
        fieldClassTextview = (TextView) findViewById(R.id.field_class_textview);
        fieldTypeDropdown = (LinearLayout) findViewById(R.id.field_type_dropdown);
        fieldTypeTextview = (TextView) findViewById(R.id.field_type_textview);
        fieldDescription = (EditText) findViewById(R.id.field_new_task_description);
        fieldTakePhoto = (TextView) findViewById(R.id.field_new_task_photo);
        fieldPhotoSlot = (ImageView) findViewById(R.id.field_new_task_photo_slot);
        fieldDueDate = (LinearLayout) findViewById(R.id.field_new_task_duedate);
        fieldDueDateTextView = (TextView) findViewById(R.id.field_new_task_duedate_textview);
        fieldAttachFile = (TextView) findViewById(R.id.field_new_task_attach);
        fieldSetReminderDate = (LinearLayout) findViewById(R.id.field_new_task_reminder_date);
        fieldSetReminderDateTextview = (TextView) findViewById(R.id.field_new_task_reminder_date_textview);
        fieldSetReminderTime = (LinearLayout) findViewById(R.id.field_new_task_reminder_time);
        fieldSetReminderTimeTextview = (TextView) findViewById(R.id.field_new_task_reminder_time_textview);

        // Initialise the dropdown box default data
        classTitle = getString(R.string.none);
        classType = getString(R.string.none);

        // Set the listeners of the UI
        fieldIcon.setOnClickListener(showIconDialog());
        fieldClassDropdown.setOnClickListener(listener());
        fieldTypeDropdown.setOnClickListener(listener());
        fieldTakePhoto.setOnClickListener(listener());
        fieldAttachFile.setOnClickListener(listener());
        fieldSetReminderDate.setOnClickListener(listener());
        fieldSetReminderTime.setOnClickListener(listener());
        fieldDueDate.setOnClickListener(listener());
        fieldPhotoSlot.setOnClickListener(photoListener());


        // Initialise the class dropdown data
        DbHelper dbHelper = new DbHelper(this);
        Cursor scheduleCursor = dbHelper.getAllScheduleData();

        // Scan through the cursor and add in each class title into the array list
        if (scheduleCursor.moveToFirst()) {
            for (int i = 0; i < scheduleCursor.getCount(); i++) {
                String classTitle = scheduleCursor.getString(scheduleCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
                if (!classTitleArray.contains(classTitle))
                    classTitleArray.add(classTitle);
                scheduleCursor.moveToNext();
            }
        }


        // Initialise the classType dropdown data
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_homework));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_test));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_revision));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_project));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_detention));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_other));

        // Check if the activity was started by an edit action
        // If the intent is not null the activity must have
        // been started through an edit action
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                // Get the task data sent through the intent
                editId = extras.getInt(getString(R.string.TASKS_EXTRA_ID));
                Cursor cursor = dbHelper.getTaskById(editId);
                String title = "";
                String classTitle = "";
                String classType = "";
                String sharer = "";
                String description = "";
                String attachment = "";
                float dueDate = 0f;
                float reminderDate = 0f;
                float reminderTime = 0f;

                if (cursor.moveToFirst()){
                    title = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
                    classTitle = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS));
                    classType = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE));
                    sharer = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_SHARER));
                    description = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION));
                    attachment = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
                    dueDate = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE));
                    reminderDate = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE));
                    reminderTime = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME));
                    mCurrentPhotoPath = Uri.parse(cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_PICTURE)));
                    previousPhotoPath = URI.create(mCurrentPhotoPath.toString());
                }

                int position = extras.getInt("position");
                FLAG_EDIT = extras.getBoolean(getString(R.string.TASKS_FLAG_EDIT));

                if (FLAG_EDIT) {
                    Cursor cursorEdit = dbHelper.getUncompletedTaskData();
                    if (cursorEdit.moveToPosition(position)) {
                        iconUriString = cursorEdit.getString(cursorEdit.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON));
                        try {
                            Bitmap setImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(iconUriString));
                            fieldIcon.setImageBitmap(setImageBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Auto-fill the text fields with the intent data
                        fieldTitle.setText(title);
                        fieldDescription.setText(description);

                        // Set the current state of the dropdown text views
                        if (classTitle.equals(""))
                            fieldClassTextview.setText(getString(R.string.none));
                        else fieldClassTextview.setText(classTitle);
                        this.classTitle = classTitle;
                        if (classType.equals(""))
                            fieldTypeTextview.setText(getString(R.string.none));
                        else fieldTypeTextview.setText(classType);
                        this.classType = classType;

                        // Set the file name of the attach file field
                        attachment = cursorEdit.getString(cursorEdit.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
                        Uri filePathUri = Uri.parse(attachment);
                        if (!attachment.equals("")) {
                            Cursor returnCursor = getContentResolver().query(filePathUri, null, null, null, null);
                            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            if (returnCursor.moveToFirst()) {
                                String fileName = returnCursor.getString(nameIndex);
                                returnCursor.close();
                                fieldAttachFile.setText(fileName);
                                this.attachedFileUriString = filePathUri.toString();
                            }
                        }

                        // Set the saved image bitmap
                        Log.v(LOG_TAG, "Photo path: " + mCurrentPhotoPath.toString());
                        if (!mCurrentPhotoPath.toString().equals("")) {
                            try {
                                Bitmap setPhotoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCurrentPhotoPath);
                                fieldPhotoSlot.setImageBitmap(Bitmap.createScaledBitmap(setPhotoBitmap, 160, 160, false));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    cursorEdit.close();
                    fieldTitle.setSelection(fieldTitle.getText().length());

                } else {
                    // Set any default data
                    Resources resources = getResources();
                    int resId = R.drawable.art_class_64dp;
                    Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId)
                            + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId));
                    iconUriString = drawableUri.toString();
                }

                // Set the current state of the due date
                if (dueDate != 0f) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis((long) dueDate);
                    float dueDateYear = c.get(Calendar.YEAR);
                    float dueDateMonth = c.get(Calendar.MONTH);
                    float dueDateDay = c.get(Calendar.DAY_OF_MONTH);
                    fieldDueDateTextView.setText(utility.formatDateString(this, ((int) dueDateYear), ((int) dueDateMonth), ((int) dueDateDay)));
                    this.dueDateMillis = c.getTimeInMillis();
                }

                // Set the current state of the reminder date and time
                if (reminderDate != 0f) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis((long) reminderDate);
                    float reminderDateYear = c.get(Calendar.YEAR);
                    float reminderDateMonth = c.get(Calendar.MONTH);
                    float reminderDateDay = c.get(Calendar.DAY_OF_MONTH);

                    Calendar today = Calendar.getInstance();
                    if (today.get(Calendar.DAY_OF_MONTH) == reminderDateDay && today.get(Calendar.MONTH) == reminderDateMonth
                            && today.get(Calendar.YEAR) == reminderDateYear)
                        fieldSetReminderDateTextview.setText(getString(R.string.today));
                    else {
                        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH) + 1);
                        if (today.get(Calendar.DAY_OF_MONTH) == reminderDateDay && today.get(Calendar.MONTH) == reminderDateMonth
                                && today.get(Calendar.YEAR) == reminderDateYear)
                            fieldSetReminderDateTextview.setText(getString(R.string.tomorrow));
                        else fieldSetReminderDateTextview.setText(utility.formatDateString(this, ((int) reminderDateYear),
                                ((int) reminderDateMonth), ((int) reminderDateDay)));
                    }
                    this.reminderDateMillis = c.getTimeInMillis();
                }

                if (reminderTime != 0f) {
                    fieldSetReminderTimeTextview.setText(utility.millisToHourTime(reminderTime * 1000));
                    this.reminderTimeSeconds = reminderTime;
                }
            }


            // Set the default state of each field if the activity
            // was not started by an edit action
            else {
                // Initialise the due date to be set for the next day
                Calendar c = Calendar.getInstance();
                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH) + 1, 0, 0);
                dueDateMillis = c.getTimeInMillis();
                fieldDueDateTextView.setText(utility.formatDateString(NewTaskActivity.this, c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));

                // Initialise the reminder date and time
                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0);
                reminderDateMillis = c.getTimeInMillis();
                fieldSetReminderDateTextview.setText(utility.formatDateString(NewTaskActivity.this, c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
                c = Calendar.getInstance();
                reminderTimeSeconds = utility.timeToMillis(c.get(Calendar.HOUR_OF_DAY) + 1, 0);
                fieldSetReminderTimeTextview.setText(utility.millisToHourTime(reminderTimeSeconds));

                iconUriString = ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + getResources().getResourcePackageName(R.drawable.art_task_64dp)
                        + '/' + getResources().getResourceTypeName(R.drawable.art_task_64dp) + '/' + getResources().getResourceEntryName(R.drawable.art_task_64dp);
            }

        }

        scheduleCursor.close();
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
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent));

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
            fieldTitle.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
        }

        if (LAUNCHED_NEW_CLASS){
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor = dbHelper.getAllScheduleData();
            if (cursor.moveToLast()) {
                String newClassTitle = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
                fieldClassTextview.setText(newClassTitle);
                classTitle = newClassTitle;
                LAUNCHED_NEW_CLASS = false;
            }

            // Auto-fill the title editText if there isn't any user-inputted title yet
            String titleText = fieldTitle.getText().toString();
            if (titleText.equals(""))
                if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                    fieldTitle.setText("");
                else fieldTitle.setText(NewTaskActivity.this.classTitle);
            if (titleText.equals(classType) && NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                fieldTitle.setText(classType);
                // Check if another class was set before
            else if (classTitleArray.contains(titleText))
                if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                    fieldTitle.setText("");
                else fieldTitle.setText(NewTaskActivity.this.classTitle);
                // Check if the classType was set before the class
            else if (classTypeArray.contains(titleText))
                fieldTitle.setText(NewTaskActivity.this.classTitle + " " + titleText);
                // Check if the title editText contains text as a result
                // of previously using the dropdown lists
            else {
                String[] splitFieldTitle = titleText.split(" ");
                if (splitFieldTitle.length == 2 && classTitleArray.contains(splitFieldTitle[0]) && classTypeArray.contains(splitFieldTitle[1])) {
                    if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                        fieldTitle.setText(splitFieldTitle[1]);
                    else
                        fieldTitle.setText(NewTaskActivity.this.classTitle + " " + splitFieldTitle[1]);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

            // Insert inputted data into the database and terminate the activity
            case R.id.action_done:
                // Validate that the title field is not empty
                if (fieldTitle.getText().toString().equals("")) {
                    Toast.makeText(NewTaskActivity.this, getString(R.string.new_tasks_toast_validation_title_not_found), Toast.LENGTH_SHORT).show();
                    return false;
                }
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS), getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS));
                if (insertTaskData())
                    startActivity(intent);
                else {
                    Log.w(LOG_TAG, "Error creating new task");
                    finish();
                }
                break;
        }

        return true;
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.field_class_dropdown:
                        showClassDropdownMenu();
                        break;
                    case R.id.field_type_dropdown:
                        showTypeDropdownMenu();
                        break;
                    case R.id.field_new_task_duedate:
                        Calendar c_duedate = Calendar.getInstance();
                        Date date_duedate = new Date();
                        c_duedate.setTime(date_duedate);
                        int year_duedate = c_duedate.get(Calendar.YEAR);
                        int month_duedate = c_duedate.get(Calendar.MONTH);
                        int day_duedate = c_duedate.get(Calendar.DAY_OF_MONTH) + 1;
                        DatePickerDialog datePickerDialog_duedate = new DatePickerDialog(NewTaskActivity.this, dueDateSetListener(), year_duedate, month_duedate, day_duedate);
                        datePickerDialog_duedate.show();
                        break;
                    case R.id.field_new_task_attach:
                        Intent attach_intent = new Intent(Intent.ACTION_PICK);
                        attach_intent.setType("*/*");
                        attach_intent.setAction(Intent.ACTION_GET_CONTENT);
                        attach_intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(attach_intent, REQUEST_FILE_GET);
                        break;
                    case R.id.field_new_task_photo:
                        // Request all permissions (for API 23+)
                        int permissionCheck = ContextCompat.checkSelfPermission(NewTaskActivity.this,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(NewTaskActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                //Show permission explanation dialog...
                                new AlertDialog.Builder(NewTaskActivity.this)
                                        .setMessage(getString(R.string.dialog_permission_rationale_take_photo))
                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ActivityCompat.requestPermissions(NewTaskActivity.this,
                                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW},
                                                        REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                                            }
                                        }).show();
                            }
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(NewTaskActivity.this);
                        builder.setTitle(getString(R.string.field_new_photo_dialog_title))
                                .setItems(R.array.field_new_photo_dialog_items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                try {
                                                    dispatchTakePictureIntent();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case 1:
                                                dispatchSelectPhotoIntent();
                                                break;
                                        }
                                    }
                                }).show();
                        break;
                    case R.id.field_new_task_reminder_date:
                        showReminderDateDropdownMenu();
                        break;
                    case R.id.field_new_task_reminder_time:
                        int hour = (int) reminderTimeSeconds / 3600;
                        int minute = (int) (reminderTimeSeconds - (hour * 3600)) / 60;
                        TimePickerDialog timePickerFragment = new TimePickerDialog(NewTaskActivity.this, onTimeSetListener(), hour, minute, true);
                        timePickerFragment.show();
                        break;
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewTaskActivity.this);
                    builder.setTitle(getString(R.string.field_new_photo_dialog_title))
                            .setItems(R.array.field_new_photo_dialog_items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            try {
                                                dispatchTakePictureIntent();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                        case 1:
                                            dispatchSelectPhotoIntent();
                                            break;
                                    }
                                }
                            }).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private boolean insertTaskData() {
        // Get the inputted text from the title and description fields
        // as well as the iconResource and database
        String title = fieldTitle.getText().toString();
        String description = fieldDescription.getText().toString();
        String icon = attachedFileUriString;

        if (classTitle.equals(getString(R.string.none)))
            classTitle = "";
        if (classType.equals(getString(R.string.none)))
            classType = "";

        DbHelper dbHelper = new DbHelper(this);

        // If the activity was launched through an edit action
        // Update the database row
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(reminderDateMillis);

        // Set the alarm for the notification
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(reminderDateMillis);
        int hour = (int) reminderTimeSeconds / 3600;
        int minute = (int) (reminderTimeSeconds - hour * 3600) / 60;
        cc.set(Calendar.HOUR_OF_DAY, hour);
        cc.set(Calendar.MINUTE, minute);
        long notificationMillis = (cc.getTimeInMillis());

        if (FLAG_EDIT) {
            // Delete previous picture if it exists
            if (!previousPhotoPath.toString().equals("")) {
                File file = new File(previousPhotoPath);
                file.delete();
            }

            Log.d(LOG_TAG, "Inserting photo path " + mCurrentPhotoPath.toString());
            Remind(new Date(notificationMillis), title, getString(R.string.notification_message_reminder), editId, iconUriString);

            if (dbHelper.updateTaskItem(editId, title, classTitle, classType, "", description, attachedFileUriString,
                    dueDateMillis, reminderDateMillis, reminderTimeSeconds, iconUriString, mCurrentPhotoPath.toString(), false)) {
                return true;
            }

            else
                Toast.makeText(NewTaskActivity.this, "Error editing task", Toast.LENGTH_SHORT).show();
        }
        // Else, insert a new database row
        else {
            // First save the taken picture into the storage
            if (mCurrentPhotoPath != null)
                saveFile(mCurrentPhotoPath);
            else mCurrentPhotoPath = Uri.parse("");

            if (dbHelper.insertTask(title, classTitle, classType, "", description, attachedFileUriString,
                    dueDateMillis, reminderDateMillis, reminderTimeSeconds, iconUriString, mCurrentPhotoPath.toString(), false)) {
                Cursor cursor = dbHelper.getUncompletedTaskData();

                if (cursor.moveToLast()) {
                    int ID = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID));
                    Remind(new Date(notificationMillis), title, getString(R.string.notification_message_reminder), ID, iconUriString);
                }

                return true;
            }

            else
                Toast.makeText(NewTaskActivity.this, "Error creating new task", Toast.LENGTH_SHORT).show();
            Log.w(LOG_TAG, "Error creating new task");
        }

        return false;
    }

    void saveFile(Uri sourceuri)
    {
        String sourceFilename= sourceuri.getPath();
        String destinationFilename = android.os.Environment.getExternalStorageDirectory().getPath()+File.separatorChar+"abc.mp3";

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {

        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {

            }
        }
    }

    public void Remind(Date dateTime, String title, String message, int ID, String iconUriString) {
        scheduleNotification(dateTime, ID, title, message);
    }

    private void scheduleNotification(final Date dateTime, final int ID, final String title, final String message) {

        final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Bitmap largeIcon = null;
        try {
            largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(iconUriString));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setBackground(largeIcon);

        Intent contentIntent = new Intent(this, TasksDetailActivity.class);
        contentIntent.putExtra(getString(R.string.KEY_TASKS_EXTRA_ID), ID);
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

                Intent notificationIntent = new Intent(NewTaskActivity.this, TaskNotificationPublisher.class);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(NewTaskActivity.this, REQUEST_NOTIFICATION_ALARM, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//                long futureInMillis = SystemClock.elapsedRealtime() + delay;
                AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                Log.v(LOG_TAG, "DateTime: " + dateTime.getDay()+"/"+dateTime.getMonth()+"/"+dateTime.getYear()
                +" "+dateTime.getHours()+":"+dateTime.getMinutes()+" "+dateTime.getSeconds());
                alarmManager.set(AlarmManager.RTC, dateTime.getTime(), pendingIntent);
            }
        });
    }

    private void showClassDropdownMenu() {
        // Set up the dropdown menu on both views
        // Set up the class dropdown menu
        PopupMenu popupMenu = new PopupMenu(this, fieldClassDropdown);

        // Add the titles to the menu as well as the item to add a new class
        popupMenu.getMenu().add(getString(R.string.none));
        for (int i = 0; i < classTitleArray.size(); i++)
            popupMenu.getMenu().add(classTitleArray.get(i));
        popupMenu.getMenu().add(getString(R.string.field_dropdown_class_menu_item_new_class));

        // Set the listener for the menu items
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Set the data to be later saved into the database
                NewTaskActivity.this.classTitle = item.getTitle().toString();

                // If Add New Class was selected, start NewScheduleActivity
                if (NewTaskActivity.this.classTitle.equals(getString(R.string.field_dropdown_class_menu_item_new_class))){
                    LAUNCHED_NEW_CLASS = true;
                    Intent intent = new Intent(NewTaskActivity.this, NewScheduleActivity.class);
                    intent.putExtra("STARTED_BY_NEWTASKACTIVITY", true);
                    startActivity(intent);
                    return true;
                }

                // Auto-fill the title editText if there isn't any user-inputted title yet
                String titleText = fieldTitle.getText().toString();
                if (titleText.equals(""))
                    if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                        fieldTitle.setText("");
                    else fieldTitle.setText(NewTaskActivity.this.classTitle);
                if (titleText.equals(classType) && NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                    fieldTitle.setText(classType);
                    // Check if another class was set before
                else if (classTitleArray.contains(titleText))
                    if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                        fieldTitle.setText("");
                    else fieldTitle.setText(NewTaskActivity.this.classTitle);
                    // Check if the classType was set before the class
                else if (classTypeArray.contains(titleText))
                    fieldTitle.setText(NewTaskActivity.this.classTitle + " " + titleText);
                    // Check if the title editText contains text as a result
                    // of previously using the dropdown lists
                else {
                    String[] splitFieldTitle = titleText.split(" ");
                    if (splitFieldTitle.length == 2 && classTitleArray.contains(splitFieldTitle[0]) && classTypeArray.contains(splitFieldTitle[1])) {
                        if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                            fieldTitle.setText(splitFieldTitle[1]);
                        else
                            fieldTitle.setText(NewTaskActivity.this.classTitle + " " + splitFieldTitle[1]);
                    }
                }
                // Set the dropdown list text to the selected item
                fieldClassTextview.setText(NewTaskActivity.this.classTitle);

                return true;
            }
        });

        popupMenu.show();
    }

    private void showTypeDropdownMenu() {
        // Initialise and inflate the menu
        PopupMenu popupMenu = new PopupMenu(NewTaskActivity.this, fieldTypeDropdown);
        popupMenu.getMenuInflater().inflate(R.menu.menu_popup_type, popupMenu.getMenu());

        // Set the menu's listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Set the data to be later saved into the database
                NewTaskActivity.this.classType = item.getTitle().toString();

                // Auto-fill the title editText if there isn't any user-inputted title yet
                String titleText = fieldTitle.getText().toString();
                if (titleText.equals("") && !NewTaskActivity.this.classType.equals(getString(R.string.none)))
                    fieldTitle.setText(NewTaskActivity.this.classType);
                if (titleText.contains(classTitle) && NewTaskActivity.this.classType.equals(getString(R.string.none)))
                    fieldTitle.setText(classTitle);
                    // Check if another classType was set before
                else if (classTypeArray.contains(titleText))
                    if (NewTaskActivity.this.classType.equals(getString(R.string.none)))
                        fieldTitle.setText("");
                    else fieldTitle.setText(NewTaskActivity.this.classType);
                    // Check if the classType was set before the class
                else if (classTitleArray.contains(titleText)) {
                    fieldTitle.setText(titleText + " " + NewTaskActivity.this.classType);
                }
                // Check if the title editText contains text as a result
                // of previously using the dropdown lists
                else {
                    String[] splitFieldTitle = titleText.split(" ");
                    if (splitFieldTitle.length == 2 && classTitleArray.contains(splitFieldTitle[0]) && classTypeArray.contains(splitFieldTitle[1])) {
                        if (NewTaskActivity.this.classType.equals(getString(R.string.none)))
                            fieldTitle.setText(splitFieldTitle[0]);
                        else
                            fieldTitle.setText(splitFieldTitle[0] + " " + NewTaskActivity.this.classType);
                    }
                }

                // Set the dropdown list text to the selected item
                fieldTypeTextview.setText(NewTaskActivity.this.classType);

                return true;
            }
        });

        popupMenu.show();
    }

    private void showReminderDateDropdownMenu() {
        PopupMenu popupMenu = new PopupMenu(this, fieldSetReminderDate);
        popupMenu.getMenuInflater().inflate(R.menu.popup_reminder_date, popupMenu.getMenu());
        final Calendar c = Calendar.getInstance();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.dropdown_reminder_date_none:
                        fieldSetReminderDateTextview.setText(getString(R.string.none));
                        reminderDateMillis = 0;
                        fieldSetReminderTime.setEnabled(false);
                        break;
                    case R.id.dropdown_reminder_date_today:
                        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                        fieldSetReminderDateTextview.setText(getString(R.string.today));
                        reminderDateMillis = c.getTimeInMillis();
                        fieldSetReminderTime.setEnabled(true);
                        break;
                    case R.id.dropdown_reminder_date_tomorrow:
                        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH) + 1);
                        fieldSetReminderDateTextview.setText(getString(R.string.tomorrow));
                        reminderDateMillis = c.getTimeInMillis();
                        fieldSetReminderTime.setEnabled(true);
                        break;
                    case R.id.dropdown_reminder_date_setdate:
                        fieldSetReminderTime.setEnabled(true);
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH) + 1;
                        DatePickerDialog datePickerDialog =
                                new DatePickerDialog(NewTaskActivity.this, reminderDateSetListener(),
                                        year, month, day);
                        datePickerDialog.show();
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    // This method is called when a date for the due date is set
    private DatePickerDialog.OnDateSetListener dueDateSetListener() {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth);
                dueDateMillis = c.getTimeInMillis();
                fieldDueDateTextView.setText(utility.formatDateString(NewTaskActivity.this, year, monthOfYear, dayOfMonth));
            }
        };
    }

    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.pdt.plume.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                mCurrentPhotoPathString = photoURI.toString();
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchSelectPhotoIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, REQUEST_IMAGE_GET_PHOTO);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    // This method is called when a file is selected after the ACTION_GET
    // intent was called from the attach file action
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Attachment Upload
        if (requestCode == REQUEST_FILE_GET && resultCode == RESULT_OK) {
            // Get the Uri and UriString from the intent and save its global variable
            Uri filePathUri = data.getData();
            attachedFileUriString = data.getDataString();

            // Get the filename of the file and set the field's text to that
            Cursor returnCursor = getContentResolver().query(filePathUri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String fileName = returnCursor.getString(nameIndex);
            returnCursor.close();
            fieldAttachFile.setText(fileName);
        }

        // Take Photo
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Set the thumbnail and set the member variable for the Uri
            mCurrentPhotoPath = Uri.parse(mCurrentPhotoPathString);
            try {
                Bitmap thumbnail = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCurrentPhotoPath);
                int scale = (int) getResources().getDisplayMetrics().density;
                fieldPhotoSlot.setImageBitmap(Bitmap.createScaledBitmap(thumbnail, 64 * scale, 64 * scale, false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Photo Upload
        if (requestCode == REQUEST_IMAGE_GET_PHOTO && resultCode == RESULT_OK) {
            Uri dataUri = data.getData();
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dataUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mCurrentPhotoPath = dataUri;
            mCurrentPhotoPathString = mCurrentPhotoPath.toString();
            int scale = (int) getResources().getDisplayMetrics().density;
            fieldPhotoSlot.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 64 * scale, 64 * scale, false));
        }

        // Custom Icon Upload
        if (requestCode == REQUEST_IMAGE_GET_ICON && resultCode == RESULT_OK) {
            Uri dataUri = data.getData();
            Bitmap setImageBitmap = null;

            try {
                setImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dataUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            fieldIcon.setImageBitmap(setImageBitmap);
        }
    }

    // This method is called when a date for the reminding notification is set
    private DatePickerDialog.OnDateSetListener reminderDateSetListener() {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth);
                reminderDateMillis = c.getTimeInMillis();
                Calendar currentDate = Calendar.getInstance();
                if (currentDate.getTimeInMillis() == reminderDateMillis)
                    fieldSetReminderDateTextview.setText(getString(R.string.today));
                else {
                    int currentYear = currentDate.get(Calendar.YEAR);
                    int currentMonth = currentDate.get(Calendar.MONTH);
                    int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);
                    currentDate.set(currentYear, currentMonth, currentDay + 1);
                    if (currentDate.getTimeInMillis() == reminderDateMillis)
                        fieldSetReminderDateTextview.setText(getString(R.string.tomorrow));
                    else
                        fieldSetReminderDateTextview.setText(utility.formatDateString(NewTaskActivity.this, year, monthOfYear, dayOfMonth));
                }
            }
        };
    }

    public TimePickerDialog.OnTimeSetListener onTimeSetListener() {
        return new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                reminderTimeSeconds = utility.timeToMillis(hourOfDay, minute) / 1000;
                fieldSetReminderTimeTextview.setText(utility.millisToHourTime(reminderTimeSeconds * 1000));
            }
        };
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

    private View.OnClickListener photoListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewTaskActivity.this);
                builder.setTitle(getString(R.string.field_existing_photo_dialog_title))
                        .setItems(R.array.field_existing_photo_dialog_items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        try {
                                            dispatchTakePictureIntent();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 1:
                                        dispatchSelectPhotoIntent();
                                        break;
                                    case 2:
                                        fieldPhotoSlot.setImageBitmap(null);
                                        mCurrentPhotoPath = null;
                                        mCurrentPhotoPathString = "";
                                        break;
                                }
                            }
                        }).show();
            }
        };
    }

    private void showBuiltInIconsDialog() {
        // Prepare grid view
        GridView gridView = new GridView(this);
        final AlertDialog dialog;

        int[] builtinIcons = getResources().getIntArray(R.array.builtin_icons);
        List<Integer> mList = new ArrayList<>();
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
                        + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId));
                iconUriString = drawableUri.toString();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void OnIconListItemSelected(int item) {
        switch (item) {
            case 0:
                showBuiltInIconsDialog();
                break;
            case 1:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(intent, REQUEST_IMAGE_GET_ICON);
                break;
        }
    }


}
