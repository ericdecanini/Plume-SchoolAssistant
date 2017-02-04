package com.pdt.plume;


import android.content.Context;
import android.util.Log;

import com.pdt.plume.data.DbContract;

import java.util.ArrayList;

public class OccurrenceTimePeriod {
    String LOG_TAG = OccurrenceTimePeriod.class.getSimpleName();

    // Basis and Week Type. The mScheduleAdapter will determine
    // the layout based on these variables
    public String basis;
    public String weekType;

    // Main header string data and day button binary data
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

    // Constructor where global variables are set
    // These global variables are then accessed by the mScheduleAdapter
    public OccurrenceTimePeriod(Context context, String timeIn, String timeOut, String timeInAlt,
                                String timeOutAlt, String periods, String occurrence) {
        super();
        // Get the days binary code and set it to the global variables
        // Get the convertible occurrence string and split it into an array
        String[] splitOccurrence = occurrence.split(":");

        // Create an Array List to get the periods selected by a class item
        ArrayList<String> periodList = new ArrayList<>();

        // Set the global variables for basis and week taskType
        basis = splitOccurrence[0];
        weekType = splitOccurrence[1];

        // Create the main header string based on the basis
        // Set main header for time basis
        if (splitOccurrence[0].equals("0")) {
            time_period = timeIn
                    + " - "
                    + timeOut;
            time_period_alt = timeInAlt
                    + " - "
                    + timeOutAlt;
        }

        // Set main header for period basis
        else if (splitOccurrence[0].equals("1")) {
            // Filter out the full array of period strings and collect all
            // selected periods into the array list
            Log.v(LOG_TAG, "Periods: " + periods);
            String[] splitPeriods = periods.split(":");
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

            // Build the main header string based on the
            // count of selected periods using the Array List
            if (periodList.size() != 0) {
                // Initialise the StringBuilder variable
                StringBuilder builder = new StringBuilder();

                // Append the 1st item
                builder.append(periodList.get(0));

                // If there is only one item selected, use the appropriate ending
                if (periodList.size() == 1) {
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_period));
                }

                // If there is more than one item selected,
                // allow a string to fit multiple periods
                else {
                    for (int ii = 1; ii < periodList.size() - 1; ii++) {
                        builder.append(", ");
                        builder.append(periodList.get(ii));
                    }
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_and));
                    builder.append(" ");
                    builder.append(periodList.get(periodList.size() - 1));
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_periods));
                }

                // Convert the StringBuilder into a string
                time_period = builder.toString();
            }

            // Build the alternate header string based on the
            // count of selected periods using the Array List
            periodList.clear();
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
            if (periodList.size() != 0) {
                // Initialise the StringBuilder variable
                StringBuilder builder = new StringBuilder();

                // Append the 1st item
                builder.append(periodList.get(0));

                // If there is only one item selected, use the appropriate ending
                if (periodList.size() == 1) {
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_period));
                }

                // If there is more than one item selected,
                // allow a string to fit multiple periods
                else {
                    for (int ii = 1; ii < periodList.size() - 1; ii++) {
                        builder.append(", ");
                        builder.append(periodList.get(ii));
                    }
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_and));
                    builder.append(" ");
                    builder.append(periodList.get(periodList.size() - 1));
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_periods));
                }

                // Convert the StringBuilder into a string
                time_period_alt = builder.toString();
            }
        }

        // Set main header for block basis
        else if (splitOccurrence[0].equals("2")) {
            // Filter out the full array of period strings and collect all
            // selected periods into the array list
            String[] splitPeriods = periods.split(":");
            if (splitPeriods[0].equals("1") || splitPeriods[0].equals("3"))
                periodList.add("1st");
            if (splitPeriods[1].equals("1") || splitPeriods[1].equals("3"))
                periodList.add("2nd");
            if (splitPeriods[2].equals("1") || splitPeriods[2].equals("3"))
                periodList.add("3rd");
            if (splitPeriods[3].equals("1") || splitPeriods[3].equals("3"))
                periodList.add("4th");

            // Build the main header string based on the
            // count of selected periods using the Array List
            if (periodList.size() != 0) {
                // Initialise the StringBuilder variable
                StringBuilder builder = new StringBuilder();

                // Append the 1st item
                builder.append(periodList.get(0));

                // If there is only one item selected, use the appropriate ending
                if (periodList.size() == 1) {
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_period));
                }

                // If there is more than one item selected,
                // allow a string to fit multiple periods
                else {
                    for (int ii = 1; ii < periodList.size() - 1; ii++) {
                        builder.append(", ");
                        builder.append(periodList.get(ii));
                    }
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_and));
                    builder.append(" ");
                    builder.append(periodList.get(periodList.size() - 1));
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_periods));
                }

                // Convert the StringBuilder into a string
                time_period = builder.toString();
            }

            // Build the alternate header string based on the
            // count of selected periods using the Array List
            periodList.clear();
            if (splitPeriods[0].equals("2") || splitPeriods[0].equals("3"))
                periodList.add("1st");
            if (splitPeriods[1].equals("2") || splitPeriods[1].equals("3"))
                periodList.add("2nd");
            if (splitPeriods[2].equals("2") || splitPeriods[2].equals("3"))
                periodList.add("3rd");
            if (splitPeriods[3].equals("2") || splitPeriods[3].equals("3"))
                periodList.add("4th");
            if (periodList.size() != 0) {
                // Initialise the StringBuilder variable
                StringBuilder builder = new StringBuilder();

                // Append the 1st item
                builder.append(periodList.get(0));

                // If there is only one item selected, use the appropriate ending
                if (periodList.size() == 1) {
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_period));
                }

                // If there is more than one item selected,
                // allow a string to fit multiple periods
                else {
                    for (int ii = 1; ii < periodList.size() - 1; ii++) {
                        builder.append(", ");
                        builder.append(periodList.get(ii));
                    }
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_and));
                    builder.append(" ");
                    builder.append(periodList.get(periodList.size() - 1));
                    builder.append(" ");
                    builder.append(context.getString(R.string.class_time_list_header_substring_periods));
                }

                // Convert the StringBuilder into a string
                time_period_alt = builder.toString();
            }
        }

        // If the class time isn't block based, set the global day binary
        // variables based on the split occurrence string binary
        if (!basis.equals("2")) {
            // Set the lighted state of each day based on the occurrence binary string
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
}
