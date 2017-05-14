package com.pdt.plume.services;

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
import com.pdt.plume.ClassFragment;
import com.pdt.plume.LoginActivity;
import com.pdt.plume.MainActivity;
import com.pdt.plume.NewScheduleActivity;
import com.pdt.plume.R;
import com.pdt.plume.Schedule;
import com.pdt.plume.ScheduleAdapter;
import com.pdt.plume.ScheduleDetailActivity;
import com.pdt.plume.ScheduleDetailFragment;
import com.pdt.plume.ScheduleFragment;
import com.pdt.plume.SettingsActivity;
import com.pdt.plume.TaskNotificationPublisher;
import com.pdt.plume.TasksDetailActivity;
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
        // Disable any notifications
        // CANCEL TASK REFERENCES
        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mFirebaseUser.getUid()).child("tasks");
        tasksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the data
                String title = dataSnapshot.child("title").getValue(String.class);
                String icon = dataSnapshot.child("icon").getValue(String.class);

                // Rebuild the notification
                final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(ClassesActivityTablet.this);
                Bitmap largeIcon = null;
                try {
                    largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                        .setBackground(largeIcon);

                Intent contentIntent = new Intent(ClassesActivityTablet.this, TasksDetailActivity.class);
                contentIntent.putExtra(getString(R.string.INTENT_EXTRA_ID), ID);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(ClassesActivityTablet.this);
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

                Intent notificationIntent = new Intent(ClassesActivityTablet.this, TaskNotificationPublisher.class);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(ClassesActivityTablet.this, REQUEST_NOTIFICATION_ALARM,
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
        final int weekNumber = preferences.getInt(getString(R.string.KEY_WEEK_NUMBER), 0);
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mFirebaseUser.getUid()).child("classes");
        classesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the key data
                String title = dataSnapshot.getKey();
                String icon = dataSnapshot.child("icon").getValue(String.class);
                String message = getString(R.string.class_notification_message,
                        Integer.toString(preferences.getInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), 0)));

                // Get the listed data
                ArrayList<Integer> timeins = new ArrayList<>();
                if (weekNumber == 0)
                    for (DataSnapshot timeinSnapshot : dataSnapshot.child("timein").getChildren())
                        timeins.add(timeinSnapshot.getValue(int.class));
                else
                    for (DataSnapshot timeinaltSnapshot : dataSnapshot.child("timeinalt").getChildren())
                        timeins.add(timeinaltSnapshot.getValue(int.class));

                Calendar c = Calendar.getInstance();
                for (int i = 0; i < timeins.size(); i++) {
                    c.setTimeInMillis(timeins.get(i));

                    // Rebuild the notification
                    final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(ClassesActivityTablet.this);
                    Bitmap largeIcon = null;
                    try {
                        largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                            .setBackground(largeIcon);

                    Intent contentIntent = new Intent(ClassesActivityTablet.this, ScheduleDetailActivity.class);
                    if (mFirebaseUser != null)
                        contentIntent.putExtra("id", title);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(ClassesActivityTablet.this);
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

                    Intent notificationIntent = new Intent(ClassesActivityTablet.this, TaskNotificationPublisher.class);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, REQUEST_NOTIFICATION_ID);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                    final PendingIntent pendingIntent = PendingIntent.getBroadcast(ClassesActivityTablet.this, REQUEST_NOTIFICATION_ALARM,
                            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(pendingIntent);
                }

            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        // Reschedule all SQLite based Task Notifications
        DbHelper dbHelper = new DbHelper(ClassesActivityTablet.this);
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
                    = new NotificationCompat.Builder(ClassesActivityTablet.this);
            Bitmap largeIcon = null;
            try {
                largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
            final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender
                    = new NotificationCompat.WearableExtender().setBackground(largeIcon);

            Intent contentIntent = new Intent(ClassesActivityTablet.this, TasksDetailActivity.class);
            contentIntent.putExtra(getString(R.string.INTENT_EXTRA_ID), ID);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ClassesActivityTablet.this);
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

            Intent notificationIntent = new Intent(ClassesActivityTablet.this, TaskNotificationPublisher.class);
            notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
            notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast
                    (ClassesActivityTablet.this, REQUEST_NOTIFICATION_ALARM,
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

            Intent contentIntent = new Intent(ClassesActivityTablet.this, ScheduleDetailActivity.class);
            contentIntent.putExtra("_ID", ID);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ClassesActivityTablet.this);
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

                    Intent notificationIntent = new Intent(ClassesActivityTablet.this, TaskNotificationPublisher.class);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 0);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                    final PendingIntent pendingIntent = PendingIntent.getBroadcast(ClassesActivityTablet.this, REQUEST_NOTIFICATION_ALARM,
                            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, finalC.getTimeInMillis(), pendingIntent);
                }
            });
        }
        classesCursor.close();

        // Execute the Sign Out operation
        mFirebaseAuth.signOut();
        loggedIn = false;
        logInOut.setTitle(getString(R.string.action_login));
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
