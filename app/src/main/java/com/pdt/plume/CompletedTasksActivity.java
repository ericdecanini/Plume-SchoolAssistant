package com.pdt.plume;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.TasksEntry;

import java.util.ArrayList;
import java.util.List;

public class CompletedTasksActivity extends AppCompatActivity {

    String LOG_TAG = CompletedTasksActivity.class.getSimpleName();
    DbHelper dbHelper = new DbHelper(this);

    // Listview Variables
    ArrayList<Task> mTasksList;
    ArrayList<Integer> taskIDs;
    ArrayList<String> taskFirebaseIDs;
    ListView listView;
    TaskAdapter mTasksAdapter;

    // CAM Variables
    private Menu mActionMenu;
    private int mOptionMenuCount;
    List<Integer> CAMselectedItemsList = new ArrayList<>();

    // Theme Variables
    int mPrimaryColor, mDarkColor, mSecondaryColor;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_tasks);

        // Initialise Firebase and SQLite
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Get references to the views
        listView = (ListView) findViewById(R.id.completed_tasks_list);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // Initialise the mScheduleAdapter of completed tasks
        // If no items were found, show the header
        mTasksList = new ArrayList<>();
        taskIDs = new ArrayList<>();
        taskFirebaseIDs = new ArrayList<>();
        mTasksAdapter = new TaskAdapter(this, R.layout.list_item_task, mTasksList);
        getCompletedTasksData();

        // Inflate the listview with the mScheduleAdapter
        listView.setAdapter(mTasksAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new ModeCallback());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create an intent to the TaskDetailActivity passing on the ID
                boolean isTablet = getResources().getBoolean(R.bool.isTablet);
                if (!isTablet) {
                    Intent intent = new Intent(CompletedTasksActivity.this, TasksDetailActivity.class);
                    intent.putExtra(getString(R.string.INTENT_FLAG_COMPLETED), true);
                    if (mFirebaseUser != null) {
                        String firebaseID = taskFirebaseIDs.get(position);
                        intent.putExtra("id", firebaseID);
                    } else {
                        int ID = taskIDs.get(position);
                        intent.putExtra("_ID", ID);
                    }

                    // Add the animation
                    intent.putExtra("icon", mTasksList.get(position).taskIcon);
                    View icon = view.findViewById(R.id.task_icon2);
                    if (icon.getTag() == null) icon = view.findViewById(R.id.task_icon);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP
                            && ((String) icon.getTag()).contains("com.pdt.plume")) {
                        // Shared element transition
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(CompletedTasksActivity.this, icon, icon.getTransitionName()).toBundle();
                        startActivity(intent, bundle);
                    } else startActivity(intent);
                } else {
                    Intent intent = new Intent(CompletedTasksActivity.this, TasksDetailActivityTablet.class);
                    intent.putExtra(getString(R.string.INTENT_FLAG_COMPLETED), true);
                    if (mFirebaseUser != null) {
                        String firebaseID = taskFirebaseIDs.get(position);
                        intent.putExtra("id", firebaseID);
                    } else {
                        int ID = taskIDs.get(position);
                        intent.putExtra("_ID", ID);
                    }
                    startActivity(intent);
                }
            }
        });

        // Initialise the FAB
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(CompletedTasksActivity.this)
                        .setTitle(getString(R.string.activity_completedTasks_dialog_title))
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mFirebaseUser != null) {
                                    DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(mUserId).child("tasks");
                                    for (int i = 0; i < taskFirebaseIDs.size(); i++) {
                                        String id = taskFirebaseIDs.get(i);
                                        tasksRef.child(id).removeValue();
                                    }
                                } else {
                                    for (int i = 0; i < taskIDs.size(); i++) {
                                        int id = taskIDs.get(i);
                                        dbHelper.deleteTaskItem(id);
                                    }
                                }

                                Intent intent = new Intent(CompletedTasksActivity.this, MainActivity.class);
                                startActivity(intent);
                                }}).show();
            }
        });

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
        }

        fab.setBackgroundTintList((ColorStateList.valueOf(mSecondaryColor)));

    }

    // Update the listview with its mTasksAdapter
    private void getCompletedTasksData() {
        mTasksList.clear();
        taskIDs.clear();
        taskFirebaseIDs.clear();

        if (mFirebaseUser != null) {
            // Get the data from Firebase
            DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("tasks");
            tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot taskSnapshot: dataSnapshot.getChildren()) {
                        boolean isTaskCompleted = taskSnapshot.child("completed").getValue(boolean.class);
                        if (isTaskCompleted) {
                            // Gather the necessary data
                            String title = taskSnapshot.child("title").getValue(String.class);
                            String icon = taskSnapshot.child("icon").getValue(String.class);
                            String sharer = taskSnapshot.child("sharer").getValue(String.class);
                            String classTitle = taskSnapshot.child("class").getValue(String.class);
                            String classType = taskSnapshot.child("type").getValue(String.class);
                            String description = taskSnapshot.child("description").getValue(String.class);
                            float duedate = taskSnapshot.child("duedate").getValue(float.class);

                            mTasksList.add(new Task(icon, title, sharer, classTitle, classType, description, "", duedate, -1, null));
                            taskFirebaseIDs.add(taskSnapshot.getKey());
                            findViewById(R.id.header_textview).setVisibility(View.GONE);
                            mTasksAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override public void onCancelled(DatabaseError databaseError) {}});
        } else {
            // Get the data from SQLite
            final Cursor cursor = dbHelper.getCompletedTaskData();
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    String title = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE));
                    String icon = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ICON));
                    String classTitle = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_CLASS));
                    String classType = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TYPE));
                    String description = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION));
                    float duedate = cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_DUEDATE));

                    mTasksList.add(new Task(icon, title, "", classTitle, classType, description, "", duedate, -1, null));
                    taskIDs.add(cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID)));
                }
                findViewById(R.id.header_textview).setVisibility(View.GONE);
            }
            cursor.close();
        }

        mTasksAdapter.notifyDataSetChanged();
    }

    // Set a task item to incomplete state so it shows in TasksFragment again
    private void UncompleteTaskItem(int position) {
        if (mFirebaseUser != null) {
            // Perform function in Firebase
            DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("tasks").child(taskFirebaseIDs.get(position));
            taskRef.child("completed").setValue(false);
        } else {
            // Perform function in SQLite
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor = dbHelper.getCompletedTaskData();
            if (cursor.moveToPosition(position)) {
                int _ID = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID));
                String title = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
                String classTitle = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS));
                String classType = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE));
                String description = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION));
                String attachment = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
                int duedate = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE));
                int reminderdate = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE));
                int remindertime = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME));
                String icon = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON));
                String picture = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_PICTURE));
                String[] pictureString = picture.split("#seperate#");
                ArrayList<Uri> pictureStringList = new ArrayList<>();
                for (int i = 0; i < pictureString.length; i++) {
                    pictureStringList.add(Uri.parse(pictureString[i]));
                }

                dbHelper.updateTaskItem(this, _ID, title, classTitle, classType,
                        description, attachment,
                        duedate, reminderdate, remindertime,
                        icon, pictureStringList, false);
                cursor.close();
        }
            // Refresh the mTasksAdapter so the restored task is no longer displayed
            getCompletedTasksData();
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
                            UncompleteTaskItem(CAMselectedItemsList.get(i));
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

            if (mFirebaseUser != null) {
                // Delete from Firebase
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    int position = CAMselectedItemsList.get(i);
                    DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserId).child("tasks");
                    tasksRef.child(taskFirebaseIDs.get(position)).removeValue();
                }
            } else {
                // Delete from SQLite
                for(int i = 0; i < CAMselectedItemsList.size(); i++) {
                    int position = CAMselectedItemsList.get(i);
                    DbHelper dbHelper = new DbHelper(CompletedTasksActivity.this);
                    Cursor cursor = dbHelper.getCompletedTaskData();
                    if (cursor.moveToPosition(position))
                        dbHelper.deleteTaskItem(cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID)));
                    cursor.close();
                }
            }


            // Notify the mTasksAdapter of the changes
            getCompletedTasksData();

            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            CompletedTasksActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            CompletedTasksActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }
    }

}
