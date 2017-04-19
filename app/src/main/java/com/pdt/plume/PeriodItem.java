package com.pdt.plume;


import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

public class PeriodItem {
    String LOG_TAG = PeriodItem.class.getSimpleName();
    Utility utility = new Utility();

    // Basis and Week Type. The mScheduleAdapter will determine
    // the layout based on these variables
    public String basis;
    public String weekType;

    // Main header string data and day button binary data
    String days;
    String days_alt;
    String occurrence;

    String timein;
    String timeout;
    String timeinalt;
    String timeoutalt;

    int timeinValue = -1;
    int timeoutValue = -1;
    int timeinaltValue = -1;
    int timeoutaltValue = -1;

    boolean[] period = {false, false, false, false, false, false, false, false};
    boolean[] periodAlt = {false, false, false, false, false, false, false, false};
    String periods;

    // Constructor where global variables are set
    // These global variables are then accessed by the mScheduleAdapter
    public PeriodItem(Context context, int timeIn, int timeOut, int timeInAlt,
                      int timeOutAlt, String periods, String occurrence) {
        super();
        this.timeinValue = timeIn;
        this.timeoutValue = timeOut;
        this.timeinaltValue = timeInAlt;
        this.timeoutaltValue = timeOutAlt;

        this.timein = utility.millisToHourTime(timeIn);
        this.timeout = utility.millisToHourTime(timeOut);
        this.timeinalt = utility.millisToHourTime(timeInAlt);
        this.timeoutalt = utility.millisToHourTime(timeOutAlt);

        this.periods = periods;
        String[] periodsArray = periods.split(":");
        for (int i = 0; i < periodsArray.length; i++) {
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
            } else if (occurrenceArray[i+2].equals("0")) {
                days[i] = "1";
                daysAlt[i] = "0";
            } else if (occurrenceArray[i+2].equals("0")) {
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

}
