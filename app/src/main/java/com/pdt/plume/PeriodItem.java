package com.pdt.plume;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import static android.view.View.Y;

public class PeriodItem implements Parcelable {
    String LOG_TAG = PeriodItem.class.getSimpleName();
    Utility utility = new Utility();

    // Basis and Week Type. The mTasksAdapter will determine
    // the layout based on these variables
    public String basis;
    public String weekType;
    public int position;

    // Main header string data and day button binary data
    String days;
    String days_alt;
    String occurrence;

    String timein;
    String timeout;
    String timeinalt;
    String timeoutalt ;

    long timeinValue = -1;
    long timeoutValue = -1;
    long timeinaltValue = -1;
    long timeoutaltValue = -1;

    boolean[] period = {false, false, false, false, false, false, false, false};
    boolean[] periodAlt = {false, false, false, false, false, false, false, false};
    String periods;

    // Constructor where global variables are set
    // These global variables are then accessed by the mTasksAdapter
    public PeriodItem(Context context, long timeIn, long timeOut, long timeInAlt,
                      long timeOutAlt, String periods, String occurrence) {
        super();
        this.timeinValue = timeIn;
        this.timeoutValue = timeOut;
        this.timeinaltValue = timeInAlt;
        this.timeoutaltValue = timeOutAlt;

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        if (minute >= 15 && minute < 45)
            minute = 30;
        if (minute >= 45 || minute < 15) {
            minute = 0;
            hour++;
        }
        c.set(0, 0, 0, hour, minute);

        if (timeinValue == -1)
            timeinValue = ((int)c.getTimeInMillis());
        if (timeinaltValue == -1)
            timeinaltValue = ((int)c.getTimeInMillis());
        c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
        if (timeoutValue == -1)
            timeoutValue = ((int)c.getTimeInMillis());
        if (timeoutaltValue == -1)
            timeoutaltValue = ((int)c.getTimeInMillis());

        this.timein = utility.millisToHourTime(timeIn);
        this.timeout = utility.millisToHourTime(timeOut);
        this.timeinalt = utility.millisToHourTime(timeInAlt);
        this.timeoutalt = utility.millisToHourTime(timeOutAlt);

        this.periods = periods;
        String[] periodsArray = periods.split(":");
        for (int i = 0; i < 7; i++) {
            if (periodsArray[i].equals("0")) {
                this.period[i] = false;
                this.periodAlt[i] = false;
            } else if (periodsArray[i].equals("1")) {
                this.period[i] = true;
                this.periodAlt[i] = false;
            } else if (periodsArray[i].equals("2")) {
                this.period[i] = false;
                this.periodAlt[i] = true;
            } else {
                this.period[i] = true;
                this.periodAlt[i] = true;
            }
        }

        this.occurrence = occurrence;
        String[] days = {"0", "0", "0", "0", "0", "0", "0"};
        String[] daysAlt = {"0", "0", "0", "0", "0", "0", "0"};
        String[] occurrenceArray = occurrence.split(":");
        this.basis = occurrenceArray[0];
        this.weekType = occurrenceArray[1];
        for (int i = 0; i < occurrenceArray.length - 2; i++) {
            if (occurrenceArray[i+2].equals("0")) {
                days[i] = "0";
                daysAlt[i] = "0";
            } else if (occurrenceArray[i+2].equals("1")) {
                days[i] = "1";
                daysAlt[i] = "0";
            } else if (occurrenceArray[i+2].equals("2")) {
                days[i] = "0";
                daysAlt[i] = "1";
            } else {
                days[i] = "1";
                daysAlt[i] = "1";
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < days.length; i++) {
            if (i != 0) builder.append(":");
            builder.append(days[i]);
        }
        this.days = builder.toString();
        builder.delete(0, builder.length());
        for (int i = 0; i < daysAlt.length; i++) {
            if (i != 0) builder.append(":");
            builder.append(daysAlt[i]);
        }
        this.days_alt = builder.toString();
    }

    protected PeriodItem(Parcel in) {
        LOG_TAG = in.readString();
        basis = in.readString();
        weekType = in.readString();
        position = in.readInt();
        days = in.readString();
        days_alt = in.readString();
        occurrence = in.readString();
        timein = in.readString();
        timeout = in.readString();
        timeinalt = in.readString();
        timeoutalt = in.readString();
        timeinValue = in.readLong();
        timeoutValue = in.readLong();
        timeinaltValue = in.readLong();
        timeoutaltValue = in.readLong();
        period = in.createBooleanArray();
        periodAlt = in.createBooleanArray();
        periods = in.readString();
    }

    public static final Creator<PeriodItem> CREATOR = new Creator<PeriodItem>() {
        @Override
        public PeriodItem createFromParcel(Parcel in) {
            return new PeriodItem(in);
        }

        @Override
        public PeriodItem[] newArray(int size) {
            return new PeriodItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(days);
        parcel.writeString(days_alt);
        parcel.writeString(occurrence);

        parcel.writeString(timein);
        parcel.writeString(timeout);
        parcel.writeString(timeinalt);
        parcel.writeString(timeoutalt);

        parcel.writeLong(timeinValue);
        parcel.writeLong(timeoutValue);
        parcel.writeLong(timeinaltValue);
        parcel.writeLong(timeoutaltValue);

        parcel.writeBooleanArray(period);
        parcel.writeBooleanArray(periodAlt);
        parcel.writeString(periods);
    }
}
