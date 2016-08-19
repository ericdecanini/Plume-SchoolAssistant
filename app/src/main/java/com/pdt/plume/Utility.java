package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

import java.util.ArrayList;


public class Utility {
    String LOG_TAG = Utility.class.getSimpleName();

    public static int getIndex(Spinner spinner, String myString){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }

    // Helper method for converting hour and minute into seconds
    public int timeToSeconds(int hourOfDay, int minute){
        return (hourOfDay * 60 * 60) + (minute * 60);
    }

    // Helper method for converting seconds into a time string
    public String secondsToTime(float seconds){
        // Return a blank string if there is no time data
        if (seconds == -1)
            return "";
        // Get the hour by dividing with decimals disregarded
        int hourOfDay = (int) seconds / 3600;
        // Get the minutes by formula as a float to
        // allow for decimals to be computed
        float floatMinute = seconds - hourOfDay * 3600;
        floatMinute = (floatMinute / 3600) * 60;
        // Convert minute from float to int
        int minute = (int) floatMinute;
        // If minute is less than 10, add a 0 to improve visual impact
        if (minute < 10)
            return hourOfDay + ":0" + minute;
        else
            return hourOfDay + ":" + minute;
    }

    // Array List update at position helper methods
    // Helper method to update a String Array List item at position
    public ArrayList<String> updateStringArrayListItemAtPosition(ArrayList<String> arrayList, int position, String newObject){
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
    public ArrayList<Integer> updateIntegerArrayListItemAtPosition(ArrayList<Integer> arrayList, int position, int newObject){
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

    // Helper method to update an OccurrenceTimePeriod Array List item at position
    public ArrayList<OccurrenceTimePeriod> updateOccurrenceTimePeriodArrayListItemAtPosition
    (ArrayList<OccurrenceTimePeriod> arrayList, int position, OccurrenceTimePeriod newObject){
        // If the sent array list is empty, return an empty array list
        if (arrayList.size() == 0)
            return new ArrayList<>();

        // Create a new instance of an Array List
        ArrayList<OccurrenceTimePeriod> newArrayList = new ArrayList<>();
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
    public ArrayList<String> createSetPeriodsArrayList(String periods, int weekNumber){
        String[] splitPeriods = periods.split(":");
        ArrayList<String> periodList = new ArrayList<>();

        // Week 1: Get regular data
        if (weekNumber == 0){
            // This will be called if the row is period based
            if (splitPeriods.length == 12){
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
            if (splitPeriods.length == 12){
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
    public boolean occurrenceMatchesCurrentDay(Context context, String occurrence, String periods, int weekNumber, int dayOfWeek) {
        // In this case, no class time would have been set
        if (occurrence.equals("-1"))
            return false;

        String[] splitOccurrence = occurrence.split(":");
        String[] splitPeriods = periods.split(":");
        ScheduleFragment.showBlockHeaderA = false;
        ScheduleFragment.showBlockHeaderB = false;

        // Block Based Day Check
        if (splitOccurrence[0].equals("2")){
            // Get the preference for the Block format and set the boolean to show the block header
            String blockFormat = ((Activity)context).getPreferences(Context.MODE_PRIVATE)
                    .getString("blockFormat", "0:1:2:1:2:1:0");
            String[] splitBlockFormat = blockFormat.split(":");

            // Day A Check
            if (splitPeriods[0].equals("1") || splitPeriods[0].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("1")){
                    ScheduleFragment.showBlockHeaderA = true;
                    return true;
                }
            if (splitPeriods[1].equals("1") || splitPeriods[1].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("1")){
                    ScheduleFragment.showBlockHeaderA = true;
                    return true;
                }
            if (splitPeriods[2].equals("1") || splitPeriods[2].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("1")){
                    ScheduleFragment.showBlockHeaderA = true;
                    return true;
                }
            if (splitPeriods[3].equals("1") || splitPeriods[3].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("1")){
                    ScheduleFragment.showBlockHeaderA = true;
                    return true;
                }

            // Day B Check
            if (splitPeriods[0].equals("2") || splitPeriods[0].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("2")){
                    ScheduleFragment.showBlockHeaderB = true;
                    return true;
                }
            if (splitPeriods[1].equals("2") || splitPeriods[1].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("2")){
                    ScheduleFragment.showBlockHeaderB = true;
                    return true;
                }
            if (splitPeriods[2].equals("2") || splitPeriods[2].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("2")){
                    ScheduleFragment.showBlockHeaderB = true;
                    return true;
                }
            if (splitPeriods[3].equals("2") || splitPeriods[3].equals("3"))
                if (splitBlockFormat[dayOfWeek - 1].equals("2")){
                    ScheduleFragment.showBlockHeaderB = true;
                    return true;
                }
        }

        // Time/Period Based Day Check
        // Week A
        else if (weekNumber == 0){
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
            if (splitOccurrence[8].equals("1") || splitOccurrence[8].equals("3"))
                if (dayOfWeek == 7)
                    return true;
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

}
