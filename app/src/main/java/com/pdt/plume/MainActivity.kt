package com.pdt.plume

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.annotation.IdRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.TaskStackBuilder
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.NotificationCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.ThemedSpinnerAdapter
import android.support.v7.widget.Toolbar

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pdt.plume.data.DbContract
import com.pdt.plume.data.DbHelper
import com.pdt.plume.services.ClassNotificationReceiver

import java.io.IOException
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

import android.os.Build.ID
import com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ALARM
import com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_INTENT
import com.pdt.plume.StaticRequestCodes.REQUEST_STORAGE_PERMISSION

import com.pdt.plume.data.DbContract.TasksEntry
import com.pdt.plume.services.ActiveNotificationService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, TasksDetailFragment.OnTaskCompleteListener, TasksDetailFragment.OnTaskDeleteListener, ScheduleDetailFragment.OnClassDeleteListener {

    // Constantly used variables
    internal var LOG_TAG = MainActivity::class.java.simpleName
    internal var utility = Utility()
    internal var isTablet = false
    internal var isLandscape: Boolean = false

    // UI Elements
    lateinit internal var mToolbar: Toolbar
    lateinit internal var mAppbar: AppBarLayout
    private var mSectionsPagerAdapter: TabsPagerAdapter? = null
    private var mViewPager: ViewPager? = null

    internal var mPrimaryColor: Int = 0
    internal var mDarkColor: Int = 0
    internal var mSecondaryColor: Int = 0

    internal var fab: FloatingActionButton? = null

    // Variables aiding schedule
    lateinit internal var weekSettings: String

    // Firebase variables
    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null
    internal var loggedIn = false
    lateinit internal var logInOut: MenuItem
    internal var callbackManager: CallbackManager? = null
    lateinit internal var requestsListener: ValueEventListener

    var spinnerPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) savedInstanceState.clear()
        FacebookSdk.sdkInitialize(applicationContext)
        setContentView(R.layout.activity_main)
        if (findViewById(R.id.fab) != null)
            fab = findViewById(R.id.fab) as FloatingActionButton
        isLandscape = resources.getBoolean(R.bool.isLandscape)
        updateWeekNumber()

        // Initialise Facebook
        val callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {

            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {

            }
        })

        // Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth!!.currentUser

        if (mFirebaseUser == null) {
            loggedIn = false
        } else {
            loggedIn = true
        }

        // Set the custom toolbar as the action bar
        mToolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = ""
        mAppbar = findViewById(R.id.appbar) as AppBarLayout

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val FIRST_LAUNCH = preferences.getBoolean(getString(R.string.KEY_FIRST_LAUNCH), true)

        // If a week has passed since using the app, let the user give the app a good rating
        val firstLaunch = Calendar.getInstance()
        val c = Calendar.getInstance()
        val firstLaunchMillis = preferences.getLong(getString(R.string.KEY_FIRST_LAUNCH_DATE), 0)
        firstLaunch.timeInMillis = firstLaunchMillis
        val day1 = firstLaunch.get(Calendar.DAY_OF_YEAR)
        val day2 = c.get(Calendar.DAY_OF_YEAR)

        val weekPassed = preferences.getBoolean(getString(R.string.KEY_WEEK_PASSED), false)
        if ((day2 >= day1 + 7 || day2 < day1) && !weekPassed) {
            preferences.edit()
                    .putBoolean(getString(R.string.KEY_WEEK_PASSED), true)
                    .apply()
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_rate_title))
                    .setMessage(getString(R.string.dialog_rate))
                    .setPositiveButton(getString(R.string.rate)) { dialogInterface, i ->
                        val uri = Uri.parse("market://details?id=" + packageName)
                        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            startActivity(myAppLinkToMarket)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(this@MainActivity, " unable to find market app", Toast.LENGTH_LONG).show()
                        }
                    }
                    .setNegativeButton(getString(R.string.not_now), null)
                    .show()
        }

        // If it's the first time running the app, perform this function
        if (FIRST_LAUNCH)
            init()

        // Check if the device is a phone or tablet, then
        // initialise the tab layout based on that
        isTablet = resources.getBoolean(R.bool.isTablet)
        if (isTablet)
            initSpinner()
        else
            initTabs()

        // Initialise Navigation Drawer
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        if (drawer != null) {
            drawer.setDrawerListener(toggle)
            toggle.syncState()
        }

        // Initialise the Navigation View and set its ItemClickListener
        // and then set its unread counter based on the number of Peer Requests
        val navigationView = findViewById(R.id.nav_view) as NavigationView
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this)


            if (mFirebaseUser != null) {
                val requestsRef = FirebaseDatabase.getInstance().reference
                        .child("users").child(mFirebaseUser!!.uid).child("requests")
                requestsListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val childrenCount = dataSnapshot.childrenCount
                        setMenuCounter(R.id.nav_requests, childrenCount.toInt())
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                }
                requestsRef.addValueEventListener(requestsListener)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mFirebaseUser != null) {
            FirebaseDatabase.getInstance().reference
                    .child("users").child(mFirebaseUser!!.uid).child("requests")
                    .removeEventListener(requestsListener)
        }
    }

    override fun onStart() {
        super.onStart()

        if (mSectionsPagerAdapter != null)
            mSectionsPagerAdapter!!.notifyDataSetChanged()

        updateWeekNumber()

        // Set the header date
        if (!isTablet) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val c = Calendar.getInstance()
            val headerTextView = findViewById(R.id.header) as TextView
            val subheader = findViewById(R.id.subheader) as TextView
            val weekType = preferences.getString(getString(R.string.KEY_PREFERENCE_WEEKTYPE), "0")
            if (weekType == "0") subheader.visibility = View.GONE
            headerTextView.text = utility.formatDateString(this, c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            val basis = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.KEY_PREFERENCE_BASIS), "0")
            if (basis == "2") {
                val blockString: String
                val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                if (day == 1 || day == 3 || day == 5)
                    blockString = utility.formatBlockString(this, 0)
                else if (day == 2 || day == 4)
                    blockString = utility.formatBlockString(this, 1)
                else
                    blockString = getString(R.string.weekend)
                subheader.text = blockString
            } else {
                val weekNumber = preferences.getString(getString(R.string.KEY_WEEK_NUMBER), "0")
                var weekString: String = preferences.getString(getString(R.string.KEY_SETTINGS_WEEK_FORMAT), "w:l")
                if (weekNumber == "0")
                    weekString = weekString.replace("w", getString(R.string.week))
                            .replace(":", " ")
                            .replace("l", getString(R.string.A))
                            .replace("n", getString(R.string.one))
                            .replace("o", getString(R.string.first))
                else
                    weekString = weekString.replace("w", getString(R.string.week))
                            .replace(":", " ")
                            .replace("l", getString(R.string.B))
                            .replace("n", getString(R.string.two))
                            .replace("o", getString(R.string.second))
                subheader.text = weekString
            }
        }

        // Initialise the theme variables
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), resources.getColor(R.color.colorPrimary))
        mSecondaryColor = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent)
        val hsv = FloatArray(3)
        Color.colorToHSV(mPrimaryColor, hsv)
        hsv[2] *= 0.8f // value component
        mDarkColor = Color.HSVToColor(hsv)

        val backgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), resources.getColor(R.color.backgroundColor))
        main_content.setBackgroundColor(backgroundColor)
        if (!isTablet) tabs.setBackgroundColor(backgroundColor)
        nav_view.setBackgroundColor(backgroundColor)

        val textColor = preferences.getInt(getString(R.string.KEY_THEME_TITLE_COLOUR), resources.getColor(R.color.gray_900))
        if (!isTablet) tabs.tabTextColors = ColorStateList.valueOf(textColor)
        val navigationView = findViewById(R.id.nav_view) as NavigationView
        (navigationView.getHeaderView(0).findViewById(R.id.header) as TextView)
                .setTextColor(textColor)
        navigationView.itemTextColor = ColorStateList.valueOf(textColor)
        navigationView.itemIconTintList = ColorStateList.valueOf(textColor)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mAppbar.background = ColorDrawable(mPrimaryColor)
        } else
            mAppbar.setBackgroundColor(mPrimaryColor)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = mDarkColor
        }
        if (isTablet)
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(mPrimaryColor))

        // Initialise the tab layout theme
        if (findViewById(R.id.tabs) != null) {
            val tabLayout = findViewById(R.id.tabs) as TabLayout
            tabLayout?.setSelectedTabIndicatorColor(mPrimaryColor)
        }

        // Initialise the fab
        if (fab != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            fab!!.backgroundTintList = ColorStateList.valueOf(mSecondaryColor)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        logInOut = menu.findItem(R.id.action_logout)
        if (loggedIn)
            logInOut.title = getString(R.string.action_logout)
        else
            logInOut.title = getString(R.string.action_login)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

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
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }

        if (id == R.id.action_logout) {
            if (loggedIn)
                logOut()
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
                loadLogInView()

            }
            return true
        }

        if (id == R.id.intro) {
//            val intent = Intent(this, MatchClassesActivity::class.java)
//            startActivity(intent)
        }

        if (id == R.id.notification) {
            val dbHelper = DbHelper(this@MainActivity)
            val tasksCursor = dbHelper.taskData
            tasksCursor.moveToFirst()
            for (i in 0..tasksCursor.count - 1) {
                // Get the data
                tasksCursor.moveToPosition(i)
                val title = tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE))
                val icon = tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON))
                val reminderDateMillis = tasksCursor.getLong(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE))
                val reminderTimeSeconds = tasksCursor.getLong(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME))
                val c = Calendar.getInstance()
                c.timeInMillis = reminderDateMillis
                val hour = reminderTimeSeconds.toInt() / 3600
                val minute = (reminderTimeSeconds - hour * 3600).toInt() / 60
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)
                val notificationMillis = c.timeInMillis

                // Rebuild the notification
                val builder = NotificationCompat.Builder(this@MainActivity)
                var largeIcon: Bitmap? = null
                try {
                    largeIcon = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val contentIntent = Intent(this@MainActivity, TasksDetailActivity::class.java)
                contentIntent.putExtra(getString(R.string.INTENT_EXTRA_ID), ID)
                val stackBuilder = TaskStackBuilder.create(this@MainActivity)
                stackBuilder.addParentStack(TasksDetailActivity::class.java)
                stackBuilder.addNextIntent(contentIntent)
                val contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0)
                builder.setContentIntent(contentPendingIntent)
                        .setSmallIcon(R.drawable.ic_assignment)
                        .setColor(resources.getColor(R.color.colorPrimary))
                        .setContentTitle(getString(R.string.notification_message_reminder))
                        .setContentText(title)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)

                val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.notify(REQUEST_NOTIFICATION_ALARM, builder.build())
            }
        }

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadLogInView()
                }
                return
            }
        }
    }

    // Include back button action to close
    // navigation drawer if open
    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer != null)
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
    }

    // Method to handle item selections of the navigation drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.v(LOG_TAG, "ItemTitle: " + item.title)
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_classes -> if (isTablet)
                startActivity(Intent(this, ClassesActivityTablet::class.java))
            else
                startActivity(Intent(this, ClassesActivity::class.java))
            R.id.nav_people -> if (isTablet)
                startActivity(Intent(this, PeopleActivity::class.java))
            else
                startActivity(Intent(this, PeopleActivity::class.java))
            R.id.nav_requests -> startActivity(Intent(
                    this, RequestsActivity::class.java
            ))
            R.id.nav_completedTasks -> startActivity(Intent(this, CompletedTasksActivity::class.java))
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        // Close the navigation drawer upon item selection
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer?.closeDrawer(GravityCompat.START)

        return true
    }

    private fun init() {
        // The boolean is falsed in ScheduleFragment
        // Open the shared preference
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()

        // Initialise the theme variables
        mPrimaryColor = resources.getColor(R.color.colorPrimary)
        mSecondaryColor = resources.getColor(R.color.colorAccent)
        editor.putInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), mPrimaryColor)
        editor.putInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), mSecondaryColor)


        // Initialise the week number
        weekSettings = "0"
        editor.putString(getString(R.string.KEY_WEEK_NUMBER), "0")
        val c = Calendar.getInstance()
        val weekOfYear = c.get(Calendar.WEEK_OF_YEAR)
        editor.putInt(getString(R.string.KEY_WEEK_OF_YEAR), weekOfYear)

        editor.putLong(getString(R.string.KEY_FIRST_LAUNCH_DATE), c.timeInMillis)

        // Trigger the notification services
        val notifIntent = Intent(this, ClassNotificationReceiver::class.java)
        notifIntent.action = "com.pdt.plume.NOTIFICATION"
        val pendingIntent = PendingIntent.getBroadcast(this,
                57, notifIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val activeNotifIntent = Intent(this, ActiveNotificationService::class.java)
        activeNotifIntent.action = "com.pdt.plume.NOTIFICATION"
        val activePendingIntent = PendingIntent.getBroadcast(this,
                58, notifIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 1)
        calendar.set(Calendar.MINUTE, 1)
        val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent)
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        alarm.cancel(activePendingIntent)
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, activePendingIntent)

        // Commit the preferences
        editor.apply()

        val intent = Intent(this, IntroActivity::class.java)
        startActivity(intent)
    }

    // Check for any  peer requests and set it in the navigation view
    private fun setMenuCounter(@IdRes itemId: Int, count: Int) {
        val navigationView = findViewById(R.id.nav_view) as NavigationView
        val view = navigationView.menu.findItem(itemId).actionView as FrameLayout
        val viewText = view.findViewById(R.id.textView) as TextView
        viewText.text = if (count > 0) count.toString() else null
        if (count == 0)
            view.visibility = View.GONE
        else
            view.visibility = View.VISIBLE
    }

    fun initTabs() {
        // Create the mTasksAdapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = TabsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections mTasksAdapter.
        mViewPager = findViewById(R.id.container) as ViewPager
        if (mViewPager != null)
            mViewPager!!.adapter = mSectionsPagerAdapter

        // Initialise the tab layout and set it up with the pager
        val tabLayout = findViewById(R.id.tabs) as TabLayout
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager)
            mSecondaryColor = PreferenceManager.getDefaultSharedPreferences(this)
                    .getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent)
            tabLayout.setSelectedTabIndicatorColor(mPrimaryColor)

            // Set the custom view of the tabs
            val linearLayout = tabLayout.getChildAt(0) as LinearLayout
            linearLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            val drawable = GradientDrawable()
            drawable.setColor(resources.getColor(R.color.dividerColor))
            drawable.setSize(6, 6)
            linearLayout.dividerPadding = 0
            linearLayout.dividerDrawable = drawable
        }

        // Check if the activity was started from the NewTaskActivity
        // and automatically direct the tab to Tasks if it has
        val intent = intent
        if (intent.hasExtra(getString(R.string.INTENT_FLAG_RETURN_TO_TASKS))) {
            mViewPager!!.currentItem = 1
        }

    }

    fun initSpinner() {
        // Get a reference to the action bar and
        // disable its title display
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setBackgroundDrawable(ColorDrawable(mPrimaryColor))
        }

        // Get a reference to the spinner UI element
        // and set its mTasksAdapter and ItemClickListener
        val spinner = findViewById(R.id.spinner) as Spinner
        if (spinner != null) {
            // Set the mTasksAdapter of the spinner
            spinner.adapter = mSpinnerAdapter(
                    mToolbar.context,
                    arrayOf(resources.getString(R.string.schedule), resources.getString(R.string.tasks)))

            // Set the Listener of the spinner
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    spinnerPosition = position
                    // When the given dropdown item is selected, show its contents in the
                    // container view.
                    when (position) {
                        0 -> {
                            val scheduleFragment = ScheduleFragment()
                            val intent = intent
                            val RETURN_TO_SCHEDULE = intent.getStringExtra(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE))
                            if (RETURN_TO_SCHEDULE != null && RETURN_TO_SCHEDULE == getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE)) {
                                intent.putExtra(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE), "")
                                val args = Bundle()
                                args.putString(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE), RETURN_TO_SCHEDULE)
                                args.putInt(getString(R.string.INTENT_EXTRA_POSITION),
                                        intent.getIntExtra(getString(R.string.INTENT_EXTRA_POSITION), -1))
                                scheduleFragment.arguments = args
                            }
                            supportFragmentManager.beginTransaction()
                                    .replace(R.id.container, scheduleFragment)
                                    .commit()
                        }
                        1 -> supportFragmentManager.beginTransaction()
                                .replace(R.id.container, TasksFragment())
                                .commit()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            // Check if the activity was started from the tasks activity
            // and automatically switch back to tasks if it did
            val intent = intent
            if (intent.hasExtra(getString(R.string.INTENT_FLAG_RETURN_TO_TASKS)))
                spinner.setSelection(Utility.getIndex(spinner, spinner.getItemAtPosition(1).toString()))
        }
    }

    override fun OnTaskComplete(ID: Int, fID: String) {
        if (mFirebaseUser != null) {
            FirebaseDatabase.getInstance().reference
                    .child("users").child(mFirebaseUser!!.uid)
                    .child("tasks").child(fID).child("completed")
                    .setValue(true)
        } else {
            val dbHelper = DbHelper(this)
            val cursor = dbHelper.getTaskById(ID)
            cursor.moveToFirst()
            val pictureArray = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_PICTURE)).split("#seperate#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val pictureList = ArrayList<Uri>()
            var i = 0
            while (i > pictureArray.size) {
                pictureList.add(Uri.parse(pictureArray[i]))
                i++
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
                    true)
        }

        val fragment = TasksFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    override fun OnClassDelete(title: String) {
        if (mFirebaseUser != null) {
            FirebaseDatabase.getInstance().reference
                    .child("users").child(mFirebaseUser!!.uid)
                    .child("classes").child(title).removeValue()
        } else {
            val dbHelper = DbHelper(this)
            dbHelper.deleteScheduleItemByTitle(title)
        }

        val fragment = ScheduleFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    override fun OnTaskDelete(ID: Int, fID: String) {
        if (mFirebaseUser != null) {
            FirebaseDatabase.getInstance().reference
                    .child("users").child(mFirebaseUser!!.uid)
                    .child("tasks").child(fID).removeValue()
        } else {
            val dbHelper = DbHelper(this)
            dbHelper.deleteTaskItem(ID)
        }

        val fragment = TasksFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    inner class TabsPagerAdapter// Default public constructor
    (fm: FragmentManager) : FragmentPagerAdapter(fm) {

        // getItem is called to instantiate the fragment for the given page.
        // and return the corresponding fragment.
        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return ScheduleFragment()
                1 -> return TasksFragment()
                else -> {
                    Log.e(LOG_TAG, "Error creating new fragment at getItem")
                    return null
                }
            }
        }

        override fun getCount(): Int {
            // Show 2 total pages.
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return resources.getString(R.string.schedule)
                1 -> return resources.getString(R.string.task)
                else -> {
                    Log.e(LOG_TAG, "Error setting tab title at getPageTitle")
                    return null
                }
            }
        }
    }

    private class mSpinnerAdapter// Constructor method where helper is initialised
    (context: Context, objects: Array<String>) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, objects), ThemedSpinnerAdapter {

        private val mDropDownHelper: ThemedSpinnerAdapter.Helper

        init {
            mDropDownHelper = ThemedSpinnerAdapter.Helper(context)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View

            // If the given row is a new row
            // Inflate the dropdown menu using a simple list item layout
            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                val inflater = mDropDownHelper.dropDownViewInflater
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            } else {
                view = convertView
            }// If the given row is simply being recycled,
            // set the view to the recycled view

            // Set the text of the dropdown list item
            val textView = view.findViewById(android.R.id.text1) as TextView
            textView.text = getItem(position)

            return view
        }

        override fun getDropDownViewTheme(): Resources.Theme? {
            return mDropDownHelper.dropDownViewTheme
        }

        override fun setDropDownViewTheme(theme: Resources.Theme?) {
            mDropDownHelper.dropDownViewTheme = theme
        }
    }

    private fun updateWeekNumber() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        weekSettings = preferences.getString(getString(R.string.KEY_SETTINGS_WEEK_NUMBER), "0")

        val c = Calendar.getInstance()
        val weekOfYear = c.get(Calendar.WEEK_OF_YEAR)
        val savedWeekOfYear = preferences.getInt(getString(R.string.KEY_WEEK_OF_YEAR), 0)
        if (weekOfYear > savedWeekOfYear) {
            val editor = preferences.edit()
            val difference = weekOfYear - savedWeekOfYear
            if (difference % 2 > 0) {
                if (weekSettings == "0")
                    editor.putString(getString(R.string.KEY_WEEK_NUMBER), "1")
                else
                    editor.putString(getString(R.string.KEY_WEEK_NUMBER), "0")
            }
            editor.putInt(getString(R.string.KEY_WEEK_OF_YEAR), weekOfYear)
                    .apply()
        } else if (weekOfYear < savedWeekOfYear) {
            preferences.edit().putInt(getString(R.string.KEY_WEEK_OF_YEAR), weekOfYear)
                    .putString(getString(R.string.KEY_WEEK_NUMBER), "0")
                    .apply()
        }

    }

    private fun loadLogInView() {
        if (isTablet)
            startActivity(Intent(this, LoginActivity::class.java))
        else
            startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun logOut() {
        // Cancel online notifications
        Utility.rescheduleNotifications(this, false)

        // Execute the Sign Out Operation
        mFirebaseAuth!!.signOut()
        loggedIn = false
        logInOut.title = getString(R.string.action_login)

        // Sign out of Facebook (if applicable)
        LoginManager.getInstance().logOut()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }
}
