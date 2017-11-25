package com.pdt.plume.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;

import java.security.acl.LastOwnerException;
import java.util.Calendar;
import java.util.logging.FileHandler;

import static android.R.attr.action;


public class ClassNotificationReceiver extends BroadcastReceiver {

    private final String SOMEACTION = "com.pdt.plume.ACTION";
    String LOG_TAG = ClassNotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "ClassNotificationReceiver has received");

        // TODO: Add the day's classes to the calendar

    }

}