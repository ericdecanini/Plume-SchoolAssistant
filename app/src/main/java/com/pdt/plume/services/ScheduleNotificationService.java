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
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.pdt.plume.MainActivity;
import com.pdt.plume.MuteAlarmReceiver;
import com.pdt.plume.R;
import com.pdt.plume.ScheduleDetailActivity;
import com.pdt.plume.TaskNotificationPublisher;
import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.ScheduleEntry;

import java.io.IOException;
import java.util.Calendar;


public class ScheduleNotificationService extends Service {

    String LOG_TAG = ScheduleNotificationService.class.getSimpleName();
    int REQUEST_NOTIFICATION_INTENT = 50;
    int REQUEST_NOTIFICATION_ALARM = 51;
    int REQUEST_MUTE_ALARM = 51;

    int mPrimaryColor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "onCreate executed");

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scheduleClassNotifications();
        scheduleMute();
        return super.onStartCommand(intent, flags, startId);
    }

    private void scheduleClassNotifications() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int notificationAdvance = preferences.getInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), 0);
        if (notificationAdvance != 0) {
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor = dbHelper.getCurrentDayScheduleDataFromSQLite(this);
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    if (cursor.moveToPosition(i)) {
                        int timeIn;
                        String weekType = preferences.getString(getString(R.string.KEY_SETTINGS_WEEK_NUMBER), "0");
                        if (weekType.equals("0"))
                            timeIn = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN));
                        else
                            timeIn = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT));

                        String title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));

                        Calendar currentTime = Calendar.getInstance();
                        Calendar timeInTime = Calendar.getInstance();
                        timeInTime.setTimeInMillis(timeIn);
                        int currentMinute = currentTime.get(Calendar.MINUTE);
                        int timeInMinute = timeInTime.get(Calendar.MINUTE);
                        if (timeInMinute < notificationAdvance)
                            timeInMinute += currentMinute;
                        int minutesBeforeClass = timeInMinute - currentMinute;

                        String message = getString(R.string.schedule_notification_message) + " " + minutesBeforeClass
                                + " " + getString(R.string.minutes);
                        if (timeIn != -1)
                            Remind(timeIn, notificationAdvance, title, message, i);
                    }
                }
            }
        }
    }

    public void Remind(int timeIn, int advance, String title, String message, int position) {
        scheduleNotification(timeIn, advance, position, title, message);
    }

    private void scheduleNotification(final int timeIn, final int advance, final int position, final String title, final String message) {
        DbHelper dbHelper = new DbHelper(this);
        Cursor cursor = dbHelper.getCurrentDayScheduleDataFromSQLite(this);
        if (cursor.moveToPosition(position)) {
            String iconUriString = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON));
            final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            Bitmap largeIcon = null;
            try {
                largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(iconUriString));
            } catch (IOException e) {
                e.printStackTrace();
            }
            final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                    .setBackground(largeIcon);

            Intent contentIntent = new Intent(this, ScheduleDetailActivity.class);
            contentIntent.putExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE), title);
            final PendingIntent contentPendingIntent = PendingIntent.getBroadcast(this, REQUEST_NOTIFICATION_INTENT, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Palette.generateAsync(largeIcon, new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    builder
                            .setContentIntent(contentPendingIntent)
                            .setSmallIcon(R.drawable.ic_class_white)
                            .setColor(palette.getVibrantColor(mPrimaryColor))
                            .setContentTitle(title)
                            .setContentText(message)
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .extend(wearableExtender)
                            .setDefaults(Notification.DEFAULT_ALL);

                    Notification notification = builder.build();

                    Intent notificationIntent = new Intent(ScheduleNotificationService.this, TaskNotificationPublisher.class);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                    final PendingIntent pendingIntent = PendingIntent.getBroadcast(ScheduleNotificationService.this, REQUEST_NOTIFICATION_ALARM, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//                long futureInMillis = SystemClock.elapsedRealtime() + delay;
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(timeIn);
                    c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) - advance);


                    if (isOnTime(c)) {
                        alarmManager.set(AlarmManager.RTC, c.getTimeInMillis(), pendingIntent);
                    }
                }
            });
        }
    }

    private void scheduleMute() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean muteIsChecked = preferences.getBoolean(getString(R.string.KEY_SETTINGS_CLASS_MUTE), false);
        if (muteIsChecked) {
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor = dbHelper.getCurrentDayScheduleDataFromSQLite(this);
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    if (cursor.moveToPosition(i)) {
                        int timeIn;
                        int timeOut;
                        String weekType = preferences.getString(getString(R.string.KEY_SETTINGS_WEEK_NUMBER), "0");
                        if (weekType.equals("0")) {
                            timeIn = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN));
                            timeOut = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT));
                        }
                        else {
                            timeIn = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT));
                            timeOut = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT));
                        }

                        Intent intent = new Intent(this, MuteAlarmReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_MUTE_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(timeIn);

                        if (isOnTime(c)) {
                            alarmManager.set(AlarmManager.RTC, timeIn, pendingIntent);
                        }
                    }
                }
            }
        }
    }

    private boolean isOnTime(Calendar c) {
        Calendar currentTime = Calendar.getInstance();
        int currentMinute = currentTime.get(Calendar.MINUTE);
        int currentHour = currentTime.get(Calendar.HOUR);
        int cMinute = currentTime.get(Calendar.MINUTE);
        int cHour = currentTime.get(Calendar.HOUR);

        if (((currentMinute - cMinute) < 5 && currentMinute - cMinute > 0) && currentHour == cHour) {
            return true;
        } else if (currentHour == (cHour - 1) && (currentMinute > (cHour + 55) && currentMinute < 60)) {
            return true;
        }
        else return false;
    }

    @Override
    public void onDestroy() {
        MainActivity.notificationServiceIsRunning = false;
        super.onDestroy();
    }
}
