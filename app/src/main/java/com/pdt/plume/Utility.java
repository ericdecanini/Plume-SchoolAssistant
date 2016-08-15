package com.pdt.plume;

import android.widget.Spinner;

import java.util.ArrayList;


public class Utility {

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

    // Helper method to update an Array List item at position
    public ArrayList<Object> updateArrayListItemAtPosition(ArrayList arrayList, int position, Object newObject){
        // Create a new instance of an Array List
        ArrayList<Object> newArrayList = new ArrayList<>();
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



}
