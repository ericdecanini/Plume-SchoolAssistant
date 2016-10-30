package com.pdt.plume;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.pdt.plume.services.ScheduleNotificationService;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Constantly used variables
    String LOG_TAG = MainActivity.class.getSimpleName();

    // GCM Variables
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private boolean isReceiverRegistered;

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
    static final int REQUEST_FILE_GET = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        //Displaying token on logcat
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(LOG_TAG, "Token: " + token);

        // Set the custom toolbar as the action bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
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

        // Initialise the Navigation View and set its listener
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null)
            navigationView.setNavigationItemSelectedListener(this);

        float sw = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels) / getResources().getDisplayMetrics().density;

        // Start the class notification service
        startService(new Intent(this, ScheduleNotificationService.class));
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSectionsPagerAdapter.notifyDataSetChanged();

        // Get the current date and toggle the week number
        // Get the current date
        Calendar c = Calendar.getInstance();
        int currentWeek = c.get(Calendar.WEEK_OF_YEAR);
        // Get the previous date
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        weekNumber = preferences.getInt("weekNumber", 1);
        int lastCheckedWeekOfYear = preferences.getInt("weekOfYear", -1);
        // Toggle the weekNumber for each week passed since last check
        // If the preference wasn't found, don't toggle and simply store the preference.
        // The weekNumber will be saved as 0 by default
        if (lastCheckedWeekOfYear != -1){
            for (int i = lastCheckedWeekOfYear; i < currentWeek; i++){
                if (weekNumber == 0)
                    weekNumber = 1;
                else weekNumber = 0;
            }
        }
        else lastCheckedWeekOfYear = currentWeek;

        // Save the new date data to SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("weekNumber", weekNumber)
                .putInt("weekOfYear", currentWeek)
                .apply();

        // Set the action bar colour according to the theme
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
            tabLayout.setSelectedTabIndicatorColor(mSecondaryColor);
        }

        // Initialise the fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
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
        // Handle navigation view item clicks here.
        switch (item.getItemId()){
            case R.id.nav_classes:
                startActivity(new Intent
                        (this, ClassesActivity.class));
                break;
//            case R.id.nav_people:
//                Toast.makeText(MainActivity.this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
//                break;
            case R.id.nav_completedTasks:
                startActivity(new Intent
                        (this, CompletedTasksActivity.class));
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
//            case R.id.nav_help:
//                Toast.makeText(MainActivity.this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
//                break;
        }

        // Close the navigation drawer upon item selection
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null)
            drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return false;
    }

    public void initTabs(){
        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null)
            mViewPager.setAdapter(mSectionsPagerAdapter);

        // Initialise the tab layout and set it up with the pager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout!= null) {
            tabLayout.setupWithViewPager(mViewPager);
            mSecondaryColor = PreferenceManager.getDefaultSharedPreferences(this)
                    .getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent);
            tabLayout.setSelectedTabIndicatorColor(mSecondaryColor);
        }

        // Check if the activity was started from the NewTaskActivity
        // and automatically direct the tab to Tasks if it has
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS))){
            mViewPager.setCurrentItem(1);
        }

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                fab.hide();
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        fab.show();
//                    }
//                }, 150);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void initSpinner(){
        // Get a reference to the action bar and
        // disable its title display
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
        
        // Get a reference to the spinner UI element
        // and set its adapter and listener
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        if (spinner != null) {
            // Set the adapter of the spinner
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

}
