package com.pdt.plume;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;

public class MuteAlarmReceiver extends BroadcastReceiver {

    String LOG_TAG = MuteAlarmReceiver.class.getSimpleName();
    Utility utility = new Utility();

    Context c;

    float unmuteTime;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

        unmuteTime = (float) intent.getIntExtra("UNMUTE_TIME", -1);
        Intent unmuteIntent = new Intent(context, UnmuteAlarmReceiver.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        int hour = utility.getHour(unmuteTime);
        int minute = utility.getMinute(unmuteTime);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2, unmuteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.v(LOG_TAG, "Alarm set for " + calendar.getTimeInMillis());
        if (unmuteTime == -1)
            return;
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

}
