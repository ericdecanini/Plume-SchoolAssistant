package com.pdt.plume.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.R;
import com.pdt.plume.Schedule;
import com.pdt.plume.ScheduleDetailActivity;
import com.pdt.plume.TaskNotificationPublisher;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.R.string.no;
import static com.pdt.plume.R.id.timein;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ALARM;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ID;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_INTENT;

public class ClassNotificationService extends Service {

    String LOG_TAG = ClassNotificationService.class.getSimpleName();
    int advance;

    // Theme Variables
    int mPrimaryColor;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;

    public ClassNotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Get the current day schedule and schedule a notification
    // for each corresponding class
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        Log.v(LOG_TAG, "ClassNotificationService started");

        // Initialise the theme variables
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        advance = preferences.getInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), 0);
        if (advance == 0) {
            stopSelf();
            return;
        }
        mPrimaryColor = preferences
                .getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));

        // Get current day schedule
        if (mFirebaseUser != null) {
            // Firebase
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("classes")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                                String title = classSnapshot.getKey();
                                String icon = classSnapshot.child("icon").getValue(String.class);
                                ArrayList<String> occurrences = new ArrayList<>();

                                String weekNumber = preferences.getString(getString(R.string.KEY_WEEK_NUMBER), "0");
                                String timeinchild;
                                if (weekNumber.equals("0"))
                                    timeinchild = "timein";
                                else timeinchild = "timeinalt";

                                for (DataSnapshot occurrenceSnapshot: classSnapshot.child("occurrence").getChildren()) {
                                    occurrences.add(occurrenceSnapshot.getKey());
                                }
                                int i = 0;
                                for (DataSnapshot timeinSnapshot: classSnapshot.child(timeinchild).getChildren()) {
                                    Calendar c = Calendar.getInstance();
                                    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                                    Calendar timeInC = Calendar.getInstance();
                                    timeInC.setTimeInMillis(timeinSnapshot.getValue(long.class));
                                    int hour = timeInC.get(Calendar.HOUR_OF_DAY);
                                    int minute = timeInC.get(Calendar.MINUTE);
                                    c.set(Calendar.HOUR_OF_DAY, hour);
                                    c.set(Calendar.MINUTE, minute - advance);
                                    long timeIn = c.getTimeInMillis();


                                    // Do the matching here
                                    String occurrence = occurrences.get(i);
                                    if (weekNumber.equals("0")) {
                                        if (occurrence.split(":")[dayOfWeek + 2].equals("1")
                                                || occurrence.split(":")[dayOfWeek + 2].equals("3")) {
                                            scheduleNotification(title, Uri.parse(icon), timeIn);
                                        }
                                    } else  if (occurrence.split(":")[dayOfWeek + 2].equals("1")
                                            || occurrence.split(":")[dayOfWeek + 2].equals("3")) {
                                        scheduleNotification(title, Uri.parse(icon), timeIn);
                                    }

                                    i++;
                                }
                            }

                            stopSelf();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } else {
            // SQLite
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor = dbHelper.getCurrentDayScheduleDataFromSQLite(this);
            for (int i = 0; i < cursor.getCount(); i++) {
                String title = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
                String icon = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));
                String occurrence = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_OCCURRENCE));
                long timeIn;
                if (preferences.getString(getString(R.string.KEY_WEEK_NUMBER), "0").equals("0"))
                    timeIn = cursor.getLong(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN));
                else timeIn = cursor.getLong(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN_ALT));

                Calendar c = Calendar.getInstance();
                Calendar timeInC = Calendar.getInstance();
                timeInC.setTimeInMillis(timeIn);
                int hour = timeInC.get(Calendar.HOUR_OF_DAY);
                int minute = timeInC.get(Calendar.MINUTE);
                c.set(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, minute - advance);
                long alarmTime = c.getTimeInMillis();


                if (occurrence.split(":")[0].equals("0"))
                    scheduleNotification(title, Uri.parse(icon), alarmTime);
            }

            stopSelf();
        }
    }

    private void scheduleNotification(String title, Uri icon, long timeIn) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Bitmap largeIcon = null;
        try {
            largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), icon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setBackground(largeIcon);

        Intent contentIntent = new Intent(this, ScheduleDetailActivity.class);
        contentIntent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title);
        PendingIntent contentPendingIntent = PendingIntent.getBroadcast(this, REQUEST_NOTIFICATION_INTENT,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = builder
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_class_white)
                .setColor(mPrimaryColor)
                .setContentTitle(title)
                .setContentText(getString(R.string.schedule_notification_message))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .extend(wearableExtender)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        Intent notificationIntent = new Intent(this, TaskNotificationPublisher.class);;
        notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_NOTIFICATION_ALARM,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, timeIn, pendingIntent);
    }

}
