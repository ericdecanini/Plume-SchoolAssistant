package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;

public class ClassesActivity extends AppCompatActivity {
    // Constantly used variables
    String LOG_TAG = ClassesActivity.class.getSimpleName();

    // CAM Variables
    private Menu mActionMenu;
    private int mOptionMenuCount;

    // UI Elements
    ListView listView;
    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    // Flags
    boolean isTablet;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;

    // List Varialbes
    ScheduleAdapter mScheduleAdapter;
    ArrayList<Schedule> mScheduleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Get references to the views
        listView = (ListView) findViewById(R.id.schedule_list);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // Check if the used device is a tablet
        isTablet = getResources().getBoolean(R.bool.isTablet);

        // Set the mScheduleAdapter and listeners of the list view
        if (listView != null) {
            listView.setAdapter(mScheduleAdapter);
            listView.setOnItemClickListener(ItemClickListener());
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new ModeCallback());

            if (isTablet)
                listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());
        }

        // Set the action of the FAB
        if (fab != null)
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ClassesActivity.this, NewScheduleActivity.class);
                    startActivity(intent);
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
        fab.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
    }

    public AdapterView.OnItemClickListener ItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DbHelper dbHelper = new DbHelper(ClassesActivity.this);
                Cursor cursor = dbHelper.getAllClassesData();
                if (cursor.moveToPosition(position)) {
                    Intent intent = new Intent(ClassesActivity.this, ScheduleDetailActivity.class);
                    intent.putExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE), cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE)));
                    startActivity(intent);
                } else {
                    Log.w(LOG_TAG, "Error getting title of selected item");
                }
            }

        };
    }

    private void refreshClassesList() {
        if (mScheduleAdapter != null)
            mScheduleAdapter.clear();

        mScheduleAdapter.clear();
        // Inflate the listview
        if (mFirebaseUser != null) {
            // Get data from Firebase
            if (mScheduleAdapter.getCount() == 0)
                findViewById(R.id.header_textview).setVisibility(View.VISIBLE);
            DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("classes");
            classesRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String title = dataSnapshot.getKey();
                    String icon = dataSnapshot.child("icon").getValue(String.class);
                    String teacher = dataSnapshot.child("teacher").getValue(String.class);
                    String room = dataSnapshot.child("room").getValue(String.class);
                    mScheduleList.add(new Schedule(ClassesActivity.this, icon, title,
                            teacher, room, "", "", ""));
                    if (mScheduleAdapter.getCount() == 0)
                        findViewById(R.id.header_textview).setVisibility(View.GONE);
                    mScheduleAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            // Get data from SQLite
            DbHelper dbHelper = new DbHelper(this);
            mScheduleList = dbHelper.getAllClassesArray(this);
            // Only show the header if there are no items in the class mScheduleAdapter
            if (mScheduleAdapter.getCount() == 0)
                findViewById(R.id.header_textview).setVisibility(View.VISIBLE);
        }
        mScheduleAdapter = new ScheduleAdapter(this,
                R.layout.list_item_schedule, mScheduleList);
    }

    // Subclass for the Contextual Action Mode
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
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode_single, menu);
            mActionMenu = menu;
            // Set the title and colour of the contextual action bar
            mode.setTitle(getString(R.string.select_items));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(getResources().getColor(R.color.gray_700));

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
                    editSelectedItem();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            // Clear the array list of selected items and revert the window colour back to normal
            CAMselectedItemsList.clear();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        private void deleteSelectedItems() {
            if (mFirebaseUser != null) {
                // Delete from Firebase
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    int position = CAMselectedItemsList.get(i);
                    Schedule schedule = mScheduleList.get(position);
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserId).child("classes")
                            .child(schedule.scheduleLesson).removeValue();
                }
            } else {
                // Delete from SQLite
                // Delete all the selected items based on the itemIDs
                // Stored in the array list
                DbHelper db = new DbHelper(ClassesActivity.this);
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    int position = CAMselectedItemsList.get(i);
                    Schedule schedule = mScheduleList.get(position);
                    db.deleteScheduleItemByTitle(schedule.scheduleLesson);
                }
            }

            // Refresh the class list mScheduleAdapter
            refreshClassesList();

            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        private void editSelectedItem(){
            Intent intent = new Intent(ClassesActivity.this, NewScheduleActivity.class);
            int position = CAMselectedItemsList.get(0);

            // Ensure that only one item is selected
            if (CAMselectedItemsList.size() == 1){
                String title =  mScheduleList.get(position).scheduleLesson;

                // Add the data to the intent for NewScheduleActivity to identify the class by.
//                intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_ID), id);
                intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_TITLE),title);
                intent.putExtra(getResources().getString(R.string.SCHEDULE_FLAG_EDIT), true);
                intent.putExtra(getResources().getString(R.string.STARTED_FROM_CLASSES_ACTIVITY), true);

                // Clear the selected items list, exit the CAM and launch the activity
                CAMselectedItemsList.clear();
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                startActivity(intent);
            }

            // If more than one item was selected, throw a warning log
            else {
                Log.w(LOG_TAG, "Cancelling event due to more than one item selected");
            }
        }
    }

}
