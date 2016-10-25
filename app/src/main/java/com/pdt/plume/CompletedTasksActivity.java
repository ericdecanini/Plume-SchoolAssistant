package com.pdt.plume;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;

public class CompletedTasksActivity extends AppCompatActivity {

    String LOG_TAG = CompletedTasksActivity.class.getSimpleName();
    ArrayList<String> taskTitles;
    ArrayList<Integer> taskIDs;
    ListView listView;
    ArrayAdapter<String> adapter;

    // CAM Variables
    private Menu mActionMenu;
    private int mOptionMenuCount;
    List<Integer> CAMselectedItemsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_tasks);

        // Inflate the listview of task titles
        final DbHelper dbHelper = new DbHelper(this);
        final Cursor cursor = dbHelper.getCompletedTaskData();
        taskTitles = new ArrayList<>();
        taskIDs = new ArrayList<>();
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                taskTitles.add(cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE)));
                taskIDs.add(cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID)));
            }
        } else findViewById(R.id.header_textview).setVisibility(View.VISIBLE);
        cursor.close();

        listView = (ListView) findViewById(R.id.completed_tasks_list);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, taskTitles);
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new ModeCallback());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the ID of the clicked item
                Cursor singleTaskItem = dbHelper.getTaskById(taskIDs.get(position));
                int ID = 0;
                if (singleTaskItem.moveToFirst())
                    ID = singleTaskItem.getInt(singleTaskItem.getColumnIndex(DbContract.TasksEntry._ID));

                // Create an intent to the TaskDetailActivity passing on the ID
                Intent intent = new Intent(CompletedTasksActivity.this, TasksDetailActivity.class);
                intent.putExtra("_ID", ID);
                intent.putExtra(getString(R.string.FLAG_TASK_COMPLETED), true);
                startActivity(intent);
            }
        });

        // Initialise the FAB
        FloatingActionButton FAB = (FloatingActionButton) findViewById(R.id.fab);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Cursor completedTasksCursor = dbHelper.getCompletedTaskData();
                new AlertDialog.Builder(CompletedTasksActivity.this)
                        .setTitle(getString(R.string.activity_completedTasks_dialog_title))
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (completedTasksCursor.moveToFirst()) {
                                    for (int i = 0; i < cursor.getCount(); i++) {
                                        completedTasksCursor.moveToPosition(i);
                                        int id = completedTasksCursor.getInt(completedTasksCursor.getColumnIndex(DbContract.TasksEntry._ID));
                                        dbHelper.deleteTaskItem(id);
                                    }
                                }
                                completedTasksCursor.close();
                            }
                        }).show();
            }
        });

    }

    private void refreshListview() {
        // Inflate the listview of task titles
        final DbHelper dbHelper = new DbHelper(this);
        final Cursor cursor = dbHelper.getCompletedTaskData();
        taskTitles.clear();
        taskIDs.clear();

        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                findViewById(R.id.header_textview).setVisibility(View.GONE);
                cursor.moveToPosition(i);
                taskTitles.add(cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE)));
                taskIDs.add(cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID)));
            }
        } else findViewById(R.id.header_textview).setVisibility(View.VISIBLE);
        cursor.close();

        adapter.notifyDataSetChanged();
    }

    private void restoreTaskItem(int position) {
        // Update the task
        DbHelper dbHelper = new DbHelper(this);
        Cursor cursor = dbHelper.getCompletedTaskData();
        if (cursor.moveToPosition(position)) {
            int _ID = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID));
            String title = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
            String classTitle = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS));
            String classType = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE));
            String sharer = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_SHARER));
            String description = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION));
            String attachment = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
            int duedate = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE));
            int reminderdate = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE));
            int remindertime = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME));
            String icon = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON));
            String picture = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_PICTURE));
            dbHelper.updateTaskItem(_ID, title, classTitle, classType,
                    sharer, description, attachment,
                    duedate, reminderdate, remindertime,
                    icon, picture, false);
            cursor.close();

            // Refresh the adapter so the restored task is no longer displayed
            refreshListview();
        }


    }

    // Subclass for the Contextual Action Mode
    private class ModeCallback implements ListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            // Get the number of list items selected
            // and set the window subtitle based on that
            final int checkedCount = listView.getCheckedItemCount();
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
            MenuInflater inflater = CompletedTasksActivity.this.getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode_completed_task, menu);
            mActionMenu = menu;

            // Set the title of the contextual action bar
            mode.setTitle(CompletedTasksActivity.this.getString(R.string.select_items));

            // Set the colour of the contextual action bar
            ColorDrawable colorDrawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                colorDrawable = new ColorDrawable(getColor(R.color.gray_500));
                CompletedTasksActivity.this.getWindow().setStatusBarColor(getResources().getColor(R.color.gray_700));
            } else {
                colorDrawable = new ColorDrawable(getResources().getColor(R.color.gray_500));
            }
            CompletedTasksActivity.this.getSupportActionBar().setBackgroundDrawable(colorDrawable);

            return true;
        }
        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            return true;
        }
        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteSelectedItems();
                    break;

                case R.id.action_restore:
                    for(int i = 0; i < CAMselectedItemsList.size(); i++) {
                            restoreTaskItem(CAMselectedItemsList.get(i));
                    }

                    // Then clear the selected items array list and emulate
                    // a back button press to exit the Action Mode
                    CAMselectedItemsList.clear();
                    CompletedTasksActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                    CompletedTasksActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

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
                CompletedTasksActivity.this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            } else {
                colorDrawable = new ColorDrawable(getResources().getColor(R.color.gray_500));
            }
            CompletedTasksActivity.this.getSupportActionBar().setBackgroundDrawable(colorDrawable);
        }

        private void deleteSelectedItems() {
            // Delete all the selected items based on the itemIDs
            // Stored in the array list
            for(int i = 0; i < CAMselectedItemsList.size(); i++) {
                int position = CAMselectedItemsList.get(i);
                DbHelper dbHelper = new DbHelper(CompletedTasksActivity.this);
                Cursor cursor = dbHelper.getCompletedTaskData();
                if (cursor.moveToPosition(position))
                    dbHelper.deleteTaskItem(cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID)));
                cursor.close();

            }

            // Notify the adapter of the changes
            refreshListview();

            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            CompletedTasksActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            CompletedTasksActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }
    }

}
