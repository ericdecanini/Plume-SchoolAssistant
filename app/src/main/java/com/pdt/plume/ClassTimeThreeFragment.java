package com.pdt.plume;


import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;


public class ClassTimeThreeFragment extends Fragment
        implements TimePickerDialog.OnTimeSetListener{

    Utility utility = new Utility();

    ArrayAdapter<String> adapter;

    ArrayList<Integer[]> isButtonChecked;
    TextView[] fieldTimeIn;
    TextView[] fieldTimeOut;
    public static int[] timeInHour;
    public static int[] timeOutHour;
    int[] timeInSeconds;
    int[] timeOutSeconds;
    int resourceId = -1;


    public ClassTimeThreeFragment() {
        // Required empty public constructor
    }

    public interface onDaysSelectedListener {
        //Pass all data through input params here
        public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds);
    }

    public interface onTimeSelectedListener {
        public void onTimeSelected(int resourceId, int previousTimeInSeconds, int previousTimeOutSeconds, int[] buttonsChecked);
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

        Bundle args = getArguments();
        String weekType = args.getString("weekType", "-1");

        ListView daysList = (ListView) rootView.findViewById(R.id.class_three_list);
        ArrayList<String> dayslistHeaders = new ArrayList<>();
        if (weekType.equals("1")){
            dayslistHeaders.add(getString(R.string.class_time_three_weekone));
            dayslistHeaders.add(getString(R.string.class_time_three_weektwo));
        } else{
            dayslistHeaders.add("");
        }
        adapter = new ArrayAdapter<>(getActivity(), R.layout.list_itemclass_time_three_timebase, R.id.class_three_item_header, dayslistHeaders);
        daysList.setAdapter(adapter);



        Button done = (Button) rootView.findViewById(R.id.class_three_done);
        Button[] sunday = {};
        Button[] monday = {};
        Button[] tuesday = {};
        Button[] wednesday = {};
        Button[] thursday = {};
        Button[] friday = {};
        Button[] saturday = {};
        for (int i = 0; i < adapter.getCount(); i++){
            isButtonChecked.add({0, 0, 0, 0, 0, 0, 0);
            sunday[i] = (Button) adapter.getView(i, daysList.getEmptyView(), daysList).findViewById(R.id.class_three_sunday);
            monday[i] = (Button) adapter.getView(i, daysList.getEmptyView(), daysList).findViewById(R.id.class_three_monday);
            tuesday[i] = (Button) adapter.getView(i, daysList.getEmptyView(), daysList).findViewById(R.id.class_three_tuesday);
            wednesday[i] = (Button) adapter.getView(i, daysList.getEmptyView(), daysList).findViewById(R.id.class_three_wednesday);
            thursday[i] = (Button) adapter.getView(i, daysList.getEmptyView(), daysList).findViewById(R.id.class_three_thursday);
            friday[i] = (Button) adapter.getView(i, daysList.getEmptyView(), daysList).findViewById(R.id.class_three_friday);
            saturday[i] = (Button) adapter.getView(i, daysList.getEmptyView(), daysList).findViewById(R.id.class_three_saturday);
            fieldTimeIn[i] = (TextView) adapter.getView(i, daysList.getEmptyView(), daysList).findViewById(R.id.field_new_schedule_timein);
            fieldTimeOut[i] = (TextView) adapter.getView(i, daysList.getEmptyView(), daysList).findViewById(R.id.field_new_schedule_timeout);

            sunday[i].setOnClickListener(listener());
            monday[i].setOnClickListener(listener());
            tuesday[i].setOnClickListener(listener());
            wednesday[i].setOnClickListener(listener());
            thursday[i].setOnClickListener(listener());
            friday[i].setOnClickListener(listener());
            saturday[i].setOnClickListener(listener());
            fieldTimeIn[i].setOnClickListener(showTimePickerDialog());
            fieldTimeOut[i].setOnClickListener(showTimePickerDialog());
            done.setOnClickListener(listener());
        }


        if (!args.containsKey("basis") && !args.containsKey("weekType")){
            for (int i = 0; i < adapter.getCount(); i++){
                int hourOfDay = args.getInt("hourOfDay");
                int minute = args.getInt("minute");
                int previousTimeInSeconds = args.getInt("timeInSeconds");
                int previousTimeOutSeconds = args.getInt("timeOutSeconds");
                isButtonChecked.clear();
                isButtonChecked.add(args.getIntegerArrayList("buttonsChecked"));
                //Get the previously clicked viewId which called the time dialog
                switch (args.getInt("resourceId")){
                    case R.id.field_new_schedule_timein:
                        timeInSeconds[i] = utility.timeToSeconds(hourOfDay, minute);
                        timeOutSeconds[i] = previousTimeOutSeconds;
                        if (minute < 10)
                            fieldTimeIn[i].setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeIn[i].setText(hourOfDay + ":" + minute);
                        fieldTimeOut[i].setText(utility.secondsToTime(previousTimeOutSeconds));
                        break;
                    case R.id.field_new_schedule_timeout:
                        timeInSeconds[i] = previousTimeInSeconds;
                        timeOutSeconds[i] = utility.timeToSeconds(hourOfDay, minute);
                        if (minute < 10)
                            fieldTimeOut[i].setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeOut[i].setText(hourOfDay + ":" + minute);
                        fieldTimeIn[i].setText(utility.secondsToTime(previousTimeInSeconds));
                        break;
                }
            }
        }
        else {
            for (int i = 0; i < adapter.getCount(); i++){
            Calendar c = Calendar.getInstance();
            timeInHour[i] = c.get(Calendar.HOUR_OF_DAY) + 1;
            timeOutHour[i] = c.get(Calendar.HOUR_OF_DAY) + 2;
            timeInSeconds[i] = utility.timeToSeconds(timeInHour[i], 0);
            timeOutSeconds[i] = utility.timeToSeconds(timeOutHour[i], 0);
            fieldTimeIn[i].setText(timeInHour + ":00");
            fieldTimeOut[i].setText(timeOutHour + ":00");
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
                        for (int i = 0; i < adapter.getCount(); i++)
                            daysSelectedListener.onDaysSelected(classDays, timeInSeconds[i], timeOutSeconds[i]);
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
                for (int i = 0; i < adapter.getCount(); i++)
                timeSelectedListener.onTimeSelected(resourceId, timeInSeconds[i], timeOutSeconds[i], isButtonChecked);
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
                for (int i = 0; i < adapter.getCount(); i++){
                    timeInSeconds[i] = utility.timeToSeconds(hourOfDay, minute);
                    fieldTimeIn[i].setText(timeString);
                }
                break;
            case R.id.field_new_schedule_timeout:
                for (int i = 0; i < adapter.getCount(); i++){
                    timeOutSeconds[i] = utility.timeToSeconds(hourOfDay, minute);
                    fieldTimeOut[i].setText(timeString);
                }
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
