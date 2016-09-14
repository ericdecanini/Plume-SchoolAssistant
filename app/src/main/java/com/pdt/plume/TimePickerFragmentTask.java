package com.pdt.plume;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import java.util.Calendar;


public class TimePickerFragmentTask extends DialogFragment {

    private Activity mActivity;
    private TimePickerDialog.OnTimeSetListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

        //  This error will remind you to implement an OnTimeSetListener
        //  in your Activity if you forget
        try {
            mListener = (TimePickerDialog.OnTimeSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTimeSetListener");
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the public static variables from NewScheduleActivity
        // to acquire edit flag and view selected

        // Get the current time
        final Calendar c = Calendar.getInstance();
        // If the NewScheduleActivity was launched through an edit action
        // Set the default time of the dialog to the corresponding
        // public static variable from the AddClassTimeThreeFragmentTime
        // and if not, base it on the current time
        int hour;

        hour = c.get(Calendar.HOUR_OF_DAY) + 1;


        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(mActivity, mListener, hour, 0,
                DateFormat.is24HourFormat(getActivity()));
    }
}