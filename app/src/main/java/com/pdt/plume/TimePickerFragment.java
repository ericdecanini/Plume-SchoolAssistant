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

        // This error will remind you to implement an OnTimeSetListener
        //   in your Activity if you forget
        try {
            mListener = (TimePickerDialog.OnTimeSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTimeSetListener");
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        boolean FLAG_EDIT = NewScheduleActivity.isEdited;
        int resourceId = NewScheduleActivity.resourceId;
        final Calendar c = Calendar.getInstance();
        int hour;
        if (resourceId == R.id.field_new_schedule_timein) {
            if (FLAG_EDIT)
                hour = ClassTimeThreeFragmentTime.timeInHour;
            else
                hour = c.get(Calendar.HOUR_OF_DAY) + 1;
        }
        else {
            if (FLAG_EDIT)
                hour = ClassTimeThreeFragmentTime.timeOutHour;
            else
                hour = c.get(Calendar.HOUR_OF_DAY) + 2;
        }


        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(mActivity, mListener, hour, 0,
                DateFormat.is24HourFormat(getActivity()));
    }
}