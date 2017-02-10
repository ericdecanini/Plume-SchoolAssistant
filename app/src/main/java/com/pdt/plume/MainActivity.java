package com.pdt.plume;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.pdt.plume.services.ScheduleNotificationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.os.Build.ID;
import static com.pdt.plume.NewTaskActivity.REQUEST_NOTIFICATION_ALARM;
import static com.pdt.plume.NewTaskActivity.REQUEST_NOTIFICATION_INTENT;
import static com.pdt.plume.ScheduleFragment.showBlockHeaderA;
import static com.pdt.plume.ScheduleFragment.showBlockHeaderB;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ID;
import static com.pdt.plume.StaticRequestCodes.REQUEST_STORAGE_PERMISSION;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Constantly used variables
    String LOG_TAG = MainActivity.class.getSimpleName();
    Utility utility = new Utility();

    // UI Elements
    Toolbar mToolbar;
    AppBarLayout mAppbar;
    private TabsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    FloatingActionButton fab;

    // Variables aiding schedule
    int weekNumber;

    // Intent Data
    public static boolean notificationServiceIsRunning = false;

    // Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    boolean loggedIn = false;
    MenuItem logInOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            loggedIn = false;
        }
        else {
            loggedIn = true;
        }

        // Set the custom toolbar as the action bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        mAppbar = (AppBarLayout) findViewById(R.id.appbar);

        // If it's the first time running the app, launch this method
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.KEY_FIRST_LAUNCH), true))
            init();

        // Check if the device is a phone or tablet, then
        // initialise the tab layout based on that
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet)
            initSpinner();
        else
            initTabs();

        // Initialise Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }

        // Initialise the Navigation View and set its ItemClickListener
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null)
            navigationView.setNavigationItemSelectedListener(this);

        float sw = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels) / getResources().getDisplayMetrics().density;

        // Start the class notification service
        if (!notificationServiceIsRunning) {
            startService(new Intent(this, ScheduleNotificationService.class));
            notificationServiceIsRunning = true;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart");
        mSectionsPagerAdapter.notifyDataSetChanged();

        Intent intent = getIntent();
//        if (intent.hasExtra(getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS))){
//            mViewPager.setCurrentItem(1);
//        }

        updateWeekNumber();

        // Set the header date
        Calendar c = Calendar.getInstance();
        TextView headerTextView = (TextView) findViewById(R.id.header);
        if (showBlockHeaderA) {
            String blockString = utility.formatBlockString(this, 0);
            headerTextView.setText(blockString);
        } else if (showBlockHeaderB) {
            String blockString = utility.formatBlockString(this, 1);
            headerTextView.setText(blockString);
        } else {
            headerTextView.setText(utility.formatDateString(this, c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
        }

        // Set the action bar colour according to the theme
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor  = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        mSecondaryColor = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent);
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mAppbar.setBackground(new ColorDrawable(mPrimaryColor));
        } else mAppbar.setBackgroundColor(mPrimaryColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }

        // Initialise the tab layout theme
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout!= null) {
            tabLayout.setSelectedTabIndicatorColor(mPrimaryColor);
        }

        // Initialise the fab
        fab.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                // Request for the permission WRITE SETTINGS
                // TODO: Test if app can function normally without this permission
