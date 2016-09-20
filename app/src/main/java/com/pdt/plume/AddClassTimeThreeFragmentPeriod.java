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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class AddClassTimeThreeFragmentPeriod extends DialogFragment {
    // Constantly Used Variables
    String LOG_TAG = AddClassTimeThreeFragmentPeriod.class.getSimpleName();

    // UI Elements
    TextView periodOne;
    TextView periodTwo;
    TextView periodThree;
    TextView periodFour;
    TextView periodFive;
    TextView periodSix;
    TextView periodSeven;
    TextView periodEight;
    TextView periodNine;
    TextView periodTen;
    TextView periodEleven;
    TextView periodTwelve;

    TextView periodOneAlt;
    TextView periodTwoAlt;
    TextView periodThreeAlt;
    TextView periodFourAlt;
    TextView periodFiveAlt;
    TextView periodSixAlt;
    TextView periodSevenAlt;
    TextView periodEightAlt;
    TextView periodNineAlt;
    TextView periodTenAlt;
    TextView periodElevenAlt;
    TextView periodTwelveAlt;

    // Fragment input storage variables
    int[] isButtonChecked = {0, 0, 0, 0, 0, 0, 0};
    String[] isPeriodChecked = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};

    // Interface variables
    onDaysSelectedListener daysSelectedListener;
    onBasisTextviewSelectedListener basisTextviewSelectedListener;
    onWeektypeTextviewSelectedListener weektypeTextviewSelectedListener;

    // Public Constructor
    public static AddClassTimeThreeFragmentPeriod newInstance(int title) {
        AddClassTimeThreeFragmentPeriod fragment = new AddClassTimeThreeFragmentPeriod();
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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.add_class_time_three_period, container, false);

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
        Button done = (Button) rootView.findViewById(R.id.class_three_done);

        ImageView sundayAlt = (ImageView) rootView.findViewById(R.id.class_three_sunday_alt);
        ImageView mondayAlt = (ImageView) rootView.findViewById(R.id.class_three_monday_alt);
        ImageView tuesdayAlt = (ImageView) rootView.findViewById(R.id.class_three_tuesday_alt);
        ImageView wednesdayAlt = (ImageView) rootView.findViewById(R.id.class_three_wednesday_alt);
        ImageView thursdayAlt = (ImageView) rootView.findViewById(R.id.class_three_thursday_alt);
        ImageView fridayAlt = (ImageView) rootView.findViewById(R.id.class_three_friday_alt);
        ImageView saturdayAlt = (ImageView) rootView.findViewById(R.id.class_three_saturday_alt);

        periodOne = (TextView) rootView.findViewById(R.id.class_three_period_one);
        periodTwo = (TextView) rootView.findViewById(R.id.class_three_period_two);
        periodThree = (TextView) rootView.findViewById(R.id.class_three_period_three);
        periodFour = (TextView) rootView.findViewById(R.id.class_three_period_four);
        periodFive = (TextView) rootView.findViewById(R.id.class_three_period_five);
        periodSix = (TextView) rootView.findViewById(R.id.class_three_period_six);
        periodSeven = (TextView) rootView.findViewById(R.id.class_three_period_seven);
        periodEight = (TextView) rootView.findViewById(R.id.class_three_period_eight);
        periodNine = (TextView) rootView.findViewById(R.id.class_three_period_nine);
        periodTen = (TextView) rootView.findViewById(R.id.class_three_period_ten);
        periodEleven = (TextView) rootView.findViewById(R.id.class_three_period_eleven);
        periodTwelve = (TextView) rootView.findViewById(R.id.class_three_period_twelve);

        periodOneAlt = (TextView) rootView.findViewById(R.id.class_three_period_one_alt);
        periodTwoAlt = (TextView) rootView.findViewById(R.id.class_three_period_two_alt);
        periodThreeAlt = (TextView) rootView.findViewById(R.id.class_three_period_three_alt);
        periodFourAlt = (TextView) rootView.findViewById(R.id.class_three_period_four_alt);
        periodFiveAlt = (TextView) rootView.findViewById(R.id.class_three_period_five_alt);
        periodSixAlt = (TextView) rootView.findViewById(R.id.class_three_period_six_alt);
        periodSevenAlt = (TextView) rootView.findViewById(R.id.class_three_period_seven_alt);
        periodEightAlt = (TextView) rootView.findViewById(R.id.class_three_period_eight_alt);
        periodNineAlt = (TextView) rootView.findViewById(R.id.class_three_period_nine_alt);
        periodTenAlt = (TextView) rootView.findViewById(R.id.class_three_period_ten_alt);
        periodElevenAlt = (TextView) rootView.findViewById(R.id.class_three_period_eleven_alt);
        periodTwelveAlt = (TextView) rootView.findViewById(R.id.class_three_period_twelve_alt);

        // Set the OnClickListeners of each UI element
        basisTextView.setOnClickListener(listener());
        weekTypeTextView.setOnClickListener(listener());

        sunday.setOnClickListener(listener());
        monday.setOnClickListener(listener());
        tuesday.setOnClickListener(listener());
        wednesday.setOnClickListener(listener());
        thursday.setOnClickListener(listener());
        friday.setOnClickListener(listener());
        saturday.setOnClickListener(listener());
        done.setOnClickListener(listener());

        sundayAlt.setOnClickListener(listener());
        mondayAlt.setOnClickListener(listener());
        tuesdayAlt.setOnClickListener(listener());
        wednesdayAlt.setOnClickListener(listener());
        thursdayAlt.setOnClickListener(listener());
        fridayAlt.setOnClickListener(listener());
        saturdayAlt.setOnClickListener(listener());

        periodOne.setOnClickListener(listener());
        periodTwo.setOnClickListener(listener());
        periodThree.setOnClickListener(listener());
        periodFour.setOnClickListener(listener());
        periodFive.setOnClickListener(listener());
        periodSix.setOnClickListener(listener());
        periodSeven.setOnClickListener(listener());
        periodEight.setOnClickListener(listener());
        periodNine.setOnClickListener(listener());
        periodTen.setOnClickListener(listener());
        periodEleven.setOnClickListener(listener());
        periodTwelve.setOnClickListener(listener());

        periodOneAlt.setOnClickListener(listener());
        periodTwoAlt.setOnClickListener(listener());
        periodThreeAlt.setOnClickListener(listener());
        periodFourAlt.setOnClickListener(listener());
        periodFiveAlt.setOnClickListener(listener());
        periodSixAlt.setOnClickListener(listener());
        periodSevenAlt.setOnClickListener(listener());
        periodEightAlt.setOnClickListener(listener());
        periodNineAlt.setOnClickListener(listener());
        periodTenAlt.setOnClickListener(listener());
        periodElevenAlt.setOnClickListener(listener());
        periodTwelveAlt.setOnClickListener(listener());

        // Get the arguments of the fragment and
        // Set the hyperlink basis week classType text accordingly
        basisTextView.setText(getString(R.string.class_time_one_periodbased));
        Bundle args = getArguments();
        if (args != null){
            if (!args.getString("weekType", "-1").equals("1")){
                //Change the layout based on weekType
                rootView.findViewById(R.id.class_time_three_week_type_alt_layout).setVisibility(View.GONE);
                weekTypeTextView.setText(getString(R.string.class_time_two_sameweek));
            }
            else weekTypeTextView.setText(getString(R.string.class_time_two_altweeks));

            // Check if the fragment was started through the list view's OnItemClick
            // If it is, receive the corresponding data and auto-fill that item's UI
            if (args.containsKey("occurrence")) {
                // Get the data from the arguments bundle
                String occurrence = args.getString("occurrence", "-1");
                String[] splitOccurrence = occurrence.split(":");
                String period = args.getString("period", "-1");
                String[] splitPeriod = period.split(":");

                // Check each item in the occurrence string's day binary
                // and set it in the activity
                if (splitOccurrence[2].equals("1") || splitOccurrence[2].equals("3")) {
                    isButtonChecked[0] = 1;
                    sunday.setImageResource(R.drawable.ui_saturday_sunday_selected);
                }
                if (splitOccurrence[3].equals("1") || splitOccurrence[3].equals("3")) {
                    isButtonChecked[1] = 1;
                    monday.setImageResource(R.drawable.ui_monday_selected);
                }
                if (splitOccurrence[4].equals("1") || splitOccurrence[4].equals("3")) {
                    isButtonChecked[2] = 1;
                    tuesday.setImageResource(R.drawable.ui_tuesday_thursday_selected);
                }
                if (splitOccurrence[5].equals("1") || splitOccurrence[5].equals("3")) {
                    isButtonChecked[3] = 1;
                    wednesday.setImageResource(R.drawable.ui_wednesday_selected);
                }
                if (splitOccurrence[6].equals("1") || splitOccurrence[6].equals("3")) {
                    isButtonChecked[4] = 1;
                    thursday.setImageResource(R.drawable.ui_tuesday_thursday_selected);
                }
                if (splitOccurrence[7].equals("1") || splitOccurrence[7].equals("3")) {
                    isButtonChecked[5] = 1;
                    friday.setImageResource(R.drawable.ui_friday_selected);
                }
                if (splitOccurrence[8].equals("1") || splitOccurrence[8].equals("3")) {
                    isButtonChecked[6] = 1;
                    saturday.setImageResource(R.drawable.ui_saturday_sunday_selected);
                }

                // Do so for alternate layout if it is available
                if (splitOccurrence[1].equals("1")) {
                    if (splitOccurrence[2].equals("2") || splitOccurrence[2].equals("3")) {
                        if (isButtonChecked[0] == 1)
                            isButtonChecked[0] = 3;
                        else isButtonChecked[0] = 2;
                        sundayAlt.setImageResource(R.drawable.ui_saturday_sunday_selected);
                    }
                    if (splitOccurrence[3].equals("2") || splitOccurrence[3].equals("3")) {
                        if (isButtonChecked[1] == 1)
                            isButtonChecked[1] = 3;
                        else isButtonChecked[1] = 2;
                        mondayAlt.setImageResource(R.drawable.ui_monday_selected);
                    }
                    if (splitOccurrence[4].equals("2") || splitOccurrence[4].equals("3")) {
                        if (isButtonChecked[2] == 1)
                            isButtonChecked[2] = 3;
                        else isButtonChecked[2] = 2;
                        tuesdayAlt.setImageResource(R.drawable.ui_tuesday_thursday_selected);
                    }
                    if (splitOccurrence[5].equals("2") || splitOccurrence[5].equals("3")) {
                        if (isButtonChecked[3] == 1)
                            isButtonChecked[3] = 3;
                        else isButtonChecked[3] = 2;
                        wednesdayAlt.setImageResource(R.drawable.ui_wednesday_selected);
                    }
                    if (splitOccurrence[6].equals("2") || splitOccurrence[6].equals("3")) {
                        if (isButtonChecked[4] == 1)
                            isButtonChecked[4] = 3;
                        else isButtonChecked[4] = 2;
                        thursdayAlt.setImageResource(R.drawable.ui_tuesday_thursday_selected);
                    }
                    if (splitOccurrence[7].equals("2") || splitOccurrence[7].equals("3")) {
                        if (isButtonChecked[5] == 1)
                            isButtonChecked[5] = 3;
                        else isButtonChecked[5] = 2;
                        fridayAlt.setImageResource(R.drawable.ui_friday_selected);
                    }
                    if (splitOccurrence[8].equals("2") || splitOccurrence[8].equals("3")) {
                        if (isButtonChecked[6] == 1)
                            isButtonChecked[6] = 3;
                        else isButtonChecked[6] = 2;
                        saturdayAlt.setImageResource(R.drawable.ui_saturday_sunday_selected);
                    }
                }

                // Do so as well for the period list
                if (splitPeriod[0].equals("1") || splitPeriod[0].equals("3")) {
                    isPeriodChecked[0] = "1";
                    periodOne.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodOne.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[1].equals("1") || splitPeriod[1].equals("3")) {
                    isPeriodChecked[1] = "1";
                    periodTwo.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodTwo.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[2].equals("1") || splitPeriod[2].equals("3")) {
                    isPeriodChecked[2] = "1";
                    periodThree.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodThree.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[3].equals("1") || splitPeriod[3].equals("3")) {
                    isPeriodChecked[3] = "1";
                    periodFour.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodFour.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[4].equals("1") || splitPeriod[4].equals("3")) {
                    isPeriodChecked[4] = "1";
                    periodFive.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodFive.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[5].equals("1") || splitPeriod[5].equals("3")) {
                    isPeriodChecked[5] = "1";
                    periodSix.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodSix.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[6].equals("1") || splitPeriod[6].equals("3")) {
                    isPeriodChecked[6] = "1";
                    periodSeven.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodSeven.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[7].equals("1") || splitPeriod[7].equals("3")) {
                    isPeriodChecked[7] = "1";
                    periodEight.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodEight.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[8].equals("1") || splitPeriod[8].equals("3")) {
                    isPeriodChecked[8] = "1";
                    periodNine.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodNine.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[9].equals("1") || splitPeriod[9].equals("3")) {
                    isPeriodChecked[9] = "1";
                    periodTen.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodTen.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[10].equals("1") || splitPeriod[10].equals("3")) {
                    isPeriodChecked[10] = "1";
                    periodEleven.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodEleven.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if (splitPeriod[11].equals("1") || splitPeriod[11].equals("3")) {
                    isPeriodChecked[11] = "1";
                    periodTwelve.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodTwelve.setTextColor(getResources().getColor(R.color.colorPrimary));
                }

                // Do so as well for its alternate layout if it is available
                if (splitOccurrence[1].equals("2")) {
                    if (splitPeriod[0].equals("2") || splitPeriod[0].equals("3")) {
                        if (isPeriodChecked[0].equals("1"))
                            isPeriodChecked[0] = "3";
                        else isPeriodChecked[0] = "2";
                        periodOneAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodOneAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[1].equals("2") || splitPeriod[1].equals("3")) {
                        if (isPeriodChecked[1].equals("1"))
                            isPeriodChecked[1] = "3";
                        else isPeriodChecked[1] = "2";
                        periodTwoAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodTwoAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[2].equals("2") || splitPeriod[2].equals("3")) {
                        if (isPeriodChecked[2].equals("1"))
                            isPeriodChecked[2] = "3";
                        else isPeriodChecked[2] = "2";
                        periodThreeAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodThreeAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[3].equals("2") || splitPeriod[3].equals("3")) {
                        if (isPeriodChecked[3].equals("1"))
                            isPeriodChecked[3] = "3";
                        else isPeriodChecked[3] = "2";
                        periodFourAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodFourAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[4].equals("2") || splitPeriod[4].equals("3")) {
                        if (isPeriodChecked[4].equals("1"))
                            isPeriodChecked[4] = "3";
                        else isPeriodChecked[4] = "2";
                        periodFiveAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodFiveAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[5].equals("2") || splitPeriod[5].equals("3")) {
                        if (isPeriodChecked[5].equals("1"))
                            isPeriodChecked[5] = "3";
                        else isPeriodChecked[5] = "2";
                        periodSixAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodSixAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[6].equals("2") || splitPeriod[6].equals("3")) {
                        if (isPeriodChecked[6].equals("1"))
                            isPeriodChecked[6] = "3";
                        else isPeriodChecked[6] = "2";
                        periodSevenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodSevenAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[7].equals("2") || splitPeriod[7].equals("3")) {
                        if (isPeriodChecked[7].equals("1"))
                            isPeriodChecked[7] = "3";
                        else isPeriodChecked[7] = "2";
                        periodEightAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodEightAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[8].equals("2") || splitPeriod[8].equals("3")) {
                        if (isPeriodChecked[8].equals("1"))
                            isPeriodChecked[8] = "3";
                        else isPeriodChecked[8] = "2";
                        periodNineAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodNineAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[9].equals("2") || splitPeriod[9].equals("3")) {
                        if (isPeriodChecked[9].equals("1"))
                            isPeriodChecked[9] = "3";
                        else isPeriodChecked[9] = "2";
                        periodTenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodTenAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[10].equals("2") || splitPeriod[10].equals("3")) {
                        if (isPeriodChecked[10].equals("1"))
                            isPeriodChecked[10] = "3";
                        else isPeriodChecked[10] = "2";
                        periodElevenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodElevenAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if (splitPeriod[11].equals("2") || splitPeriod[11].equals("3")) {
                        if (isPeriodChecked[11].equals("1"))
                            isPeriodChecked[11] = "3";
                        else isPeriodChecked[11] = "2";
                        periodTwelveAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                        periodTwelveAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                }
            }
        }

        return rootView;
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    // In the case that a day button was selected
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

                    // In the case that an alternate day button was selected
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

                    // In the case that a period button was selected
                    case R.id.class_three_period_one:
                        if (isPeriodChecked[0].equals("0")) {
                            isPeriodChecked[0] = "1";
                            periodOne.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodOne.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[0].equals("1")) {
                            isPeriodChecked[0] = "0";
                            periodOne.setBackgroundResource(R.drawable.bg_period_button);
                            periodOne.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[0].equals("2")) {
                            isPeriodChecked[0] = "3";
                            periodOne.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodOne.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[0].equals("3")) {
                            isPeriodChecked[0] = "2";
                            periodOne.setBackgroundResource(R.drawable.bg_period_button);
                            periodOne.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_two:
                        if (isPeriodChecked[1].equals("0")) {
                            isPeriodChecked[1] = "1";
                            periodTwo.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwo.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[1].equals("1")) {
                            isPeriodChecked[1] = "0";
                            periodTwo.setBackgroundResource(R.drawable.bg_period_button);
                            periodTwo.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[1].equals("2")) {
                            isPeriodChecked[1] = "3";
                            periodTwo.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwo.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[1].equals("3")) {
                            isPeriodChecked[1] = "2";
                            periodTwo.setBackgroundResource(R.drawable.bg_period_button);
                            periodTwo.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_three:
                        if (isPeriodChecked[2].equals("0")) {
                            isPeriodChecked[2] = "1";
                            periodThree.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodThree.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[2].equals("1")) {
                            isPeriodChecked[2] = "0";
                            periodThree.setBackgroundResource(R.drawable.bg_period_button);
                            periodThree.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[2].equals("2")) {
                            isPeriodChecked[2] = "3";
                            periodThree.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodThree.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[2].equals("3")) {
                            isPeriodChecked[2] = "2";
                            periodThree.setBackgroundResource(R.drawable.bg_period_button);
                            periodThree.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_four:
                        if (isPeriodChecked[3].equals("0")) {
                            isPeriodChecked[3] = "1";
                            periodFour.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFour.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[3].equals("1")) {
                            isPeriodChecked[3] = "0";
                            periodFour.setBackgroundResource(R.drawable.bg_period_button);
                            periodFour.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[3].equals("2")) {
                            isPeriodChecked[3] = "3";
                            periodFour.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFour.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[3].equals("3")) {
                            isPeriodChecked[3] = "2";
                            periodFour.setBackgroundResource(R.drawable.bg_period_button);
                            periodFour.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_five:
                        if (isPeriodChecked[4].equals("0")) {
                            isPeriodChecked[4] = "1";
                            periodFive.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFive.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[4].equals("1")) {
                            isPeriodChecked[4] = "0";
                            periodFive.setBackgroundResource(R.drawable.bg_period_button);
                            periodFive.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[4].equals("2")) {
                            isPeriodChecked[4] = "3";
                            periodFive.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFive.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[4].equals("3")) {
                            isPeriodChecked[4] = "2";
                            periodFive.setBackgroundResource(R.drawable.bg_period_button);
                            periodFive.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_six:
                        if (isPeriodChecked[5].equals("0")) {
                            isPeriodChecked[5] = "1";
                            periodSix.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSix.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[5].equals("1")) {
                            isPeriodChecked[5] = "0";
                            periodSix.setBackgroundResource(R.drawable.bg_period_button);
                            periodSix.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[5].equals("2")) {
                            isPeriodChecked[5] = "3";
                            periodSix.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSix.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[5].equals("3")) {
                            isPeriodChecked[5] = "2";
                            periodSix.setBackgroundResource(R.drawable.bg_period_button);
                            periodSix.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_seven:
                        if (isPeriodChecked[6].equals("0")) {
                            isPeriodChecked[6] = "1";
                            periodSeven.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSeven.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[6].equals("1")) {
                            isPeriodChecked[6] = "0";
                            periodSeven.setBackgroundResource(R.drawable.bg_period_button);
                            periodSeven.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[6].equals("2")) {
                            isPeriodChecked[6] = "3";
                            periodSeven.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSeven.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[6].equals("3")) {
                            isPeriodChecked[6] = "2";
                            periodSeven.setBackgroundResource(R.drawable.bg_period_button);
                            periodSeven.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_eight:
                        if (isPeriodChecked[7].equals("0")) {
                            isPeriodChecked[7] = "1";
                            periodEight.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEight.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[7].equals("1")) {
                            isPeriodChecked[7] = "0";
                            periodEight.setBackgroundResource(R.drawable.bg_period_button);
                            periodEight.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[7].equals("2")) {
                            isPeriodChecked[7] = "3";
                            periodEight.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEight.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[7].equals("3")) {
                            isPeriodChecked[7] = "2";
                            periodEight.setBackgroundResource(R.drawable.bg_period_button);
                            periodEight.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_nine:
                        if (isPeriodChecked[8].equals("0")) {
                            isPeriodChecked[8] = "1";
                            periodNine.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodNine.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[8].equals("1")) {
                            isPeriodChecked[8] = "0";
                            periodNine.setBackgroundResource(R.drawable.bg_period_button);
                            periodNine.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[8].equals("2")) {
                            isPeriodChecked[8] = "3";
                            periodNine.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodNine.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[8].equals("3")) {
                            isPeriodChecked[8] = "2";
                            periodNine.setBackgroundResource(R.drawable.bg_period_button);
                            periodNine.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_ten:
                        if (isPeriodChecked[9].equals("0")) {
                            isPeriodChecked[9] = "1";
                            periodTen.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTen.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[9].equals("1")) {
                            isPeriodChecked[9] = "0";
                            periodTen.setBackgroundResource(R.drawable.bg_period_button);
                            periodTen.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[9].equals("2")) {
                            isPeriodChecked[9] = "3";
                            periodTen.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTen.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[9].equals("3")) {
                            isPeriodChecked[9] = "2";
                            periodTen.setBackgroundResource(R.drawable.bg_period_button);
                            periodTen.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_eleven:
                        if (isPeriodChecked[10].equals("0")) {
                            isPeriodChecked[10] = "1";
                            periodEleven.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEleven.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[10].equals("1")) {
                            isPeriodChecked[10] = "0";
                            periodEleven.setBackgroundResource(R.drawable.bg_period_button);
                            periodEleven.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[10].equals("2")) {
                            isPeriodChecked[10] = "3";
                            periodEleven.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEleven.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[10].equals("3")) {
                            isPeriodChecked[10] = "2";
                            periodEleven.setBackgroundResource(R.drawable.bg_period_button);
                            periodEleven.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_twelve:
                        if (isPeriodChecked[11].equals("0")) {
                            isPeriodChecked[11] = "1";
                            periodTwelve.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwelve.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[11].equals("1")) {
                            isPeriodChecked[11] = "0";
                            periodTwelve.setBackgroundResource(R.drawable.bg_period_button);
                            periodTwelve.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[11].equals("2")) {
                            isPeriodChecked[11] = "3";
                            periodTwelve.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwelve.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[11].equals("3")) {
                            isPeriodChecked[11] = "2";
                            periodTwelve.setBackgroundResource(R.drawable.bg_period_button);
                            periodTwelve.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;

                    // In the case that an alternate period button was selected
                    case R.id.class_three_period_one_alt:
                        if (isPeriodChecked[0].equals("0")) {
                            isPeriodChecked[0] = "2";
                            periodOneAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodOneAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[0].equals("1")) {
                            isPeriodChecked[0] = "3";
                            periodOneAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodOneAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[0].equals("2")) {
                            isPeriodChecked[0] = "0";
                            periodOneAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodOneAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[0].equals("3")) {
                            isPeriodChecked[0] = "1";
                            periodOneAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodOneAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_two_alt:
                        if (isPeriodChecked[1].equals("0")) {
                            isPeriodChecked[1] = "2";
                            periodTwoAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwoAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[1].equals("1")) {
                            isPeriodChecked[1] = "3";
                            periodTwoAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwoAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[1].equals("2")) {
                            isPeriodChecked[1] = "0";
                            periodTwoAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodTwoAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[1].equals("3")) {
                            isPeriodChecked[1] = "1";
                            periodTwoAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodTwoAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_three_alt:
                        if (isPeriodChecked[2].equals("0")) {
                            isPeriodChecked[2] = "2";
                            periodThreeAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodThreeAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[2].equals("1")) {
                            isPeriodChecked[2] = "3";
                            periodThreeAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodThreeAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[2].equals("2")) {
                            isPeriodChecked[2] = "0";
                            periodThreeAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodThreeAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[2].equals("3")) {
                            isPeriodChecked[2] = "1";
                            periodThreeAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodThreeAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_four_alt:
                        if (isPeriodChecked[3].equals("0")) {
                            isPeriodChecked[3] = "2";
                            periodFourAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFourAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[3].equals("1")) {
                            isPeriodChecked[3] = "3";
                            periodFourAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFourAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[3].equals("2")) {
                            isPeriodChecked[3] = "0";
                            periodFourAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodFourAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[3].equals("3")) {
                            isPeriodChecked[3] = "1";
                            periodFourAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodFourAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_five_alt:
                        if (isPeriodChecked[4].equals("0")) {
                            isPeriodChecked[4] = "2";
                            periodFiveAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFiveAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[4].equals("1")) {
                            isPeriodChecked[4] = "3";
                            periodFiveAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFiveAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[4].equals("2")) {
                            isPeriodChecked[4] = "0";
                            periodFiveAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodFiveAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[4].equals("3")) {
                            isPeriodChecked[4] = "1";
                            periodFiveAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodFiveAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_six_alt:
                        if (isPeriodChecked[5].equals("0")) {
                            isPeriodChecked[5] = "2";
                            periodSixAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSixAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[5].equals("1")) {
                            isPeriodChecked[5] = "3";
                            periodSixAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSixAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[5].equals("2")) {
                            isPeriodChecked[5] = "0";
                            periodSixAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodSixAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[5].equals("3")) {
                            isPeriodChecked[5] = "1";
                            periodSixAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodSixAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_seven_alt:
                        if (isPeriodChecked[6].equals("0")) {
                            isPeriodChecked[6] = "2";
                            periodSevenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSevenAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[6].equals("1")) {
                            isPeriodChecked[6] = "3";
                            periodSevenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSevenAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[6].equals("2")) {
                            isPeriodChecked[6] = "0";
                            periodSevenAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodSevenAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[6].equals("3")) {
                            isPeriodChecked[6] = "1";
                            periodSevenAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodSevenAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_eight_alt:
                        if (isPeriodChecked[7].equals("0")) {
                            isPeriodChecked[7] = "2";
                            periodEightAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEightAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[7].equals("1")) {
                            isPeriodChecked[7] = "3";
                            periodEightAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEightAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[7].equals("2")) {
                            isPeriodChecked[7] = "0";
                            periodEightAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodEightAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[7].equals("3")) {
                            isPeriodChecked[7] = "1";
                            periodEightAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodEightAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_nine_alt:
                        if (isPeriodChecked[8].equals("0")) {
                            isPeriodChecked[8] = "2";
                            periodNineAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodNineAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[8].equals("1")) {
                            isPeriodChecked[8] = "3";
                            periodNineAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodNineAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[8].equals("2")) {
                            isPeriodChecked[8] = "0";
                            periodNineAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodNineAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[8].equals("3")) {
                            isPeriodChecked[8] = "1";
                            periodNineAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodNineAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_ten_alt:
                        if (isPeriodChecked[9].equals("0")) {
                            isPeriodChecked[9] = "2";
                            periodTenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTenAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[9].equals("1")) {
                            isPeriodChecked[9] = "3";
                            periodTenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTenAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[9].equals("2")) {
                            isPeriodChecked[9] = "0";
                            periodTenAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodTenAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[9].equals("3")) {
                            isPeriodChecked[9] = "1";
                            periodTenAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodTenAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_eleven_alt:
                        if (isPeriodChecked[10].equals("0")) {
                            isPeriodChecked[10] = "2";
                            periodElevenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodElevenAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[10].equals("1")) {
                            isPeriodChecked[10] = "3";
                            periodElevenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodElevenAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[10].equals("2")) {
                            isPeriodChecked[10] = "0";
                            periodElevenAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodElevenAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[10].equals("3")) {
                            isPeriodChecked[10] = "1";
                            periodElevenAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodElevenAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;
                    case R.id.class_three_period_twelve_alt:
                        if (isPeriodChecked[11].equals("0")) {
                            isPeriodChecked[11] = "2";
                            periodTwelveAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwelveAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[11].equals("1")) {
                            isPeriodChecked[11] = "3";
                            periodTwelveAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwelveAlt.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (isPeriodChecked[11].equals("2")) {
                            isPeriodChecked[11] = "0";
                            periodTwelveAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodTwelveAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        else if (isPeriodChecked[11].equals("3")) {
                            isPeriodChecked[11] = "1";
                            periodTwelveAlt.setBackgroundResource(R.drawable.bg_period_button);
                            periodTwelveAlt.setTextColor(getResources().getColor(R.color.white));
                        }
                        break;

                    // In the case that it's one of the hyperlink text views to the
                    // previous stages of the add class time process
                    case R.id.class_time_one_value:
                        basisTextviewSelectedListener.onBasisTextviewSelected();
                        break;

                    // 1 is the fixed value passed as the basis because the activity itself
                    // (PeriodBased) was launched as a result of the basis being 0
                    case R.id.class_time_two_value:
                        weektypeTextviewSelectedListener.onWeektypeTextViewSelectedListener("1");
                        break;

                    case R.id.class_three_done:
                        String classDays = processClassDaysString();
                        String periods = processPeriodsString();

                        // Validate that at least one day has been selected
                        if (classDays.equals("0:0:0:0:0:0:0")) {
                            Toast.makeText(getContext(), getString(R.string.new_schedule_toast_validation_no_days_selected),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Validate that at least one period has been selected
                        if (periods.equals("0:0:0:0:0:0:0:0:0:0:0:0")) {
                            Toast.makeText(getContext(), getString(R.string.new_schedule_toast_validation_no_period_selected),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // If the fragment was started through the list view's OnItemClick
                        // Pass true as the Edit Flag to tell the activity to update instead
                        // the occurrence item instead of adding a new item
                        boolean FLAG_EDIT = getArguments().containsKey("occurrence");
                        int rowId = getArguments().getInt("rowId");
                        daysSelectedListener.onDaysSelected(classDays, -1, -1,
                                -1, -1, periods, FLAG_EDIT, rowId);
                        break;
                }
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

    private String processPeriodsString(){
        // Creates the convertible period string
        // for database storage
        return isPeriodChecked[0] + ":"
                + isPeriodChecked[1] + ":"
                + isPeriodChecked[2] + ":"
                + isPeriodChecked[3] + ":"
                + isPeriodChecked[4] + ":"
                + isPeriodChecked[5] + ":"
                + isPeriodChecked[6] + ":"
                + isPeriodChecked[7] + ":"
                + isPeriodChecked[8] + ":"
                + isPeriodChecked[9] + ":"
                + isPeriodChecked[10] + ":"
                + isPeriodChecked[11];
    }

}
