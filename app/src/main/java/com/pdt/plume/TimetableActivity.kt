package com.pdt.plume

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pdt.plume.data.DbContract
import com.pdt.plume.data.DbHelper
import kotlinx.android.synthetic.main.activity_timetable.*
import java.util.*
import kotlin.collections.ArrayList

class TimetableActivity : AppCompatActivity(), NumberPicker.OnValueChangeListener {

    override fun onValueChange(p0: NumberPicker?, p1: Int, p2: Int) {

    }

    val LOG_TAG = TimetableActivity::class.java.simpleName
    val utility = Utility()

    // Firebase variables
    var mFirebaseUser: FirebaseUser? = null
    lateinit var mUserId: String

    // List variables
    val sundayList = ArrayList<Schedule>()
    val mondayList = ArrayList<Schedule>()
    val tuesdayList = ArrayList<Schedule>()
    val wednesdayList = ArrayList<Schedule>()
    val thursdayList = ArrayList<Schedule>()
    val fridayList = ArrayList<Schedule>()
    val saturdayList = ArrayList<Schedule>()

    val sundayDurationList = ArrayList<Long>()
    val mondayDurationList = ArrayList<Long>()
    val tuesdayDurationList = ArrayList<Long>()
    val wednesdayDurationList = ArrayList<Long>()
    val thursdayDurationList = ArrayList<Long>()
    val fridayDurationList = ArrayList<Long>()
    val saturdayDurationList = ArrayList<Long>()

    val sundayTimeIns = ArrayList<Long>()
    val mondayTimeIns = ArrayList<Long>()
    val tuesdayTimeIns = ArrayList<Long>()
    val wednesdayTimeIns = ArrayList<Long>()
    val thursdayTimeIns = ArrayList<Long>()
    val fridayTimeIns = ArrayList<Long>()
    val saturdayTimeIns = ArrayList<Long>()

    val allDurations = ArrayList<Long>()
    val allTimeIns = ArrayList<Long>()
    val allTimeOuts = ArrayList<Long>()
    val periodTimeIns = ArrayList<Long>()

    // Theme variables
    private var mPrimaryColour: Int = 0
    private var mDarkColour: Int = 0
    private var mSecondaryColour: Int = 0
    private var mBackgroundColour: Int = 0
    private var mDarkBackgroundColour: Int = 0
    private var mTextColour: Int = 0

    var timetableGenerated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        // Initialise Firebase
        mFirebaseUser = FirebaseAuth.getInstance().currentUser
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser!!.uid

