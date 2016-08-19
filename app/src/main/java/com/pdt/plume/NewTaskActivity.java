package com.pdt.plume;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;

public class NewTaskActivity extends AppCompatActivity {
    // Constantly used variables
    String LOG_TAG = NewTaskActivity.class.getSimpleName();

    // UI Elements
    EditText fieldTitle;
    CheckBox fieldShared;
    EditText fieldDescription;
    int iconResource = R.drawable.placeholder_sixtyfour;

    LinearLayout fieldClassDropdown;
    TextView fieldClassTextview;
    LinearLayout fieldTypeDropdown;
    TextView fieldTypeTextview;

    // UI Data
    ArrayList<String> classTitleArray = new ArrayList<>();
    ArrayList<String> classTypeArray = new ArrayList<>();
    String classTitle;
    String type;

    // Intent Data
    boolean FLAG_EDIT = false;
    int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        // Get references to the UI elements
        fieldTitle = (EditText) findViewById(R.id.field_new_task_title);
        fieldDescription = (EditText) findViewById(R.id.field_new_task_description);
        fieldClassDropdown = (LinearLayout) findViewById(R.id.field_class_dropdown);
        fieldClassTextview = (TextView) findViewById(R.id.field_class_textview);
        fieldTypeDropdown = (LinearLayout) findViewById(R.id.field_type_dropdown);
        fieldTypeTextview = (TextView) findViewById(R.id.field_type_textview);

        // Initialise the dropdown box default data
        classTitle = getString(R.string.none);
        type = getString(R.string.none);

        // Set the listeners of the UI
        fieldClassDropdown.setOnClickListener(listener());
        fieldTypeDropdown.setOnClickListener(listener());

        // Initialise the class dropdown data
        DbHelper dbHelper = new DbHelper(this);
        Cursor cursor = dbHelper.getAllScheduleData();
        // Scan through the cursor and add in each class title into the array list
        if (cursor.moveToFirst()){
            for (int i = 0; i < cursor.getCount(); i++){
                String classTitle = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
                if (!classTitleArray.contains(classTitle))
                    classTitleArray.add(classTitle);
            }
        }
        cursor.close();

        // Initialise the type dropdown data
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_homework));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_test));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_revision));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_project));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_other));

        // Check if the activity was started by an edit action
        // If the intent is not null the activity must have
        // been started through an edit action
        Intent intent = getIntent();
        if (intent != null){
            Bundle extras = intent.getExtras();
            if (extras != null){
                // Get the task data sent through the intent
                editId = extras.getInt(getResources().getString(R.string.TASKS_EXTRA_ID));
                String title = extras.getString(getResources().getString(R.string.TASKS_EXTRA_TITLE));
                String sharer = extras.getString(getResources().getString(R.string.TASKS_EXTRA_SHARER));
                String description = extras.getString(getResources().getString(R.string.TASKS_EXTRA_DESCRIPTION));
                String attachment = extras.getString(getResources().getString(R.string.TASKS_EXTRA_ATTACHMENT));
                float dueDate = extras.getFloat(getResources().getString(R.string.TASKS_EXTRA_DUEDATE));
                float alarmTime = extras.getFloat(getResources().getString(R.string.TASKS_EXTRA_ALARMTIME));
                FLAG_EDIT = extras.getBoolean(getResources().getString(R.string.TASKS_FLAG_EDIT));

                // Auto-fill the text fields with the intent data
                fieldTitle.setText(title);
                fieldDescription.setText(description);
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

    private boolean insertTaskData(){
        // Get the inputted text from the title and description fields
        // as well as the iconResource and database
        String title = fieldTitle.getText().toString();
        String description = fieldDescription.getText().toString();
        int icon = iconResource;
        DbHelper dbHelper = new DbHelper(this);

        // If the activity was launched through an edit action
        // Update the database row
        if (FLAG_EDIT){
            if (dbHelper.updateTaskItem(editId, title, classTitle, type, "", description, "", 0f, 0f, icon)){
                return true;
            } else Toast.makeText(NewTaskActivity.this, "Error editing task", Toast.LENGTH_SHORT).show();
        }
        // Else, insert a new database row
        else {
            if (dbHelper.insertTask(title, classTitle, type, "", description, "", 0, 0, icon)){
                return true;
            } else Toast.makeText(NewTaskActivity.this, "Error creating new task", Toast.LENGTH_SHORT).show();
            Log.v(LOG_TAG, "Error creating new task");
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
                    fieldTitle.setText(NewTaskActivity.this.classTitle);
                // Check if another class was set before
                else if (classTitleArray.contains(titleText))
                    if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                        fieldTitle.setText("");
                    else fieldTitle.setText(NewTaskActivity.this.classTitle);
                // Check if the type was set before the class
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
                NewTaskActivity.this.type = item.getTitle().toString();

                // Auto-fill the title editText if there isn't any user-inputted title yet
                String titleText = fieldTitle.getText().toString();
                if (titleText.equals(""))
                    fieldTitle.setText(NewTaskActivity.this.type);
                // Check if another type was set before
                else if (classTypeArray.contains(titleText))
                    if (NewTaskActivity.this.type.equals(getString(R.string.none)))
                        fieldTitle.setText("");
                    else fieldTitle.setText(NewTaskActivity.this.type);
                // Check if the type was set before the class
                else if (classTitleArray.contains(titleText)){
                    fieldTitle.setText(titleText + " " + NewTaskActivity.this.type);
                }
                // Check if the title editText contains text as a result
                // of previously using the dropdown lists
                else {
                    String[] splitFieldTitle = titleText.split(" ");
                    if (splitFieldTitle.length == 2 && classTitleArray.contains(splitFieldTitle[0]) && classTypeArray.contains(splitFieldTitle[1])){
                        if (NewTaskActivity.this.type.equals(getString(R.string.none)))
                            fieldTitle.setText(splitFieldTitle[0]);
                        else fieldTitle.setText(splitFieldTitle[0] + " " + NewTaskActivity.this.type);
                    }
                }

                // Set the dropdown list text to the selected item
                fieldTypeTextview.setText(NewTaskActivity.this.type);

                return true;
            }
        });

        popupMenu.show();
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
                }
            }
        };
    }
}
