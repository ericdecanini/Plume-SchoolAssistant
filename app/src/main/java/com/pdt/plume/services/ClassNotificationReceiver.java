package com.pdt.plume.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;

import java.util.Calendar;
import java.util.logging.FileHandler;

import static android.R.attr.action;


public class ClassNotificationReceiver extends BroadcastReceiver {

    private final String SOMEACTION = "com.pdt.plume.ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        Time now = new Time();
        now.setToNow();

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        String action = intent.getAction();
        if (SOMEACTION.equals(action)) {
            if (hour > 3)
            context.startService(new Intent(context, ClassNotificationService.class));
        }
    }

}