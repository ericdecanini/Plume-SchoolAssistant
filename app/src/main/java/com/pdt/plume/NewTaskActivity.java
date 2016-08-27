package com.pdt.plume;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NewTaskActivity extends AppCompatActivity
        implements TimePickerDialog.OnTimeSetListener,
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

    LinearLayout fieldDueDate;
    TextView fieldDueDateTextView;
    TextView fieldAttachFile;
    LinearLayout fieldSetReminderDate;
    TextView fieldSetReminderDateTextview;
    LinearLayout fieldSetReminderTime;
    TextView fieldSetReminderTimeTextview;

    // UI Data
    String scheduleIconUriString;
    ArrayList<String> classTitleArray = new ArrayList<>();
    ArrayList<String> classTypeArray = new ArrayList<>();
    String classTitle = "None";
    String classType = "None";
    float dueDateMillis;

    long reminderDateMillis;
    float reminderTimeSeconds;

    String attachedFileUriString = "";

    private Integer[] mThumbIds = {
            R.drawable.art_business_64dp,
            R.drawable.art_childdevelopment_64dp,
            R.drawable.art_french_64dp,
            R.drawable.art_geography_64dp,
            R.drawable.art_ict_64dp,
            R.drawable.art_maths_64dp,
            R.drawable.art_spanish_64dp
    };


    // Intent Data
    boolean FLAG_EDIT = false;
    int editId = -1;
    static final int REQUEST_FILE_GET = 1;
    static final int REQUEST_IMAGE_GET = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        // Get references to the UI elements
        fieldTitle = (EditText) findViewById(R.id.field_new_task_title);
        fieldIcon = (ImageView) findViewById(R.id.field_new_task_icon);
        fieldDescription = (EditText) findViewById(R.id.field_new_task_description);
        fieldClassDropdown = (LinearLayout) findViewById(R.id.field_class_dropdown);
        fieldClassTextview = (TextView) findViewById(R.id.field_class_textview);
        fieldTypeDropdown = (LinearLayout) findViewById(R.id.field_type_dropdown);
        fieldTypeTextview = (TextView) findViewById(R.id.field_type_textview);
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
        fieldAttachFile.setOnClickListener(listener());
        fieldSetReminderDate.setOnClickListener(listener());
        fieldSetReminderTime.setOnClickListener(listener());
        fieldDueDate.setOnClickListener(listener());


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
                String title = extras.getString(getString(R.string.TASKS_EXTRA_TITLE));
                String classTitle = extras.getString(getString(R.string.TASKS_EXTRA_CLASS));
                String classType = extras.getString(getString(R.string.TASKS_EXTRA_TYPE));
                String sharer = extras.getString(getString(R.string.TASKS_EXTRA_SHARER));
                String description = extras.getString(getString(R.string.TASKS_EXTRA_DESCRIPTION));
                String attachment = extras.getString(getString(R.string.TASKS_EXTRA_ATTACHMENT));
                float dueDate = extras.getFloat(getString(R.string.TASKS_EXTRA_DUEDATE));
                float reminderDate = extras.getFloat(getString(R.string.TASKS_EXTRA_REMINDERDATE));
                float reminderTime = extras.getFloat(getString(R.string.TASKS_EXTRA_REMINDERTIME));
                int position = extras.getInt("position");
                FLAG_EDIT = extras.getBoolean(getString(R.string.TASKS_FLAG_EDIT));

                if (FLAG_EDIT){
                    Cursor cursor = dbHelper.getTaskData();
                    if (cursor.moveToPosition(position)){
                        scheduleIconUriString = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON));
                        Bitmap setImageBitmap = null;
                        try {
                            setImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(scheduleIconUriString));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        fieldIcon.setImageBitmap(setImageBitmap);

                        // Auto-fill the text fields with the intent data
                        fieldTitle.setText(title);
                        fieldDescription.setText(description);

                        // Set the current state of the dropdown text views
                        fieldClassTextview.setText(classTitle);
                        this.classTitle = classTitle;
                        fieldTypeTextview.setText(classType);
                        this.classType = classType;

                        // Set the file name of the attach file field
                        attachment = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
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
                    }
                } else {
                    // Set any default data
                    Resources resources = getResources();
                    int resId = R.drawable.art_class;
                    Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId)
                            + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
                    scheduleIconUriString = drawableUri.toString();
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
                    fieldSetReminderDateTextview.setText(utility.formatDateString(this, ((int) reminderDateYear), ((int) reminderDateMonth), ((int) reminderDateDay)));
                    this.reminderDateMillis = c.getTimeInMillis();
                }

                if (reminderTime != 0f){
                    fieldSetReminderTimeTextview.setText(utility.secondsToTime(reminderTime));
                    this.reminderTimeSeconds = reminderTime;
                }
            }


            // Set the default state of each field if the activity
            // was not started by an edit action
            else {
                // Initialise the due date to be set for the next day
                Calendar c = Calendar.getInstance();
                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0);
                dueDateMillis = c.getTimeInMillis();
                fieldDueDateTextView.setText(utility.formatDateString(NewTaskActivity.this, c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH) + 1));

                // Initialise the reminder date and time
                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0);
                reminderDateMillis = c.getTimeInMillis();
                fieldSetReminderDateTextview.setText(utility.formatDateString(NewTaskActivity.this, c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
                c = Calendar.getInstance();
                reminderTimeSeconds = utility.timeToSeconds(c.get(Calendar.HOUR_OF_DAY) + 1, 0);
                fieldSetReminderTimeTextview.setText(utility.secondsToTime(reminderTimeSeconds));
            }

        }

        scheduleCursor.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            // Without this, the up button will not do anything and return the error 'Cancelling event due to no window focus'
            case android.R.id.home:
                finish();
                break;

            // Insert inputted data into the database and terminate the activity
            case R.id.action_done:
                // Validate that the title field is not empty
                if (fieldTitle.getText().toString().equals("")){
                    Toast.makeText(NewTaskActivity.this, getString(R.string.new_tasks_toast_validation_title_not_found), Toast.LENGTH_SHORT).show();
                    return false;
                }
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS), getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS));
                if (insertTaskData())
                    startActivity(intent);
                else {
                    Log.v(LOG_TAG, "Error creating new task");
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
                switch (v.getId()){
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
                        int day_duedate = c_duedate.get(Calendar.DAY_OF_MONTH);
                        DatePickerDialog datePickerDialog_duedate = new DatePickerDialog(NewTaskActivity.this, dueDateSetListener(), year_duedate, month_duedate, day_duedate);
                        datePickerDialog_duedate.show();
                        break;
                    case R.id.field_new_task_attach:
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("*/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, REQUEST_FILE_GET);
                        break;
                    case R.id.field_new_task_reminder_date:
                        showReminderDateDropdownMenu();
                        break;
                    case R.id.field_new_task_reminder_time:
                        DialogFragment timePickerFragment = new TimePickerFragment();
                        timePickerFragment.show(getSupportFragmentManager(), "time picker");
                        break;
                }
            }
        };
    }

    private boolean insertTaskData(){
        // Get the inputted text from the title and description fields
        // as well as the iconResource and database
        String title = fieldTitle.getText().toString();
        String description = fieldDescription.getText().toString();
        String icon = attachedFileUriString;
        DbHelper dbHelper = new DbHelper(this);

        // If the activity was launched through an edit action
        // Update the database row
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(reminderDateMillis);

        if (FLAG_EDIT){
            if (dbHelper.updateTaskItem(editId, title, classTitle, classType, "", description, attachedFileUriString,
                    dueDateMillis, reminderDateMillis, reminderTimeSeconds, scheduleIconUriString)){
                return true;
            } else Toast.makeText(NewTaskActivity.this, "Error editing task", Toast.LENGTH_SHORT).show();
        }
        // Else, insert a new database row
        else {
            if (dbHelper.insertTask(title, classTitle, classType, "", description, attachedFileUriString,
                    dueDateMillis, reminderDateMillis, reminderTimeSeconds, scheduleIconUriString)){
                return true;
            } else Toast.makeText(NewTaskActivity.this, "Error creating new task", Toast.LENGTH_SHORT).show();
            Log.w(LOG_TAG, "Error creating new task");
        }

        return false;
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
                    if (splitFieldTitle.length == 2 && classTitleArray.contains(splitFieldTitle[0]) && classTypeArray.contains(splitFieldTitle[1])){
                        if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                            fieldTitle.setText(splitFieldTitle[1]);
                        else fieldTitle.setText(NewTaskActivity.this.classTitle + " " + splitFieldTitle[1]);
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
                else if (classTitleArray.contains(titleText)){
                    fieldTitle.setText(titleText + " " + NewTaskActivity.this.classType);
                }
                // Check if the title editText contains text as a result
                // of previously using the dropdown lists
                else {
                    String[] splitFieldTitle = titleText.split(" ");
                    if (splitFieldTitle.length == 2 && classTitleArray.contains(splitFieldTitle[0]) && classTypeArray.contains(splitFieldTitle[1])){
                        if (NewTaskActivity.this.classType.equals(getString(R.string.none)))
                            fieldTitle.setText(splitFieldTitle[0]);
                        else fieldTitle.setText(splitFieldTitle[0] + " " + NewTaskActivity.this.classType);
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
                switch (item.getItemId()){
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
                        int day = c.get(Calendar.DAY_OF_MONTH);
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

    // This method is called when a file is selected after the ACTION_GET
    // intent was called from the attach file action
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILE_GET && resultCode == RESULT_OK){
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

    // This method is called when a time for the reminding notification is set
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        reminderTimeSeconds = utility.timeToSeconds(hourOfDay, minute);
        fieldSetReminderTimeTextview.setText(utility.secondsToTime(reminderTimeSeconds));
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
                        + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
                scheduleIconUriString = drawableUri.toString();
                dialog.dismiss();
            }
        });
        dialog.show();
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



}
