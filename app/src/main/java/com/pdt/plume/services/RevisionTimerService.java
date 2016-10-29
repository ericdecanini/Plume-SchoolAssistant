package com.pdt.plume.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.pdt.plume.R;
import com.pdt.plume.TasksDetailActivity;
import com.pdt.plume.Utility;

import java.util.Timer;
import java.util.TimerTask;


public class RevisionTimerService extends Service {

    String LOG_TAG = RevisionTimerService.class.getSimpleName();
    Utility utility = new Utility();

    private static Timer timer = new Timer();
    private Context c;

    int notifyID = 20;
    String title = "";
    int countdown;
    int minuteInterval = 60;
    int seconds;

    static int REQUEST_TIMER_NOTIFICATION = 30;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        c = this;
        startService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        title = intent.getStringExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE));
        countdown = intent.getIntExtra(getString(R.string.KEY_TASKS_EXTRA_REVISION_TIME), 1);
        seconds = countdown * 60;
        Log.v(LOG_TAG, "Seconds at start: " + seconds);

        // Create the notification that will be updated each minute
        if (countdown == 1) {
            Intent toBePendingIntent = new Intent(RevisionTimerService.this, TasksDetailActivity.class);
            intent.putExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE), title);
            PendingIntent pendingIntent = PendingIntent.getActivity(RevisionTimerService.this, REQUEST_TIMER_NOTIFICATION, toBePendingIntent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(RevisionTimerService.this)
                    .setSmallIcon(R.drawable.ic_access_alarm_white_24dp)
                    .setContentTitle(title)
                    .setContentText(getString(R.string.minute_left))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(notifyID, builder.build());
        }
        else {
            Intent toBePendingIntent = new Intent(RevisionTimerService.this, TasksDetailActivity.class);
            intent.putExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE), title);
            PendingIntent pendingIntent = PendingIntent.getActivity(RevisionTimerService.this, REQUEST_TIMER_NOTIFICATION, toBePendingIntent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(RevisionTimerService.this)
                    .setSmallIcon(R.drawable.ic_access_alarm_white_24dp)
                    .setContentTitle(title)
                    .setContentText(countdown + " " + getString(R.string.minutes_left))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(notifyID, builder.build());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void startService() {
        timer.scheduleAtFixedRate(new mainTask(), 0, 1000);
    }

    private class mainTask extends TimerTask {
        public void run() {
            minuteInterval--;
            seconds--;
            if (minuteInterval <= 0) {
                countdown--;
                if (countdown == 0) {
                    // TIMER END
                    Intent intent = new Intent(RevisionTimerService.this, TasksDetailActivity.class);
                    intent.putExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE), title);
                    PendingIntent pendingIntent = PendingIntent.getActivity(RevisionTimerService.this, REQUEST_TIMER_NOTIFICATION, intent, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(RevisionTimerService.this)
                            .setSmallIcon(R.drawable.ic_access_alarm_white_24dp)
                            .setContentTitle(title)
                            .setContentText(getString(R.string.timer_end))
                            .setContentIntent(pendingIntent)
                            .setOngoing(false)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setDefaults(Notification.DEFAULT_ALL);
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(notifyID, builder.build());
                    sendMessageToActivity("STOP_SERVICE");
                    stopSelf();
                }
                else if (countdown == 1) {
                    // Update the notification
                    Intent intent = new Intent(RevisionTimerService.this, TasksDetailActivity.class);
                    intent.putExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE), title);
                    PendingIntent pendingIntent = PendingIntent.getActivity(RevisionTimerService.this, REQUEST_TIMER_NOTIFICATION, intent, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(RevisionTimerService.this)
                            .setSmallIcon(R.drawable.ic_access_alarm_white_24dp)
                            .setContentTitle(title)
                            .setContentText(getString(R.string.minute_left))
                            .setContentIntent(pendingIntent)
                            .setOngoing(true);
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(notifyID, builder.build());
                }
                else {
                    // Update the notification
                    Intent intent = new Intent(RevisionTimerService.this, TasksDetailActivity.class);
                    intent.putExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE), title);
                    PendingIntent pendingIntent = PendingIntent.getActivity(RevisionTimerService.this, REQUEST_TIMER_NOTIFICATION, intent, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(RevisionTimerService.this)
                            .setSmallIcon(R.drawable.ic_access_alarm_white_24dp)
                            .setContentTitle(title)
                            .setContentIntent(pendingIntent)
                            .setContentText(countdown + " " + getString(R.string.minutes_left))
                            .setOngoing(true);
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(notifyID, builder.build());
                }
            }
            // Set the timer on the UI of the activity
            sendMessageToActivity(utility.secondsToMinuteTime(seconds));
        }
    }

    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }


    private final Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
        }
    };

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("GPSLocationUpdates");
        // You can also include some extra data.
        intent.putExtra("Status", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
