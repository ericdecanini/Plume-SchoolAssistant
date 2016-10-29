package com.pdt.plume;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
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
    int REQUEST_UNMUTE_ALARM = 52;

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

        Log.v(LOG_TAG, "Mute alarm received");

        long unmuteTime = (long) intent.getIntExtra("UNMUTE_TIME", -1);
        Log.v(LOG_TAG, "Unmute time: " + unmuteTime);
        Intent unmuteIntent = new Intent(context, UnmuteAlarmReceiver.class);
        unmuteIntent.putExtra(context.getString(R.string.KEY_SCHEDULE_MUTE_CURRENT_VOLUME), currentVolume);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_UNMUTE_ALARM, unmuteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean muteSettingIsChecked = preferences.getBoolean(context.getString(R.string.KEY_SETTINGS_CLASS_MUTE), false);

        if (unmuteTime != -1)
            if (muteSettingIsChecked)
                alarmManager.set(AlarmManager.RTC_WAKEUP, unmuteTime, pendingIntent);
    }

}
