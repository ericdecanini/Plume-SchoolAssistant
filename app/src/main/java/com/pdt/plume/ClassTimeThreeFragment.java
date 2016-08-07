package com.pdt.plume;


import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;


public class ClassTimeThreeFragment extends Fragment
        implements TimePickerDialog.OnTimeSetListener{

    Utility utility = new Utility();

    int[] isButtonChecked = {0, 0, 0, 0, 0, 0, 0};
    TextView fieldTimeIn;
    TextView fieldTimeOut;
    public static int timeInHour;
    public static int timeOutHour;
    int timeInSeconds;
    int timeOutSeconds;
    int resourceId = -1;


    public ClassTimeThreeFragment() {
        // Required empty public constructor
    }

    public interface onDaysSelectedListener {
        //Pass all data through input params here
        public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds);
    }

    onDaysSelectedListener daysSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            daysSelectedListener = (onDaysSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.class_time_three, container, false);
        Button sunday = (Button) rootView.findViewById(R.id.class_three_sunday);
        Button monday = (Button) rootView.findViewById(R.id.class_three_monday);
        Button tuesday = (Button) rootView.findViewById(R.id.class_three_tuesday);
        Button wednesday = (Button) rootView.findViewById(R.id.class_three_wednesday);
        Button thursday = (Button) rootView.findViewById(R.id.class_three_thursday);
        Button friday = (Button) rootView.findViewById(R.id.class_three_friday);
        Button saturday = (Button) rootView.findViewById(R.id.class_three_saturday);
        Button done = (Button) rootView.findViewById(R.id.class_three_done);
        fieldTimeIn = (TextView) rootView.findViewById(R.id.field_new_schedule_timein);
        fieldTimeOut = (TextView) rootView.findViewById(R.id.field_new_schedule_timeout);

        sunday.setOnClickListener(listener());
        monday.setOnClickListener(listener());
        tuesday.setOnClickListener(listener());
        wednesday.setOnClickListener(listener());
        thursday.setOnClickListener(listener());
        friday.setOnClickListener(listener());
        saturday.setOnClickListener(listener());
        done.setOnClickListener(listener());
        fieldTimeIn.setOnClickListener(showTimePickerDialog());
        fieldTimeOut.setOnClickListener(showTimePickerDialog());

        Calendar c = Calendar.getInstance();
        timeInHour = c.get(Calendar.HOUR_OF_DAY) + 1;
        timeOutHour = c.get(Calendar.HOUR_OF_DAY) + 2;
        timeInSeconds = utility.timeToSeconds(timeInHour, 0);
        timeOutSeconds = utility.timeToSeconds(timeOutHour, 0);
        String timeInDefault = timeInHour + ":00";
        String timeOutDefault = timeOutHour + ":00";
        fieldTimeIn.setText(timeInDefault);
        fieldTimeOut.setText(timeOutDefault);

        return rootView;
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.class_three_sunday:
                        if (isButtonChecked[0] == 1)
                            isButtonChecked[0] = 0;
                        else isButtonChecked[0] = 1;
                        break;
                    case R.id.class_three_monday:
                        if (isButtonChecked[1] == 1)
                            isButtonChecked[1] = 0;
                        else isButtonChecked[1] = 1;
                        break;
                    case R.id.class_three_tuesday:
                        if (isButtonChecked[2] == 1)
                            isButtonChecked[2] = 0;
                        else isButtonChecked[2] = 1;
                        break;
                    case R.id.class_three_wednesday:
                        if (isButtonChecked[3] == 1)
                            isButtonChecked[3] = 0;
                        else isButtonChecked[3] = 1;
                        break;
                    case R.id.class_three_thursday:
                        if (isButtonChecked[4] == 1)
                            isButtonChecked[4] = 0;
                        else isButtonChecked[4] = 1;
                        break;
                    case R.id.class_three_friday:
                        if (isButtonChecked[5] == 1)
                            isButtonChecked[5] = 0;
                        else isButtonChecked[5] = 1;
                        break;
                    case R.id.class_three_saturday:
                        if (isButtonChecked[6] == 1)
                            isButtonChecked[6] = 0;
                        else isButtonChecked[6] = 1;
                        break;
                    case R.id.class_three_done:
                        String classDays = processClassDaysString();
                        daysSelectedListener.onDaysSelected(classDays, timeInSeconds, timeOutSeconds);
                        break;
                }
            }
        };
    }

    private View.OnClickListener showTimePickerDialog() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resourceId = v.getId();
                DialogFragment timePickerFragment = new TimePickerFragment();
                if (resourceId != -1)
                    timePickerFragment.show(getActivity().getSupportFragmentManager(), "time picker");
            }
        };
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String timeString;
        if (minute < 10)
            timeString = hourOfDay + ":0" + minute;
        else
            timeString = hourOfDay + ":" + minute;
        switch (resourceId) {
            case R.id.field_new_schedule_timein:
                timeInSeconds = utility.timeToSeconds(hourOfDay, minute);
                fieldTimeIn.setText(timeString);
                break;
            case R.id.field_new_schedule_timeout:
                timeOutSeconds = utility.timeToSeconds(hourOfDay, minute);
                fieldTimeOut.setText(timeString);
                break;
        }
    }

    private String processClassDaysString(){
        return isButtonChecked[0] + ":"
                + isButtonChecked[1] + ":"
                + isButtonChecked[2] + ":"
                + isButtonChecked[3] + ":"
                + isButtonChecked[4] + ":"
                + isButtonChecked[5] + ":"
                + isButtonChecked[6];
    }

}
