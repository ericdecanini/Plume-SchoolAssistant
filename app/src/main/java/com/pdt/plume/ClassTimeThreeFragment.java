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
import android.widget.ImageView;
import android.widget.TimePicker;

import java.util.Calendar;


public class ClassTimeThreeFragment extends Fragment
        implements TimePickerDialog.OnTimeSetListener{

    Utility utility = new Utility();

    int[] isButtonChecked = {0, 0, 0, 0, 0, 0, 0};
    EditText fieldTimeIn;
    EditText fieldTimeOut;
    EditText fieldTimeInAlt;
    EditText fieldTimeOutAlt;
    public static int timeInHour;
    public static int timeOutHour;
    public static int timeInAltHour;
    public static int timeOutAltHour;
    int timeInSeconds;
    int timeOutSeconds;
    int timeInAltSeconds;
    int timeOutAltSeconds;
    int resourceId = -1;


    public ClassTimeThreeFragment() {
        // Required empty public constructor
    }

    public interface onDaysSelectedListener {
        //Pass all data through input params here
        public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds, int timeInAltSeconds, int timeOutAltSeconds);
    }

    public interface onTimeSelectedListener {
        public void onTimeSelected(int resourceId, int previousTimeInSeconds, int previousTimeOutSeconds, int previousTimeInAltSeconds, int previousTimeOutAltSeconds, int[] buttonsChecked);
    }

    onDaysSelectedListener daysSelectedListener;
    onTimeSelectedListener timeSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            daysSelectedListener = (onDaysSelectedListener) context;
            timeSelectedListener = (onTimeSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.class_time_three, container, false);
        ImageView sunday = (ImageView) rootView.findViewById(R.id.class_three_sunday);
        ImageView monday = (ImageView) rootView.findViewById(R.id.class_three_monday);
        ImageView tuesday = (ImageView) rootView.findViewById(R.id.class_three_tuesday);
        ImageView wednesday = (ImageView) rootView.findViewById(R.id.class_three_wednesday);
        ImageView thursday = (ImageView) rootView.findViewById(R.id.class_three_thursday);
        ImageView friday = (ImageView) rootView.findViewById(R.id.class_three_friday);
        ImageView saturday = (ImageView) rootView.findViewById(R.id.class_three_saturday);
        fieldTimeIn = (EditText) rootView.findViewById(R.id.field_new_schedule_timein);
        fieldTimeOut = (EditText) rootView.findViewById(R.id.field_new_schedule_timeout);
        Button done = (Button) rootView.findViewById(R.id.class_three_done);

        ImageView sundayAlt = (ImageView) rootView.findViewById(R.id.class_three_sunday_alt);
        ImageView mondayAlt = (ImageView) rootView.findViewById(R.id.class_three_monday_alt);
        ImageView tuesdayAlt = (ImageView) rootView.findViewById(R.id.class_three_tuesday_alt);
        ImageView wednesdayAlt = (ImageView) rootView.findViewById(R.id.class_three_wednesday_alt);
        ImageView thursdayAlt = (ImageView) rootView.findViewById(R.id.class_three_thursday_alt);
        ImageView fridayAlt = (ImageView) rootView.findViewById(R.id.class_three_friday_alt);
        ImageView saturdayAlt = (ImageView) rootView.findViewById(R.id.class_three_saturday_alt);
        fieldTimeInAlt = (EditText) rootView.findViewById(R.id.field_new_schedule_timein_alt);
        fieldTimeOutAlt = (EditText) rootView.findViewById(R.id.field_new_schedule_timeout_alt);

        sunday.setOnClickListener(listener());
        monday.setOnClickListener(listener());
        tuesday.setOnClickListener(listener());
        wednesday.setOnClickListener(listener());
        thursday.setOnClickListener(listener());
        friday.setOnClickListener(listener());
        saturday.setOnClickListener(listener());
        fieldTimeIn.setOnClickListener(showTimePickerDialog());
        fieldTimeOut.setOnClickListener(showTimePickerDialog());
        done.setOnClickListener(listener());

        sundayAlt.setOnClickListener(listener());
        mondayAlt.setOnClickListener(listener());
        tuesdayAlt.setOnClickListener(listener());
        wednesdayAlt.setOnClickListener(listener());
        thursdayAlt.setOnClickListener(listener());
        fridayAlt.setOnClickListener(listener());
        saturdayAlt.setOnClickListener(listener());
        fieldTimeInAlt.setOnClickListener(showTimePickerDialog());
        fieldTimeOutAlt.setOnClickListener(showTimePickerDialog());

        Bundle args = getArguments();
        if (args != null){
            if (!args.getString("weekType", "-1").equals("1"))
                //Change the layout based on weekType
                rootView.findViewById(R.id.class_time_three_week_type_alt_layout).setVisibility(View.GONE);

            //Set the variables for timeIn and timeOut if the fragment was restarted by onTimeSet
            if (args.containsKey("hourOfDay")){
                int hourOfDay = args.getInt("hourOfDay");
                int minute = args.getInt("minute");
                int previousTimeInSeconds = args.getInt("timeInSeconds");
                int previousTimeOutSeconds = args.getInt("timeOutSeconds");
                int previousTimeInAltSeconds = args.getInt("timeInAltSeconds");
                int previousTimeOutAltSeconds = args.getInt("timeOutAltSeconds");
                isButtonChecked = args.getIntArray("buttonsChecked");;
                switch (args.getInt("resourceId")){
                    case R.id.field_new_schedule_timein:
                        timeInSeconds = utility.timeToSeconds(hourOfDay, minute);
                        timeOutSeconds = previousTimeOutSeconds;
                        timeInAltSeconds = previousTimeInAltSeconds;
                        timeOutAltSeconds = previousTimeOutAltSeconds;
                        if (minute < 10)
                            fieldTimeIn.setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeIn.setText(hourOfDay + ":" + minute);
                        fieldTimeOut.setText(utility.secondsToTime(previousTimeOutSeconds));
                        fieldTimeInAlt.setText(utility.secondsToTime(previousTimeInAltSeconds));
                        fieldTimeOutAlt.setText(utility.secondsToTime(previousTimeOutAltSeconds));
                        break;
                    case R.id.field_new_schedule_timeout:
                        timeInSeconds = previousTimeInSeconds;
                        timeOutSeconds = utility.timeToSeconds(hourOfDay, minute);
                        timeInAltSeconds = previousTimeInAltSeconds;
                        timeOutAltSeconds = previousTimeOutAltSeconds;
                        if (minute < 10)
                            fieldTimeOut.setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeOut.setText(hourOfDay + ":" + minute);
                        fieldTimeIn.setText(utility.secondsToTime(previousTimeInSeconds));
                        fieldTimeInAlt.setText(utility.secondsToTime(previousTimeInAltSeconds));
                        fieldTimeOutAlt.setText(utility.secondsToTime(previousTimeOutAltSeconds));
                        break;
                    case R.id.field_new_schedule_timein_alt:
                        timeInSeconds = previousTimeInSeconds;
                        timeOutSeconds = previousTimeOutSeconds;
                        timeInAltSeconds = utility.timeToSeconds(hourOfDay, minute);
                        timeOutAltSeconds = previousTimeOutAltSeconds;
                        if (minute < 10)
                            fieldTimeInAlt.setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeInAlt.setText(hourOfDay + ":" + minute);
                        fieldTimeIn.setText(utility.secondsToTime(previousTimeInSeconds));
                        fieldTimeOut.setText(utility.secondsToTime(previousTimeOutSeconds));
                        fieldTimeOutAlt.setText(utility.secondsToTime(previousTimeOutAltSeconds));
                        break;
                    case R.id.field_new_schedule_timeout_alt:
                        timeInSeconds = previousTimeInSeconds;
                        timeOutSeconds = previousTimeOutSeconds;
                        timeInAltSeconds = previousTimeInAltSeconds;
                        timeOutAltSeconds = utility.timeToSeconds(hourOfDay, minute);
                        if (minute < 10)
                            fieldTimeOutAlt.setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeOutAlt.setText(hourOfDay + ":" + minute);
                        fieldTimeIn.setText(utility.secondsToTime(previousTimeInSeconds));
                        fieldTimeOut.setText(utility.secondsToTime(previousTimeOutSeconds));
                        fieldTimeInAlt.setText(utility.secondsToTime(previousTimeInAltSeconds));
                        break;
                }
            }
            else {
                Calendar c = Calendar.getInstance();
                timeInHour = c.get(Calendar.HOUR_OF_DAY) + 1;
                timeOutHour = c.get(Calendar.HOUR_OF_DAY) + 2;
                timeInAltHour = c.get(Calendar.HOUR_OF_DAY) + 1;
                timeOutAltHour = c.get(Calendar.HOUR_OF_DAY) + 2;
                timeInSeconds = utility.timeToSeconds(timeInHour, 0);
                timeOutSeconds = utility.timeToSeconds(timeOutHour, 0);
                timeInAltSeconds = utility.timeToSeconds(timeInHour, 0);
                timeOutAltSeconds = utility.timeToSeconds(timeOutHour, 0);
                fieldTimeIn.setText(timeInHour + ":00");
                fieldTimeOut.setText(timeOutHour + ":00");
                fieldTimeInAlt.setText(timeInAltHour + ":00");
                fieldTimeOutAlt.setText(timeOutAltHour + ":00");
            }
        }
        return rootView;
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.class_three_sunday:
                        if (isButtonChecked[0] == 0)
                            isButtonChecked[0] = 1;
                        else if (isButtonChecked[0] == 1)
                            isButtonChecked[0] = 0;
                        else if (isButtonChecked[0] == 2)
                            isButtonChecked[0] = 3;
                        else if (isButtonChecked[0] == 3)
                            isButtonChecked[0] = 2;
                        break;
                    case R.id.class_three_monday:
                        if (isButtonChecked[1] == 0)
                            isButtonChecked[1] = 1;
                        else if (isButtonChecked[1] == 1)
                            isButtonChecked[1] = 0;
                        else if (isButtonChecked[1] == 2)
                            isButtonChecked[1] = 3;
                        else if (isButtonChecked[1] == 3)
                            isButtonChecked[1] = 2;
                        break;
                    case R.id.class_three_tuesday:
                        if (isButtonChecked[2] == 0)
                            isButtonChecked[2] = 1;
                        else if (isButtonChecked[2] == 1)
                            isButtonChecked[2] = 0;
                        else if (isButtonChecked[2] == 2)
                            isButtonChecked[2] = 3;
                        else if (isButtonChecked[2] == 3)
                            isButtonChecked[2] = 2;
                        break;
                    case R.id.class_three_wednesday:
                        if (isButtonChecked[3] == 0)
                            isButtonChecked[3] = 1;
                        else if (isButtonChecked[3] == 1)
                            isButtonChecked[3] = 0;
                        else if (isButtonChecked[3] == 2)
                            isButtonChecked[3] = 3;
                        else if (isButtonChecked[3] == 3)
                            isButtonChecked[3] = 2;
                        break;
                    case R.id.class_three_thursday:
                        if (isButtonChecked[4] == 0)
                            isButtonChecked[4] = 1;
                        else if (isButtonChecked[4] == 1)
                            isButtonChecked[4] = 0;
                        else if (isButtonChecked[4] == 2)
                            isButtonChecked[4] = 3;
                        else if (isButtonChecked[4] == 3)
                            isButtonChecked[4] = 2;
                        break;
                    case R.id.class_three_friday:
                        if (isButtonChecked[5] == 0)
                            isButtonChecked[5] = 1;
                        else if (isButtonChecked[5] == 1)
                            isButtonChecked[5] = 0;
                        else if (isButtonChecked[5] == 2)
                            isButtonChecked[5] = 3;
                        else if (isButtonChecked[5] == 3)
                            isButtonChecked[5] = 2;
                        break;
                    case R.id.class_three_saturday:
                        if (isButtonChecked[6] == 0)
                            isButtonChecked[6] = 1;
                        else if (isButtonChecked[6] == 1)
                            isButtonChecked[6] = 0;
                        else if (isButtonChecked[6] == 2)
                            isButtonChecked[6] = 3;
                        else if (isButtonChecked[6] == 3)
                            isButtonChecked[6] = 2;
                        break;

                    case R.id.class_three_sunday_alt:
                        if (isButtonChecked[0] == 0)
                            isButtonChecked[0] = 2;
                        else if (isButtonChecked[0] == 1)
                            isButtonChecked[0] = 3;
                        else if (isButtonChecked[0] == 2)
                            isButtonChecked[0] = 0;
                        else if (isButtonChecked[0] == 3)
                            isButtonChecked[0] = 1;
                        break;
                    case R.id.class_three_monday_alt:
                        if (isButtonChecked[1] == 0)
                            isButtonChecked[1] = 2;
                        else if (isButtonChecked[1] == 1)
                            isButtonChecked[1] = 3;
                        else if (isButtonChecked[1] == 2)
                            isButtonChecked[1] = 0;
                        else if (isButtonChecked[1] == 3)
                            isButtonChecked[1] = 1;
                        break;
                    case R.id.class_three_tuesday_alt:
                        if (isButtonChecked[2] == 0)
                            isButtonChecked[2] = 2;
                        else if (isButtonChecked[2] == 1)
                            isButtonChecked[2] = 3;
                        else if (isButtonChecked[2] == 2)
                            isButtonChecked[2] = 0;
                        else if (isButtonChecked[2] == 3)
                            isButtonChecked[2] = 1;
                        break;
                    case R.id.class_three_wednesday_alt:
                        if (isButtonChecked[3] == 0)
                            isButtonChecked[3] = 2;
                        else if (isButtonChecked[3] == 1)
                            isButtonChecked[3] = 3;
                        else if (isButtonChecked[3] == 2)
                            isButtonChecked[3] = 0;
                        else if (isButtonChecked[3] == 3)
                            isButtonChecked[3] = 1;
                        break;
                    case R.id.class_three_thursday_alt:
                        if (isButtonChecked[4] == 0)
                            isButtonChecked[4] = 2;
                        else if (isButtonChecked[4] == 1)
                            isButtonChecked[4] = 3;
                        else if (isButtonChecked[4] == 2)
                            isButtonChecked[4] = 0;
                        else if (isButtonChecked[4] == 3)
                            isButtonChecked[4] = 1;
                        break;
                    case R.id.class_three_friday_alt:
                        if (isButtonChecked[5] == 0)
                            isButtonChecked[5] = 2;
                        else if (isButtonChecked[5] == 1)
                            isButtonChecked[5] = 3;
                        else if (isButtonChecked[5] == 2)
                            isButtonChecked[5] = 0;
                        else if (isButtonChecked[5] == 3)
                            isButtonChecked[5] = 1;
                        break;
                    case R.id.class_three_saturday_alt:
                        if (isButtonChecked[6] == 0)
                            isButtonChecked[6] = 2;
                        else if (isButtonChecked[6] == 1)
                            isButtonChecked[6] = 3;
                        else if (isButtonChecked[6] == 2)
                            isButtonChecked[6] = 0;
                        else if (isButtonChecked[6] == 3)
                            isButtonChecked[6] = 1;
                        break;
                    case R.id.class_three_done:
                        String classDays = processClassDaysString();
                        daysSelectedListener.onDaysSelected(classDays, timeInSeconds, timeOutSeconds, timeInAltSeconds, timeOutAltSeconds);
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
                timeSelectedListener.onTimeSelected(resourceId, timeInSeconds, timeOutSeconds, timeInAltSeconds, timeOutAltSeconds, isButtonChecked);
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
            case R.id.field_new_schedule_timein_alt:
                timeInAltSeconds = utility.timeToSeconds(hourOfDay, minute);
                fieldTimeInAlt.setText(timeString);
                break;
            case R.id.field_new_schedule_timeout_alt:
                timeOutAltSeconds = utility.timeToSeconds(hourOfDay, minute);
                fieldTimeOutAlt.setText(timeString);
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