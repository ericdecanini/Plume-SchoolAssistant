package com.pdt.plume;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    TextView periodOneAlt;
    TextView periodTwoAlt;
    TextView periodThreeAlt;
    TextView periodFourAlt;
    TextView periodFiveAlt;
    TextView periodSixAlt;
    TextView periodSevenAlt;
    TextView periodEightAlt;

    TextView header, headerAlt;

    int mPrimaryColor;
    int mSecondaryColor;

    // Fragment input storage variables
    int[] isButtonChecked = {0, 0, 0, 0, 0, 0, 0};
    String[] isPeriodChecked = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};

    // Interface variables
    onDaysSelectedListener daysSelectedListener;
    onBasisTextviewSelectedListener basisTextviewSelectedListener;
    onWeektypeTextviewSelectedListener weektypeTextviewSelectedListener;
    boolean FLAG_EDIT = false;
    int rowID = 0;

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

        header = (TextView) rootView.findViewById(R.id.class_three_item_header);
        TextView sunday = (TextView) rootView.findViewById(R.id.class_three_sunday);
        TextView monday = (TextView) rootView.findViewById(R.id.class_three_monday);
        TextView tuesday = (TextView) rootView.findViewById(R.id.class_three_tuesday);
        TextView wednesday = (TextView) rootView.findViewById(R.id.class_three_wednesday);
        TextView thursday = (TextView) rootView.findViewById(R.id.class_three_thursday);
        TextView friday = (TextView) rootView.findViewById(R.id.class_three_friday);
        TextView saturday = (TextView) rootView.findViewById(R.id.class_three_saturday);
        LinearLayout done = (LinearLayout) rootView.findViewById(R.id.class_three_done);
        LinearLayout cancel = (LinearLayout) rootView.findViewById(R.id.class_three_cancel);

        headerAlt = (TextView) rootView.findViewById(R.id.class_three_item_header_alt);
        TextView sundayAlt = (TextView) rootView.findViewById(R.id.class_three_sunday_alt);
        TextView mondayAlt = (TextView) rootView.findViewById(R.id.class_three_monday_alt);
        TextView tuesdayAlt = (TextView) rootView.findViewById(R.id.class_three_tuesday_alt);
        TextView wednesdayAlt = (TextView) rootView.findViewById(R.id.class_three_wednesday_alt);
        TextView thursdayAlt = (TextView) rootView.findViewById(R.id.class_three_thursday_alt);
        TextView fridayAlt = (TextView) rootView.findViewById(R.id.class_three_friday_alt);
        TextView saturdayAlt = (TextView) rootView.findViewById(R.id.class_three_saturday_alt);

        periodOne = (TextView) rootView.findViewById(R.id.class_three_period_one);
        periodTwo = (TextView) rootView.findViewById(R.id.class_three_period_two);
        periodThree = (TextView) rootView.findViewById(R.id.class_three_period_three);
        periodFour = (TextView) rootView.findViewById(R.id.class_three_period_four);
        periodFive = (TextView) rootView.findViewById(R.id.class_three_period_five);
        periodSix = (TextView) rootView.findViewById(R.id.class_three_period_six);
        periodSeven = (TextView) rootView.findViewById(R.id.class_three_period_seven);
        periodEight = (TextView) rootView.findViewById(R.id.class_three_period_eight);

        periodOneAlt = (TextView) rootView.findViewById(R.id.class_three_period_one_alt);
        periodTwoAlt = (TextView) rootView.findViewById(R.id.class_three_period_two_alt);
        periodThreeAlt = (TextView) rootView.findViewById(R.id.class_three_period_three_alt);
        periodFourAlt = (TextView) rootView.findViewById(R.id.class_three_period_four_alt);
        periodFiveAlt = (TextView) rootView.findViewById(R.id.class_three_period_five_alt);
        periodSixAlt = (TextView) rootView.findViewById(R.id.class_three_period_six_alt);
        periodSevenAlt = (TextView) rootView.findViewById(R.id.class_three_period_seven_alt);
        periodEightAlt = (TextView) rootView.findViewById(R.id.class_three_period_eight_alt);

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
        cancel.setOnClickListener(listener());

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

        periodOneAlt.setOnClickListener(listener());
        periodTwoAlt.setOnClickListener(listener());
        periodThreeAlt.setOnClickListener(listener());
        periodFourAlt.setOnClickListener(listener());
        periodFiveAlt.setOnClickListener(listener());
        periodSixAlt.setOnClickListener(listener());
        periodSevenAlt.setOnClickListener(listener());
        periodEightAlt.setOnClickListener(listener());

        // Initialise the theme
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR),
                getResources().getColor(R.color.colorPrimary));
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR),
                getResources().getColor(R.color.colorAccent));

        // Get the arguments of the fragment and
        // Set the hyperlink basis week taskType text accordingly
        basisTextView.setText(getString(R.string.class_time_one_periodbased));
        basisTextView.setTextColor(getResources().getColor(R.color.gray_200));
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("FLAG_EDIT")) {
                FLAG_EDIT = args.getBoolean("FLAG_EDIT");
                rowID = args.getInt("rowID");
            }
            if (args.containsKey("occurrence")) {
                FLAG_EDIT = true;
                rowID = args.getInt("rowId");
            }

            if (!args.getString("weekType", "-1").equals("1")) {
                //Change the layout based on weekType
                rootView.findViewById(R.id.class_time_three_week_type_alt_layout).setVisibility(View.GONE);
                header.setText(getString(R.string.class_time_three_select_days));
                weekTypeTextView.setText(getString(R.string.class_time_two_sameweek));
            } else {
                weekTypeTextView.setText(getString(R.string.class_time_two_altweeks));
                header.setText(getString(R.string.week_one));
            }
            weekTypeTextView.setTextColor(getResources().getColor(R.color.gray_200));

            // Check if the fragment was started through the list view's OnItemClick
            // If it is, receive the corresponding data and auto-fill that item's UI
            if (args.containsKey("occurrence")) {
                // Get the data from the arguments bundle
                FLAG_EDIT = true;
                String occurrence = args.getString("occurrence", "-1");
                String[] splitOccurrence = occurrence.split(":");
                String period = args.getString("period", "-1");
                String[] splitPeriod = period.split(":");

                // Check each item in the occurrence string's day binary
                // and set it in the activity
                if (splitOccurrence[2].equals("1") || splitOccurrence[2].equals("3")) {
                    isButtonChecked[0] = 1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        sunday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        sunday.getBackground().setAlpha(180);
                    }
                }
                if (splitOccurrence[3].equals("1") || splitOccurrence[3].equals("3")) {
                    isButtonChecked[1] = 1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        monday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        monday.getBackground().setAlpha(180);
                    }
                }
                if (splitOccurrence[4].equals("1") || splitOccurrence[4].equals("3")) {
                    isButtonChecked[2] = 1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tuesday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        tuesday.getBackground().setAlpha(180);
                    }
                }
                if (splitOccurrence[5].equals("1") || splitOccurrence[5].equals("3")) {
                    isButtonChecked[3] = 1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        wednesday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        wednesday.getBackground().setAlpha(180);
                    }
                }
                if (splitOccurrence[6].equals("1") || splitOccurrence[6].equals("3")) {
                    isButtonChecked[4] = 1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        thursday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        thursday.getBackground().setAlpha(180);
                    }
                }
                if (splitOccurrence[7].equals("1") || splitOccurrence[7].equals("3")) {
                    isButtonChecked[5] = 1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        friday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        friday.getBackground().setAlpha(180);
                    }
                }
                if (splitOccurrence[8].equals("1") || splitOccurrence[8].equals("3")) {
                    isButtonChecked[6] = 1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        saturday.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                        saturday.getBackground().setAlpha(180);
                    }
                }

                // Do so for alternate layout if it is available
                if (splitOccurrence[1].equals("1")) {
                    if (splitOccurrence[2].equals("2") || splitOccurrence[2].equals("3")) {
                        if (isButtonChecked[0] == 1)
                            isButtonChecked[0] = 3;
                        else isButtonChecked[0] = 2;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            sundayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            sundayAlt.getBackground().setAlpha(180);
                        }
                    }
                    if (splitOccurrence[3].equals("2") || splitOccurrence[3].equals("3")) {
                        if (isButtonChecked[1] == 1)
                            isButtonChecked[1] = 3;
                        else isButtonChecked[1] = 2;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mondayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            mondayAlt.getBackground().setAlpha(180);
                        }
                    }
                    if (splitOccurrence[4].equals("2") || splitOccurrence[4].equals("3")) {
                        if (isButtonChecked[2] == 1)
                            isButtonChecked[2] = 3;
                        else isButtonChecked[2] = 2;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            tuesdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            tuesdayAlt.getBackground().setAlpha(180);
                        }
                    }
                    if (splitOccurrence[5].equals("2") || splitOccurrence[5].equals("3")) {
                        if (isButtonChecked[3] == 1)
                            isButtonChecked[3] = 3;
                        else isButtonChecked[3] = 2;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            wednesdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            wednesdayAlt.getBackground().setAlpha(180);
                        }
                    }
                    if (splitOccurrence[6].equals("2") || splitOccurrence[6].equals("3")) {
                        if (isButtonChecked[4] == 1)
                            isButtonChecked[4] = 3;
                        else isButtonChecked[4] = 2;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            thursdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            thursdayAlt.getBackground().setAlpha(180);
                        }
                    }
                    if (splitOccurrence[7].equals("2") || splitOccurrence[7].equals("3")) {
                        if (isButtonChecked[5] == 1)
                            isButtonChecked[5] = 3;
                        else isButtonChecked[5] = 2;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            fridayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            fridayAlt.getBackground().setAlpha(180);
                        }
                    }
                    if (splitOccurrence[8].equals("2") || splitOccurrence[8].equals("3")) {
                        if (isButtonChecked[6] == 1)
                            isButtonChecked[6] = 3;
                        else isButtonChecked[6] = 2;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            saturdayAlt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                            saturdayAlt.getBackground().setAlpha(180);
                        }
                    }
                }

                // Do so as well for the period list
                if (splitPeriod[0].equals("1") || splitPeriod[0].equals("3")) {
                    isPeriodChecked[0] = "1";
                    periodOne.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodOne.getBackground().setAlpha(180);
                }
                if (splitPeriod[1].equals("1") || splitPeriod[1].equals("3")) {
                    isPeriodChecked[1] = "1";
                    periodTwo.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodTwo.getBackground().setAlpha(180);
                }
                if (splitPeriod[2].equals("1") || splitPeriod[2].equals("3")) {
                    isPeriodChecked[2] = "1";
                    periodThree.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodThree.getBackground().setAlpha(180);
                }
                if (splitPeriod[3].equals("1") || splitPeriod[3].equals("3")) {
                    isPeriodChecked[3] = "1";
                    periodFour.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodFour.getBackground().setAlpha(180);
                }
                if (splitPeriod[4].equals("1") || splitPeriod[4].equals("3")) {
                    isPeriodChecked[4] = "1";
                    periodFive.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodFive.getBackground().setAlpha(180);
                }
                if (splitPeriod[5].equals("1") || splitPeriod[5].equals("3")) {
                    isPeriodChecked[5] = "1";
                    periodSix.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodSix.getBackground().setAlpha(180);
                }
                if (splitPeriod[6].equals("1") || splitPeriod[6].equals("3")) {
                    isPeriodChecked[6] = "1";
                    periodSeven.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodSeven.getBackground().setAlpha(180);
                }
                if (splitPeriod[7].equals("1") || splitPeriod[7].equals("3")) {
                    isPeriodChecked[7] = "1";
                    periodEight.setBackgroundResource(R.drawable.bg_period_button_active);
                    periodEight.getBackground().setAlpha(180);

                    // Do so as well for its alternate layout if it is available
                    if (splitOccurrence[1].equals("1")) {
                        if (splitPeriod[0].equals("2") || splitPeriod[0].equals("3")) {
                            if (isPeriodChecked[0].equals("1"))
                                isPeriodChecked[0] = "3";
                            else isPeriodChecked[0] = "2";
                            periodOneAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodOneAlt.getBackground().setAlpha(180);
                        }
                        if (splitPeriod[1].equals("2") || splitPeriod[1].equals("3")) {
                            if (isPeriodChecked[1].equals("1"))
                                isPeriodChecked[1] = "3";
                            else isPeriodChecked[1] = "2";
                            periodTwoAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwoAlt.getBackground().setAlpha(180);
                        }
                        if (splitPeriod[2].equals("2") || splitPeriod[2].equals("3")) {
                            if (isPeriodChecked[2].equals("1"))
                                isPeriodChecked[2] = "3";
                            else isPeriodChecked[2] = "2";
                            periodThreeAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodThreeAlt.getBackground().setAlpha(180);
                        }
                        if (splitPeriod[3].equals("2") || splitPeriod[3].equals("3")) {
                            if (isPeriodChecked[3].equals("1"))
                                isPeriodChecked[3] = "3";
                            else isPeriodChecked[3] = "2";
                            periodFourAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFourAlt.getBackground().setAlpha(180);
                        }
                        if (splitPeriod[4].equals("2") || splitPeriod[4].equals("3")) {
                            if (isPeriodChecked[4].equals("1"))
                                isPeriodChecked[4] = "3";
                            else isPeriodChecked[4] = "2";
                            periodFiveAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFiveAlt.getBackground().setAlpha(180);
                        }
                        if (splitPeriod[5].equals("2") || splitPeriod[5].equals("3")) {
                            if (isPeriodChecked[5].equals("1"))
                                isPeriodChecked[5] = "3";
                            else isPeriodChecked[5] = "2";
                            periodSixAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSixAlt.getBackground().setAlpha(180);
                        }
                        if (splitPeriod[6].equals("2") || splitPeriod[6].equals("3")) {
                            if (isPeriodChecked[6].equals("1"))
                                isPeriodChecked[6] = "3";
                            else isPeriodChecked[6] = "2";
                            periodSevenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSevenAlt.getBackground().setAlpha(180);
                        }
                        if (splitPeriod[7].equals("2") || splitPeriod[7].equals("3")) {
                            if (isPeriodChecked[7].equals("1"))
                                isPeriodChecked[7] = "3";
                            else isPeriodChecked[7] = "2";
                            periodEightAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEightAlt.getBackground().setAlpha(180);
                        }

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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[0] == 1){
                            isButtonChecked[0] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[0] == 2){
                            isButtonChecked[0] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[0] == 3){
                            isButtonChecked[0] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_monday:
                        if (isButtonChecked[1] == 0){
                            isButtonChecked[1] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[1] == 1){
                            isButtonChecked[1] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[1] == 2){
                            isButtonChecked[1] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[1] == 3){
                            isButtonChecked[1] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_tuesday:
                        if (isButtonChecked[2] == 0){
                            isButtonChecked[2] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[2] == 1){
                            isButtonChecked[2] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[2] == 2) {
                            isButtonChecked[2] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[2] == 3){
                            isButtonChecked[2] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_wednesday:
                        if (isButtonChecked[3] == 0) {
                            isButtonChecked[3] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[3] == 1){
                            isButtonChecked[3] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[3] == 2){
                            isButtonChecked[3] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[3] == 3){
                            isButtonChecked[3] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_thursday:
                        if (isButtonChecked[4] == 0){
                            isButtonChecked[4] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[4] == 1){
                            isButtonChecked[4] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[4] == 2){
                            isButtonChecked[4] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[4] == 3){
                            isButtonChecked[4] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_friday:
                        if (isButtonChecked[5] == 0){
                            isButtonChecked[5] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[5] == 1){
                            isButtonChecked[5] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[5] == 2){
                            isButtonChecked[5] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[5] == 3){
                            isButtonChecked[5] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_saturday:
                        if (isButtonChecked[6] == 0){
                            isButtonChecked[6] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[6] == 1){
                            isButtonChecked[6] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[6] == 2){
                            isButtonChecked[6] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[6] == 3){
                            isButtonChecked[6] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;

                    // In the case that an alternate day button was selected
                    case R.id.class_three_sunday_alt:
                        if (isButtonChecked[0] == 0){
                            isButtonChecked[0] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[0] == 1){
                            isButtonChecked[0] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[0] == 2){
                            isButtonChecked[0] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[0] == 3){
                            isButtonChecked[0] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_monday_alt:
                        if (isButtonChecked[1] == 0){
                            isButtonChecked[1] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[1] == 1){
                            isButtonChecked[1] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[1] == 2){
                            isButtonChecked[1] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[1] == 3){
                            isButtonChecked[1] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_tuesday_alt:
                        if (isButtonChecked[2] == 0){
                            isButtonChecked[2] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[2] == 1){
                            isButtonChecked[2] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[2] == 2){
                            isButtonChecked[2] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[2] == 3){
                            isButtonChecked[2] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_wednesday_alt:
                        if (isButtonChecked[3] == 0){
                            isButtonChecked[3] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[3] == 1){
                            isButtonChecked[3] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[3] == 2){
                            isButtonChecked[3] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[3] == 3){
                            isButtonChecked[3] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_thursday_alt:
                        if (isButtonChecked[4] == 0){
                            isButtonChecked[4] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[4] == 1){
                            isButtonChecked[4] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[4] == 2){
                            isButtonChecked[4] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[4] == 3){
                            isButtonChecked[4] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_friday_alt:
                        if (isButtonChecked[5] == 0){
                            isButtonChecked[5] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[5] == 1){
                            isButtonChecked[5] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[5] == 2){
                            isButtonChecked[5] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[5] == 3){
                            isButtonChecked[5] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;
                    case R.id.class_three_saturday_alt:
                        if (isButtonChecked[6] == 0){
                            isButtonChecked[6] = 2;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[6] == 1){
                            isButtonChecked[6] = 3;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_period_button_active));
                                v.getBackground().setAlpha(180);
                            }
                        }
                        else if (isButtonChecked[6] == 2){
                            isButtonChecked[6] = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        else if (isButtonChecked[6] == 3){
                            isButtonChecked[6] = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setBackgroundDrawable(null);
                            }
                        }
                        break;

                    // In the case that a period button was selected
                    case R.id.class_three_period_one:
                        if (isPeriodChecked[0].equals("0")) {
                            isPeriodChecked[0] = "1";
                            periodOne.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodOne.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[0].equals("1")) {
                            isPeriodChecked[0] = "0";
                            periodOne.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[0].equals("2")) {
                            isPeriodChecked[0] = "3";
                            periodOne.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodOne.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[0].equals("3")) {
                            isPeriodChecked[0] = "2";
                            periodOne.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_two:
                        if (isPeriodChecked[1].equals("0")) {
                            isPeriodChecked[1] = "1";
                            periodTwo.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwo.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[1].equals("1")) {
                            isPeriodChecked[1] = "0";
                            periodTwo.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[1].equals("2")) {
                            isPeriodChecked[1] = "3";
                            periodTwo.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwo.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[1].equals("3")) {
                            isPeriodChecked[1] = "2";
                            periodTwo.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_three:
                        if (isPeriodChecked[2].equals("0")) {
                            isPeriodChecked[2] = "1";
                            periodThree.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodThree.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[2].equals("1")) {
                            isPeriodChecked[2] = "0";
                            periodThree.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[2].equals("2")) {
                            isPeriodChecked[2] = "3";
                            periodThree.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodThree.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[2].equals("3")) {
                            isPeriodChecked[2] = "2";
                            periodThree.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_four:
                        if (isPeriodChecked[3].equals("0")) {
                            isPeriodChecked[3] = "1";
                            periodFour.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFour.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[3].equals("1")) {
                            isPeriodChecked[3] = "0";
                            periodFour.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[3].equals("2")) {
                            isPeriodChecked[3] = "3";
                            periodFour.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFour.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[3].equals("3")) {
                            isPeriodChecked[3] = "2";
                            periodFour.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_five:
                        if (isPeriodChecked[4].equals("0")) {
                            isPeriodChecked[4] = "1";
                            periodFive.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFive.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[4].equals("1")) {
                            isPeriodChecked[4] = "0";
                            periodFive.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[4].equals("2")) {
                            isPeriodChecked[4] = "3";
                            periodFive.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFive.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[4].equals("3")) {
                            isPeriodChecked[4] = "2";
                            periodFive.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_six:
                        if (isPeriodChecked[5].equals("0")) {
                            isPeriodChecked[5] = "1";
                            periodSix.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSix.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[5].equals("1")) {
                            isPeriodChecked[5] = "0";
                            periodSix.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[5].equals("2")) {
                            isPeriodChecked[5] = "3";
                            periodSix.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSix.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[5].equals("3")) {
                            isPeriodChecked[5] = "2";
                            periodSix.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_seven:
                        if (isPeriodChecked[6].equals("0")) {
                            isPeriodChecked[6] = "1";
                            periodSeven.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSeven.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[6].equals("1")) {
                            isPeriodChecked[6] = "0";
                            periodSeven.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[6].equals("2")) {
                            isPeriodChecked[6] = "3";
                            periodSeven.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSeven.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[6].equals("3")) {
                            isPeriodChecked[6] = "2";
                            periodSeven.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_eight:
                        if (isPeriodChecked[7].equals("0")) {
                            isPeriodChecked[7] = "1";
                            periodEight.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEight.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[7].equals("1")) {
                            isPeriodChecked[7] = "0";
                            periodEight.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[7].equals("2")) {
                            isPeriodChecked[7] = "3";
                            periodEight.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEight.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[7].equals("3")) {
                            isPeriodChecked[7] = "2";
                            periodEight.setBackgroundDrawable(null);
                        }
                        break;

                    // In the case that an alternate period button was selected
                    case R.id.class_three_period_one_alt:
                        if (isPeriodChecked[0].equals("0")) {
                            isPeriodChecked[0] = "2";
                            periodOneAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodOneAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[0].equals("1")) {
                            isPeriodChecked[0] = "3";
                            periodOneAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodOneAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[0].equals("2")) {
                            isPeriodChecked[0] = "0";
                            periodOneAlt.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[0].equals("3")) {
                            isPeriodChecked[0] = "1";
                            periodOneAlt.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_two_alt:
                        if (isPeriodChecked[1].equals("0")) {
                            isPeriodChecked[1] = "2";
                            periodTwoAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwoAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[1].equals("1")) {
                            isPeriodChecked[1] = "3";
                            periodTwoAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodTwoAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[1].equals("2")) {
                            isPeriodChecked[1] = "0";
                            periodTwoAlt.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[1].equals("3")) {
                            isPeriodChecked[1] = "1";
                            periodTwoAlt.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_three_alt:
                        if (isPeriodChecked[2].equals("0")) {
                            isPeriodChecked[2] = "2";
                            periodThreeAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodThreeAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[2].equals("1")) {
                            isPeriodChecked[2] = "3";
                            periodThreeAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodThreeAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[2].equals("2")) {
                            isPeriodChecked[2] = "0";
                            periodThreeAlt.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[2].equals("3")) {
                            isPeriodChecked[2] = "1";
                            periodThreeAlt.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_four_alt:
                        if (isPeriodChecked[3].equals("0")) {
                            isPeriodChecked[3] = "2";
                            periodFourAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFourAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[3].equals("1")) {
                            isPeriodChecked[3] = "3";
                            periodFourAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFourAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[3].equals("2")) {
                            isPeriodChecked[3] = "0";
                            periodFourAlt.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[3].equals("3")) {
                            isPeriodChecked[3] = "1";
                            periodFourAlt.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_five_alt:
                        if (isPeriodChecked[4].equals("0")) {
                            isPeriodChecked[4] = "2";
                            periodFiveAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFiveAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[4].equals("1")) {
                            isPeriodChecked[4] = "3";
                            periodFiveAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodFiveAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[4].equals("2")) {
                            isPeriodChecked[4] = "0";
                            periodFiveAlt.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[4].equals("3")) {
                            isPeriodChecked[4] = "1";
                            periodFiveAlt.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_six_alt:
                        if (isPeriodChecked[5].equals("0")) {
                            isPeriodChecked[5] = "2";
                            periodSixAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSixAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[5].equals("1")) {
                            isPeriodChecked[5] = "3";
                            periodSixAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSixAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[5].equals("2")) {
                            isPeriodChecked[5] = "0";
                            periodSixAlt.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[5].equals("3")) {
                            isPeriodChecked[5] = "1";
                            periodSixAlt.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_seven_alt:
                        if (isPeriodChecked[6].equals("0")) {
                            isPeriodChecked[6] = "2";
                            periodSevenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSevenAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[6].equals("1")) {
                            isPeriodChecked[6] = "3";
                            periodSevenAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodSevenAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[6].equals("2")) {
                            isPeriodChecked[6] = "0";
                            periodSevenAlt.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[6].equals("3")) {
                            isPeriodChecked[6] = "1";
                            periodSevenAlt.setBackgroundDrawable(null);
                        }
                        break;
                    case R.id.class_three_period_eight_alt:
                        if (isPeriodChecked[7].equals("0")) {
                            isPeriodChecked[7] = "2";
                            periodEightAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEightAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[7].equals("1")) {
                            isPeriodChecked[7] = "3";
                            periodEightAlt.setBackgroundResource(R.drawable.bg_period_button_active);
                            periodEightAlt.getBackground().setAlpha(180);
                        }
                        else if (isPeriodChecked[7].equals("2")) {
                            isPeriodChecked[7] = "0";
                            periodEightAlt.setBackgroundDrawable(null);
                        }
                        else if (isPeriodChecked[7].equals("3")) {
                            isPeriodChecked[7] = "1";
                            periodEightAlt.setBackgroundDrawable(null);
                        }
                        break;


                    // In the case that it's one of the hyperlink text views to the
                    // previous stages of the add class time process
                    case R.id.class_time_one_value:
                        basisTextviewSelectedListener.onBasisTextviewSelected(FLAG_EDIT, rowID);
                        break;

                    // 1 is the fixed value passed as the basis because the activity itself
                    // (PeriodBased) was launched as a result of the basis being 0
                    case R.id.class_time_two_value:
                        weektypeTextviewSelectedListener.onWeektypeTextViewSelectedListener("1", FLAG_EDIT, rowID);
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
                        if (getArguments().containsKey("occurrence")) {
                            FLAG_EDIT = true;
                            rowID = getArguments().getInt("rowId");
                        }
                        daysSelectedListener.onDaysSelected(classDays, -1, -1,
                                -1, -1, periods, FLAG_EDIT, rowID);
                        break;

                    case R.id.class_three_cancel:
                        dismiss();
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
        String periods =  isPeriodChecked[0] + ":"
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

        return periods;
    }

}
