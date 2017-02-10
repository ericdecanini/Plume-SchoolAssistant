package com.pdt.plume;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.Calendar;

import static com.pdt.plume.R.id.header;


public class AddClassTimeThreeFragmentTime extends DialogFragment{

    // Constantly used variables
    String LOG_TAG = AddClassTimeThreeFragmentTime.class.getSimpleName();
    Utility utility = new Utility();

    // UI Elements
    EditText fieldTimeIn;
    EditText fieldTimeOut;
    EditText fieldTimeInAlt;
    EditText fieldTimeOutAlt;

    int mPrimaryColor;
    int mSecondaryColor;

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
    int rowID = -1;
    boolean FLAG_EDIT = false;

    // Interface variables
    onDaysSelectedListener daysSelectedListener;
    onTimeSelectedListener timeSelectedListener;
    onBasisTextviewSelectedListener basisTextviewSelectedListener;
    onWeektypeTextviewSelectedListener weektypeTextviewSelectedListener;

    // Public Constructor
    public static AddClassTimeThreeFragmentTime newInstance(int title) {
        AddClassTimeThreeFragmentTime fragment = new AddClassTimeThreeFragmentTime();
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
        public void onTimeSelected(int resourceId, String classDays, int previousTimeInSeconds, int previousTimeOutSeconds,
                                   int previousTimeInAltSeconds, int previousTimeOutAltSeconds, int[] buttonsChecked,
                                   boolean FLAG_EDIT, int rowID);
    }
    public interface onBasisTextviewSelectedListener {
        //Pass all data through input params here
        public void onBasisTextviewSelected(boolean FLAG_EDIT, int rowID);
    }
    public interface onWeektypeTextviewSelectedListener {
        //Pass all data through input params here
        public void onWeektypeTextViewSelectedListener(String basis, boolean FLAG_EDIT, int rowID);
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
        View rootView = inflater.inflate(R.layout.add_class_time_three_time, container, false);

        // Get references to each UI element
        TextView basisTextView = (TextView) rootView.findViewById(R.id.class_time_one_value);
        TextView weekTypeTextView = (TextView) rootView.findViewById(R.id.class_time_two_value);

        TextView header = (TextView) rootView.findViewById(R.id.class_three_item_header);
        TextView sunday = (TextView) rootView.findViewById(R.id.class_three_sunday);
        TextView monday = (TextView) rootView.findViewById(R.id.class_three_monday);
        TextView tuesday = (TextView) rootView.findViewById(R.id.class_three_tuesday);
        TextView wednesday = (TextView) rootView.findViewById(R.id.class_three_wednesday);
        TextView thursday = (TextView) rootView.findViewById(R.id.class_three_thursday);
        TextView friday = (TextView) rootView.findViewById(R.id.class_three_friday);
        TextView saturday = (TextView) rootView.findViewById(R.id.class_three_saturday);
        fieldTimeIn = (EditText) rootView.findViewById(R.id.field_new_schedule_timein);
        fieldTimeOut = (EditText) rootView.findViewById(R.id.field_new_schedule_timeout);
        LinearLayout done = (LinearLayout) rootView.findViewById(R.id.class_three_done);
        LinearLayout cancel = (LinearLayout) rootView.findViewById(R.id.class_three_cancel);

        TextView sundayAlt = (TextView) rootView.findViewById(R.id.class_three_sunday_alt);
        TextView mondayAlt = (TextView) rootView.findViewById(R.id.class_three_monday_alt);
        TextView tuesdayAlt = (TextView) rootView.findViewById(R.id.class_three_tuesday_alt);
        TextView wednesdayAlt = (TextView) rootView.findViewById(R.id.class_three_wednesday_alt);
        TextView thursdayAlt = (TextView) rootView.findViewById(R.id.class_three_thursday_alt);
        TextView fridayAlt = (TextView) rootView.findViewById(R.id.class_three_friday_alt);
        TextView saturdayAlt = (TextView) rootView.findViewById(R.id.class_three_saturday_alt);
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
        cancel.setOnClickListener(listener());

