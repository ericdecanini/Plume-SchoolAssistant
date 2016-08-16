package com.pdt.plume;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


public class ClassTimeThreeFragmentTime extends DialogFragment{

    // Constantly used variables
    String LOG_TAG = ClassTimeThreeFragmentTime.class.getSimpleName();
    Utility utility = new Utility();

    // UI Elements
    EditText fieldTimeIn;
    EditText fieldTimeOut;
    EditText fieldTimeInAlt;
    EditText fieldTimeOutAlt;

    // Fragment input storage variables
    int[] isButtonChecked = {0, 0, 0, 0, 0, 0, 0};
    int timeInSeconds;
    int timeOutSeconds;
    int timeInAltSeconds;
    int timeOutAltSeconds;

    public static int timeInHour;
    public static int timeOutHour;
    public static int timeInAltHour;
    public static int timeOutAltHour;

    // View IDs passed along activities
    int resourceId = -1;

    // Interface variables
    onDaysSelectedListener daysSelectedListener;
    onTimeSelectedListener timeSelectedListener;
    onBasisTextviewSelectedListener basisTextviewSelectedListener;
    onWeektypeTextviewSelectedListener weektypeTextviewSelectedListener;

    // Public Constructor
    public static ClassTimeThreeFragmentTime newInstance(int title) {
        ClassTimeThreeFragmentTime fragment = new ClassTimeThreeFragmentTime();
        Bundle args = new Bundle();
        args.putInt("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    // Interfaces used to pass data to NewScheduleActivity
    public interface onDaysSelectedListener {
        //Pass all data through input params here
        public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds,
                                   int timeInAltSeconds, int timeOutAltSeconds, String periods,
                                   boolean FLAG_EDIT, int rowId);
    }
    public interface onTimeSelectedListener {
        public void onTimeSelected(int resourceId, int previousTimeInSeconds, int previousTimeOutSeconds, int previousTimeInAltSeconds, int previousTimeOutAltSeconds, int[] buttonsChecked);
    }
    public interface onBasisTextviewSelectedListener {
        //Pass all data through input params here
        public void onBasisTextviewSelected();
    }
    public interface onWeektypeTextviewSelectedListener {
        //Pass all data through input params here
        public void onWeektypeTextViewSelectedListener(String basis);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            daysSelectedListener = (onDaysSelectedListener) context;
            timeSelectedListener = (onTimeSelectedListener) context;
            basisTextviewSelectedListener = (onBasisTextviewSelectedListener) context;
            weektypeTextviewSelectedListener = (onWeektypeTextviewSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set the fragment's window size to match the screen
        Window window = this.getDialog().getWindow();
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.class_time_three_time, container, false);

        // Get references to each UI element
        TextView basisTextView = (TextView) rootView.findViewById(R.id.class_time_one_value);
        TextView weekTypeTextView = (TextView) rootView.findViewById(R.id.class_time_two_value);

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

        // Set OnClickListeners to the UI elements
        basisTextView.setOnClickListener(listener());
        weekTypeTextView.setOnClickListener(listener());

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

        // Set the text of the hyperlink basis text to the time based string annotation
        basisTextView.setText(getString(R.string.class_time_one_timebased));

        // Get the arguments of the fragment.
        // Check week type and if it is 'Same each week', hide the alternate layout.
        // Set the hyperlink week type text accordingly
        // Check if fragment was restarted via Time Set, restore previous state
        Bundle args = getArguments();
        if (args != null){
            // Hide the alternate layout if the week type selected is 0 (Same time every week)
            // and set the hyperlink week type text to the selected week type text
            if (!args.getString("weekType", "-1").equals("1")){
                // If weekType is 0 (Same each week), hide the alternate layout
                rootView.findViewById(R.id.class_time_three_week_type_alt_layout).setVisibility(View.GONE);
                weekTypeTextView.setText(getString(R.string.class_time_two_sameweek));
            }
            else weekTypeTextView.setText(getString(R.string.class_time_two_altweeks));

            // Check if the fragment was launched from the OnTimeSet override method in NewScheduleActivity
            // If it is, get the fragment's previous state data and update the fragment data and UI accordingly
            // If the fragment contains the 'hourOfDay' string, it must contain other previous state data
            if (args.containsKey("hourOfDay")){
                // Get previous state data
                int hourOfDay = args.getInt("hourOfDay");
                int minute = args.getInt("minute");
                int previousTimeInSeconds = args.getInt("timeInSeconds");
                int previousTimeOutSeconds = args.getInt("timeOutSeconds");
                int previousTimeInAltSeconds = args.getInt("timeInAltSeconds");
                int previousTimeOutAltSeconds = args.getInt("timeOutAltSeconds");
                isButtonChecked = args.getIntArray("buttonsChecked");

                // Set the default values of the time fields accordingly
                // as well as update the fragment's global variables of time
                // Global variables updated: timeInSeconds, timeOutSeconds, timeInAltSeconds, timeOutAltSeconds
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

            // If not, check if the fragment was started through the list view's OnItemClick
            // If it is, receive the corresponding data and auto-fill that item's UI
            else if (args.containsKey("occurrence")){
                // Get the data from the arguments bundle
                String occurrence = args.getString("occurrence", "-1");
                String[] splitOccurrence = occurrence.split(":");
                int timeInSeconds = args.getInt("timeInSeconds");
                int timeOutSeconds = args.getInt("timeOutSeconds");
                int timeInAltSeconds = args.getInt("timeInAltSeconds");
                int timeOutAltSeconds = args.getInt("timeOutAltSeconds");

                // Check each item in the occurrence string's day binary
                // and set it in the activity
                if (splitOccurrence[2].equals("1") || splitOccurrence[2].equals("3")){
                    isButtonChecked[0] = 1;
                    sunday.setImageResource(R.drawable.ui_saturday_sunday_selected);
                }
                if (splitOccurrence[3].equals("1") || splitOccurrence[3].equals("3")){
                    isButtonChecked[1] = 1;
                    monday.setImageResource(R.drawable.ui_monday_selected);
                }
                if (splitOccurrence[4].equals("1") || splitOccurrence[4].equals("3")){
                    isButtonChecked[2] = 1;
                    tuesday.setImageResource(R.drawable.ui_tuesday_thursday_selected);
                }
                if (splitOccurrence[5].equals("1") || splitOccurrence[5].equals("3")){
                    isButtonChecked[3] = 1;
                    wednesday.setImageResource(R.drawable.ui_wednesday_selected);
                }
                if (splitOccurrence[6].equals("1") || splitOccurrence[6].equals("3")){
                    isButtonChecked[4] = 1;
                    thursday.setImageResource(R.drawable.ui_tuesday_thursday_selected);
                }
                if (splitOccurrence[7].equals("1") || splitOccurrence[7].equals("3")){
                    isButtonChecked[5] = 1;
                    friday.setImageResource(R.drawable.ui_friday_selected);
                }
                if (splitOccurrence[8].equals("1") || splitOccurrence[8].equals("3")){
                    isButtonChecked[6] = 1;
                    saturday.setImageResource(R.drawable.ui_saturday_sunday_selected);
                }

                // Do so for alternate layout if it is available
                if (splitOccurrence[1].equals("1")){
                    if (splitOccurrence[2].equals("2") || splitOccurrence[2].equals("3")){
                        if (isButtonChecked[0] == 1)
                            isButtonChecked[0] = 3;
                        else isButtonChecked[0] = 2;
                        sundayAlt.setImageResource(R.drawable.ui_saturday_sunday_selected);
                    }
                    if (splitOccurrence[3].equals("2") || splitOccurrence[3].equals("3")){
                        if (isButtonChecked[1] == 1)
                            isButtonChecked[1] = 3;
                        else isButtonChecked[1] = 2;
                        mondayAlt.setImageResource(R.drawable.ui_monday_selected);
                    }
                    if (splitOccurrence[4].equals("2") || splitOccurrence[4].equals("3")){
                        if (isButtonChecked[2] == 1)
                            isButtonChecked[2] = 3;
                        else isButtonChecked[2] = 2;
                        tuesdayAlt.setImageResource(R.drawable.ui_tuesday_thursday_selected);
                    }
                    if (splitOccurrence[5].equals("2") || splitOccurrence[5].equals("3")){
                        if (isButtonChecked[3] == 1)
                            isButtonChecked[3] = 3;
                        else isButtonChecked[3] = 2;
                        wednesdayAlt.setImageResource(R.drawable.ui_wednesday_selected);
                    }
                    if (splitOccurrence[6].equals("2") || splitOccurrence[6].equals("3")){
                        if (isButtonChecked[4] == 1)
                            isButtonChecked[4] = 3;
                        else isButtonChecked[4] = 2;
                        thursdayAlt.setImageResource(R.drawable.ui_tuesday_thursday_selected);
                    }
                    if (splitOccurrence[7].equals("2") || splitOccurrence[7].equals("3")){
                        if (isButtonChecked[5] == 1)
                            isButtonChecked[5] = 3;
                        else isButtonChecked[5] = 2;
                        fridayAlt.setImageResource(R.drawable.ui_friday_selected);
                    }
                    if (splitOccurrence[8].equals("2") || splitOccurrence[8].equals("3")){
                        if (isButtonChecked[6] == 1)
                            isButtonChecked[6] = 3;
                        else isButtonChecked[6] = 2;
                        saturdayAlt.setImageResource(R.drawable.ui_saturday_sunday_selected);
                    }
                }

                // Set the global time variables
                this.timeInSeconds = timeInSeconds;
                this.timeOutSeconds = timeOutSeconds;
                this.timeInAltSeconds = timeInAltSeconds;
                this.timeOutAltSeconds = timeOutAltSeconds;

                // Set the UI of the time variables
                fieldTimeIn.setText(utility.secondsToTime(timeInSeconds));
                fieldTimeOut.setText(utility.secondsToTime(timeOutSeconds));
                fieldTimeInAlt.setText(utility.secondsToTime(timeInAltSeconds));
                fieldTimeOutAlt.setText(utility.secondsToTime(timeOutAltSeconds));
            }

            // If the fragment was not restarted from the OnTimeSet override method in NewScheduleActivity
            // Set the global variables for the time based on the current time
            // And update the UI elements accordingly
            else {
                Calendar c = Calendar.getInstance();
                // Set the default value of the global time variables
                // These variables are also used as default values in TimePickerFragment
                timeInHour = c.get(Calendar.HOUR_OF_DAY) + 1;
                timeOutHour = c.get(Calendar.HOUR_OF_DAY) + 2;
                timeInAltHour = c.get(Calendar.HOUR_OF_DAY) + 1;
                timeOutAltHour = c.get(Calendar.HOUR_OF_DAY) + 2;

                // These variables are the exact convertable time data
                // Used in the list view and stored in the database
                timeInSeconds = utility.timeToSeconds(timeInHour, 0);
                timeOutSeconds = utility.timeToSeconds(timeOutHour, 0);
                timeInAltSeconds = utility.timeToSeconds(timeInAltHour, 0);
                timeOutAltSeconds = utility.timeToSeconds(timeOutAltHour, 0);

                // Update the UI elements accordingly
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
                    // In the case that it's one of the day buttons
                    case R.id.class_three_sunday:
                        if (isButtonChecked[0] == 0){
                            isButtonChecked[0] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_selected);
                        }
                        else if (isButtonChecked[0] == 1){
                            isButtonChecked[0] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_unselected);
                        }
                        else if (isButtonChecked[0] == 2){
                            isButtonChecked[0] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_selected);
                        }
                        else if (isButtonChecked[0] == 3){
                            isButtonChecked[0] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_unselected);
                        }
                        break;
                    case R.id.class_three_monday:
                        if (isButtonChecked[1] == 0){
                            isButtonChecked[1] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_monday_selected);
                        }
                        else if (isButtonChecked[1] == 1){
                            isButtonChecked[1] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_monday_unselected);
                        }
                        else if (isButtonChecked[1] == 2){
                            isButtonChecked[1] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_monday_selected);
                        }
                        else if (isButtonChecked[1] == 3){
                            isButtonChecked[1] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_monday_unselected);
                        }
                        break;
                    case R.id.class_three_tuesday:
                        if (isButtonChecked[2] == 0){
                            isButtonChecked[2] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_selected);
                        }
                        else if (isButtonChecked[2] == 1){
                            isButtonChecked[2] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_unselected);
                        }
                        else if (isButtonChecked[2] == 2) {
                            isButtonChecked[2] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_selected);
                        }
                        else if (isButtonChecked[2] == 3){
                            isButtonChecked[2] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_unselected);
                        }
                        break;
                    case R.id.class_three_wednesday:
                        if (isButtonChecked[3] == 0) {
                            isButtonChecked[3] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_wednesday_selected);
                        }
                        else if (isButtonChecked[3] == 1){
                            isButtonChecked[3] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_wednesday_unselected);
                        }
                        else if (isButtonChecked[3] == 2){
                            isButtonChecked[3] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_wednesday_selected);
                        }
                        else if (isButtonChecked[3] == 3){
                            isButtonChecked[3] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_wednesday_unselected);
                        }
                        break;
                    case R.id.class_three_thursday:
                        if (isButtonChecked[4] == 0){
                            isButtonChecked[4] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_selected);
                        }
                        else if (isButtonChecked[4] == 1){
                            isButtonChecked[4] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_unselected);
                        }
                        else if (isButtonChecked[4] == 2){
                            isButtonChecked[4] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_selected);
                        }
                        else if (isButtonChecked[4] == 3){
                            isButtonChecked[4] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_unselected);
                        }
                        break;
                    case R.id.class_three_friday:
                        if (isButtonChecked[5] == 0){
                            isButtonChecked[5] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_friday_selected);
                        }
                        else if (isButtonChecked[5] == 1){
                            isButtonChecked[5] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_friday_unselected);
                        }
                        else if (isButtonChecked[5] == 2){
                            isButtonChecked[5] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_friday_selected);
                        }
                        else if (isButtonChecked[5] == 3){
                            isButtonChecked[5] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_friday_unselected);
                        }
                        break;
                    case R.id.class_three_saturday:
                        if (isButtonChecked[6] == 0){
                            isButtonChecked[6] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_selected);
                        }
                        else if (isButtonChecked[6] == 1){
                            isButtonChecked[6] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_unselected);
                        }
                        else if (isButtonChecked[6] == 2){
                            isButtonChecked[6] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_selected);
                        }
                        else if (isButtonChecked[6] == 3){
                            isButtonChecked[6] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_unselected);
                        }
                        break;

                    // In the case that it's one of the alternate day buttons
                    case R.id.class_three_sunday_alt:
                        if (isButtonChecked[0] == 0){
                            isButtonChecked[0] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_selected);
                        }
                        else if (isButtonChecked[0] == 1){
                            isButtonChecked[0] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_selected);
                        }
                        else if (isButtonChecked[0] == 2){
                            isButtonChecked[0] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_unselected);
                        }
                        else if (isButtonChecked[0] == 3){
                            isButtonChecked[0] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_unselected);
                        }
                        break;
                    case R.id.class_three_monday_alt:
                        if (isButtonChecked[1] == 0){
                            isButtonChecked[1] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_monday_selected);
                        }
                        else if (isButtonChecked[1] == 1){
                            isButtonChecked[1] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_monday_selected);
                        }
                        else if (isButtonChecked[1] == 2){
                            isButtonChecked[1] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_monday_unselected);
                        }
                        else if (isButtonChecked[1] == 3){
                            isButtonChecked[1] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_monday_unselected);
                        }
                        break;
                    case R.id.class_three_tuesday_alt:
                        if (isButtonChecked[2] == 0){
                            isButtonChecked[2] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_selected);
                        }
                        else if (isButtonChecked[2] == 1){
                            isButtonChecked[2] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_selected);
                        }
                        else if (isButtonChecked[2] == 2){
                            isButtonChecked[2] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_unselected);
                        }
                        else if (isButtonChecked[2] == 3){
                            isButtonChecked[2] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_unselected);
                        }
                        break;
                    case R.id.class_three_wednesday_alt:
                        if (isButtonChecked[3] == 0){
                            isButtonChecked[3] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_wednesday_selected);
                        }
                        else if (isButtonChecked[3] == 1){
                            isButtonChecked[3] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_wednesday_selected);
                        }
                        else if (isButtonChecked[3] == 2){
                            isButtonChecked[3] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_wednesday_unselected);
                        }
                        else if (isButtonChecked[3] == 3){
                            isButtonChecked[3] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_wednesday_unselected);
                        }
                        break;
                    case R.id.class_three_thursday_alt:
                        if (isButtonChecked[4] == 0){
                            isButtonChecked[4] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_selected);
                        }
                        else if (isButtonChecked[4] == 1){
                            isButtonChecked[4] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_selected);
                        }
                        else if (isButtonChecked[4] == 2){
                            isButtonChecked[4] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_unselected);
                        }
                        else if (isButtonChecked[4] == 3){
                            isButtonChecked[4] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_tuesday_thursday_unselected);
                        }
                        break;
                    case R.id.class_three_friday_alt:
                        if (isButtonChecked[5] == 0){
                            isButtonChecked[5] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_friday_selected);
                        }
                        else if (isButtonChecked[5] == 1){
                            isButtonChecked[5] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_friday_selected);
                        }
                        else if (isButtonChecked[5] == 2){
                            isButtonChecked[5] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_friday_unselected);
                        }
                        else if (isButtonChecked[5] == 3){
                            isButtonChecked[5] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_friday_unselected);
                        }
                        break;
                    case R.id.class_three_saturday_alt:
                        if (isButtonChecked[6] == 0){
                            isButtonChecked[6] = 2;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_selected);
                        }
                        else if (isButtonChecked[6] == 1){
                            isButtonChecked[6] = 3;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_selected);
                        }
                        else if (isButtonChecked[6] == 2){
                            isButtonChecked[6] = 0;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_unselected);
                        }
                        else if (isButtonChecked[6] == 3){
                            isButtonChecked[6] = 1;
                            ((ImageView) v).setImageResource(R.drawable.ui_saturday_sunday_unselected);
                        }
                        break;

                    // In the case that it's one of the hyperlink text views to the
                    // previous stages of the add class time process
                    case R.id.class_time_one_value:
                        basisTextviewSelectedListener.onBasisTextviewSelected();
                        break;

                    // 0 is the fixed value passed as the basis because the activity itself
                    // (TimeBased) was launched as a result of the basis being 0
                    case R.id.class_time_two_value:
                        weektypeTextviewSelectedListener.onWeektypeTextViewSelectedListener("0");
                        break;

                    // In the case that the 'Done' button was clicked.
                    // This runs the interface that leads to the insertion of data into the database
                    // and into the list view in the NewScheduleActivity
                    case R.id.class_three_done:
                        String classDays = processClassDaysString();

                        // Validate that at least one day has been selected
                        if (classDays.equals("0:0:0:0:0:0:0")) {
                            Toast.makeText(getContext(), getString(R.string.new_schedule_toast_validation_no_days_selected),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // If the fragment was started through the list view's OnItemClick
                        // Pass true as the Edit Flag to tell the activity to update instead
                        // the occurrence item instead of adding a new item
                        boolean FLAG_EDIT = getArguments().containsKey("occurrence");
                        int rowId = getArguments().getInt("rowId");
                        daysSelectedListener.onDaysSelected(classDays, timeInSeconds, timeOutSeconds,
                                timeInAltSeconds, timeOutAltSeconds, "-1", FLAG_EDIT, rowId);
                        break;
                }
            }
        };
    }

    private View.OnClickListener showTimePickerDialog() {
        // OnClickListener set to launch a TimePickerFragment to input the time on a particular field
        // When this happens, data of the current state of the fragment is sent to the NewScheduleActivity
        // As the TimeSetListener can only be implemented in an activity and not a fragment
        // Therefore upon Time Set, the fragment is restarted along with data sent through the interface
        // and data from the TimePickerDialog and the UI elements are updated with the corresponding data

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the id of the view clicked. This id is the view whose text will be updated
                // Upon restart of the fragment
                resourceId = v.getId();

                // Launch a new TimePickerFragment
                DialogFragment timePickerFragment = new TimePickerFragment();
                if (resourceId != -1)
                    timePickerFragment.show(getActivity().getSupportFragmentManager(), "time picker");

                // Launch the interface to send the fragment's current state data to the activity
                timeSelectedListener.onTimeSelected(resourceId, timeInSeconds, timeOutSeconds, timeInAltSeconds, timeOutAltSeconds, isButtonChecked);
            }
        };
    }

    private String processClassDaysString(){
        // Creates the third part of the occurrence string based on the
        // array of buttons (days) checked
        return isButtonChecked[0] + ":"
                + isButtonChecked[1] + ":"
                + isButtonChecked[2] + ":"
                + isButtonChecked[3] + ":"
                + isButtonChecked[4] + ":"
                + isButtonChecked[5] + ":"
                + isButtonChecked[6];
    }

}