//                boolean permissionCheck = Settings.System.canWrite(this);
//                if (!permissionCheck) {
//                    Intent intent = new Intent();
//                    intent.setAction("android.settings.action.MANAGE_WRITE_SETTINGS");
//                    intent.setData(Uri.parse("package:" + getPackageName()));
//                    startActivity(intent);
//                } else
                loadLogInView();

            }
            return true;
        }

        if (id == R.id.intro) {
            Intent intent = new Intent(this, Intro.class);
            startActivity(intent);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadLogInView();
                }
                return;
        }
    }

    // Include back button action to close
    // navigation drawer if open
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null)
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
            else {
                super.onBackPressed();
            }
    }

    // Method to handle item selections of the navigation drawer
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.v(LOG_TAG, "ItemTitle: " + item.getTitle());
        // Handle navigation view item clicks here.
        switch (item.getItemId()){
            case R.id.nav_classes:
                startActivity(new Intent
                        (this, ClassesActivity.class));
                break;
            case R.id.nav_people:
                startActivity(new Intent
                        (this, PeopleActivity.class));
                break;
            case R.id.nav_requests:
                startActivity(new Intent(
                        this, RequestsActivity.class
                ));
                break;
            case R.id.nav_completedTasks:
                startActivity(new Intent
                        (this, CompletedTasksActivity.class));
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        // Close the navigation drawer upon item selection
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null)
            drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void init() {
        // The boolean is falsed in ScheduleFragment
        // Open the shared preference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        // Initialise the theme variables
        mPrimaryColor = getResources().getColor(R.color.colorPrimary);
        mSecondaryColor = getResources().getColor(R.color.colorAccent);
        editor.putInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), mPrimaryColor);
        editor.putInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), mSecondaryColor);

        // Initialise the week number
        weekNumber = 0;
        editor.putString(getString(R.string.KEY_WEEK_NUMBER), "0");

        // Commit the preferences
        editor.apply();

        Intent intent = new Intent(this, Intro.class);
        startActivity(intent);
    }

    public void initTabs(){
        // Create the mScheduleAdapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections mScheduleAdapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null)
            mViewPager.setAdapter(mSectionsPagerAdapter);

        // Initialise the tab layout and set it up with the pager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout!= null) {
            tabLayout.setupWithViewPager(mViewPager);
            mSecondaryColor = PreferenceManager.getDefaultSharedPreferences(this)
                    .getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent);
            tabLayout.setSelectedTabIndicatorColor(mPrimaryColor);

            // Set the custom view of the tabs
            LinearLayout linearLayout = (LinearLayout)tabLayout.getChildAt(0);
            linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(Color.GRAY);
            drawable.setSize(2, 2);
            linearLayout.setDividerPadding(0);
            linearLayout.setDividerDrawable(drawable);
        }

        // Check if the activity was started from the NewTaskActivity
        // and automatically direct the tab to Tasks if it has
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS))){
            mViewPager.setCurrentItem(1);
        }

    }

    public void initSpinner(){
        // Get a reference to the action bar and
        // disable its title display
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

        // Get a reference to the spinner UI element
        // and set its mScheduleAdapter and ItemClickListener
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        if (spinner != null) {
            // Set the mScheduleAdapter of the spinner
            spinner.setAdapter(new mSpinnerAdapter(
                    mToolbar.getContext(),
                    new String[]{
                            getResources().getString(R.string.tab_one),
                            getResources().getString(R.string.tab_two),
                    }));

            // Set the Listener of the spinner
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // When the given dropdown item is selected, show its contents in the
                    // container view.
                    switch (position){
                        case 0:
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, new ScheduleFragment())
                                    .commit();
                            break;
                        case 1:
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, new TasksFragment())
                                    .commit();
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Check if the activity was started from the tasks activity
            // and automatically switch back to tasks if it did
            Intent intent = getIntent();
            if (intent.hasExtra(getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS)))
                spinner.setSelection(Utility.getIndex(spinner, spinner.getItemAtPosition(1).toString()));
        }
    }

    public class TabsPagerAdapter extends FragmentPagerAdapter {

        // Default public constructor
        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // getItem is called to instantiate the fragment for the given page.
        // and return the corresponding fragment.
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new ScheduleFragment();
                case 1:
                    return new TasksFragment();
                default:
                    Log.e(LOG_TAG, "Error creating new fragment at getItem");
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.tab_one);
                case 1:
                    return getResources().getString(R.string.tab_two);
                default:
                    Log.e(LOG_TAG, "Error setting tab name at getPageTitle");
                    return null;
            }
        }
    }

    private static class mSpinnerAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {

        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        // Constructor method where helper is initialised
        public mSpinnerAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            // If the given row is a new row
            // Inflate the dropdown menu using a simple list item layout
            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            // If the given row is simply being recycled,
            // set the view to the recycled view
            else {
                view = convertView;
            }

            // Set the text of the dropdown list item
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Resources.Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Resources.Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }

    private void updateWeekNumber() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Calendar c = Calendar.getInstance();
        int weekOfYear = c.get(Calendar.WEEK_OF_YEAR);
        String weekSetting = preferences.getString(getString(R.string.KEY_WEEK_NUMBER_SETTING), "0");
        int weekNumber;


        if (weekSetting.equals("0")) {
            // Week A will be on an odd number week
            if ((weekOfYear & 0x01) != 0)
                weekNumber = 0;
            else weekNumber = 1;
        } else {
            // Week A will be on an even number week
            if ((weekOfYear & 0x01) != 0)
                weekNumber = 1;
            else weekNumber = 0;
        }

        // Save the new date data to SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("weekNumber", weekNumber)
                .apply();
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void logOut() {
        // Disable any notifications
        // CANCEL TASK NOTIFICATIONS
        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mFirebaseUser.getUid()).child("tasks");
        tasksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the data
                String title = dataSnapshot.child("title").getValue(String.class);
                String icon = dataSnapshot.child("icon").getValue(String.class);

                // Rebuild the notification
                final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                Bitmap largeIcon = null;
                try {
                    largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                        .setBackground(largeIcon);

                Intent contentIntent = new Intent(MainActivity.this, TasksDetailActivity.class);
                contentIntent.putExtra(getString(R.string.KEY_TASKS_EXTRA_ID), ID);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
                stackBuilder.addParentStack(TasksDetailActivity.class);
                stackBuilder.addNextIntent(contentIntent);
                final PendingIntent contentPendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQUEST_NOTIFICATION_INTENT, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentPendingIntent)
                        .setSmallIcon(R.drawable.ic_assignment)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentTitle(getString(R.string.notification_message_reminder))
                        .setContentText(title)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .extend(wearableExtender)
                        .setDefaults(Notification.DEFAULT_ALL);

                Notification notification = builder.build();

                Intent notificationIntent = new Intent(MainActivity.this, TaskNotificationPublisher.class);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQUEST_NOTIFICATION_ALARM,
                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}});

        // CANCEL CLASS NOTIFICATIONS
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mFirebaseUser.getUid()).child("classes");
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                    // Get the key data
                    String title = classSnapshot.getKey();
                    String icon = classSnapshot.child("icon").getValue(String.class);
                    String message = getString(R.string.class_notification_message,
                            Integer.toString(preferences.getInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), 0)));

                    // Get the listed data
                    ArrayList<Integer> timeins = new ArrayList<>();
                    if (weekNumber == 0)
                        for (DataSnapshot timeinSnapshot : classSnapshot.child("timein").getChildren())
                            timeins.add(timeinSnapshot.getValue(int.class));
                    else
                        for (DataSnapshot timeinaltSnapshot : classSnapshot.child("timeinalt").getChildren())
                            timeins.add(timeinaltSnapshot.getValue(int.class));


                    Calendar c = Calendar.getInstance();
                    for (int i = 0; i < timeins.size(); i++) {
                        // Rebuild the notification
                        c.setTimeInMillis(((long) timeins.get(i)));

                        final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                        Bitmap largeIcon = null;
                        try {
                            largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                                .setBackground(largeIcon);

                        Intent contentIntent = new Intent(MainActivity.this, ScheduleDetailActivity.class);
                        if (mFirebaseUser != null)
                            contentIntent.putExtra("id", title);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
                        stackBuilder.addParentStack(ScheduleDetailActivity.class);
                        stackBuilder.addNextIntent(contentIntent);
                        final PendingIntent contentPendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQUEST_NOTIFICATION_INTENT, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        builder.setContentIntent(contentPendingIntent)
                                .setSmallIcon(R.drawable.ic_assignment)
                                .setColor(getResources().getColor(R.color.colorPrimary))
                                .setContentTitle(title)
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .extend(wearableExtender)
                                .setDefaults(Notification.DEFAULT_ALL);

                        Notification notification = builder.build();

                        Intent notificationIntent = new Intent(MainActivity.this, TaskNotificationPublisher.class);
                        notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, REQUEST_NOTIFICATION_ID);
                        notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                        final PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQUEST_NOTIFICATION_ALARM,
                                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC, c.getTimeInMillis(), pendingIntent);
                    }
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        // Reschedule all SQLite based Task Notifications
        DbHelper dbHelper = new DbHelper(MainActivity.this);
        Cursor tasksCursor = dbHelper.getTaskData();
        tasksCursor.moveToFirst();
        for (int i = 0; i < tasksCursor.getCount(); i++) {
            // Get the data
            tasksCursor.moveToPosition(i);
            String title = tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
            String icon = tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON));
            long reminderDateMillis = tasksCursor.getLong(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE));
            long reminderTimeSeconds = tasksCursor.getLong(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME));
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(reminderDateMillis);
            int hour = (int) reminderTimeSeconds / 3600;
            int minute = (int) (reminderTimeSeconds - hour * 3600) / 60;
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            long notificationMillis = (c.getTimeInMillis());

            // Rebuild the notification
            final android.support.v4.app.NotificationCompat.Builder builder
                    = new NotificationCompat.Builder(MainActivity.this);
            Bitmap largeIcon = null;
            try {
                largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
            final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender
                    = new NotificationCompat.WearableExtender().setBackground(largeIcon);

            Intent contentIntent = new Intent(MainActivity.this, TasksDetailActivity.class);
            contentIntent.putExtra(getString(R.string.KEY_TASKS_EXTRA_ID), ID);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
            stackBuilder.addParentStack(TasksDetailActivity.class);
            stackBuilder.addNextIntent(contentIntent);
            final PendingIntent contentPendingIntent = PendingIntent.getBroadcast
                    (MainActivity.this, REQUEST_NOTIFICATION_INTENT,
                            contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentPendingIntent)
                    .setSmallIcon(R.drawable.ic_assignment)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(getString(R.string.notification_message_reminder))
                    .setContentText(title)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .extend(wearableExtender)
                    .setDefaults(Notification.DEFAULT_ALL);

            Notification notification = builder.build();

            Intent notificationIntent = new Intent(MainActivity.this, TaskNotificationPublisher.class);
            notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
            notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast
                    (MainActivity.this, REQUEST_NOTIFICATION_ALARM,
                            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (reminderDateMillis > 0)
                alarmManager.set(AlarmManager.RTC, new Date(notificationMillis).getTime(), pendingIntent);
        }
        tasksCursor.close();

        // Reschedule all SQLite based Class Notifications
        Cursor classesCursor = dbHelper.getCurrentDayScheduleDataFromSQLite(this);
        final Calendar c = Calendar.getInstance();
        final int forerunnerTime = preferences.getInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), 0);
        for (int i = 0; i < classesCursor.getCount(); i++) {
            classesCursor.moveToPosition(i);
            final String title = classesCursor.getString(classesCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
            String icon = classesCursor.getString(classesCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));
            int ID = classesCursor.getInt(classesCursor.getColumnIndex(DbContract.ScheduleEntry._ID));
            long timeInValue = classesCursor.getLong(classesCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN));
            c.setTimeInMillis(timeInValue);
            c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) - forerunnerTime);

            final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            Bitmap largeIcon = null;
            try {
                largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
            final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                    .setBackground(largeIcon);

            Intent contentIntent = new Intent(MainActivity.this, ScheduleDetailActivity.class);
            contentIntent.putExtra("_ID", ID);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
            stackBuilder.addParentStack(ScheduleDetailActivity.class);
            stackBuilder.addNextIntent(contentIntent);
            final PendingIntent contentPendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQUEST_NOTIFICATION_INTENT,
                    contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Palette.generateAsync(largeIcon, new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    builder.setContentIntent(contentPendingIntent)
                            .setSmallIcon(R.drawable.ic_assignment)
                            .setColor(getResources().getColor(R.color.colorPrimary))
                            .setContentTitle(title)
                            .setContentText(getString(R.string.class_notification_message, Integer.toString(forerunnerTime)))
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .extend(wearableExtender)
                            .setDefaults(Notification.DEFAULT_ALL);

                    Notification notification = builder.build();

                    Intent notificationIntent = new Intent(MainActivity.this, TaskNotificationPublisher.class);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 0);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                    final PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQUEST_NOTIFICATION_ALARM,
                            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, c.getTimeInMillis(), pendingIntent);
                }
            });
        }
        classesCursor.close();

        // Execute the Sign Out Operation
        mFirebaseAuth.signOut();
        loggedIn = false;
        logInOut.setTitle(getString(R.string.action_login));
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
