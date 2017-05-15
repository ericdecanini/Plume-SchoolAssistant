package com.pdt.plume;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ALARM;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_INTENT;


public class Utility {

    String LOG_TAG = Utility.class.getSimpleName();

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public void cancelOfflineClassNotifications(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int mPrimaryColor = preferences.getInt(context.getString(R.string.KEY_THEME_PRIMARY_COLOR),
                context.getResources().getColor(R.color.colorPrimary));
        DbHelper dbHelper = new DbHelper(context);
        Cursor cursor = dbHelper.getCurrentDayScheduleDataFromSQLite(context);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String title = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
            String icon = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));
            String occurrence = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_OCCURRENCE));
            long timeIn;
            if (preferences.getString(context.getString(R.string.KEY_WEEK_NUMBER), "0").equals("0"))
                timeIn = cursor.getLong(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN));
            else timeIn = cursor.getLong(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN_ALT));

            Calendar c = Calendar.getInstance();
            Calendar timeInC = Calendar.getInstance();
            timeInC.setTimeInMillis(timeIn);
            int hour = timeInC.get(Calendar.HOUR_OF_DAY);
            int minute = timeInC.get(Calendar.MINUTE);
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            long alarmTime = c.getTimeInMillis();


            if (occurrence.split(":")[0].equals("0")) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                Bitmap largeIcon = null;
                try {
                    largeIcon = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(icon));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                        .setBackground(largeIcon);

                Intent contentIntent = new Intent(context, ScheduleDetailActivity.class);
                contentIntent.putExtra(context.getString(R.string.INTENT_EXTRA_CLASS), title);
                PendingIntent contentPendingIntent = PendingIntent.getBroadcast(context, REQUEST_NOTIFICATION_INTENT,
                        contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = builder
                        .setContentIntent(contentPendingIntent)
                        .setSmallIcon(R.drawable.ic_class_white)
                        .setColor(mPrimaryColor)
                        .setContentTitle(title)
                        .setContentText(context.getString(R.string.schedule_notification_message))
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .extend(wearableExtender)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .build();

                Intent notificationIntent = new Intent(context, TaskNotificationPublisher.class);;
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_NOTIFICATION_ALARM,
                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static String getReadablePeriodsString(String periods) {
        StringBuilder builder = new StringBuilder();
        String[] periodsArray = periods.split(":");
        for (int i = 0; i < periodsArray.length; i++) {
            if (periodsArray[i].equals("1")) {
                if (!builder.toString().equals(""))
                    builder.append(", ");
                builder.append(String.valueOf(i + 1));
                if (i == 0) builder.append("st");
                else if (i == 1) builder.append("nd");
                else if (i == 2) builder.append("rd");
                else builder.append("th");
            }
        }
        return builder.toString();
    }

    public static String getReadableDaysString(String days) {
        String[] dayInitials = {"S", "M", "T", "W", "T", "F", "S"};

        StringBuilder builder = new StringBuilder();
        String[] daysArray = days.split(":");
        for (int i = 0; i < daysArray.length; i++) {
            if (daysArray[i].equals("1")) {
                if (!builder.toString().equals(""))
                    builder.append(", ");
                builder.append(dayInitials[i]);
            }
        }
        return builder.toString();
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static int getIndex(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }

    // Helper method for converting hour and minute into seconds
    public int timeToMillis(int hourOfDay, int minute) {
        return ((hourOfDay * 60 * 60) + (minute * 60)) * 1000;
    }

    // Helper method for converting millis into a time string
    public String millisToHourTime(long millis) {
        // Return a blank string if there is no time data
        if (millis == -1)
            return "";
        millis /= 1000;
        // Get the hour by dividing with decimals disregarded
        int hourOfDay = (int) millis / 3600;
        // Get the minutes by formula as a float to
        // allow for decimals to be computed
        float floatMinute = millis - hourOfDay * 3600;
        floatMinute = (floatMinute / 3600) * 60;
        // Convert minute from float to int
        int minute = (int) floatMinute;

        if (hourOfDay == 24)
            hourOfDay = 0;

        // If minute is less than 10, add a 0 to improve visual impact
        if (minute < 10)
            return hourOfDay + ":0" + minute;
        else
            return hourOfDay + ":" + minute;
    }

    // Helper method for converting seconds into a time string
    public String secondsToMinuteTime(float seconds) {
        // Return a blank string if there is no time data
        if (seconds == -1)
            return "";
        // Get the minute
        int minute = (int) seconds / 60;
        // Seconds will be param - minute * 60
        int secondsInTime = (int) seconds - (minute * 60);
        // Return the formatted string
        String minuteString = "";
        String secondsString = "";
        if (minute < 10)
            minuteString = "0" + Integer.toString(minute);
        else minuteString = Integer.toString(minute);
        if (secondsInTime < 10)
            secondsString = "0" + Integer.toString(secondsInTime);
        else secondsString = Integer.toString(secondsInTime);
        return minuteString + ":" + secondsString;
    }

    public int getHour(float millis) {
        return (int) (millis * 1000) * 3600;
    }

    public int getMinute(float millis) {
        int hour = (int) (millis * 1000) * 3600;
        int minute = (int) millis - (hour * 1000 * 3600);
        return minute;
    }

    // Array List update at position helper methods
    // Helper method to update a String Array List item at position
    public ArrayList<String> updateStringArrayListItemAtPosition(ArrayList<String> arrayList, int position, String newObject) {
        // If the sent array list is empty, return an empty array list
        if (arrayList.size() == 0)
            return new ArrayList<>();

        // Create a new instance of an Array List
        ArrayList<String> newArrayList = new ArrayList<>();
        // Add all the previous items below position of the
        // old Array List into the new one
        for (int i = 0; i < position; i++)
            newArrayList.add(arrayList.get(i));
        // Add the new object at the top of the array list where
        // the sent position should be
        newArrayList.add(newObject);
        // Add all the remaining previous items from 1 position
        // above the sent position into the new Array List
        for (int i = position + 1; i < arrayList.size(); i++)
            newArrayList.add(arrayList.get(i));
        // Finally, return the new Array List
        return newArrayList;
    }

    // Helper method to update an Integer Array List item at position
    public ArrayList<Integer> updateIntegerArrayListItemAtPosition(ArrayList<Integer> arrayList, int position, int newObject) {
        // If the sent array list is empty, return an empty array list
        if (arrayList.size() == 0)
            return new ArrayList<>();

        // Create a new instance of an Array List
        ArrayList<Integer> newArrayList = new ArrayList<>();
        // Add all the previous items below position of the
        // old Array List into the new one
        for (int i = 0; i < position; i++)
            newArrayList.add(arrayList.get(i));
        // Add the new object at the top of the array list where
        // the sent position should be
        newArrayList.add(newObject);
        // Add all the remaining previous items from 1 position
        // above the sent position into the new Array List
        for (int i = position + 1; i < arrayList.size(); i++)
            newArrayList.add(arrayList.get(i));
        // Finally, return the new Array List
        return newArrayList;
    }

    // Array List update at position helper methods
    // Helper method to update a String Array List item at position
    public ArrayList<String> deleteObjectAtPosition(ArrayList arrayList, int position) {
        // If the sent array list is empty, return an empty array list
        if (arrayList.size() == 0)
            return new ArrayList();

        // Create a new instance of an Array List
        ArrayList newArrayList = new ArrayList();
        // Add all the previous items below position of the
        // old Array List into the new one
        for (int i = 0; i < position; i++)
            newArrayList.add(arrayList.get(i));
        // Add all the remaining previous items from 1 position
        // above the sent position into the new Array List
        for (int i = position + 1; i < arrayList.size(); i++)
            newArrayList.add(arrayList.get(i));
        // Finally, return the new Array List
        return newArrayList;
    }

    // Helper method to update an PeriodItem Array List item at position
    public ArrayList<PeriodItem> updateOccurrenceTimePeriodArrayListItemAtPosition
    (ArrayList<PeriodItem> arrayList, int position, PeriodItem newObject) {
        // If the sent array list is empty, return an empty array list
        if (arrayList.size() == 0)
            return new ArrayList<>();

        // Create a new instance of an Array List
        ArrayList<PeriodItem> newArrayList = new ArrayList<>();
        // Add all the previous items below position of the
        // old Array List into the new one
        for (int i = 0; i < position; i++)
            newArrayList.add(arrayList.get(i));
        // Add the new object at the top of the array list where
        // the sent position should be
        newArrayList.add(newObject);
        // Add all the remaining previous items from 1 position
        // above the sent position into the new Array List
        for (int i = position + 1; i < arrayList.size(); i++)
            newArrayList.add(arrayList.get(i));
        // Finally, return the new Array List
        return newArrayList;
    }

    // Helper method to create an arrayList of set periods based on the periods string
    public ArrayList<String> createSetPeriodsArrayList(String periods, String weekNumber) {
        String[] splitPeriods = periods.split(":");
        ArrayList<String> periodList = new ArrayList<>();

        // Week 1: Get regular data
        if (weekNumber.equals("0")) {
            // This will be called if the row is period based
            if (splitPeriods.length == 12) {
                if (splitPeriods[0].equals("1") || splitPeriods[0].equals("3"))
                    periodList.add("1st");
                if (splitPeriods[1].equals("1") || splitPeriods[1].equals("3"))
                    periodList.add("2nd");
                if (splitPeriods[2].equals("1") || splitPeriods[2].equals("3"))
                    periodList.add("3rd");
                if (splitPeriods[3].equals("1") || splitPeriods[3].equals("3"))
                    periodList.add("4th");
                if (splitPeriods[4].equals("1") || splitPeriods[4].equals("3"))
                    periodList.add("5th");
                if (splitPeriods[5].equals("1") || splitPeriods[5].equals("3"))
                    periodList.add("6th");
                if (splitPeriods[6].equals("1") || splitPeriods[6].equals("3"))
                    periodList.add("7th");
                if (splitPeriods[7].equals("1") || splitPeriods[7].equals("3"))
                    periodList.add("8th");
                if (splitPeriods[8].equals("1") || splitPeriods[8].equals("3"))
                    periodList.add("9th");
                if (splitPeriods[9].equals("1") || splitPeriods[9].equals("3"))
                    periodList.add("10th");
                if (splitPeriods[10].equals("1") || splitPeriods[10].equals("3"))
                    periodList.add("11th");
                if (splitPeriods[11].equals("1") || splitPeriods[11].equals("3"))
                    periodList.add("12th");
            }
            if (splitPeriods.length == 8) {
                if (splitPeriods[0].equals("1") || splitPeriods[0].equals("3"))
                    periodList.add("1st");
                if (splitPeriods[1].equals("1") || splitPeriods[1].equals("3"))
                    periodList.add("2nd");
                if (splitPeriods[2].equals("1") || splitPeriods[2].equals("3"))
                    periodList.add("3rd");
                if (splitPeriods[3].equals("1") || splitPeriods[3].equals("3"))
                    periodList.add("4th");
                if (splitPeriods[4].equals("1") || splitPeriods[4].equals("3"))
                    periodList.add("5th");
                if (splitPeriods[5].equals("1") || splitPeriods[5].equals("3"))
                    periodList.add("6th");
                if (splitPeriods[6].equals("1") || splitPeriods[6].equals("3"))
                    periodList.add("7th");
                if (splitPeriods[7].equals("1") || splitPeriods[7].equals("3"))
                    periodList.add("8th");
            }

            // This will be called if the row is block based
            if (splitPeriods.length == 4) {
                if (splitPeriods[0].equals("1") || splitPeriods[0].equals("3"))
                    periodList.add("1st");
                if (splitPeriods[1].equals("1") || splitPeriods[1].equals("3"))
                    periodList.add("2nd");
                if (splitPeriods[2].equals("1") || splitPeriods[2].equals("3"))
                    periodList.add("3rd");
                if (splitPeriods[3].equals("1") || splitPeriods[3].equals("3"))
                    periodList.add("4th");
            }
        }
        // Week 2: Get alternate data
        else {
            // This will be called if the row is period based
            if (splitPeriods.length == 12) {
                if (splitPeriods[0].equals("2") || splitPeriods[0].equals("3"))
                    periodList.add("1st");
                if (splitPeriods[1].equals("2") || splitPeriods[1].equals("3"))
                    periodList.add("2nd");
                if (splitPeriods[2].equals("2") || splitPeriods[2].equals("3"))
                    periodList.add("3rd");
                if (splitPeriods[3].equals("2") || splitPeriods[3].equals("3"))
                    periodList.add("4th");
                if (splitPeriods[4].equals("2") || splitPeriods[4].equals("3"))
                    periodList.add("5th");
                if (splitPeriods[5].equals("2") || splitPeriods[5].equals("3"))
                    periodList.add("6th");
                if (splitPeriods[6].equals("2") || splitPeriods[6].equals("3"))
                    periodList.add("7th");
                if (splitPeriods[7].equals("2") || splitPeriods[7].equals("3"))
                    periodList.add("8th");
                if (splitPeriods[8].equals("2") || splitPeriods[8].equals("3"))
                    periodList.add("9th");
                if (splitPeriods[9].equals("2") || splitPeriods[9].equals("3"))
                    periodList.add("10th");
                if (splitPeriods[10].equals("2") || splitPeriods[10].equals("3"))
                    periodList.add("11th");
                if (splitPeriods[11].equals("2") || splitPeriods[11].equals("3"))
                    periodList.add("12th");
            }
            if (splitPeriods.length == 8) {
                if (splitPeriods[0].equals("2") || splitPeriods[0].equals("3"))
                    periodList.add("1st");
                if (splitPeriods[1].equals("2") || splitPeriods[1].equals("3"))
                    periodList.add("2nd");
                if (splitPeriods[2].equals("2") || splitPeriods[2].equals("3"))
                    periodList.add("3rd");
                if (splitPeriods[3].equals("2") || splitPeriods[3].equals("3"))
                    periodList.add("4th");
                if (splitPeriods[4].equals("2") || splitPeriods[4].equals("3"))
                    periodList.add("5th");
                if (splitPeriods[5].equals("2") || splitPeriods[5].equals("3"))
                    periodList.add("6th");
                if (splitPeriods[6].equals("2") || splitPeriods[6].equals("3"))
                    periodList.add("7th");
                if (splitPeriods[7].equals("2") || splitPeriods[7].equals("3"))
                    periodList.add("8th");
            }

            // This will be called if the row is block based
            if (splitPeriods.length == 4) {
                if (splitPeriods[0].equals("2") || splitPeriods[0].equals("3"))
                    periodList.add("1st");
                if (splitPeriods[1].equals("2") || splitPeriods[1].equals("3"))
                    periodList.add("2nd");
                if (splitPeriods[2].equals("2") || splitPeriods[2].equals("3"))
                    periodList.add("3rd");
                if (splitPeriods[3].equals("2") || splitPeriods[3].equals("3"))
                    periodList.add("4th");
            }

        }

        return periodList;
    }

    // Helper method to check if occurrence matches current day
    public boolean occurrenceMatchesCurrentDay(Context context, String occurrence, String periods, String weekNumber, int dayOfWeek) {
        Log.v(LOG_TAG, "Occurrence " + occurrence + " matches");
        // In this case, no class time would have been set
        if (occurrence.equals("-1"))
            return false;

        String[] splitOccurrence = occurrence.split(":");
        String[] splitPeriods = periods.split(":");

        // 1ST CHECK: Block Based Day Check
        if (splitOccurrence[0].equals("2")) {
            // Get the preference for the Block format and set the boolean to show the block header
            String blockFormat = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("blockformat", "0:1:2:1:2:1:0");
            String[] splitBlockFormat = blockFormat.split(":");

            // Day A Check
            if (splitPeriods[0].equals("1") || splitPeriods[0].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("1")) {
                    return true;
                }
            if (splitPeriods[1].equals("1") || splitPeriods[1].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("1")) {
                    return true;
                }
            if (splitPeriods[2].equals("1") || splitPeriods[2].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("1")) {
                    return true;
                }
            if (splitPeriods[3].equals("1") || splitPeriods[3].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("1")) {
                    return true;
                }

            // Day B Check
            if (splitPeriods[0].equals("2") || splitPeriods[0].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("2")) {
                    return true;
                }
            if (splitPeriods[1].equals("2") || splitPeriods[1].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("2")) {
                    return true;
                }
            if (splitPeriods[2].equals("2") || splitPeriods[2].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("2")) {
                    return true;
                }
            if (splitPeriods[3].equals("2") || splitPeriods[3].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("2")) {
                    return true;
                }
        }

        // 2ND CHECK: Time/Period Based Day Check
        // Week A
        else if (weekNumber.equals("0") || splitOccurrence[1].equals("0")) {
            if (splitOccurrence[2].equals("1") || splitOccurrence[2].equals("3"))
                if (dayOfWeek == 1)
                    return true;
            if (splitOccurrence[3].equals("1") || splitOccurrence[3].equals("3"))
                if (dayOfWeek == 2)
                    return true;
            if (splitOccurrence[4].equals("1") || splitOccurrence[4].equals("3"))
                if (dayOfWeek == 3)
                    return true;
            if (splitOccurrence[5].equals("1") || splitOccurrence[5].equals("3"))
                if (dayOfWeek == 4)
                    return true;
            if (splitOccurrence[6].equals("1") || splitOccurrence[6].equals("3"))
                if (dayOfWeek == 5)
                    return true;
            if (splitOccurrence[7].equals("1") || splitOccurrence[7].equals("3"))
                if (dayOfWeek == 6)
                    return true;
            if (splitOccurrence[8].equals("1") || splitOccurrence[8].equals("3")) {
                if (dayOfWeek == 7)
                    return true;
            }
        }
        // Week B
        else {
            if ((splitOccurrence[2].equals("2") || splitOccurrence[2].equals("3"))
                    || (splitOccurrence[1].equals("0") && (splitOccurrence[2].equals("1") || splitOccurrence[2].equals("3"))))
                if (dayOfWeek == 1)
                    return true;
            if ((splitOccurrence[3].equals("2") || splitOccurrence[3].equals("3"))
                    || (splitOccurrence[1].equals("0") && (splitOccurrence[3].equals("1") || splitOccurrence[3].equals("3"))))
                if (dayOfWeek == 2)
                    return true;
            if ((splitOccurrence[4].equals("2") || splitOccurrence[4].equals("3"))
                    || (splitOccurrence[1].equals("0") && (splitOccurrence[4].equals("1") || splitOccurrence[3].equals("3"))))
                if (dayOfWeek == 3)
                    return true;
            if ((splitOccurrence[5].equals("2") || splitOccurrence[5].equals("3"))
                    || (splitOccurrence[1].equals("0") && (splitOccurrence[5].equals("1") || splitOccurrence[4].equals("3"))))
                if (dayOfWeek == 4)
                    return true;
            if ((splitOccurrence[6].equals("2") || splitOccurrence[6].equals("3"))
                    || (splitOccurrence[1].equals("0") && (splitOccurrence[6].equals("1") || splitOccurrence[5].equals("3"))))
                if (dayOfWeek == 5)
                    return true;
            if ((splitOccurrence[7].equals("2") || splitOccurrence[7].equals("3"))
                    || (splitOccurrence[1].equals("0") && (splitOccurrence[7].equals("1") || splitOccurrence[7].equals("3"))))
                if (dayOfWeek == 6)
                    return true;
            if ((splitOccurrence[8].equals("2") || splitOccurrence[8].equals("3"))
                    || (splitOccurrence[1].equals("0") && (splitOccurrence[8].equals("1") || splitOccurrence[8].equals("3"))))
                if (dayOfWeek == 7)
                    return true;
        }
        // If above methods did not resolve, return false by default
        return false;
    }

    public String formatDateString(Context context, int year, int monthOfYear, int dayOfMonth) {
        // Get the current time and day of the week
        Calendar c = Calendar.getInstance();
        c.set(year, monthOfYear, dayOfMonth);

        // Create a date formatter and create a new string with the formatted date
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dateFormatPreference = prefs.getString(context.getString(R.string.KEY_SETTINGS_DATE_FORMAT), "EEE, dd MMM yyyy");
        boolean hasOrdinal = false;
        if (dateFormatPreference.equals("EEE :d: MMMM yyyy") || dateFormatPreference.equals("EEEE :d: MMMM yyyy"))
            hasOrdinal = true;

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormatPreference, java.util.Locale.getDefault());
        String formattedDate = formatter.format(c.getTime());
        if (hasOrdinal) {
            String[] formattedDateSplit = formattedDate.split(":");
            String day = formattedDateSplit[1];
            switch (day) {
                case "1":
                case "21":
                case "31":
                    return formattedDateSplit[0] + formattedDateSplit[1] + "st" + formattedDateSplit[2];
                case "2":
                case "22":
                    return formattedDateSplit[0] + formattedDateSplit[1] + "nd" + formattedDateSplit[2];
                case "3":
                case "23":
                    return formattedDateSplit[0] + formattedDateSplit[1] + "rd" + formattedDateSplit[2];
                default:
                    return formattedDateSplit[0] + formattedDateSplit[1] + "th" + formattedDateSplit[2];
            }
        }
        return formattedDate;
    }

    public String formatBlockString(Context context, int day) {
        // Get the formatter string
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String blockFormat = prefs.getString(context.getString(R.string.KEY_SETTINGS_BLOCK_FORMAT), "d:l");

        if (day == 0) {
            switch (blockFormat) {
                case "d:l":
                    return context.getResources().getStringArray(R.array.settings_block_format_entries)[0];
                case "l:d":
                    return context.getResources().getStringArray(R.array.settings_block_format_entries)[1];
                case "d:n":
                    return context.getResources().getStringArray(R.array.settings_block_format_entries)[2];
                case "n:d":
                    return context.getResources().getStringArray(R.array.settings_block_format_entries)[3];
            }
        } else {
            switch (blockFormat) {
                case "d:l":
                    return context.getResources().getStringArray(R.array.settings_block_format_entries_b)[0];
                case "l:d":
                    return context.getResources().getStringArray(R.array.settings_block_format_entries_b)[1];
                case "d:n":
                    return context.getResources().getStringArray(R.array.settings_block_format_entries_b)[2];
                case "n:d":
                    return context.getResources().getStringArray(R.array.settings_block_format_entries_b)[3];
            }
        }

        // If nothing has been returned by this point, return d:l by default
        Log.w(LOG_TAG, "WARNING: End of block format reached. Returning Day A by default");
        return "Day A";
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filePath,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

}