        val observer = timetable.viewTreeObserver;
        observer.addOnGlobalLayoutListener({ ->
            // Generate the timetable
            if (!timetableGenerated) {
                generateTimetable()
                timetableGenerated = true
            }
        })
    }

    override fun onStart() {
        super.onStart()

        // Initialise the theme variables
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        mPrimaryColour = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), resources.getColor(R.color.colorPrimary))
        mSecondaryColour = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), resources.getColor(R.color.colorAccent))
        mBackgroundColour = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), resources.getColor(R.color.backgroundColor))
        mTextColour = preferences.getInt(getString(R.string.KEY_THEME_TEXT_COLOUR), resources.getColor(R.color.black_0_54))
        var hsv = FloatArray(3)
        Color.colorToHSV(mPrimaryColour, hsv)
        hsv[2] *= 0.8f
        mDarkColour = Color.HSVToColor(hsv)
        Color.colorToHSV(mBackgroundColour, hsv)
        hsv[2] *= 0.9f
        mDarkBackgroundColour = Color.HSVToColor(hsv)


        // Apply the theme
        if (supportActionBar != null)
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(mPrimaryColour))

        master_layout.setBackgroundColor(mBackgroundColour)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = mDarkColour
            spinner.indeterminateTintList = ColorStateList.valueOf(mSecondaryColour)
        }

        // Set text and background colour of day headers
        sun.setTextColor(mPrimaryColour)
        mon.setTextColor(mPrimaryColour)
        tue.setTextColor(mPrimaryColour)
        wed.setTextColor(mPrimaryColour)
        thu.setTextColor(mPrimaryColour)
        fri.setTextColor(mPrimaryColour)
        sat.setTextColor(mPrimaryColour)

        sun.setBackgroundColor(mDarkBackgroundColour)
        mon.setBackgroundColor(mDarkBackgroundColour)
        tue.setBackgroundColor(mDarkBackgroundColour)
        wed.setBackgroundColor(mDarkBackgroundColour)
        thu.setBackgroundColor(mDarkBackgroundColour)
        fri.setBackgroundColor(mDarkBackgroundColour)
        sat.setBackgroundColor(mDarkBackgroundColour)

    }

    private fun generateTimetable() {
        // Refresh layout and show progress bar
        spinner.visibility = View.VISIBLE
        timetable.removeAllViews()

        // Get preference for schedule type and week number
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val scheduleType = preferences.getString(getString(R.string.KEY_PREFERENCE_BASIS), "0")
        val weekType = preferences.getString(getString(R.string.KEY_PREFERENCE_WEEKTYPE), "0")
        var weekNumber = preferences.getString(getString(R.string.KEY_WEEK_NUMBER), "0")

        if (weekType == "0")
            weekNumber = "0"

        // Download the data and store them in the array lists
        if (mFirebaseUser != null) {
            // Get data from Firebase
            Log.v(LOG_TAG, "Registering listener...")
            FirebaseDatabase.getInstance().reference
                    .child("users").child(mUserId)
                    .child("classes").addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(p0: DataSnapshot?) {
                    // Hide the spinner and splash
                    Log.v(LOG_TAG, "OnDataChange")
                    splash.visibility = View.GONE
                    spinner.visibility = View.GONE

                    // Loop through the class snapshots, get their occurrence and time in and out
                    // Then add add them to the appropriate list
                    for (classSnapshot in p0!!.children) {
                        val title = classSnapshot.key
                        val icon = classSnapshot.child("icon").value as String? ?: return
                        val teacher = classSnapshot.child("teacher").value as String
                        val room = classSnapshot.child("room").value as String

                        // Create the lists of occurrences and timeins and outs
                        val occurrences = ArrayList<String>()
                        val timeins = ArrayList<Long>()
                        val timeouts = ArrayList<Long>()
                        val periods = ArrayList<String>()

                        // Loop through their corresponding snapshots

                        // Use alt if week 2
                        var timeinchild: String
                        var timeoutchild: String
                        if (weekType == "0" || weekNumber == "0") {
                            timeinchild = "timein"
                            timeoutchild = "timeout"
                        } else {
                            timeinchild = "timeinalt"
                            timeoutchild = "timeoutalt"
                        }

                        for (occurrenceSnapshot in classSnapshot.child("occurrence").children)
                            occurrences.add(occurrenceSnapshot.value as String)
                        for (timeinSnapshot in classSnapshot.child(timeinchild).children)
                            timeins.add(timeinSnapshot.value as Long)
                        for (timeoutSnapshot in classSnapshot.child(timeoutchild).children)
                            timeouts.add(timeoutSnapshot.value as Long)
                        for (periodsSnapshot in classSnapshot.child("periods").children)
                            periods.add(periodsSnapshot.value as String)

                        allTimeIns.addAll(timeins)
                        allTimeOuts.addAll(timeouts)

                        // Loop through a number of days to match occurrences and place them into lists
                        for (i in 0 until occurrences.size) {
                            for (day in 0..6) {
                                // Use utility matching method to match occurrences and add them to corresponding lists
                                if (utility.occurrenceMatchesCurrentDay(this@TimetableActivity, occurrences[i],
                                        periods[i], weekNumber, day)) {
                                    val schedule = Schedule(this@TimetableActivity, icon, title, teacher,
                                            room, "", "", periods[i])
                                    val duration = timeouts[i] - timeins[i]

                                    when (day) {
                                        1 -> {
                                            sundayList.add(schedule)
                                            sundayDurationList.add(duration)
                                            sundayTimeIns.add(timeins[i])
                                        }
                                        2 -> {
                                            mondayList.add(schedule)
                                            mondayDurationList.add(duration)
                                            mondayTimeIns.add(timeins[i])
                                        }
                                        3 -> {
                                            tuesdayList.add(schedule)
                                            tuesdayDurationList.add(duration)
                                            tuesdayTimeIns.add(timeins[i])
                                        }
                                        4 -> {
                                            wednesdayList.add(schedule)
                                            wednesdayDurationList.add(duration)
                                            wednesdayTimeIns.add(timeins[i])
                                        }
                                        5 -> {
                                            thursdayList.add(schedule)
                                            thursdayDurationList.add(duration)
                                            thursdayTimeIns.add(timeins[i])
                                        }
                                        6 -> {
                                            fridayList.add(schedule)
                                            fridayDurationList.add(duration)
                                            fridayTimeIns.add(timeins[i])
                                        }
                                        0 -> {
                                            saturdayList.add(schedule)
                                            saturdayDurationList.add(duration)
                                            saturdayTimeIns.add(timeins[i])
                                        }
                                    }

                                    allDurations.add(duration)
                                }
                            }
                        }
                    }

                    // Data is gathered. Generate the UI
                    applyDataToUI()
                }

                override fun onCancelled(p0: DatabaseError?) {
                    spinner.visibility = View.GONE
                    splash.visibility = View.VISIBLE
                    Log.v(LOG_TAG, "OnCancelled")
                }

            })
        } else {
            // Get the data from SQLite
            splash.visibility = View.GONE
            spinner.visibility = View.GONE

            val dbHelper = DbHelper(this)
            val cursor = dbHelper.allClassesData

            // Loop through each class in the cursor and add its data into the array lists
            for (position in 0 until cursor.count) {
                cursor.moveToPosition(position)

                val title = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE))
                val icon = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON))
                val teacher = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TEACHER))
                val room = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ROOM))

                val occurrence = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_OCCURRENCE))
                val periods = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_PERIODS))

                var timeInIndex: String
                var timeOutIndex: String
                if (weekNumber == "0" || weekType == "0") {
                    timeInIndex = DbContract.ScheduleEntry.COLUMN_TIMEIN
                    timeOutIndex = DbContract.ScheduleEntry.COLUMN_TIMEOUT
                } else {
                    timeInIndex = DbContract.ScheduleEntry.COLUMN_TIMEIN_ALT
                    timeOutIndex = DbContract.ScheduleEntry.COLUMN_TIMEOUT_ALT
                }

                val timein = cursor.getLong(cursor.getColumnIndex(timeInIndex))
                val timeout = cursor.getLong(cursor.getColumnIndex(timeOutIndex))
                allTimeIns.add(timein)
                allTimeOuts.add(timeout)

                // Check and add the data into the list
                for (day in 0..6) {
                    // Use utility matching method to match occurrences and add them to corresponding lists
                    if (utility.occurrenceMatchesCurrentDay(this@TimetableActivity, occurrence,
                            periods, weekNumber, day)) {
                        val schedule = Schedule(this@TimetableActivity, icon, title, teacher,
                                room, "", "", periods)
                        val duration = timeout - timein

                        when (day) {
                            1 -> {
                                sundayList.add(schedule)
                                sundayDurationList.add(duration)
                                sundayTimeIns.add(timein)
                            }
                            2 -> {
                                mondayList.add(schedule)
                                mondayDurationList.add(duration)
                                mondayTimeIns.add(timein)
                            }
                            3 -> {
                                tuesdayList.add(schedule)
                                tuesdayDurationList.add(duration)
                                tuesdayTimeIns.add(timein)
                            }
                            4 -> {
                                wednesdayList.add(schedule)
                                wednesdayDurationList.add(duration)
                                wednesdayTimeIns.add(timein)
                            }
                            5 -> {
                                thursdayList.add(schedule)
                                thursdayDurationList.add(duration)
                                thursdayTimeIns.add(timein)
                            }
                            6 -> {
                                fridayList.add(schedule)
                                fridayDurationList.add(duration)
                                fridayTimeIns.add(timein)
                            }
                            0 -> {
                                saturdayList.add(schedule)
                                saturdayDurationList.add(duration)
                                saturdayTimeIns.add(timein)
                            }
                        }

                        allDurations.add(duration)
                    }
                }
            }
            // Data is gathered. Generate the UI
            applyDataToUI()
        }
    }

    private fun applyDataToUI() {
        // Create the timetable
        // Set weekend columns visible if there are classes taking place
        var columnNum = 5
        var sundayIsVisible = 0

        if (sundayList.size > 0) {
            sun.visibility = View.VISIBLE
            columnNum++
            sundayIsVisible = 1
        }
        if (saturdayList.size > 0) {
            sat.visibility = View.VISIBLE
            columnNum++
        }

        Log.v(LOG_TAG, "All durations size: ${allDurations.size}")
        if (allDurations.size == 0) return

        // Find the most common, earliest, and latest duration of a class
        val modeDuration = getPopularElement(allDurations.toLongArray())
        val earliestTimeIn = getSmallestElement(allTimeIns.toLongArray())
        var latestTimeIn = getLargestElement(allTimeIns.toLongArray())
        val latestTimeOut = getLargestElement(allTimeOuts.toLongArray())

        if (latestTimeIn == earliestTimeIn) latestTimeIn++

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        var periodDuration = preferences.getLong(getString(R.string.KEY_PREFERENCES_PERIOD_DURATION), -1)
        if (periodDuration < 1) {
            periodDuration = modeDuration
            preferences.edit().putLong(getString(R.string.KEY_PREFERENCES_PERIOD_DURATION), periodDuration)
                    .apply()
        }

        // Calculate the coordinates and dimensions of the timetable
        val fullTimetableHeight = timetable.height.toLong()
        val timetableY = timetable.y.toLong()

        val density = resources.displayMetrics.density
        val columnWidth = (resources.displayMetrics.widthPixels - (32 * density)) / columnNum
        val maxViewHeight = mapValues(getLargestElement(allDurations.toLongArray()), 0, latestTimeOut - earliestTimeIn, 0, fullTimetableHeight).toInt()

        var timetableHeight = fullTimetableHeight
        val newMaxViewHeight = maxViewHeight

        val twentyFourHours = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.KEY_SETTINGS_CLOCK_FORMAT), true)
        val index = if (twentyFourHours) 0 else 1

        // Do Sunday periods
        for (i in 0 until sundayList.size) {
            val title = sundayList[i].scheduleLesson
            val icon = sundayList[i].scheduleIcon
            val timein = sundayTimeIns[i]
            val duration = sundayDurationList[i]
            val colour = Utility.getColorFromIcon(this@TimetableActivity, icon)

            // Map the values
            val viewHeight = mapValues(duration, 0, latestTimeOut - earliestTimeIn, 0, timetableHeight).toInt()
            val viewWidth = columnWidth.toInt()

            // Create the background
            // Background layer 1
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
            val colourBitmap = Bitmap.createBitmap(iconBitmap.width, iconBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(colourBitmap)
            canvas.drawColor(colour)
            val backgroundTile = Utility.scaleDown(overlayBitmaps(colourBitmap, iconBitmap), viewWidth / 2f, false)

            // Background layer 2
            val skimBitmap = Utility.convertDrawableResToBitmap(this, R.drawable.shape_scrim_centre, viewWidth, viewHeight)
            var tileBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565)
            val canvas1 = Canvas(tileBitmap)
            val paint = Paint()
            paint.shader = BitmapShader(backgroundTile, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
            canvas1.drawPaint(paint)
            val background = BitmapDrawable(overlayBitmaps(tileBitmap, skimBitmap))

            // Create the view
            val period = TextView(this@TimetableActivity)
            period.text = "$title\n${utility.millisToHourTime(this, timein).split(";")[index]}"
            period.setTextColor(resources.getColor(R.color.white))
            if (icon.contains("art_") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                period.background = background
            else period.setBackgroundColor(colour)
            period.gravity = Gravity.CENTER
            period.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                period.transitionName = getString(R.string.INTENT_EXTRA_TRANSITION)
            }

            // Add the view and set the position
            timetable.addView(period)
            val position = 0
            period.x = 32 * density + columnWidth * position
            period.y = mapValues(timein, earliestTimeIn, latestTimeOut, 0, timetableHeight).toFloat()

            // Add the click listener of the view to go to ScheduleDetailActivity
            period.setOnClickListener({ v ->
                val intent = Intent(this, ScheduleDetailActivity::class.java)
                intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title)
                intent.putExtra("icon", icon)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && icon.contains("art_")) {
                    val bundle = ActivityOptions.makeSceneTransitionAnimation(this, v, v.transitionName).toBundle()
                    startActivity(intent, bundle)
                } else {
                    startActivity(intent)
                }
            })
        }

        // Do Monday periods
        for (i in 0 until mondayList.size) {
            val title = mondayList[i].scheduleLesson
            val icon = mondayList[i].scheduleIcon
            val timein = mondayTimeIns[i]
            val duration = mondayDurationList[i]
            val colour = Utility.getColorFromIcon(this@TimetableActivity, icon)

            // Map the values
            val viewHeight = mapValues(duration, 0, latestTimeOut - earliestTimeIn, 0, timetableHeight).toInt()
            val viewWidth = columnWidth.toInt()

            // Create the background
            // Background layer 1
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
            val colourBitmap = Bitmap.createBitmap(iconBitmap.width, iconBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(colourBitmap)
            canvas.drawColor(colour)
            val backgroundTile = Utility.scaleDown(overlayBitmaps(colourBitmap, iconBitmap), viewWidth / 2f, false)

            // Background layer 2
            val skimBitmap = Utility.convertDrawableResToBitmap(this, R.drawable.shape_scrim_centre, viewWidth, viewHeight)
            var tileBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565)
            val canvas1 = Canvas(tileBitmap)
            val paint = Paint()
            paint.shader = BitmapShader(backgroundTile, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
            canvas1.drawPaint(paint)
            val background = BitmapDrawable(overlayBitmaps(tileBitmap, skimBitmap))

            // Create the view
            val period = TextView(this@TimetableActivity)
            period.text = "$title\n${utility.millisToHourTime(this, timein).split(";")[index]}"
            period.setTextColor(resources.getColor(R.color.white))
            if (icon.contains("art_") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                period.background = background
            else period.setBackgroundColor(colour)
            period.gravity = Gravity.CENTER
            period.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                period.transitionName = getString(R.string.INTENT_EXTRA_TRANSITION)
            }

            // Add the view and set the position
            timetable.addView(period)
            val position = 0 + sundayIsVisible
            period.x = 32 * density + columnWidth * position
            period.y = mapValues(timein, earliestTimeIn, latestTimeOut, 0, timetableHeight).toFloat()

            // Add the click listener of the view to go to ScheduleDetailActivity
            period.setOnClickListener({ v ->
                val intent = Intent(this, ScheduleDetailActivity::class.java)
                intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title)
                intent.putExtra("icon", icon)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && icon.contains("art_")) {
                    val bundle = ActivityOptions.makeSceneTransitionAnimation(this, v, v.transitionName).toBundle()
                    startActivity(intent, bundle)
                } else {
                    startActivity(intent)
                }
            })
        }

        // Do Tuesday periods
        for (i in 0 until tuesdayList.size) {
            val title = tuesdayList[i].scheduleLesson
            val icon = tuesdayList[i].scheduleIcon
            val timein = tuesdayTimeIns[i]
            val duration = tuesdayDurationList[i]
            val colour = Utility.getColorFromIcon(this@TimetableActivity, icon)

            // Map the values
            val viewHeight = mapValues(duration, 0, latestTimeOut - earliestTimeIn, 0, timetableHeight).toInt()
            val viewWidth = columnWidth.toInt()

            // Create the background
            // Background layer 1
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
            val colourBitmap = Bitmap.createBitmap(iconBitmap.width, iconBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(colourBitmap)
            canvas.drawColor(colour)
            val backgroundTile = Utility.scaleDown(overlayBitmaps(colourBitmap, iconBitmap), viewWidth / 2f, false)

            // Background layer 2
            val skimBitmap = Utility.convertDrawableResToBitmap(this, R.drawable.shape_scrim_centre, viewWidth, viewHeight)
            var tileBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565)
            val canvas1 = Canvas(tileBitmap)
            val paint = Paint()
            paint.shader = BitmapShader(backgroundTile, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
            canvas1.drawPaint(paint)
            val background = BitmapDrawable(overlayBitmaps(tileBitmap, skimBitmap))

            // Create the view
            val period = TextView(this@TimetableActivity)
            period.text = "$title\n${utility.millisToHourTime(this, timein).split(";")[index]}"
            period.setTextColor(resources.getColor(R.color.white))
            if (icon.contains("art_") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                period.background = background
            else period.setBackgroundColor(colour)
            period.gravity = Gravity.CENTER
            period.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                period.transitionName = getString(R.string.INTENT_EXTRA_TRANSITION)
            }

            // Add the view and set the position
            timetable.addView(period)
            val position = 1 + sundayIsVisible
            period.x = 32 * density + columnWidth * position
            period.y = mapValues(timein, earliestTimeIn, latestTimeOut, 0, timetableHeight).toFloat()

            // Add the click listener of the view to go to ScheduleDetailActivity
            period.setOnClickListener({ v ->
                val intent = Intent(this, ScheduleDetailActivity::class.java)
                intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title)
                intent.putExtra("icon", icon)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && icon.contains("art_")) {
                    val bundle = ActivityOptions.makeSceneTransitionAnimation(this, v, v.transitionName).toBundle()
                    startActivity(intent, bundle)
                } else {
                    startActivity(intent)
                }
            })
        }

        // Do Wednesday periods
        for (i in 0 until wednesdayList.size) {
            val title = wednesdayList[i].scheduleLesson
            val icon = wednesdayList[i].scheduleIcon
            val timein = wednesdayTimeIns[i]
            val duration = wednesdayDurationList[i]
            val colour = Utility.getColorFromIcon(this@TimetableActivity, icon)

            // Map the values
            val viewHeight = mapValues(duration, 0, latestTimeOut - earliestTimeIn, 0, timetableHeight).toInt()
            val viewWidth = columnWidth.toInt()

            // Create the background
            // Background layer 1
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
            val colourBitmap = Bitmap.createBitmap(iconBitmap.width, iconBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(colourBitmap)
            canvas.drawColor(colour)
            val backgroundTile = Utility.scaleDown(overlayBitmaps(colourBitmap, iconBitmap), viewWidth / 2f, false)

            // Background layer 2
            val skimBitmap = Utility.convertDrawableResToBitmap(this, R.drawable.shape_scrim_centre, viewWidth, viewHeight)
            var tileBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565)
            val canvas1 = Canvas(tileBitmap)
            val paint = Paint()
            paint.shader = BitmapShader(backgroundTile, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
            canvas1.drawPaint(paint)
            val background = BitmapDrawable(overlayBitmaps(tileBitmap, skimBitmap))

            // Create the view
            val period = TextView(this@TimetableActivity)
            period.text = "$title\n${utility.millisToHourTime(this, timein).split(";")[index]}"
            period.setTextColor(resources.getColor(R.color.white))
            if (icon.contains("art_") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                period.background = background
            else period.setBackgroundColor(colour)
            period.gravity = Gravity.CENTER
            period.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                period.transitionName = getString(R.string.INTENT_EXTRA_TRANSITION)
            }

            // Add the view and set the position
            timetable.addView(period)
            val position = 2 + sundayIsVisible
            period.x = 32 * density + columnWidth * position
            period.y = mapValues(timein, earliestTimeIn, latestTimeOut, 0, timetableHeight).toFloat()

            // Add the click listener of the view to go to ScheduleDetailActivity
            period.setOnClickListener({ v ->
                val intent = Intent(this, ScheduleDetailActivity::class.java)
                intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title)
                intent.putExtra("icon", icon)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && icon.contains("art_")) {
                    val bundle = ActivityOptions.makeSceneTransitionAnimation(this, v, v.transitionName).toBundle()
                    startActivity(intent, bundle)
                } else {
                    startActivity(intent)
                }
            })
        }

        // Do Thursday periods
        for (i in 0 until thursdayList.size) {
            val title = thursdayList[i].scheduleLesson
            val icon = thursdayList[i].scheduleIcon
            val timein = thursdayTimeIns[i]
            val duration = thursdayDurationList[i]
            val colour = Utility.getColorFromIcon(this@TimetableActivity, icon)

            // Map the values
            val viewHeight = mapValues(duration, 0, latestTimeOut - earliestTimeIn, 0, timetableHeight).toInt()
            val viewWidth = columnWidth.toInt()

            // Create the background
            // Background layer 1
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
            val colourBitmap = Bitmap.createBitmap(iconBitmap.width, iconBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(colourBitmap)
            canvas.drawColor(colour)
            val backgroundTile = Utility.scaleDown(overlayBitmaps(colourBitmap, iconBitmap), viewWidth / 2f, false)

            // Background layer 2
            val skimBitmap = Utility.convertDrawableResToBitmap(this, R.drawable.shape_scrim_centre, viewWidth, viewHeight)
            var tileBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565)
            val canvas1 = Canvas(tileBitmap)
            val paint = Paint()
            paint.shader = BitmapShader(backgroundTile, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
            canvas1.drawPaint(paint)
            val background = BitmapDrawable(overlayBitmaps(tileBitmap, skimBitmap))

            // Create the view
            val period = TextView(this@TimetableActivity)
            period.text = "$title\n${utility.millisToHourTime(this, timein).split(";")[index]}"
            period.setTextColor(resources.getColor(R.color.white))
            if (icon.contains("art_") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                period.background = background
            else period.setBackgroundColor(colour)
            period.gravity = Gravity.CENTER
            period.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                period.transitionName = getString(R.string.INTENT_EXTRA_TRANSITION)
            }

            // Add the view and set the position
            timetable.addView(period)
            val position = 3 + sundayIsVisible
            period.x = 32 * density + columnWidth * position
            period.y = mapValues(timein, earliestTimeIn, latestTimeOut, 0, timetableHeight).toFloat()

            // Add the click listener of the view to go to ScheduleDetailActivity
            period.setOnClickListener({ v ->
                val intent = Intent(this, ScheduleDetailActivity::class.java)
                intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title)
                intent.putExtra("icon", icon)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && icon.contains("art_")) {
                    val bundle = ActivityOptions.makeSceneTransitionAnimation(this, v, v.transitionName).toBundle()
                    startActivity(intent, bundle)
                } else {
                    startActivity(intent)
                }
            })
        }

        // Do Friday periods
        for (i in 0 until fridayList.size) {
            val title = fridayList[i].scheduleLesson
            val icon = fridayList[i].scheduleIcon
            val timein = fridayTimeIns[i]
            val duration = fridayDurationList[i]
            val colour = Utility.getColorFromIcon(this@TimetableActivity, icon)

            // Map the values
            Log.v(LOG_TAG, "Timetable stuff: $duration, $latestTimeOut, $earliestTimeIn, $timetableHeight")
            val viewHeight = mapValues(duration, 0, latestTimeOut - earliestTimeIn, 0, timetableHeight).toInt()
            val viewWidth = columnWidth.toInt()

            // Create the background
            // Background layer 1
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
            val colourBitmap = Bitmap.createBitmap(iconBitmap.width, iconBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(colourBitmap)
            canvas.drawColor(colour)
            val backgroundTile = Utility.scaleDown(overlayBitmaps(colourBitmap, iconBitmap), viewWidth / 2f, false)

            // Background layer 2
            val skimBitmap = Utility.convertDrawableResToBitmap(this, R.drawable.shape_scrim_centre, viewWidth, viewHeight)
            var tileBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565)
            val canvas1 = Canvas(tileBitmap)
            val paint = Paint()
            paint.shader = BitmapShader(backgroundTile, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
            canvas1.drawPaint(paint)
            val background = BitmapDrawable(overlayBitmaps(tileBitmap, skimBitmap))

            // Create the view
            val period = TextView(this@TimetableActivity)
            period.text = "$title\n${utility.millisToHourTime(this, timein).split(";")[index]}"
            period.setTextColor(resources.getColor(R.color.white))
            if (icon.contains("art_") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                period.background = background
            else period.setBackgroundColor(colour)
            period.gravity = Gravity.CENTER
            period.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                period.transitionName = getString(R.string.INTENT_EXTRA_TRANSITION)
            }

            // Add the view and set the position
            timetable.addView(period)
            val position = 4 + sundayIsVisible
            period.x = 32 * density + columnWidth * position
            period.y = mapValues(timein, earliestTimeIn, latestTimeOut, 0, timetableHeight).toFloat()

            // Add the click listener of the view to go to ScheduleDetailActivity
            period.setOnClickListener({ v ->
                val intent = Intent(this, ScheduleDetailActivity::class.java)
                intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title)
                intent.putExtra("icon", icon)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && icon.contains("art_")) {
                    val bundle = ActivityOptions.makeSceneTransitionAnimation(this, v, v.transitionName).toBundle()
                    startActivity(intent, bundle)
                } else {
                    startActivity(intent)
                }
            })
        }

        // Do Saturday periods
        for (i in 0 until saturdayList.size) {
            val title = saturdayList[i].scheduleLesson
            val icon = saturdayList[i].scheduleIcon
            val timein = saturdayTimeIns[i]
            val duration = saturdayDurationList[i]
            val colour = Utility.getColorFromIcon(this@TimetableActivity, icon)

            // Map the values
            val viewHeight = mapValues(duration, 0, latestTimeOut - earliestTimeIn, 0, timetableHeight).toInt()
            val viewWidth = columnWidth.toInt()

            // Create the background
            // Background layer 1
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
            val colourBitmap = Bitmap.createBitmap(iconBitmap.width, iconBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(colourBitmap)
            canvas.drawColor(colour)
            val backgroundTile = Utility.scaleDown(overlayBitmaps(colourBitmap, iconBitmap), viewWidth / 2f, false)

            // Background layer 2
            val skimBitmap = Utility.convertDrawableResToBitmap(this, R.drawable.shape_scrim_centre, viewWidth, viewHeight)
            var tileBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565)
            val canvas1 = Canvas(tileBitmap)
            val paint = Paint()
            paint.shader = BitmapShader(backgroundTile, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
            canvas1.drawPaint(paint)
            val background = BitmapDrawable(overlayBitmaps(tileBitmap, skimBitmap))

            // Create the view
            val period = TextView(this@TimetableActivity)
            period.text = "$title\n${utility.millisToHourTime(this, timein).split(";")[index]}"
            period.setTextColor(resources.getColor(R.color.white))
            if (icon.contains("art_") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                period.background = background
            else period.setBackgroundColor(colour)
            period.gravity = Gravity.CENTER
            period.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                period.transitionName = getString(R.string.INTENT_EXTRA_TRANSITION)
            }

            // Add the view and set the position
            timetable.addView(period)
            val position = 5 + sundayIsVisible
            period.x = 32 * density + columnWidth * position
            period.y = mapValues(timein, earliestTimeIn, latestTimeOut, 0, timetableHeight).toFloat()

            // Add the click listener of the view to go to ScheduleDetailActivity
            period.setOnClickListener({ v ->
                val intent = Intent(this, ScheduleDetailActivity::class.java)
                intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title)
                intent.putExtra("icon", icon)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && icon.contains("art_")) {
                    val bundle = ActivityOptions.makeSceneTransitionAnimation(this, v, v.transitionName).toBundle()
                    startActivity(intent, bundle)
                } else {
                    startActivity(intent)
                }
            })
        }

        // Add the period signs
        val viewWidth = (32 * density).toInt()
        val viewHeight = mapValues(periodDuration, 0, latestTimeOut - earliestTimeIn, 0, timetableHeight).toInt()

        // Copy the values into a temp array list to check for double periods
        val tempArrayList = ArrayList<Long>()
        for (i in 0 until allTimeIns.size) {
            if (allDurations[i] >= periodDuration - 600000 && !tempArrayList.contains(allTimeIns[i])) {
                tempArrayList.add(allTimeIns[i])
                val remainingPeriodTime = allDurations[i] - periodDuration
                if (remainingPeriodTime >= periodDuration - 600000) {
                    val secondTimeIn = allTimeIns[i] + periodDuration
                    if (!tempArrayList.contains(secondTimeIn))
                        tempArrayList.add(secondTimeIn)
                }
            }
        }

        periodTimeIns.clear()
        periodTimeIns.addAll(tempArrayList)
        tempArrayList.clear()
        periodTimeIns.sort()

        // Check for any free periods
        tempArrayList.addAll(periodTimeIns)
        for (i in 0 until periodTimeIns.size) {
            if (i + 2 < periodTimeIns.size) {
                val nextTimeIn = periodTimeIns[i + 1]
                val periodAfterCurrent = periodTimeIns[i] + periodDuration

                if (nextTimeIn >= periodAfterCurrent + 1000000 && !tempArrayList.contains(periodAfterCurrent)) {
                    // Free period!
                    tempArrayList.add(periodTimeIns[i] + periodDuration)
                }
            }
        }

        periodTimeIns.clear()
        periodTimeIns.addAll(tempArrayList)
        tempArrayList.clear()
        periodTimeIns.sort()

        for (i in 0 until periodTimeIns.size) {
            val yPosition = mapValues(periodTimeIns[i], earliestTimeIn, latestTimeOut, 0, timetableHeight).toFloat()
            val period = TextView(this@TimetableActivity)
            period.text = String.format(Integer.toString(i + 1))
            period.setTypeface(period.typeface, Typeface.BOLD)
            period.setTextColor(mPrimaryColour)
            period.setBackgroundColor(mDarkBackgroundColour)
            period.gravity = Gravity.CENTER
            period.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)

            // Add the view and set the position
            timetable.addView(period)
            period.x = 0f
            period.y = yPosition
        }

        // Draw the line indicating the time
        val line = View(this)
        line.setBackgroundColor(mPrimaryColour)
        line.layoutParams = FrameLayout.LayoutParams(resources.displayMetrics.widthPixels, 4)
        val c = Calendar.getInstance()
        val currentHour = c.get(Calendar.HOUR_OF_DAY)
        val currentMinute = c.get(Calendar.MINUTE)
        val currentMillis = utility.timeToMillis(currentHour, currentMinute).toLong()
        if (currentMillis in (earliestTimeIn + 1)..(latestTimeOut - 1)) {
            timetable.addView(line)
            line.y = mapValues(currentMillis, earliestTimeIn, latestTimeOut, 0, fullTimetableHeight).toFloat()
        }

    }

    private fun overlayBitmaps(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
        val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bmp1, Matrix(), null)
        canvas.drawBitmap(bmp2, 0f, 0f, null)
        return bmOverlay
    }

    fun mapValues(x: Long, in_min: Long, in_max: Long, out_min: Long, out_max: Long): Long =
            (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min

    fun getPopularElement(a: LongArray): Long {
        if (a.isEmpty()) return -1

        var count: Long = 1
        var tempCount: Long
        var popular = a[0]
        var temp: Long = 0
        for (i in 0 until a.size - 1) {
            temp = a[i]
            tempCount = 0
            (1 until a.size)
                    .filter { temp == a[it] }
                    .forEach { tempCount++ }

            if (tempCount > count) {
                popular = temp
                count = tempCount
            }
        }
        return popular
    }

    fun getSmallestElement(array: LongArray): Long {
        if (array.isEmpty()) return -1

        return (0 until array.size)
                .map { array[it] }
                .min()
                ?: array[0]
    }

    fun getLargestElement(array: LongArray): Long {
        if (array.isEmpty()) return -1

        return (0 until array.size)
                .map { array[it] }
                .max()
                ?: array[0]
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_timetable, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.period_duration -> {
                // Use a number picker dialog to edit the duration of one period
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                val periodLength = preferences.getLong(getString(R.string.KEY_PREFERENCES_PERIOD_DURATION), 0)
                val lengthInMinutes = utility.getMinuteIgnoringHours(periodLength)

                val d = Dialog(this)
                d.setTitle("NumberPicker")
                d.setContentView(R.layout.dialog_number_picker)
                val b1 = d.findViewById(R.id.button_done) as Button
                val np = d.findViewById(R.id.number_picker) as NumberPicker
                val ht = d.findViewById(R.id.header_textview) as TextView

                ht.text = getString(R.string.period_duration)
                np.maxValue = 600
                np.minValue = 0
                np.value = lengthInMinutes
                np.wrapSelectorWheel = false
                np.setOnValueChangedListener(this)
                b1.setOnClickListener {
                    val millis = (np.value * 60 * 1000).toLong()
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putLong(getString(R.string.KEY_PREFERENCES_PERIOD_DURATION), millis)
                            .apply()
                    generateTimetable()
                    d.dismiss()
                }
                d.show()
                return true
            }
        }
        return false
    }
}