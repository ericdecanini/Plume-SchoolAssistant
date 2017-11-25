package com.pdt.plume

import android.app.*
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.support.annotation.IdRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.ThemedSpinnerAdapter
import android.support.v7.widget.Toolbar

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat.startActivity
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

import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pdt.plume.data.DbHelper
import com.pdt.plume.services.ClassNotificationReceiver

import java.util.ArrayList
import java.util.Calendar

import android.support.v4.view.PagerAdapter
import com.facebook.*
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.ShareOpenGraphAction
import com.facebook.share.model.ShareOpenGraphContent
import com.facebook.share.model.ShareOpenGraphObject
import com.facebook.share.widget.ShareDialog
import com.pdt.plume.R.bool.isTablet
import com.pdt.plume.StaticRequestCodes.REQUEST_STORAGE_PERMISSION

import com.pdt.plume.data.DbContract.TasksEntry
import com.pdt.plume.services.ActiveNotificationService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        TasksDetailFragment.OnTaskCompleteListener, TasksDetailFragment.OnTaskDeleteListener,
        ScheduleDetailFragment.OnClassDeleteListener {

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

    // Date Variables
    var year = -1
    var month = -1
    var day = -1
    val dateArgs = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) savedInstanceState.clear()
        FacebookSdk.sdkInitialize(applicationContext)
        setContentView(R.layout.activity_main)
        if (findViewById(R.id.fab) != null)
            fab = findViewById(R.id.fab) as FloatingActionButton
        isLandscape = resources.getBoolean(R.bool.isLandscape)
        updateWeekNumber()

        // Initialise Date
        val c = Calendar.getInstance()
        year = c.get(Calendar.YEAR)
        month = c.get(Calendar.MONTH)
        day = c.get(Calendar.DAY_OF_MONTH)

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

        // If its 2 or more days from first app launch, prompt for a review
        val twoDaysAfterFirstLaunch = Calendar.getInstance()
        twoDaysAfterFirstLaunch.timeInMillis = firstLaunchMillis
        twoDaysAfterFirstLaunch.set(Calendar.DAY_OF_YEAR, twoDaysAfterFirstLaunch.get(Calendar.DAY_OF_YEAR + 2))
        val reviewPrompted = preferences.getBoolean(getString(R.string.KEY_REVIEW_PROMPTED), false)

        if (!reviewPrompted && System.currentTimeMillis() > twoDaysAfterFirstLaunch.timeInMillis) {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.review_dialog_title))
                    .setMessage(getString(R.string.review_dialog_message))
                    .setNegativeButton(getString(R.string.not_now), null)
                    .setPositiveButton(getString(R.string.review), { _, _ ->
                        // TODO: Write intent for review
                        val intent = Intent()
                        startActivity(intent)
                    })
                    .show()
            preferences.edit().putBoolean(getString(R.string.KEY_REVIEW_PROMPTED), true).apply()
        }

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

        // View other day schedules
        if (header != null)
            header.setOnClickListener(View.OnClickListener {
                val datePickerDialog = DatePickerDialog(this@MainActivity, DatePickerDialog.OnDateSetListener { datePicker, y, m, d ->
                    val c = Calendar.getInstance()
                    c.set(y, m, d)
                    val current = Calendar.getInstance()
                    if (utility.datesMatch(c, current))
                        reset.visibility = View.GONE
                    else reset.visibility = View.VISIBLE
                    year = y
                    month = m
                    day = d
                    dateArgs.putInt("year", y)
                    dateArgs.putInt("month", m)
                    dateArgs.putInt("day", d)
                    header.text = utility.formatDateString(this@MainActivity, y, m, d)
                    if (isTablet) {
                        val fragment = ScheduleFragment()
                        fragment.arguments = dateArgs
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.container, fragment)
                                .commit()
                    } else {
                        if (mSectionsPagerAdapter != null) {
                            mSectionsPagerAdapter!!.notifyDataSetChanged()
                        }

                    }
                },
                        year, month, day)
                datePickerDialog.show()
            })

        if (!isTablet)
            reset.setOnClickListener({
                reset.visibility = View.GONE
                val c = Calendar.getInstance()
                year = c.get(Calendar.YEAR)
                month = c.get(Calendar.MONTH)
                day = c.get(Calendar.DAY_OF_MONTH)
                dateArgs.putInt("year", year)
                dateArgs.putInt("month", month)
                dateArgs.putInt("day", day)
                header.text = utility.formatDateString(this@MainActivity, year, month, day)
                if (isTablet) {
                    val fragment = ScheduleFragment()
                    fragment.arguments = dateArgs
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.container, fragment)
                            .commit()
                } else {
                    if (mSectionsPagerAdapter != null) {
                        mSectionsPagerAdapter!!.notifyDataSetChanged()
                    }

                }
            })

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

        Log.v(LOG_TAG, "Day: " + day)

        updateWeekNumber()

        // Set the header date
        if (!isTablet) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val headerTextView = findViewById(R.id.header) as TextView
            val subheader = findViewById(R.id.subheader) as TextView
            val weekType = preferences.getString(getString(R.string.KEY_PREFERENCE_WEEKTYPE), "0")
            if (weekType == "0") subheader.visibility = View.GONE
            headerTextView.text = utility.formatDateString(this, year,
                    month, day)
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

        val textColor = preferences.getInt(getString(R.string.KEY_THEME_TEXT_COLOUR), resources.getColor(R.color.gray_900))
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
            val notifIntent = Intent(this, ClassNotificationReceiver::class.java)
            notifIntent.action = "com.pdt.plume.NOTIFICATION"
            val pendingIntent = PendingIntent.getBroadcast(this,
                    57, notifIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            // Set the time for the class notification service
            val calendar = Calendar.getInstance()

            val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.cancel(pendingIntent)

//            alarm.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
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

            R.id.nav_requests -> startActivity(Intent(this, RequestsActivity::class.java))

            R.id.nav_completedTasks -> startActivity(Intent(this, CompletedTasksActivity::class.java))

            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            R.id.nav_timetable -> startActivity(Intent(this, TimetableActivity::class.java))

            R.id.nav_share_facebook -> {
                val content = ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.pdt.plume"))
                        .build()
                ShareDialog.show(this, content)
            }

            R.id.nav_share_twitter -> {
                shareToTwitter()
            }

            R.id.nav_review -> {
                val uri = Uri.parse("market://details?id=" + packageName)
                val openPlayStore = Intent(Intent.ACTION_VIEW, uri)
                try {
                    startActivity(openPlayStore)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show()
                }

            }

        }

        // Close the navigation drawer upon item selection
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer?.closeDrawer(GravityCompat.START)

        return true
    }

    fun shareToTwitter() {
        val tweetIntent = Intent(Intent.ACTION_SEND)
        val message = "${getString(R.string.intent_share)}\n\nhttps://play.google.com/store/apps/details?id=com.pdt.plume"
        tweetIntent.putExtra(Intent.EXTRA_TEXT, message)
        tweetIntent.type = "text/plain"

        val packManager = packageManager
        val resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

        var resolved = false
        for (resolveInfo in resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name)
                resolved = true
                break
            }
        }
        if (resolved) {
            startActivity(tweetIntent);
        } else {
            val i = Intent()
            val message = "${getString(R.string.intent_share)}\nhttps://play.google.com/store/apps/details?id=com.pdt.plume"
            i.putExtra(Intent.EXTRA_TEXT, message)
            i.action = Intent.ACTION_VIEW
            i.data = Uri.parse("https://twitter.com/intent/tweet?text=" + urlEncode("https://play.google.com/store/apps/details?id=com.pdt.plume"))
            startActivity(i)
            Toast.makeText(this, "Twitter app isn't found", Toast.LENGTH_LONG).show()
        }
    }

    private fun urlEncode(s: String): String {
        return try {
            URLEncoder.encode(s, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            Log.wtf(LOG_TAG, "UTF-8 should always be supported", e)
            ""
        }

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
        // Get the intent for the class notification service
        val notifIntent = Intent(this, ClassNotificationReceiver::class.java)
        notifIntent.action = "com.pdt.plume.NOTIFICATION"
        val pendingIntent = PendingIntent.getBroadcast(this,
                57, notifIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        // Get the intent for the active notification service
        val activeNotifIntent = Intent(this, ActiveNotificationService::class.java)
        activeNotifIntent.action = "com.pdt.plume.NOTIFICATION"
        val activePendingIntent = PendingIntent.getBroadcast(this,
                58, notifIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        // Set the time for the class notification service
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 1)
        calendar.set(Calendar.MINUTE, 1)

        // Set the alarm for the class notification service
        val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent)
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)

        // Set the time for the active notification service
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
        // disable its category display
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

        override fun getItemPosition(`object`: Any?): Int = PagerAdapter.POSITION_NONE

        // getItem is called to instantiate the fragment for the given page.
        // and return the corresponding fragment.
        override fun getItem(position: Int): Fragment? {
            Log.v(LOG_TAG, "getItem")
            val scheduleFragment = ScheduleFragment()
            scheduleFragment.arguments = dateArgs
            when (position) {
                0 -> return scheduleFragment
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
                    Log.e(LOG_TAG, "Error setting tab category at getPageTitle")
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
