package com.pdt.plume;

import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.os.Build.ID;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ALARM;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ID;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_INTENT;

public class ClassesActivityTablet extends AppCompatActivity
    implements ScheduleDetailFragment.OnClassDeleteListener {
    // Constantly used variables
    String LOG_TAG = ClassesActivityTablet.class.getSimpleName();
    View rootView;

    // CAM Variables
    private Menu mActionMenu;
    private int mOptionMenuCount;

    // UI Elements
    AppBarLayout appbar;
    ListView listView;
    ProgressBar spinner;

    // Theme Variables
    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    // Flags
    boolean isTablet;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;
    boolean loggedIn = false;
    MenuItem logInOut;

    // List Varialbes
    ScheduleAdapter mScheduleAdapter;
    ArrayList<Schedule> mScheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);
        rootView = findViewById(R.id.container);
        isTablet = getResources().getBoolean(R.bool.isTablet);

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor  = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent));
        int backgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), getResources().getColor(R.color.backgroundColor));
        if (getResources().getBoolean(R.bool.isLandscape)) {
            findViewById(R.id.main_content).setBackgroundColor(backgroundColor);
        }

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            loggedIn = true;
            mUserId = mFirebaseUser.getUid();
        }

        // Set the mTasksAdapter and listeners of the list view
        queryClasses();
        mScheduleAdapter = new ScheduleAdapter(this, R.layout.list_item_schedule, mScheduleList);

        if (!isTablet) initPhone();
        else initTablet();
    }

    void initPhone() {
        // Initialise the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        appbar = (AppBarLayout) findViewById(R.id.appbar);

        // Set the picture on top of the activity
        if (mFirebaseUser != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ImageView icon = (ImageView) findViewById(R.id.icon);
                    String iconUri = dataSnapshot.child("icon").getValue(String.class);
                    icon.setVisibility(View.VISIBLE);
                    if (iconUri != null)
                        icon.setImageURI(Uri.parse(iconUri));
                    else icon.setImageResource(R.drawable.art_profile_default);
                    String defaultIconUri = "android.resource://com.pdt.plume/drawable/art_profile_default";
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserId).child("icon").setValue(defaultIconUri);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }



        // Initialise the ProgressBar
        spinner = (ProgressBar) findViewById(R.id.progressBar);

        listView = (ListView) findViewById(R.id.schedule_list);
        TextView newClassTextView = (TextView) findViewById(R.id.new_class);

        // Set the action of the new class
        if (newClassTextView != null)
            newClassTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ClassesActivityTablet.this, NewScheduleActivity.class);
                    startActivity(intent);
                }
            });

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }
        appbar.setBackgroundColor(mPrimaryColor);
        newClassTextView.setTextColor(mPrimaryColor);

        // Initialise the listview
        if (listView != null) {
            listView.setAdapter(mScheduleAdapter);
            listView.setOnItemClickListener(ItemClickListener());
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new ModeCallback());

            if (isTablet)
                listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());
        }

    }

    void initTablet() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(mDarkColor);

        Fragment fragment = new ClassFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        logInOut = menu.findItem(R.id.action_logout);
        if (loggedIn)
            logInOut.setTitle(getString(R.string.action_logout));
        else logInOut.setTitle(getString(R.string.action_login));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_logout) {
            if (loggedIn)
                logOut();
            else {
                loadLogInView();

            }
            return true;
        }

        return false;
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    private void logOut() {
        // Cancel online notifications
        Utility.rescheduleNotifications(this, false);

        // Execute the Sign Out operation
        mFirebaseAuth.signOut();
        loggedIn = false;
        logInOut.setTitle(getString(R.string.action_login));
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    public AdapterView.OnItemClickListener ItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mFirebaseUser != null) {
                    // Query from Firebase
                    String title = mScheduleList.get(position).scheduleLesson;
                    String icon = mScheduleList.get(position).scheduleIcon;
                    Intent intent = new Intent(ClassesActivityTablet.this, ScheduleDetailActivity.class);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title);
                    intent.putExtra("icon", icon);

                    // Add a transition if the device is Lollipop or above
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // Shared element transition
                        View iconView = view.findViewById(R.id.schedule_icon);
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation
                                (ClassesActivityTablet.this, iconView, iconView.getTransitionName()).toBundle();
                        startActivity(intent, bundle);
                    } else startActivity(intent);
                } else {
                    // Query from SQLite
                    DbHelper dbHelper = new DbHelper(ClassesActivityTablet.this);
                    Cursor cursor = dbHelper.getAllClassesData();
                    if (cursor.moveToPosition(position)) {
                        Intent intent = new Intent(ClassesActivityTablet.this, ScheduleDetailActivity.class);
                        intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE)));
                        intent.putExtra("icon", cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON)));

                        // Add a transition if the device is Lollipop or above
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isTablet) {
                            // Shared element transition
                            View iconView = view.findViewById(R.id.schedule_icon);
                            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation
                                    (ClassesActivityTablet.this, iconView, iconView.getTransitionName()).toBundle();
                            startActivity(intent, bundle);
                        } else startActivity(intent);
                    } else {
                        Log.w(LOG_TAG, "Error getting title of selected item");
                    }
                }
            }
        };
    }

    private void queryClasses() {
        if (spinner != null)
            spinner.setVisibility(View.VISIBLE);
        if (mFirebaseUser != null) {
            // Check if the classes ref doesn't exist
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("classes").getChildrenCount() == 0) {
//                        spinner.setVisibility(View.GONE);
                        findViewById(R.id.header_textview).setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            // Get data from Firebase
            mScheduleList.clear();
            DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("classes");

            classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long snapshotCount = dataSnapshot.getChildrenCount();
                    long i = 0;
                    if (snapshotCount == 0 && !isTablet) spinner.setVisibility(View.GONE);
                    for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                        // Hide progress bar when query is done
                        i++;
                        if (i == snapshotCount && !isTablet)
                            spinner.setVisibility(View.GONE);

                        String title = classSnapshot.getKey();
                        String icon = classSnapshot.child("icon").getValue(String.class);
                        String teacher = classSnapshot.child("teacher").getValue(String.class);
                        String room = classSnapshot.child("room").getValue(String.class);
                        if (icon != null)
                            mScheduleList.add(new Schedule(ClassesActivityTablet.this, icon, title,
                                    teacher, room, " ", " ", ""));

                        if (mScheduleAdapter != null)
                            mScheduleAdapter.notifyDataSetChanged();

                        if (!isTablet) {
                            if (mScheduleList.size() == 0)
                                findViewById(R.id.header_textview).setVisibility(View.VISIBLE);
                            else {
                                findViewById(R.id.header_textview).setVisibility(View.GONE);
                                spinner.setVisibility(View.GONE);
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if (isTablet) return;
                    spinner.setVisibility(View.GONE);
                    TextView headerTextView = (TextView) findViewById(R.id.header_textview);
                    headerTextView.setVisibility(View.VISIBLE);
                    headerTextView.setText(getString(R.string.check_internet));
                }
            });
        } else {
            // Get data from SQLite
            DbHelper dbHelper = new DbHelper(this);
            mScheduleList = dbHelper.getAllClassesArray(this);
            if (mScheduleAdapter != null)
                mScheduleAdapter.notifyDataSetChanged();

            // Only show the header if there are no items in the class mTasksAdapter
            if (spinner != null)
                spinner.setVisibility(View.GONE);
            if (!isTablet) {
                if (mScheduleList.size() == 0)
                    findViewById(R.id.header_textview).setVisibility(View.VISIBLE);
                else findViewById(R.id.header_textview).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void OnClassDelete(String title) {
        if (mFirebaseUser != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mFirebaseUser.getUid())
                    .child("classes").child(title).removeValue();
        } else {
            DbHelper dbHelper = new DbHelper(this);
            dbHelper.deleteScheduleItemByTitle(title);
        }

        ScheduleFragment fragment = new ScheduleFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
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
            ArrayList<Schedule> scheduleList = new ArrayList<>();
            if (mFirebaseUser != null) {
                // Delete from Firebase
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    int position = CAMselectedItemsList.get(i);
                    Schedule schedule = mScheduleList.get(position);
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserId).child("classes")
                            .child(schedule.scheduleLesson).removeValue();
                    scheduleList.add(schedule);
                }
            } else {
                // Delete from SQLite
                // Delete all the selected items based on the itemIDs
                // Stored in the array list
                DbHelper db = new DbHelper(ClassesActivityTablet.this);
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    int position = CAMselectedItemsList.get(i);
                    Schedule schedule = mScheduleList.get(position);
                    db.deleteScheduleItemByTitle(schedule.scheduleLesson);
                    scheduleList.add(schedule);
                }
            }

            mScheduleList.removeAll(scheduleList);
            mScheduleAdapter.notifyDataSetChanged();

            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        private void editSelectedItem(){
            Intent intent = new Intent(ClassesActivityTablet.this, NewScheduleActivity.class);
            int position = CAMselectedItemsList.get(0);

            // Ensure that only one item is selected
            if (CAMselectedItemsList.size() == 1){
                String title =  mScheduleList.get(position).scheduleLesson;

                // Add the data to the intent for NewScheduleActivity to identify the class by.
//                intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_ID), id);
                intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_TITLE),title);
                intent.putExtra(getResources().getString(R.string.INTENT_FLAG_EDIT), true);
                intent.putExtra(getResources().getString(R.string.INTENT_FLAG_RETURN_TO_CLASSES), true);

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
