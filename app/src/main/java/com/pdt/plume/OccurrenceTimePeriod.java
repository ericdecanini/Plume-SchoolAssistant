package com.pdt.plume;


import com.pdt.plume.data.DbContract;

import java.util.ArrayList;

public class OccurrenceTimePeriod {
    Utility utility = new Utility();

    public String weekType;

    public String time_period;
    public String sunday = "0";
    public String monday = "0";
    public String tuesday = "0";
    public String wednesday = "0";
    public String thursday = "0";
    public String friday = "0";
    public String saturday = "0";

    public String time_period_alt;
    public String sunday_alt = "0";
    public String monday_alt = "0";
    public String tuesday_alt = "0";
    public String wednesday_alt = "0";
    public String thursday_alt = "0";
    public String friday_alt = "0";
    public String saturday_alt = "0";


    public OccurrenceTimePeriod(String timeIn, String timeOut, String timeInAlt, String timeOutAlt, String periods, String occurrence) {
        super();
        String[] splitOccurrence = occurrence.split(":");
        ArrayList<String> periodList = new ArrayList<>();
        weekType = splitOccurrence[1];
        if (splitOccurrence[0].equals("0")) {
            time_period = timeIn
                    + " - "
                    + timeOut;
            time_period_alt = timeInAlt
                    + " - "
                    + timeOutAlt;
        } else if (splitOccurrence[0].equals("1")) {
            String[] splitPeriods = periods.split(":");
            if (splitPeriods[0].equals("1"))
                periodList.add("1st");
            if (splitPeriods[1].equals("1"))
                periodList.add("2nt");
            if (splitPeriods[2].equals("1"))
                periodList.add("3rt");
            if (splitPeriods[3].equals("1"))
                periodList.add("4th");
            if (splitPeriods[4].equals("1"))
                periodList.add("5th");
            if (splitPeriods[5].equals("1"))
                periodList.add("6th");
            if (splitPeriods[6].equals("1"))
                periodList.add("7th");
            if (splitPeriods[7].equals("1"))
                periodList.add("8th");
            if (splitPeriods[8].equals("1"))
                periodList.add("9th");
            if (splitPeriods[9].equals("1"))
                periodList.add("10th");
            if (splitPeriods[10].equals("1"))
                periodList.add("11th");
            if (splitPeriods[11].equals("1"))
                periodList.add("12th");

            if (periodList.size() != 0) {
                StringBuilder builder = new StringBuilder();
                builder.append(periodList.get(0));
                if (periodList.size() == 1) {
                    builder.append(" period");
                } else {
                    for (int ii = 1; ii < periodList.size() - 1; ii++) {
                        builder.append(", ");
                        builder.append(periodList.get(ii));
                    }
                    builder.append("and ");
                    builder.append(periodList.get(periodList.size() - 1));
                    builder.append(" periods");
                }
                time_period = builder.toString();
            }
        }

        if (splitOccurrence[2].equals("0")) {
            sunday = "0";
            sunday_alt = "0";
        } else if (splitOccurrence[2].equals("1")) {
            sunday = "1";
            sunday_alt = "0";
        } else if (splitOccurrence[2].equals("2")) {
            sunday = "0";
            sunday_alt = "1";
        } else if (splitOccurrence[2].equals("3")) {
            sunday = "1";
            sunday_alt = "1";
        }

        if (splitOccurrence[3].equals("0")) {
            monday = "0";
            monday_alt = "0";
        } else if (splitOccurrence[3].equals("1")) {
            monday = "1";
            monday_alt = "0";
        } else if (splitOccurrence[3].equals("2")) {
            monday = "0";
            monday_alt = "1";
        } else if (splitOccurrence[3].equals("3")) {
            monday = "1";
            monday_alt = "1";
        }

        if (splitOccurrence[4].equals("0")) {
            tuesday = "0";
            tuesday_alt = "0";
        } else if (splitOccurrence[4].equals("1")) {
            tuesday = "1";
            tuesday_alt = "0";
        } else if (splitOccurrence[4].equals("2")) {
            tuesday = "0";
            tuesday_alt = "1";
        } else if (splitOccurrence[4].equals("3")) {
            tuesday = "1";
            tuesday_alt = "1";
        }

        if (splitOccurrence[5].equals("0")) {
            wednesday = "0";
            wednesday_alt = "0";
        } else if (splitOccurrence[5].equals("1")) {
            wednesday = "1";
            wednesday_alt = "0";
        } else if (splitOccurrence[5].equals("2")) {
            wednesday = "0";
            wednesday_alt = "1";
        } else if (splitOccurrence[5].equals("3")) {
            wednesday = "1";
            wednesday_alt = "1";
        }

        if (splitOccurrence[6].equals("0")) {
            thursday = "0";
            thursday_alt = "0";
        } else if (splitOccurrence[6].equals("1")) {
            thursday = "1";
            thursday_alt = "0";
        } else if (splitOccurrence[6].equals("2")) {
            thursday = "0";
            thursday_alt = "1";
        } else if (splitOccurrence[6].equals("3")) {
            thursday = "1";
            thursday_alt = "1";
        }

        if (splitOccurrence[7].equals("0")) {
            friday = "0";
            friday_alt = "0";
        } else if (splitOccurrence[7].equals("1")) {
            friday = "1";
            friday_alt = "0";
        } else if (splitOccurrence[7].equals("2")) {
            friday = "0";
            friday_alt = "1";
        } else if (splitOccurrence[7].equals("3")) {
            friday = "1";
            friday_alt = "1";
        }

        if (splitOccurrence[8].equals("0")) {
            saturday = "0";
            saturday_alt = "0";
        } else if (splitOccurrence[8].equals("1")) {
            saturday = "1";
            saturday_alt = "0";
        } else if (splitOccurrence[8].equals("2")) {
            saturday = "0";
            saturday_alt = "1";
        } else if (splitOccurrence[8].equals("3")) {
            saturday = "1";
            saturday_alt = "1";
        }

    }
}
