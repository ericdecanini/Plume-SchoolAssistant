package com.pdt.plume;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class UnmuteAlarmReceiver extends BroadcastReceiver {

    String LOG_TAG = UnmuteAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int returnVolume = intent.getIntExtra(context.getString(R.string.KEY_SCHEDULE_MUTE_CURRENT_VOLUME),
                audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));


        Log.v(LOG_TAG, "Unmute alarm received");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean muteSettingIsChecked = preferences.getBoolean(context.getString(R.string.KEY_SETTINGS_CLASS_MUTE), false);
        if (muteSettingIsChecked) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, returnVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
        }
    }

}
