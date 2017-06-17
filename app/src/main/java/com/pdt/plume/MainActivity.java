package com.pdt.plume;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
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
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
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
import android.support.v7.app.AlertDialog;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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
import com.pdt.plume.services.ClassNotificationReceiver;
import com.pdt.plume.services.ClassesActivityTablet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.os.Build.ID;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ALARM;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ID;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_INTENT;
import static com.pdt.plume.StaticRequestCodes.REQUEST_STORAGE_PERMISSION;

import com.pdt.plume.data.DbContract.TasksEntry;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        TasksDetailFragment.OnTaskCompleteListener, TasksDetailFragment.OnTaskDeleteListener,
        ScheduleDetailFragment.OnClassDeleteListener {

    // Constantly used variables
    String LOG_TAG = MainActivity.class.getSimpleName();
    Utility utility = new Utility();
    boolean isTablet = false;
    boolean isLandscape;

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
    String weekSettings;

    // Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    boolean loggedIn = false;
    MenuItem logInOut;
    CallbackManager callbackManager;
    ValueEventListener requestsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        isLandscape = getResources().getBoolean(R.bool.isLandscape);

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent));
        updateWeekNumber();

        // Initialise Facebook
        CallbackManager callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        // Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            loggedIn = false;
        } else {
            loggedIn = true;
        }

        // Set the custom toolbar as the action bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        mAppbar = (AppBarLayout) findViewById(R.id.appbar);

        boolean FIRST_LAUNCH = preferences.getBoolean(getString(R.string.KEY_FIRST_LAUNCH), true);

//         If it's the first time launching this version, perform this function
//        if (!FIRST_LAUNCH && preferences.getBoolean(getString(R.string.Version), true)) {
//            // Show the changelog dialog
//            preferences.edit()
//                    .putBoolean(getString(R.string.Version), false)
//                    .apply();
//
//            new AlertDialog.Builder(this)
//                    .setTitle(getString(R.string.changelog_title, getString(R.string.Version)))
//                    .setMessage(getString(R.string.changelog))
//                    .setPositiveButton(getString(R.string.ok), null)
//                    .show();
//        }

        // If a week has passed since using the app, let the user give the app a good rating
        Calendar firstLaunch = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        long firstLaunchMillis = preferences.getLong(getString(R.string.KEY_FIRST_LAUNCH_DATE), 0);
        firstLaunch.setTimeInMillis(firstLaunchMillis);
        int day1 = firstLaunch.get(Calendar.DAY_OF_YEAR);
        int day2 = c.get(Calendar.DAY_OF_YEAR);

        boolean weekPassed = preferences.getBoolean(getString(R.string.KEY_WEEK_PASSED), false);
        Log.v(LOG_TAG, "Week passed: " + weekPassed);
        if ((day2 >= day1 + 7 || day2 < day1) && !weekPassed) {
            preferences.edit()
                    .putBoolean(getString(R.string.KEY_WEEK_PASSED), true)
                    .apply();
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_rate_title))
                    .setMessage(getString(R.string.dialog_rate))
                    .setPositiveButton(getString(R.string.rate), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Uri uri = Uri.parse("market://details?id=" + getPackageName());
                            Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                            try {
                                startActivity(myAppLinkToMarket);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(MainActivity.this, " unable to find market app", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.not_now), null)
                    .show();
        }

        // If it's the first time running the app, perform this function
        if (FIRST_LAUNCH)
            init();

        // Check if the device is a phone or tablet, then
        // initialise the tab layout based on that
        isTablet = getResources().getBoolean(R.bool.isTablet);
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
        // and then set its unread counter based on the number of Peer Requests
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);

            if (mFirebaseUser != null) {
                DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mFirebaseUser.getUid()).child("requests");
                requestsListener =  new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long childrenCount = dataSnapshot.getChildrenCount();
                        setMenuCounter(R.id.nav_requests, ((int) childrenCount));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                requestsRef.addValueEventListener(requestsListener);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFirebaseUser != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mFirebaseUser.getUid()).child("requests")
                    .removeEventListener(requestsListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mSectionsPagerAdapter != null)
            mSectionsPagerAdapter.notifyDataSetChanged();

        Intent intent = getIntent();
