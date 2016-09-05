package com.pdt.plume;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Region;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;

import java.io.IOException;

public class AlarmReceiver extends BroadcastReceiver {

    String LOG_TAG = AlarmReceiver.class.getSimpleName();

    Context c;
    PendingIntent contentIntent;
    int resourceId;
    String title;
    String message;
    android.support.v4.app.NotificationCompat.WearableExtender wearableExtender;
    NotificationManagerCompat manager;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        title = intent.getStringExtra("title");
         message = intent.getStringExtra("message");
        int ID = intent.getIntExtra("ID", -1);
        Log.v(LOG_TAG, "EditId: " + ID);
        String iconUriString = intent.getStringExtra("iconUriString");
        Intent notIntent = new Intent(context, TasksDetailActivity.class);
        notIntent.putExtra("ID", ID);
         contentIntent = PendingIntent.getActivity(context, 0, notIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        manager = NotificationManagerCompat.from(context);


         resourceId = R.drawable.ic_assignment;
//        if (app.SelectedModel.VehicleType == "Car")
//            resourceId = Resource.Drawable.Car;
//        else if (App.SelectedModel.VehicleType == "Bike")
//            resourceId = Resource.Drawable.Bike;
//        else
//            resourceId = Resource.Drawable.Other;

        wearableExtender = new NotificationCompat.WearableExtender()
                .setBackground(BitmapFactory.decodeResource(context.getResources(), resourceId));


        Bitmap largeIcon = null;
        try {
            largeIcon = MediaStore.Images.Media.getBitmap(c.getContentResolver(), Uri.parse(iconUriString));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Palette.generateAsync(largeIcon, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //Generate a notification with just short text and small icon
                android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                        .setContentIntent(contentIntent)
                        .setSmallIcon(resourceId)
                        .setColor(palette.getVibrantColor(c.getResources().getColor(R.color.colorPrimary)))
                        .setContentTitle(title)
                        .setContentText(message)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .extend(wearableExtender)
                        .setDefaults(Notification.DEFAULT_ALL);

                Notification notification = builder.build();
                manager.notify(0, notification);
            }
        });




    }

}
