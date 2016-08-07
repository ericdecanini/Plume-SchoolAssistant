package com.pdt.plume;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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

    public int timeToSeconds(int hourOfDay, int minute){
        return (hourOfDay * 60 * 60) + (minute * 60);
    }

    public String secondsToTime(float seconds){
        int hourOfDay = (int) seconds / 3600;
        float tempMinute = seconds - hourOfDay * 3600;
        tempMinute = (tempMinute / 3600) * 60;
        int minute = (int) tempMinute;
        if (minute < 10)
            return hourOfDay + ":0" + minute;
        else
            return hourOfDay + ":" + minute;
    }



}