//        if (intent.hasExtra(getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS))){
//            mViewPager.setCurrentItem(1);
//        }

        updateWeekNumber();

        // Set the header date
        if (!isTablet) {
            Calendar c = Calendar.getInstance();
            TextView headerTextView = (TextView) findViewById(R.id.header);
            TextView subheader = (TextView) findViewById(R.id.subheader);
            headerTextView.setText(utility.formatDateString(this, c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
            String basis = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.KEY_PREFERENCE_BASIS), "0");
            if (basis.equals("2")) {
                String blockString;
                int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                if (day == 1 || day == 3 || day == 5)
                    blockString = utility.formatBlockString(this, 0);
                else if (day == 2 || day == 4)
                    blockString = utility.formatBlockString(this, 1);
                else blockString = getString(R.string.weekend);
                subheader.setText(blockString);
            } else {
                String weekNumber = PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(getString(R.string.KEY_WEEK_NUMBER), "0");
                String weekString = PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(getString(R.string.KEY_SETTINGS_WEEK_FORMAT), "w:l");
                if (weekNumber.equals("0"))
                    weekString = weekString.replace("w", getString(R.string.week))
                    .replace(":", " ")
                    .replace("l", getString(R.string.A))
                    .replace("n", getString(R.string.one))
                    .replace("o", getString(R.string.first));
                else weekString = weekString.replace("w", getString(R.string.week))
                        .replace(":", " ")
                        .replace("l", getString(R.string.B))
                        .replace("n", getString(R.string.two))
                        .replace("o", getString(R.string.second));
                subheader.setText(weekString);
            }
        }

        // Set the action bar colour according to the theme
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
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
        if (isTablet)
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));

        // Initialise the tab layout theme
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setSelectedTabIndicatorColor(mPrimaryColor);
        }

        // Initialise the fab
        if (fab != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
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

        if (id == R.id.storage) {
//            FirebaseStorage storage = FirebaseStorage.getInstance();
//            StorageReference storageRef = storage.getReference();
//            StorageReference mountainsRef = storageRef.child("mountains.jpg");
//
//            ImageView imageView = (ImageView) findViewById(R.id.test);
//            imageView.setDrawingCacheEnabled(true);
//            imageView.buildDrawingCache();
//            Bitmap bitmap = imageView.getDrawingCache();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] data = baos.toByteArray();
//
//            UploadTask uploadTask = mountainsRef.putBytes(data);
//            uploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    // Handle nonsuccessful uploads
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                    Log.v(LOG_TAG, "UPLOAD SUCCESSFUL: " + downloadUrl.toString());
//                }
//            });
        }

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
            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);
        }

        if (id == R.id.notification) {
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
                contentIntent.putExtra(getString(R.string.INTENT_EXTRA_ID), ID);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
                stackBuilder.addParentStack(TasksDetailActivity.class);
                stackBuilder.addNextIntent(contentIntent);
                final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);
                builder.setContentIntent(contentPendingIntent)
                        .setSmallIcon(R.drawable.ic_assignment)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentTitle(getString(R.string.notification_message_reminder))
                        .setContentText(title)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .extend(wearableExtender)
                        .setDefaults(Notification.DEFAULT_ALL);

                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(REQUEST_NOTIFICATION_ALARM, builder.build());
            }
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
            } else {
                super.onBackPressed();
            }
    }

    // Method to handle item selections of the navigation drawer
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.v(LOG_TAG, "ItemTitle: " + item.getTitle());
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_classes:
                if (isTablet) startActivity(new Intent
                            (this, ClassesActivityTablet.class));
                else startActivity(new Intent
                        (this, ClassesActivity.class));
                break;
            case R.id.nav_people:
                if (isTablet) startActivity(new Intent
                        (this, PeopleActivityTablet.class));
                else startActivity(new Intent
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
        weekSettings = "0";
        editor.putString(getString(R.string.KEY_WEEK_NUMBER), "0");
        Calendar c = Calendar.getInstance();
        int weekOfYear = c.get(Calendar.WEEK_OF_YEAR);
        editor.putInt(getString(R.string.KEY_WEEK_OF_YEAR), weekOfYear);

        editor.putLong(getString(R.string.KEY_FIRST_LAUNCH_DATE), c.getTimeInMillis());

        // Trigger the notification service
        Intent notifIntent = new Intent(this, ClassNotificationReceiver.class);
        notifIntent.setAction("com.pdt.plume.NOTIFICATION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                57, notifIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 1);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        // Commit the preferences
        editor.apply();

        Intent intent = new Intent(this, IntroActivity.class);
        startActivity(intent);
    }

    // Check for any  peer requests and set it in the navigation view
    private void setMenuCounter(@IdRes int itemId, int count) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        FrameLayout view = (FrameLayout) navigationView.getMenu().findItem(itemId).getActionView();
        TextView viewText = (TextView) view.findViewById(R.id.textView);
        viewText.setText(count > 0 ? String.valueOf(count) : null);
        if (count == 0)
            view.setVisibility(View.GONE);
        else view.setVisibility(View.VISIBLE);
    }

    public void initTabs() {
        // Create the mTasksAdapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections mTasksAdapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null)
            mViewPager.setAdapter(mSectionsPagerAdapter);

        // Initialise the tab layout and set it up with the pager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
            mSecondaryColor = PreferenceManager.getDefaultSharedPreferences(this)
                    .getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent);
            tabLayout.setSelectedTabIndicatorColor(mPrimaryColor);

            // Set the custom view of the tabs
            LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
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
        if (intent.hasExtra(getString(R.string.INTENT_FLAG_RETURN_TO_TASKS))) {
            mViewPager.setCurrentItem(1);
        }

    }

    public void initSpinner() {
        // Get a reference to the action bar and
        // disable its title display
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        }

        // Get a reference to the spinner UI element
        // and set its mTasksAdapter and ItemClickListener
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        if (spinner != null) {
            // Set the mTasksAdapter of the spinner
            spinner.setAdapter(new mSpinnerAdapter(
                    mToolbar.getContext(),
                    new String[]{
                            getResources().getString(R.string.schedule),
                            getResources().getString(R.string.tasks),
                    }));

            // Set the Listener of the spinner
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // When the given dropdown item is selected, show its contents in the
                    // container view.
                    switch (position) {
                        case 0:
                            ScheduleFragment scheduleFragment = new ScheduleFragment();
                            Intent intent = getIntent();
                            String RETURN_TO_SCHEDULE = intent.getStringExtra(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE));
                            if (RETURN_TO_SCHEDULE != null && RETURN_TO_SCHEDULE
                                    .equals(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE))) {
                                intent.putExtra(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE), "");
                                Bundle args = new Bundle();
                                args.putString(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE), RETURN_TO_SCHEDULE);
                                args.putInt(getString(R.string.INTENT_EXTRA_POSITION),
                                        intent.getIntExtra(getString(R.string.INTENT_EXTRA_POSITION), -1));
                                scheduleFragment.setArguments(args);
                            }
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, scheduleFragment)
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
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            // Check if the activity was started from the tasks activity
            // and automatically switch back to tasks if it did
            Intent intent = getIntent();
            if (intent.hasExtra(getString(R.string.INTENT_FLAG_RETURN_TO_TASKS)))
                spinner.setSelection(Utility.getIndex(spinner, spinner.getItemAtPosition(1).toString()));
        }
    }

    @Override
    public void OnTaskComplete(int ID, String fID) {
        if (mFirebaseUser != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mFirebaseUser.getUid())
                    .child("tasks").child(fID).child("completed")
                    .setValue(true);
        } else {
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor = dbHelper.getTaskById(ID);
            cursor.moveToFirst();
            String[] pictureArray = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_PICTURE)).split("#seperate#");
            ArrayList<Uri> pictureList = new ArrayList<>();
            for (int i = 0; i > pictureArray.length; i++) {
                pictureList.add(Uri.parse(pictureArray[i]));
            }

            dbHelper.updateTaskItem(this, ID,
                    cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_CLASS)),
                    cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT)),
                    cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_DUEDATE)),
                    cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE)),
                    cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_REMINDER_TIME)),
                    cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ICON)),
                    pictureList,
                    true);
        }

        TasksFragment fragment = new TasksFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
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

    @Override
    public void OnTaskDelete(int ID, String fID) {
        if (mFirebaseUser != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mFirebaseUser.getUid())
                    .child("tasks").child(fID).removeValue();
        } else {
            DbHelper dbHelper = new DbHelper(this);
            dbHelper.deleteTaskItem(ID);
        }

        TasksFragment fragment = new TasksFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
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
            switch (position) {
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
                    return getResources().getString(R.string.schedule);
                case 1:
                    return getResources().getString(R.string.task);
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
        weekSettings = preferences.getString(getString(R.string.KEY_SETTINGS_WEEK_NUMBER), "0");

        Calendar c = Calendar.getInstance();
        int weekOfYear = c.get(Calendar.WEEK_OF_YEAR);
        int savedWeekOfYear = preferences.getInt(getString(R.string.KEY_WEEK_OF_YEAR), 0);
        if (weekOfYear > savedWeekOfYear) {
            SharedPreferences.Editor editor = preferences.edit();
            int difference = weekOfYear - savedWeekOfYear;
            if (difference % 2 > 0) {
                if (weekSettings.equals("0"))
                    editor.putString(getString(R.string.KEY_WEEK_NUMBER), "1");
                else editor.putString(getString(R.string.KEY_WEEK_NUMBER), "0");;
            }
            editor.putInt(getString(R.string.KEY_WEEK_OF_YEAR), weekOfYear)
                    .apply();
        } else if (weekOfYear < savedWeekOfYear) {
            preferences.edit().putInt(getString(R.string.KEY_WEEK_OF_YEAR), weekOfYear)
                    .putString(getString(R.string.KEY_WEEK_NUMBER), "0")
                    .apply();
        }

        Log.v(LOG_TAG, "Week number: " + preferences.getString(getString(R.string.KEY_WEEK_NUMBER), "0"));

    }

    private void loadLogInView() {
        if (isTablet) startActivity(new Intent(this, LoginActivityTablet.class));
        else startActivity(new Intent(this, LoginActivity.class));
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
                contentIntent.putExtra(getString(R.string.INTENT_EXTRA_ID), ID);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
                stackBuilder.addParentStack(TasksDetailActivity.class);
                stackBuilder.addNextIntent(contentIntent);
                final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);
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
                if (pendingIntent != null)
                    alarmManager.cancel(pendingIntent);
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

        // CANCEL CLASS NOTIFICATIONS
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mFirebaseUser.getUid()).child("classes");
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                    // Get the key data
                    String title = classSnapshot.getKey();
                    String icon = classSnapshot.child("icon").getValue(String.class);
                    String message = getString(R.string.class_notification_message,
                            Integer.toString(preferences.getInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), 0)));

                    // Get the listed data
                    ArrayList<Integer> timeins = new ArrayList<>();
                    if (weekSettings.equals("0"))
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
                        final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);
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
                        alarmManager.cancel(pendingIntent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
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
            contentIntent.putExtra(getString(R.string.INTENT_EXTRA_ID), ID);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
            stackBuilder.addParentStack(TasksDetailActivity.class);
            stackBuilder.addNextIntent(contentIntent);
            final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);
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
        Calendar c = Calendar.getInstance();
        final int forerunnerTime = preferences.getInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), 0);
        if (classesCursor != null)
            for (int i = 0; i < classesCursor.getCount(); i++) {
                classesCursor.moveToPosition(i);
                final String title = classesCursor.getString(classesCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
                String icon = classesCursor.getString(classesCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));
                int ID = classesCursor.getInt(classesCursor.getColumnIndex(DbContract.ScheduleEntry._ID));

                long timeInValue = classesCursor.getLong(classesCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN));
                c = Calendar.getInstance();
                Calendar timeInCalendar = Calendar.getInstance();
                timeInCalendar.setTimeInMillis(timeInValue);
                c.set(Calendar.HOUR, timeInCalendar.get(Calendar.HOUR) - 1);
                c.set(Calendar.MINUTE, timeInCalendar.get(Calendar.MINUTE) - forerunnerTime);
                Calendar current = Calendar.getInstance();
                if (c.getTimeInMillis() < current.getTimeInMillis())
                    c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
                c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) - forerunnerTime);

                // Build the notification
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
                final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);

                final Calendar finalC = c;
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
                        alarmManager.set(AlarmManager.RTC, finalC.getTimeInMillis(), pendingIntent);
                    }
                });
            }
        if (classesCursor != null)
            classesCursor.close();

        // Execute the Sign Out Operation
        mFirebaseAuth.signOut();
        loggedIn = false;
        logInOut.setTitle(getString(R.string.action_login));

        // Sign out of Facebook (if applicable)
        LoginManager.getInstance().logOut();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
