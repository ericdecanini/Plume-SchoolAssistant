package com.pdt.plume;

import android.widget.Spinner;

/**
 * Created by user on 30/07/2016.
 */
public class Utlility {

    public static int getIndex(Spinner spinner, String myString){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }

}
