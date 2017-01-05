package com.pdt.plume;


import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.pdt.plume.R.string.re;

/**
 * A simple {@link Fragment} subclass.
 */
public class TasksFragment extends Fragment {
    // Constantly used variables
    String LOG_TAG = TasksFragment.class.getSimpleName();
    Utility utility = new Utility();
    DbHelper dbHelper;

    // UI Elements
    ListView listView;
    private Menu mActionMenu;
    private int mOptionsMenuCount;
    FloatingActionButton fab;
    TextView headerTextView;

    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    // Flags
    boolean isTablet;

    // List Variables
    ArrayList<Task> tasksArray = new ArrayList<>();
    TaskAdapter mTasksAdapter;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;
    ArrayList<String> FirebaseIdList = new ArrayList<>();

    // Required empty public constructor
    public TasksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Initialise Firebase and SQLite
        dbHelper = new DbHelper(getActivity());
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();


        // Get references to the views
        headerTextView = (TextView) rootView.findViewById(R.id.header_textview);
        listView = (ListView) rootView.findViewById(R.id.tasks_list);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        // Check if the used device is a tablet
        isTablet = getResources().getBoolean(R.bool.isTablet);

        // Get a reference to the list view and create its mScheduleAdapter
        // using the current day schedule data
        if (mFirebaseUser != null) {
            // Get the data from Firebase
            DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("tasks");
            tasksRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String icon = dataSnapshot.child("icon").getValue(String.class);
                    String title = dataSnapshot.child("title").getValue(String.class);
                    String sharer = dataSnapshot.child("sharer").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    float duedate = dataSnapshot.child("duedate").getValue(float.class);
                    float reminderdate = dataSnapshot.child("reminderdate").getValue(float.class);
                    tasksArray.add(new Task(icon, title, sharer, description, "", duedate, reminderdate));
                }
                @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override public void onCancelled(DatabaseError databaseError) {}
            });
        }
        else {
            // Get the data from SQLite
            tasksArray = dbHelper.getUncompletedTaskArray();
        }

        mTasksAdapter = new TaskAdapter(getContext(), R.layout.list_item_task, tasksArray);

        // The header text view will only be visible if there is no items in the task mScheduleAdapter
        if (mTasksAdapter.getCount() == 0)
            headerTextView.setVisibility(View.VISIBLE);

        // Set the mScheduleAdapter and listeners of the listview
        if (listView != null) {
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setAdapter(mTasksAdapter);
            listView.setOnItemClickListener(ItemClickListener());
            listView.setMultiChoiceModeListener(new ModeCallback());
            if (getResources().getBoolean(R.bool.isTablet))
                listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());

            if (isTablet)
                listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());
        }

        // Set the action of the FAB
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewTaskActivity.class);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), R.color.colorPrimary);
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent);
        fab.setBackgroundTintList((ColorStateList.valueOf(mSecondaryColor)));
    }

    public AdapterView.OnItemClickListener ItemClickListener() {

        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                // If the used device is a tablet, replace the
                // right-hand side fragment with a TasksDetailFragment
                // passing the data of the clicked row to the fragment
                if (isTablet) {
                    TasksDetailFragment fragment = new TasksDetailFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_container, fragment)
                            .commit();
                }

                // If the used device is a phone, start a new TasksDetailActivity
                // passing the data of the clicked row to the fragment
                else {
                    final Intent intent = new Intent(getActivity(), TasksDetailActivity.class);
                    intent.putExtra(getString(R.string.KEY_TASKS_EXTRA_ID), position);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        // Shared element transition
                        View icon = view.findViewById(R.id.task_icon);
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), icon, icon.getTransitionName()).toBundle();
                        startActivity(intent, bundle);
                    } else startActivity(intent);
                }
            }
        };
    }

    private class ModeCallback implements ListView.MultiChoiceModeListener {

        List<Integer> CAMselectedItemsList = new ArrayList<>();

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
                    mOptionsMenuCount = 0;
                    mode.setSubtitle("One item selected");
                    break;

                default:
                    mOptionsMenuCount = 1;
                    mode.setSubtitle("" + checkedCount + " items selected");
                    break;

            }

            // If the clicked item became selected, add it to
            // an array list of selected items
            if (checked)
                CAMselectedItemsList.add(position);

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
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode_single, menu);
            mActionMenu = menu;

            // Set the title and colour of the contextual action bar
            mode.setTitle("Select Items");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.gray_700));

            int colorFrom = mPrimaryColor;
            int colorTo = getResources().getColor(R.color.gray_500);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(200); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    getActivity().findViewById(R.id.toolbar).setBackgroundColor((int) animator.getAnimatedValue());
                    if (!isTablet)
                        getActivity().findViewById(R.id.tabs).setBackgroundColor((int) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();

            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            // Checks the count of items selected.
            // If it is one, show the edit menu action.
            // If it is more than one, hide the edit menu action.
            MenuItem menuItem = mActionMenu.findItem(R.id.action_edit);
            if (mOptionsMenuCount == 0)
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
                    editSelectedItem(CAMselectedItemsList.get(0));
                    break;

                default:
                    Toast.makeText(getActivity(), "Clicked " + item.getTitle(),
                            Toast.LENGTH_SHORT).show();
                    break;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            // Clear the array list of selected items and revert the window colour back to normal
            CAMselectedItemsList.clear();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getActivity().getWindow().setStatusBarColor(mDarkColor);

            int colorFrom = getResources().getColor(R.color.gray_500);
            int colorTo = mPrimaryColor;
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(800); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    getActivity().findViewById(R.id.toolbar).setBackgroundColor((int) animator.getAnimatedValue());
                    if (!isTablet)
                        getActivity().findViewById(R.id.tabs).setBackgroundColor((int) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();
        }

        private void deleteSelectedItems() {

            if (mFirebaseUser != null) {
                // Delete data from Firebase
                DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("tasks");
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    tasksRef.child(FirebaseIdList.get(CAMselectedItemsList.get(i))).removeValue();
                    FirebaseIdList.remove(CAMselectedItemsList.get(i));
                    tasksArray.remove(CAMselectedItemsList.get(i));
                }

                // Refresh the list mScheduleAdapter
                mTasksAdapter.notifyDataSetChanged();
                if (mTasksAdapter.getCount() == 0)
                    headerTextView.setVisibility(View.VISIBLE);
                else headerTextView.setVisibility(View.GONE);
            } else {
                // Delete data from SQLite
                DbHelper db = new DbHelper(getActivity());
                Cursor cursor = db.getUncompletedTaskData();

                // Delete all the selected items based on the itemIDs
                // Stored in the array list
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    if (cursor.moveToPosition(CAMselectedItemsList.get(i))) {
                        db.deleteTaskItem(cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID)));
                    }
                }
                cursor.close();

                // Query all the tasks data again from SQLite
                mTasksAdapter.clear();
                mTasksAdapter.addAll(db.getTaskDataArray());

                // Refresh the mScheduleAdapter
                mTasksAdapter.notifyDataSetChanged();
                if (mTasksAdapter.getCount() == 0)
                    headerTextView.setVisibility(View.VISIBLE);
                else headerTextView.setVisibility(View.GONE);
            }

            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        private void editSelectedItem(final int position) {
            // Ensure that only one item is selected
            if (CAMselectedItemsList.size() == 1) {
                // Initialise intent data variables
                int id;
                final String[] icon = new String[1];
                final String[] title = new String[1];
                final String[] classTitle = new String[1];
                final String[] classType = new String[1];
                final String[] description = new String[1];
                final String[] photo = new String[1];
                final String[] attachment = new String[1];
                final float[] dueDate = new float[1];
                final float[] reminderDate = new float[1];
                final float[] reminderTime = new float[1];
                final Intent intent = new Intent(getActivity(), NewTaskActivity.class);

                if (mFirebaseUser != null) {
                    // Get the data from Firebase
                    DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserId).child("tasks").child(FirebaseIdList.get(CAMselectedItemsList.get(0)));
                    taskRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String id = dataSnapshot.getKey();
                            icon[0] = dataSnapshot.child("icon").getValue(String.class);
                            title[0] = dataSnapshot.child("title").getValue(String.class);
                            classTitle[0] = dataSnapshot.child("class").getValue(String.class);
                            classType[0] = dataSnapshot.child("type").getValue(String.class);
                            description[0] = dataSnapshot.child("description").getValue(String.class);
                            photo[0] = dataSnapshot.child("photo").getValue(String.class);
                            attachment[0] = dataSnapshot.child("attachment").getValue(String.class);
                            dueDate[0] = dataSnapshot.child("duedate").getValue(float.class);
                            reminderDate[0] = dataSnapshot.child("reminderdate").getValue(float.class);
                            reminderTime[0] = dataSnapshot.child("remindertime").getValue(float.class);

                            intent.putExtra("id", id);
                            intent.putExtra("icon", icon[0]);
                            intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_TITLE), title[0]);
                            intent.putExtra(getString(R.string.TASKS_EXTRA_CLASS), classTitle[0]);
                            intent.putExtra(getString(R.string.TASKS_EXTRA_TYPE), classType[0]);
                            intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_DESCRIPTION), description[0]);
                            intent.putExtra("photo", photo[0]);
                            intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_ATTACHMENT), attachment[0]);
                            intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_DUEDATE), dueDate[0]);
                            intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_REMINDERDATE), reminderDate[0]);
                            intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_REMINDERTIME), reminderTime[0]);

                            // Create an intent to NewScheduleActivity and include the selected
                            // item's id, title, and an edit flag as extras
                            intent.putExtra("position", position);
                            intent.putExtra(getResources().getString(R.string.TASKS_FLAG_EDIT), true);

                            // Clear the selected items list, exit the CAM and launch the activity
                            CAMselectedItemsList.clear();
                            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                            startActivity(intent);
                        }
                        @Override public void onCancelled(DatabaseError databaseError) {}});

                    while (title.equals("")) {
                        // Sleep until intent is sent by the value added listener
                    }
                }
                else {
                    // Get the data from SQLite
                    // Get a reference to the database and
                    // Get a cursor of the Task Data
                    DbHelper db = new DbHelper(getActivity());
                    Cursor cursor = db.getUncompletedTaskData();

                    // Move the cursor to the position of the selected item
                    if (cursor.moveToPosition(CAMselectedItemsList.get(0))) {
                        // Get its Data
                        id = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID));
                        icon[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON));
                        title[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
                        classTitle[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS));
                        classType[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE));
                        description[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION));
                        photo[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_PICTURE));
                        attachment[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
                        dueDate[0] = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE));
                        reminderDate[0] = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE));
                        reminderTime[0] = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME));
                        cursor.close();

                        intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_ID), id);
                        intent.putExtra("icon", icon[0]);
                        intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_TITLE), title[0]);
                        intent.putExtra(getString(R.string.TASKS_EXTRA_CLASS), classTitle[0]);
                        intent.putExtra(getString(R.string.TASKS_EXTRA_TYPE), classType[0]);
                        intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_DESCRIPTION), description[0]);
                        intent.putExtra("photo", photo[0]);
                        intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_ATTACHMENT), attachment[0]);
                        intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_DUEDATE), dueDate[0]);
                        intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_REMINDERDATE), reminderDate[0]);
                        intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_REMINDERTIME), reminderTime[0]);

                        // Create an intent to NewScheduleActivity and include the selected
                        // item's id, title, and an edit flag as extras
                        intent.putExtra("position", position);
                        intent.putExtra(getResources().getString(R.string.TASKS_FLAG_EDIT), true);

                        // Clear the selected items list, exit the CAM and launch the activity
                        CAMselectedItemsList.clear();
                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                        startActivity(intent);
                    }

                }
            }

            // If more than one item was selected, throw a warning log
            else {
                Log.w(LOG_TAG, "Cancelling event due to more than one item selected");
            }
        }

    }

}