        sundayAlt.setOnClickListener(listener());
        mondayAlt.setOnClickListener(listener());
        tuesdayAlt.setOnClickListener(listener());
        wednesdayAlt.setOnClickListener(listener());
        thursdayAlt.setOnClickListener(listener());
        fridayAlt.setOnClickListener(listener());
        saturdayAlt.setOnClickListener(listener());
        fieldTimeInAlt.setOnClickListener(showTimePickerDialog());
        fieldTimeOutAlt.setOnClickListener(showTimePickerDialog());

        // Initialise the theme
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR),
                getResources().getColor(R.color.colorPrimary));
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR),
                getResources().getColor(R.color.colorAccent));

        // Set the text of the hyperlink basis text to the time based string annotation
        basisTextView.setText(getString(R.string.class_time_one_timebased));

        // Get the arguments of the fragment.
        // Check week taskType and if it is 'Same each week', hide the alternate layout.
        // Set the hyperlink week taskType text accordingly
        // Check if fragment was restarted via Time Set, restore previous state
        Bundle args = getArguments();
        if (args != null){
            if (args.containsKey("FLAG_EDIT")) {
                FLAG_EDIT = true;
                rowID = args.getInt("rowID");
            }
            if (args.containsKey("occurrence")) {
                FLAG_EDIT = true;
                rowID = args.getInt("rowId");

            }
            // Hide the alternate layout if the week taskType selected is 0 (Same time every week)
            // and set the hyperlink week taskType text to the selected week taskType text
            if (!args.getString("weekType", "-1").equals("1")){
                // If weekType is 0 (Same each week), hide the alternate layout
                rootView.findViewById(R.id.class_time_three_week_type_alt_layout).setVisibility(View.GONE);
                header.setText(getString(R.string.class_time_three_select_days));
                weekTypeTextView.setText(getString(R.string.class_time_two_sameweek));
            }
            else {
                weekTypeTextView.setText(getString(R.string.class_time_two_altweeks));
                header.setText(getString(R.string.class_three_weekone));
            }

            // Check if the fragment was launched from the OnTimeSet override method in NewScheduleActivity
            // If it is, get the fragment's previous state data and update the fragment data and UI accordingly
            // If the fragment contains the 'hourOfDay' string, it must contain other previous state data
            if (args.containsKey("hourOfDay")){
                // Get previous state data
                FLAG_EDIT = args.getBoolean("FLAG_EDIT", false);
                String weekType = args.getString("weekType", "-1");
                String classDays = args.getString("classDays", "-1");
                String[] splitClassDays = classDays.split(":");
                int hourOfDay = args.getInt("hourOfDay");
                int minute = args.getInt("minute");
                int previousTimeInSeconds = args.getInt("timeInSeconds");
                int previousTimeOutSeconds = args.getInt("timeOutSeconds");
                int previousTimeInAltSeconds = args.getInt("timeInAltSeconds");
                int previousTimeOutAltSeconds = args.getInt("timeOutAltSeconds");
                isButtonChecked = args.getIntArray("buttonsChecked");

                // Auto-fill the days row and binary data
                if (splitClassDays[0].equals("1") || splitClassDays[0].equals("3")){
                    isButtonChecked[0] = 1;
                    sunday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    sunday.getBackground().setAlpha(180);

                }
                if (splitClassDays[1].equals("1") || splitClassDays[1].equals("3")){
                    isButtonChecked[1] = 1;
                    monday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    monday.getBackground().setAlpha(180);
                }
                if (splitClassDays[2].equals("1") || splitClassDays[2].equals("3")){
                    isButtonChecked[2] = 1;
                    tuesday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    tuesday.getBackground().setAlpha(180);
                }
                if (splitClassDays[3].equals("1") || splitClassDays[3].equals("3")){
                    isButtonChecked[3] = 1;
                    wednesday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    wednesday.getBackground().setAlpha(180);
                }
                if (splitClassDays[4].equals("1") || splitClassDays[4].equals("3")){
                    isButtonChecked[4] = 1;
                    thursday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    thursday.getBackground().setAlpha(180);
                }
                if (splitClassDays[5].equals("1") || splitClassDays[5].equals("3")){
                    isButtonChecked[5] = 1;
                    friday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    friday.getBackground().setAlpha(180);
                }
                if (splitClassDays[6].equals("1") || splitClassDays[6].equals("3")){
                    isButtonChecked[6] = 1;
                    saturday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    saturday.getBackground().setAlpha(180);
                }

                // Do so for alternate layout if it is available
                if (weekType.equals("1")){
                    if (splitClassDays[0].equals("2") || splitClassDays[0].equals("3")){
                        if (isButtonChecked[0] == 1)
                            isButtonChecked[0] = 3;
                        else isButtonChecked[0] = 2;
                        sundayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        sundayAlt.getBackground().setAlpha(180);
                    }
                    if (splitClassDays[1].equals("2") || splitClassDays[1].equals("3")){
                        if (isButtonChecked[1] == 1)
                            isButtonChecked[1] = 3;
                        else isButtonChecked[1] = 2;
                        mondayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        mondayAlt.getBackground().setAlpha(180);
                    }
                    if (splitClassDays[2].equals("2") || splitClassDays[2].equals("3")){
                        if (isButtonChecked[2] == 1)
                            isButtonChecked[2] = 3;
                        else isButtonChecked[2] = 2;
                        tuesdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        tuesdayAlt.getBackground().setAlpha(180);
                    }
                    if (splitClassDays[3].equals("2") || splitClassDays[3].equals("3")){
                        if (isButtonChecked[3] == 1)
                            isButtonChecked[3] = 3;
                        else isButtonChecked[3] = 2;
                        wednesdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        wednesdayAlt.getBackground().setAlpha(180);
                    }
                    if (splitClassDays[4].equals("2") || splitClassDays[4].equals("3")){
                        if (isButtonChecked[4] == 1)
                            isButtonChecked[4] = 3;
                        else isButtonChecked[4] = 2;
                        thursdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        thursdayAlt.getBackground().setAlpha(180);
                    }
                    if (splitClassDays[5].equals("2") || splitClassDays[5].equals("3")){
                        if (isButtonChecked[5] == 1)
                            isButtonChecked[5] = 3;
                        else isButtonChecked[5] = 2;
                        fridayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        fridayAlt.getBackground().setAlpha(180);
                    }
                    if (splitClassDays[6].equals("2") || splitClassDays[6].equals("3")){
                        if (isButtonChecked[6] == 1)
                            isButtonChecked[6] = 3;
                        else isButtonChecked[6] = 2;
                        saturdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        saturdayAlt.getBackground().setAlpha(180);
                    }
                }

                // Set the default values of the time fields accordingly
                // as well as update the fragment's global variables of time
                // Global variables updated: timeInSeconds, timeOutSeconds, timeInAltSeconds, timeOutAltSeconds
                switch (args.getInt("resourceId")){
                    case R.id.field_new_schedule_timein:
                        timeInSeconds = utility.timeToMillis(hourOfDay, minute);
                        timeOutSeconds = previousTimeOutSeconds;
                        timeInAltSeconds = previousTimeInAltSeconds;
                        timeOutAltSeconds = previousTimeOutAltSeconds;
                        if (minute < 10)
                            fieldTimeIn.setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeIn.setText(hourOfDay + ":" + minute);
                        fieldTimeOut.setText(utility.millisToHourTime(previousTimeOutSeconds));
                        fieldTimeInAlt.setText(utility.millisToHourTime(previousTimeInAltSeconds));
                        fieldTimeOutAlt.setText(utility.millisToHourTime(previousTimeOutAltSeconds));
                        break;

                    case R.id.field_new_schedule_timeout:
                        timeInSeconds = previousTimeInSeconds;
                        timeOutSeconds = utility.timeToMillis(hourOfDay, minute);
                        timeInAltSeconds = previousTimeInAltSeconds;
                        timeOutAltSeconds = previousTimeOutAltSeconds;
                        if (minute < 10)
                            fieldTimeOut.setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeOut.setText(hourOfDay + ":" + minute);
                        fieldTimeIn.setText(utility.millisToHourTime(previousTimeInSeconds));
                        fieldTimeInAlt.setText(utility.millisToHourTime(previousTimeInAltSeconds));
                        fieldTimeOutAlt.setText(utility.millisToHourTime(previousTimeOutAltSeconds));
                        break;

                    case R.id.field_new_schedule_timein_alt:
                        timeInSeconds = previousTimeInSeconds;
                        timeOutSeconds = previousTimeOutSeconds;
                        timeInAltSeconds = utility.timeToMillis(hourOfDay, minute);
                        timeOutAltSeconds = previousTimeOutAltSeconds;
                        if (minute < 10)
                            fieldTimeInAlt.setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeInAlt.setText(hourOfDay + ":" + minute);
                        fieldTimeIn.setText(utility.millisToHourTime(previousTimeInSeconds));
                        fieldTimeOut.setText(utility.millisToHourTime(previousTimeOutSeconds));
                        fieldTimeOutAlt.setText(utility.millisToHourTime(previousTimeOutAltSeconds));
                        break;

                    case R.id.field_new_schedule_timeout_alt:
                        timeInSeconds = previousTimeInSeconds;
                        timeOutSeconds = previousTimeOutSeconds;
                        timeInAltSeconds = previousTimeInAltSeconds;
                        timeOutAltSeconds = utility.timeToMillis(hourOfDay, minute);
                        if (minute < 10)
                            fieldTimeOutAlt.setText(hourOfDay + ":0" + minute);
                        else
                            fieldTimeOutAlt.setText(hourOfDay + ":" + minute);
                        fieldTimeIn.setText(utility.millisToHourTime(previousTimeInSeconds));
                        fieldTimeOut.setText(utility.millisToHourTime(previousTimeOutSeconds));
                        fieldTimeInAlt.setText(utility.millisToHourTime(previousTimeInAltSeconds));
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
                    sunday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    sunday.getBackground().setAlpha(180);
                }
                if (splitOccurrence[3].equals("1") || splitOccurrence[3].equals("3")){
                    isButtonChecked[1] = 1;
                    monday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    monday.getBackground().setAlpha(180);
                }
                if (splitOccurrence[4].equals("1") || splitOccurrence[4].equals("3")){
                    isButtonChecked[2] = 1;
                    tuesday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    tuesday.getBackground().setAlpha(180);
                }
                if (splitOccurrence[5].equals("1") || splitOccurrence[5].equals("3")){
                    isButtonChecked[3] = 1;
                    wednesday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    wednesday.getBackground().setAlpha(180);
                }
                if (splitOccurrence[6].equals("1") || splitOccurrence[6].equals("3")){
                    isButtonChecked[4] = 1;
                    thursday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    thursday.getBackground().setAlpha(180);
                }
                if (splitOccurrence[7].equals("1") || splitOccurrence[7].equals("3")){
                    isButtonChecked[5] = 1;
                    friday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    friday.getBackground().setAlpha(180);
                }
                if (splitOccurrence[8].equals("1") || splitOccurrence[8].equals("3")){
                    isButtonChecked[6] = 1;
                    saturday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                    saturday.getBackground().setAlpha(180);
                }

                // Do so for alternate layout if it is available
                if (splitOccurrence[1].equals("1")){
                    if (splitOccurrence[2].equals("2") || splitOccurrence[2].equals("3")){
                        if (isButtonChecked[0] == 1)
                            isButtonChecked[0] = 3;
                        else isButtonChecked[0] = 2;
                        sundayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        sundayAlt.getBackground().setAlpha(180);
                    }
                    if (splitOccurrence[3].equals("2") || splitOccurrence[3].equals("3")){
                        if (isButtonChecked[1] == 1)
                            isButtonChecked[1] = 3;
                        else isButtonChecked[1] = 2;
                        mondayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        mondayAlt.getBackground().setAlpha(180);
                    }
                    if (splitOccurrence[4].equals("2") || splitOccurrence[4].equals("3")){
                        if (isButtonChecked[2] == 1)
                            isButtonChecked[2] = 3;
                        else isButtonChecked[2] = 2;
                        tuesdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        tuesdayAlt.getBackground().setAlpha(180);
                    }
                    if (splitOccurrence[5].equals("2") || splitOccurrence[5].equals("3")){
                        if (isButtonChecked[3] == 1)
                            isButtonChecked[3] = 3;
                        else isButtonChecked[3] = 2;
                        wednesdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        wednesdayAlt.getBackground().setAlpha(180);
                    }
                    if (splitOccurrence[6].equals("2") || splitOccurrence[6].equals("3")){
                        if (isButtonChecked[4] == 1)
                            isButtonChecked[4] = 3;
                        else isButtonChecked[4] = 2;
                        thursdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        thursdayAlt.getBackground().setAlpha(180);
                    }
                    if (splitOccurrence[7].equals("2") || splitOccurrence[7].equals("3")){
                        if (isButtonChecked[5] == 1)
                            isButtonChecked[5] = 3;
                        else isButtonChecked[5] = 2;
                        fridayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        fridayAlt.getBackground().setAlpha(180);
                    }
                    if (splitOccurrence[8].equals("2") || splitOccurrence[8].equals("3")){
                        if (isButtonChecked[6] == 1)
                            isButtonChecked[6] = 3;
                        else isButtonChecked[6] = 2;
                        saturdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        saturdayAlt.getBackground().setAlpha(180);
                    }
                }

                // Set the global time variables
                this.timeInSeconds = timeInSeconds;
                this.timeOutSeconds = timeOutSeconds;
                this.timeInAltSeconds = timeInAltSeconds;
                this.timeOutAltSeconds = timeOutAltSeconds;

                // Set the UI of the time variables
                fieldTimeIn.setText(utility.millisToHourTime(timeInSeconds));
                fieldTimeOut.setText(utility.millisToHourTime(timeOutSeconds));
                fieldTimeInAlt.setText(utility.millisToHourTime(timeInAltSeconds));
                fieldTimeOutAlt.setText(utility.millisToHourTime(timeOutAltSeconds));
            }

            // If the fragment was not restarted from the OnTimeSet override method in NewScheduleActivity
            // Set the global variables for the time based on the current time
            // And update the UI elements accordingly
            else {
                Calendar c = Calendar.getInstance();
                // Set the default value of the global time variables
                // These variables are also used as default values in TimePickerFragmentSchedule
                timeInHour = c.get(Calendar.HOUR_OF_DAY) + 1;
                timeOutHour = c.get(Calendar.HOUR_OF_DAY) + 2;
                timeInAltHour = c.get(Calendar.HOUR_OF_DAY) + 1;
                timeOutAltHour = c.get(Calendar.HOUR_OF_DAY) + 2;

                // These variables are the exact convertable time data
                // Used in the list view and stored in the database
                timeInSeconds = utility.timeToMillis(timeInHour, 0);
                timeOutSeconds = utility.timeToMillis(timeOutHour, 0);
                timeInAltSeconds = utility.timeToMillis(timeInAltHour, 0);
                timeOutAltSeconds = utility.timeToMillis(timeOutAltHour, 0);

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
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[0] == 1){
                            isButtonChecked[0] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[0] == 2){
                            isButtonChecked[0] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[0] == 3){
                            isButtonChecked[0] = 2;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_monday:
                        if (isButtonChecked[1] == 0){
                            isButtonChecked[1] = 1;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[1] == 1){
                            isButtonChecked[1] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[1] == 2){
                            isButtonChecked[1] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[1] == 3){
                            isButtonChecked[1] = 2;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_tuesday:
                        if (isButtonChecked[2] == 0){
                            isButtonChecked[2] = 1;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[2] == 1){
                            isButtonChecked[2] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[2] == 2) {
                            isButtonChecked[2] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[2] == 3){
                            isButtonChecked[2] = 2;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_wednesday:
                        if (isButtonChecked[3] == 0) {
                            isButtonChecked[3] = 1;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[3] == 1){
                            isButtonChecked[3] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[3] == 2){
                            isButtonChecked[3] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[3] == 3){
                            isButtonChecked[3] = 2;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_thursday:
                        if (isButtonChecked[4] == 0){
                            isButtonChecked[4] = 1;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[4] == 1){
                            isButtonChecked[4] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[4] == 2){
                            isButtonChecked[4] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[4] == 3){
                            isButtonChecked[4] = 2;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_friday:
                        if (isButtonChecked[5] == 0){
                            isButtonChecked[5] = 1;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[5] == 1){
                            isButtonChecked[5] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[5] == 2){
                            isButtonChecked[5] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[5] == 3){
                            isButtonChecked[5] = 2;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_saturday:
                        if (isButtonChecked[6] == 0){
                            isButtonChecked[6] = 1;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[6] == 1){
                            isButtonChecked[6] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[6] == 2){
                            isButtonChecked[6] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[6] == 3){
                            isButtonChecked[6] = 2;
                            v.setBackgroundDrawable(null);
                        }
                        break;

                    // In the case that it's one of the alternate day buttons
                    case R.id.class_three_sunday_alt:
                        if (isButtonChecked[0] == 0){
                            isButtonChecked[0] = 2;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[0] == 1){
                            isButtonChecked[0] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[0] == 2){
                            isButtonChecked[0] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[0] == 3){
                            isButtonChecked[0] = 1;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_monday_alt:
                        if (isButtonChecked[1] == 0){
                            isButtonChecked[1] = 2;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[1] == 1){
                            isButtonChecked[1] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[1] == 2){
                            isButtonChecked[1] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[1] == 3){
                            isButtonChecked[1] = 1;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_tuesday_alt:
                        if (isButtonChecked[2] == 0){
                            isButtonChecked[2] = 2;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[2] == 1){
                            isButtonChecked[2] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[2] == 2){
                            isButtonChecked[2] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[2] == 3){
                            isButtonChecked[2] = 1;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_wednesday_alt:
                        if (isButtonChecked[3] == 0){
                            isButtonChecked[3] = 2;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[3] == 1){
                            isButtonChecked[3] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[3] == 2){
                            isButtonChecked[3] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[3] == 3){
                            isButtonChecked[3] = 1;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_thursday_alt:
                        if (isButtonChecked[4] == 0){
                            isButtonChecked[4] = 2;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[4] == 1){
                            isButtonChecked[4] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[4] == 2){
                            isButtonChecked[4] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[4] == 3){
                            isButtonChecked[4] = 1;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_friday_alt:
                        if (isButtonChecked[5] == 0){
                            isButtonChecked[5] = 2;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[5] == 1){
                            isButtonChecked[5] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[5] == 2) {
                            isButtonChecked[5] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[5] == 3){
                            isButtonChecked[5] = 1;
                            v.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_saturday_alt:
                        if (isButtonChecked[6] == 0){
                            isButtonChecked[6] = 2;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[6] == 1){
                            isButtonChecked[6] = 3;
                            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            v.getBackground().setAlpha(180);
                        }
                        else if (isButtonChecked[6] == 2){
                            isButtonChecked[6] = 0;
                            v.setBackgroundDrawable(null);
                        }
                        else if (isButtonChecked[6] == 3){
                            isButtonChecked[6] = 1;
                            v.setBackgroundDrawable(null);
                        }
                        break;

                    // In the case that it's one of the hyperlink text views to the
                    // previous stages of the add class time process
                    case R.id.class_time_one_value:
                        basisTextviewSelectedListener.onBasisTextviewSelected(FLAG_EDIT, rowID);
                        break;

                    // 0 is the fixed value passed as the basis because the activity itself
                    // (TimeBased) was launched as a result of the basis being 0
                    case R.id.class_time_two_value:
                        weektypeTextviewSelectedListener.onWeektypeTextViewSelectedListener("0", FLAG_EDIT, rowID);
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
//                        if (getArguments().containsKey("occurrence")) {
//                            FLAG_EDIT = getArguments().getBoolean("FLAG_EDIT", false);
//                            rowID = getArguments().getInt("rowId");
//                        }
                        // Log all data here
                        Log.v(LOG_TAG, "Period log: " + classDays + "\n" + timeInSeconds + "\n" + timeOutSeconds
                        + "\n" + timeInAltSeconds + "\n" + timeOutAltSeconds + "\n " + FLAG_EDIT + "\n" + rowID);

                        daysSelectedListener.onDaysSelected(classDays, timeInSeconds, timeOutSeconds,
                                timeInAltSeconds, timeOutAltSeconds, "-1", FLAG_EDIT, rowID);
                        break;

                    case R.id.class_three_cancel:
                        dismiss();
                        break;
                }


            }
        };
    }

    private View.OnClickListener showTimePickerDialog() {
        // OnClickListener set to launch a TimePickerFragmentSchedule to input the time on a particular field
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

                // Launch a new TimePickerFragmentSchedule
                DialogFragment timePickerFragment = new TimePickerFragmentSchedule();
                if (resourceId != -1)
                    timePickerFragment.show(getActivity().getSupportFragmentManager(), "time picker");

                // Launch the interface to send the fragment's current state data to the activity
                timeSelectedListener.onTimeSelected(resourceId, processClassDaysString(), timeInSeconds, timeOutSeconds,
                        timeInAltSeconds, timeOutAltSeconds, isButtonChecked,
                        FLAG_EDIT, rowID);
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