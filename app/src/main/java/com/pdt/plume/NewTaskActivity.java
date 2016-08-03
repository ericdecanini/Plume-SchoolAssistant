package com.pdt.plume;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.pdt.plume.data.DbHelper;

public class NewTaskActivity extends AppCompatActivity {
    String LOG_TAG = NewTaskActivity.class.getSimpleName();

    EditText fieldTitle;
    CheckBox fieldShared;
    EditText fieldDescription;

    boolean FLAG_EDIT = false;
    int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        fieldTitle = (EditText) findViewById(R.id.field_new_task_title);
        fieldDescription = (EditText) findViewById(R.id.field_new_task_description);

        Intent intent = getIntent();
        if (intent != null){
            Bundle extras = intent.getExtras();
            if (extras != null){
                editId = extras.getInt(getResources().getString(R.string.TASKS_EXTRA_ID));
                String title = extras.getString(getResources().getString(R.string.TASKS_EXTRA_TITLE));
                String sharer = extras.getString(getResources().getString(R.string.TASKS_EXTRA_SHARER));
                String description = extras.getString(getResources().getString(R.string.TASKS_EXTRA_DESCRIPTION));
                String attachment = extras.getString(getResources().getString(R.string.TASKS_EXTRA_ATTACHMENT));
                float dueDate = extras.getFloat(getResources().getString(R.string.TASKS_EXTRA_DUEDATE));
                float alarmTime = extras.getFloat(getResources().getString(R.string.TASKS_EXTRA_ALARMTIME));
                FLAG_EDIT = extras.getBoolean(getResources().getString(R.string.TASKS_FLAG_EDIT));

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
            case android.R.id.home:
                finish();
                break;
            case R.id.action_done:
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
        String title = fieldTitle.getText().toString();
        String description = fieldDescription.getText().toString();
        int icon = R.drawable.placeholder_sixtyfour;

        DbHelper dbHelper = new DbHelper(this);
        if (FLAG_EDIT){
            if (dbHelper.updateTaskItem(editId, title, "", description, "", 0f, 0f, icon)){
                return true;
            } else Toast.makeText(NewTaskActivity.this, "Error editing task", Toast.LENGTH_SHORT).show();
        } else {
            if (dbHelper.insertTask(title, "", description, "", 0, 0, icon)){
                return true;
            } else Toast.makeText(NewTaskActivity.this, "Error creating new task", Toast.LENGTH_SHORT).show();
            Log.v(LOG_TAG, "Error creating new task");
        }
        return false;
    }
}
