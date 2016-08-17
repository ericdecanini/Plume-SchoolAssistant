package com.pdt.plume;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import java.util.Calendar;


public class TimePickerFragment extends DialogFragment {

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
        boolean FLAG_EDIT = NewScheduleActivity.isEdited;
        int resourceId = NewScheduleActivity.resourceId;

        // Get the current time
        final Calendar c = Calendar.getInstance();
        // If the NewScheduleActivity was launched through an edit action
        // Set the default time of the dialog to the corresponding
        // public static variable from the AddClassTimeThreeFragmentTime
        // and if not, base it on the current time
        int hour;

        if (resourceId == R.id.field_new_schedule_timein) {
            if (FLAG_EDIT)
                hour = AddClassTimeThreeFragmentTime.timeInHour;
            else
                hour = c.get(Calendar.HOUR_OF_DAY) + 1;
        }
        else {
            if (FLAG_EDIT)
                hour = AddClassTimeThreeFragmentTime.timeOutHour;
            else
                hour = c.get(Calendar.HOUR_OF_DAY) + 2;
        }


        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(mActivity, mListener, hour, 0,
                DateFormat.is24HourFormat(getActivity()));
    }
}