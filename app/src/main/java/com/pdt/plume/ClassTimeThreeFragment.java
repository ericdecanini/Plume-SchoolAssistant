package com.pdt.plume;


import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
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
        implements TimePickerDialog.OnTimeSetListener {

    Utility utility = new Utility();
    String LOG_TAG = ClassTimeThreeFragment.class.getSimpleName();

    ArrayAdapter<String> adapter;

    ArrayList<Integer> isButtonChecked = new ArrayList<>();
    ArrayList<TextView> fieldTimeIn = new ArrayList<>();
    ArrayList<TextView> fieldTimeOut = new ArrayList<>();
    public static int[] timeInHour = {0, 0};
    public static int[] timeOutHour = {0, 0};
    public static int listCount;
    public static int listPosition;
    int[] timeInSeconds = {0, 0};
    int[] timeOutSeconds = {0, 0};
    int resourceId = -1;


    public ClassTimeThreeFragment() {
        // Required empty public constructor
    }

    public interface onDaysSelectedListener {
        //Pass all data through input params here
        public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds);
    }

    public interface onTimeSelectedListener {
        public void onTimeSelected(int resourceId, int previousTimeInSeconds, int previousTimeOutSeconds, ArrayList<Integer> buttonsChecked);
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
        String weekType = "";
        if (args != null)
        weekType = args.getString("weekType", "-1");

        ListView daysList = (ListView) rootView.findViewById(R.id.class_three_list);
        ArrayList<String> dayslistHeaders = new ArrayList<>();
        if (weekType.equals("1")) {
            dayslistHeaders.add(getString(R.string.class_time_three_weekone));
            dayslistHeaders.add(getString(R.string.class_time_three_weektwo));
            listCount = 2;
        } else {
            dayslistHeaders.add("");
        }
        adapter = new ArrayAdapter<>(getActivity(), R.layout.list_itemclass_time_three_timebase, R.id.class_three_item_header, dayslistHeaders);
        daysList.setAdapter(adapter);
        listCount = adapter.getCount();
        for (int i = 0; i < 7; i ++)
            isButtonChecked.add(0);


        Button done = (Button) rootView.findViewById(R.id.class_three_done);
        ArrayList<Button> sunday = new ArrayList<>();
        ArrayList<Button> monday = new ArrayList<>();
        ArrayList<Button> tuesday = new ArrayList<>();
        ArrayList<Button> wednesday = new ArrayList<>();
        ArrayList<Button> thursday = new ArrayList<>();
        ArrayList<Button> friday = new ArrayList<>();
        ArrayList<Button> saturday = new ArrayList<>();
        for (int i = 0; i < listCount; i++) {
            sunday.add((Button) adapter.getView(i, null, daysList).findViewById(R.id.class_three_sunday));
            monday.add((Button) adapter.getView(i, null, daysList).findViewById(R.id.class_three_monday));
            tuesday.add((Button) adapter.getView(i, null, daysList).findViewById(R.id.class_three_tuesday));
            wednesday.add((Button) adapter.getView(i, null, daysList).findViewById(R.id.class_three_wednesday));
            thursday.add((Button) adapter.getView(i, null, daysList).findViewById(R.id.class_three_thursday));
            friday.add((Button) adapter.getView(i, null, daysList).findViewById(R.id.class_three_friday));
            saturday.add((Button) adapter.getView(i, null, daysList).findViewById(R.id.class_three_saturday));
            fieldTimeIn.add((TextView) adapter.getView(i, null, daysList).findViewById(R.id.field_new_schedule_timein));
            fieldTimeOut.add((TextView) adapter.getView(i, null, daysList).findViewById(R.id.field_new_schedule_timeout));

            sunday.get(i).setOnClickListener(listener(i));
            monday.get(i).setOnClickListener(listener(i));
            tuesday.get(i).setOnClickListener(listener(i));
            wednesday.get(i).setOnClickListener(listener(i));
            thursday.get(i).setOnClickListener(listener(i));
            friday.get(i).setOnClickListener(listener(i));
            saturday.get(i).setOnClickListener(listener(i));
            fieldTimeIn.get(i).setOnClickListener(showTimePickerDialog(i));
            fieldTimeOut.get(i).setOnClickListener(showTimePickerDialog(i));
            done.setOnClickListener(listener(i));
        }


        if (!args.containsKey("basis") && !args.containsKey("weekType")) {
            for (int i = 0; i < listCount; i++) {
                int hourOfDay = args.getInt("hourOfDay");
                int minute = args.getInt("minute");
                int previousTimeInSeconds = args.getInt("timeInSeconds");
                int previousTimeOutSeconds = args.getInt("timeOutSeconds");
                isButtonChecked.clear();
//                int[] integers = args.getIntArray("buttonsChecked");
//                ArrayList<Integer> newIntegers = new ArrayList<>();
//                for (int ii = 0; ii < integers.length; ii++)
//                    newIntegers.add(integers[ii]);
                isButtonChecked = args.getIntegerArrayList("buttonsChecked");
                //Get the previously clicked viewId which called the time dialog
                switch (args.getInt("resourceId")) {
                    case R.id.field_new_schedule_timein:
                        timeInSeconds[i] = utility.timeToSeconds(hourOfDay, minute);
                        timeOutSeconds[i] = previousTimeOutSeconds;
                        if (minute < 10)
                            fieldTimeIn.get(i).setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeIn.get(i).setText(hourOfDay + ":" + minute);
                        fieldTimeOut.get(i).setText(utility.secondsToTime(previousTimeOutSeconds));
                        break;
                    case R.id.field_new_schedule_timeout:
                        timeInSeconds[i] = previousTimeInSeconds;
                        timeOutSeconds[i] = utility.timeToSeconds(hourOfDay, minute);
                        if (minute < 10)
                            fieldTimeOut.get(i).setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeOut.get(i).setText(hourOfDay + ":" + minute);
                        fieldTimeIn.get(i).setText(utility.secondsToTime(previousTimeInSeconds));
                        break;
                }
            }
        } else {
            for (int i = 0; i < listCount; i++) {
                Calendar c = Calendar.getInstance();
                timeInHour[i] = c.get(Calendar.HOUR_OF_DAY) + 1;
                timeOutHour[i] = c.get(Calendar.HOUR_OF_DAY) + 2;
                timeInSeconds[i] = utility.timeToSeconds(timeInHour[i], 0);
                timeOutSeconds[i] = utility.timeToSeconds(timeOutHour[i], 0);
                fieldTimeIn.get(i).setText(timeInHour + ":00");
                fieldTimeOut.get(i).setText(timeOutHour + ":00");
            }
        }

        return rootView;
    }

    private View.OnClickListener listener(final int position) {
        Log.v(LOG_TAG, "listener has been executed");
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.class_three_sunday:
                        if (isButtonChecked.get(0) == 1)
                            utility.updateArrayListItemAtPosition(isButtonChecked, 0, 0);
                        else utility.updateArrayListItemAtPosition(isButtonChecked, 0, 1);
                        break;
                    case R.id.class_three_monday:
                        if (isButtonChecked.get(1) == 1)
                            utility.updateArrayListItemAtPosition(isButtonChecked, 1, 0);
                        else utility.updateArrayListItemAtPosition(isButtonChecked, 1, 1);
                        break;
                    case R.id.class_three_tuesday:
                        if (isButtonChecked.get(2) == 1)
                            utility.updateArrayListItemAtPosition(isButtonChecked, 2, 0);
                        else utility.updateArrayListItemAtPosition(isButtonChecked, 2, 1);
                        break;
                    case R.id.class_three_wednesday:
                        if (isButtonChecked.get(3) == 1)
                            utility.updateArrayListItemAtPosition(isButtonChecked, 3, 0);
                        else utility.updateArrayListItemAtPosition(isButtonChecked, 3, 1);
                        break;
                    case R.id.class_three_thursday:
                        if (isButtonChecked.get(4) == 1)
                            utility.updateArrayListItemAtPosition(isButtonChecked, 4, 0);
                        else utility.updateArrayListItemAtPosition(isButtonChecked, 4, 1);
                        break;
                    case R.id.class_three_friday:
                        if (isButtonChecked.get(5) == 1)
                            utility.updateArrayListItemAtPosition(isButtonChecked, 5, 0);
                        else utility.updateArrayListItemAtPosition(isButtonChecked, 5, 1);
                        break;
                    case R.id.class_three_saturday:
                        if (isButtonChecked.get(6) == 1)
                            utility.updateArrayListItemAtPosition(isButtonChecked, 6, 0);
                        else utility.updateArrayListItemAtPosition(isButtonChecked, 6, 1);
                        break;
                    case R.id.class_three_done:
                        String classDays = processClassDaysString();
                        for (int i = 0; i < listCount; i++)
                            daysSelectedListener.onDaysSelected(classDays, timeInSeconds[i], timeOutSeconds[i]);
                        break;
                }
            }
        };
    }

    private View.OnClickListener showTimePickerDialog(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "showTimePickerDialog has been executed");
                listPosition = position;
                resourceId = v.getId();
                DialogFragment timePickerFragment = new TimePickerFragment();
                if (resourceId != -1)
                    timePickerFragment.show(getActivity().getSupportFragmentManager(), "time picker");
                    timeSelectedListener.onTimeSelected(resourceId, timeInSeconds[listPosition], timeOutSeconds[listCount], isButtonChecked);
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
                for (int i = 0; i < listCount; i++){
                    timeInSeconds[i] = utility.timeToSeconds(hourOfDay, minute);
                    fieldTimeIn.get(i).setText(timeString);
                }
                break;
            case R.id.field_new_schedule_timeout:
                for (int i = 0; i < listCount; i++){
                    timeOutSeconds[i] = utility.timeToSeconds(hourOfDay, minute);
                    fieldTimeOut.get(i).setText(timeString);
                }
                break;
        }
    }

    private String processClassDaysString(){
        return isButtonChecked.get(0) + ":"
                + isButtonChecked.get(1) + ":"
                + isButtonChecked.get(2) + ":"
                + isButtonChecked.get(3) + ":"
                + isButtonChecked.get(4) + ":"
                + isButtonChecked.get(5) + ":"
                + isButtonChecked.get(6);
    }

}
