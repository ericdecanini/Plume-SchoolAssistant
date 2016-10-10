package com.pdt.plume;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class AddClassTimeThreeFragmentBlock extends DialogFragment {
    // Constantly Used Variables
    String LOG_TAG = AddClassTimeThreeFragmentBlock.class.getSimpleName();

    // UI Elements
    TextView periodOneA;
    TextView periodTwoA;
    TextView periodThreeA;
    TextView periodFourA;

    TextView periodOneB;
    TextView periodTwoB;
    TextView periodThreeB;
    TextView periodFourB;

    int mPrimaryColor;
    int mSecondaryColor;

    // Fragment input storage variables
    String[] isPeriodChecked = {"0", "0", "0", "0"};

    // Interface variables
    onDaysSelectedListener daysSelectedListener;
    onBasisTextviewSelectedListener basisTextviewSelectedListener;

    // Public Constructor
    public static AddClassTimeThreeFragmentBlock newInstance(int title) {
        AddClassTimeThreeFragmentBlock fragment = new AddClassTimeThreeFragmentBlock();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            daysSelectedListener = (onDaysSelectedListener) context;
            basisTextviewSelectedListener = (onBasisTextviewSelectedListener) context;
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
        View rootView = inflater.inflate(R.layout.add_class_time_three_block, container, false);

        // Get references to each UI element
        TextView basisTextView = (TextView) rootView.findViewById(R.id.class_time_one_value);

        periodOneA = (TextView) rootView.findViewById(R.id.class_three_period_one);
        periodTwoA = (TextView) rootView.findViewById(R.id.class_three_period_two);
        periodThreeA = (TextView) rootView.findViewById(R.id.class_three_period_three);
        periodFourA = (TextView) rootView.findViewById(R.id.class_three_period_four);

        periodOneB = (TextView) rootView.findViewById(R.id.class_three_period_one_alt);
        periodTwoB = (TextView) rootView.findViewById(R.id.class_three_period_two_alt);
        periodThreeB = (TextView) rootView.findViewById(R.id.class_three_period_three_alt);
        periodFourB = (TextView) rootView.findViewById(R.id.class_three_period_four_alt);

        Button done = (Button) rootView.findViewById(R.id.class_three_done);

        // Set OnClickListeners to each UI element
        basisTextView.setOnClickListener(listener());

        periodOneA.setOnClickListener(listener());
        periodTwoA.setOnClickListener(listener());
        periodThreeA.setOnClickListener(listener());
        periodFourA.setOnClickListener(listener());

        periodOneB.setOnClickListener(listener());
        periodTwoB.setOnClickListener(listener());
        periodThreeB.setOnClickListener(listener());
        periodFourB.setOnClickListener(listener());

        done.setOnClickListener(listener());

        // Initialise the theme
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR),
                getResources().getColor(R.color.colorPrimary));
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR),
                getResources().getColor(R.color.colorAccent));

        // Set text of hyperlink basis text
        basisTextView.setText(getString(R.string.class_time_one_blockbased));
        basisTextView.setTextColor(PreferenceManager.getDefaultSharedPreferences(getContext()).
                getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent)));

        // Initialise the theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            periodOneA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            periodTwoA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            periodThreeA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            periodFourA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));

            periodOneB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            periodTwoB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            periodThreeB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            periodFourB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
        }

        // Get the arguments to check if the fragment was launched through an
        // ItemClick action from the NewScheduleActivity's list view
        Bundle args = getArguments();
        if (args.containsKey("occurrence")){
            // Get the data from the arguments bundle
            String occurrence = args.getString("occurrence", "-1");
            String[] splitOccurrence = occurrence.split(":");
            String period = args.getString("period", "-1");
            String[] splitPeriod = period.split(":");

            // Check each item in the period string's binary
            // and set it in the activity
            if (splitPeriod[0].equals("1") || splitPeriod[0].equals("3")) {
                isPeriodChecked[0] = "1";
                periodOneA.setTextColor(mSecondaryColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    periodOneA.setBackgroundTintList(null);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) periodOneA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            if (splitPeriod[1].equals("1") || splitPeriod[1].equals("3")) {
                isPeriodChecked[1] = "1";
                periodTwoA.setTextColor(mSecondaryColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    periodTwoA.setBackgroundTintList(null);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) periodTwoA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            if (splitPeriod[2].equals("1") || splitPeriod[2].equals("3")) {
                isPeriodChecked[2] = "1";
                periodThreeA.setTextColor(mSecondaryColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    periodThreeA.setBackgroundTintList(null);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) periodThreeA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            if (splitPeriod[3].equals("1") || splitPeriod[3].equals("3")) {
                isPeriodChecked[3] = "1";
                periodThreeA.setTextColor(mSecondaryColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    periodFourA.setBackgroundTintList(null);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) periodFourA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));

            // Do so as well for its alternate layout if it is available
            if (splitOccurrence[1].equals("1")) {
                if (splitPeriod[0].equals("2") || splitPeriod[0].equals("3")) {
                    if (isPeriodChecked[0].equals("1"))
                        isPeriodChecked[0] = "3";
                    else isPeriodChecked[0] = "2";
                    periodOneB.setTextColor(mSecondaryColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        periodOneB.setBackgroundTintList(null);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) periodOneB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                if (splitPeriod[1].equals("2") || splitPeriod[1].equals("3")) {
                    if (isPeriodChecked[1].equals("1"))
                        isPeriodChecked[1] = "3";
                    else isPeriodChecked[1] = "2";
                    periodTwoB.setTextColor(mSecondaryColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        periodTwoB.setBackgroundTintList(null);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) periodTwoB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                if (splitPeriod[2].equals("2") || splitPeriod[2].equals("3")) {
                    if (isPeriodChecked[2].equals("1"))
                        isPeriodChecked[2] = "3";
                    else isPeriodChecked[2] = "2";
                    periodThreeB.setTextColor(mSecondaryColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        periodThreeB.setBackgroundTintList(null);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) periodThreeB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                if (splitPeriod[3].equals("2") || splitPeriod[3].equals("3")) {
                    if (isPeriodChecked[3].equals("1"))
                        isPeriodChecked[3] = "3";
                    else isPeriodChecked[3] = "2";
                    periodFourB.setTextColor(mSecondaryColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        periodFourB.setBackgroundTintList(null);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) periodFourB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
            }
        }

        return rootView;
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    // In the case that a period button was selected
                    case R.id.class_three_period_one:
                        if (isPeriodChecked[0].equals("0")) {
                            isPeriodChecked[0] = "1";
                            periodOneA.setTextColor(mSecondaryColor);
                            periodOneA.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodOneA.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[0].equals("1")) {
                            isPeriodChecked[0] = "0";
                            periodOneA.setTextColor(getResources().getColor(R.color.white));
                            periodOneA.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodOneA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        else if (isPeriodChecked[0].equals("2")) {
                            isPeriodChecked[0] = "3";
                            periodOneA.setTextColor(mSecondaryColor);
                            periodOneA.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodOneA.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[0].equals("3")) {
                            isPeriodChecked[0] = "2";
                            periodOneA.setTextColor(getResources().getColor(R.color.white));
                            periodOneA.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodOneA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        break;
                    case R.id.class_three_period_two:
                        if (isPeriodChecked[1].equals("0")) {
                            isPeriodChecked[1] = "1";
                            periodTwoA.setTextColor(mSecondaryColor);
                            periodTwoA.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodTwoA.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[1].equals("1")) {
                            isPeriodChecked[1] = "0";
                            periodTwoA.setTextColor(getResources().getColor(R.color.white));
                            periodTwoA.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodTwoA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        else if (isPeriodChecked[1].equals("2")) {
                            isPeriodChecked[1] = "3";
                            periodTwoA.setTextColor(mSecondaryColor);
                            periodTwoA.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodTwoA.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[1].equals("3")) {
                            isPeriodChecked[1] = "2";
                            periodTwoA.setTextColor(getResources().getColor(R.color.white));
                            periodTwoA.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodTwoA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        break;
                    case R.id.class_three_period_three:
                        if (isPeriodChecked[2].equals("0")) {
                            isPeriodChecked[2] = "1";
                            periodThreeA.setTextColor(mSecondaryColor);
                            periodThreeA.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodThreeA.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[2].equals("1")) {
                            isPeriodChecked[2] = "0";
                            periodThreeA.setTextColor(getResources().getColor(R.color.white));
                            periodThreeA.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodThreeA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        else if (isPeriodChecked[2].equals("2")) {
                            isPeriodChecked[2] = "3";
                            periodThreeA.setTextColor(mSecondaryColor);
                            periodThreeA.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodThreeA.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[2].equals("3")) {
                            isPeriodChecked[2] = "2";
                            periodThreeA.setTextColor(getResources().getColor(R.color.white));
                            periodThreeA.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodThreeA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        break;
                    case R.id.class_three_period_four:
                        if (isPeriodChecked[3].equals("0")) {
                            isPeriodChecked[3] = "1";
                            periodFourA.setTextColor(mSecondaryColor);
                            periodFourA.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodFourA.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[3].equals("1")) {
                            isPeriodChecked[3] = "0";
                            periodFourA.setTextColor(getResources().getColor(R.color.white));
                            periodFourA.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodFourA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        else if (isPeriodChecked[3].equals("2")) {
                            isPeriodChecked[3] = "3";
                            periodFourA.setTextColor(mSecondaryColor);
                            periodFourA.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodFourA.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[3].equals("3")) {
                            isPeriodChecked[3] = "2";
                            periodFourA.setTextColor(getResources().getColor(R.color.white));
                            periodFourA.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodFourA.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        break;

                    // In the case that an alternate period button was selected
                    case R.id.class_three_period_one_alt:
                        if (isPeriodChecked[0].equals("0")) {
                            isPeriodChecked[0] = "2";
                            periodOneB.setTextColor(mSecondaryColor);
                            periodOneB.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodOneB.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[0].equals("1")) {
                            isPeriodChecked[0] = "3";
                            periodOneB.setTextColor(mSecondaryColor);
                            periodOneB.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodOneB.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[0].equals("2")) {
                            isPeriodChecked[0] = "0";
                            periodOneB.setTextColor(getResources().getColor(R.color.white));
                            periodOneB.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodOneB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        else if (isPeriodChecked[0].equals("3")) {
                            isPeriodChecked[0] = "1";
                            periodOneB.setTextColor(getResources().getColor(R.color.white));
                            periodOneB.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodOneB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        break;
                    case R.id.class_three_period_two_alt:
                        if (isPeriodChecked[1].equals("0")) {
                            isPeriodChecked[1] = "1";
                            periodTwoB.setTextColor(mSecondaryColor);
                            periodTwoB.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodTwoB.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[1].equals("1")) {
                            isPeriodChecked[1] = "3";
                            periodTwoB.setTextColor(mSecondaryColor);
                            periodTwoB.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodTwoB.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[1].equals("2")) {
                            isPeriodChecked[1] = "0";
                            periodTwoB.setTextColor(getResources().getColor(R.color.white));
                            periodTwoB.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodTwoB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        else if (isPeriodChecked[1].equals("3")) {
                            isPeriodChecked[1] = "1";
                            periodTwoB.setTextColor(getResources().getColor(R.color.white));
                            periodTwoB.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodTwoB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        break;
                    case R.id.class_three_period_three_alt:
                        if (isPeriodChecked[2].equals("0")) {
                            isPeriodChecked[2] = "2";
                            periodThreeB.setTextColor(mSecondaryColor);
                            periodThreeB.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodThreeB.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[2].equals("1")) {
                            isPeriodChecked[2] = "3";
                            periodThreeB.setTextColor(mSecondaryColor);
                            periodThreeB.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodThreeB.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[2].equals("2")) {
                            isPeriodChecked[2] = "0";
                            periodThreeB.setTextColor(getResources().getColor(R.color.white));
                            periodThreeB.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodThreeB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        else if (isPeriodChecked[2].equals("3")) {
                            isPeriodChecked[2] = "1";
                            periodThreeB.setTextColor(getResources().getColor(R.color.white));
                            periodThreeB.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodThreeB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        break;
                    case R.id.class_three_period_four_alt:
                        if (isPeriodChecked[3].equals("0")) {
                            isPeriodChecked[3] = "2";
                            periodFourB.setTextColor(mSecondaryColor);
                            periodFourB.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodFourB.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[3].equals("1")) {
                            isPeriodChecked[3] = "3";
                            periodFourB.setTextColor(mSecondaryColor);
                            periodFourB.setBackgroundResource(R.drawable.bg_period_button_active);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodFourB.setBackgroundTintList(null);
                            }
                        }
                        else if (isPeriodChecked[3].equals("2")) {
                            isPeriodChecked[3] = "0";
                            periodFourB.setTextColor(getResources().getColor(R.color.white));

                            periodFourB.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodFourB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        else if (isPeriodChecked[3].equals("3")) {
                            isPeriodChecked[3] = "1";
                            periodFourB.setTextColor(getResources().getColor(R.color.white));

                            periodFourB.setBackgroundResource(R.drawable.bg_period_button);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                periodFourB.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));
                            }
                        }
                        break;

                    // In the case that the hyperlink basis text view was selected
                    case R.id.class_time_one_value:
                        basisTextviewSelectedListener.onBasisTextviewSelected();
                        break;

                    case R.id.class_three_done:
                        String periods = processPeriodsString();

                        // Validate that at least one period has been selected
                        if (periods.equals("0:0:0:0")) {
                            Toast.makeText(getContext(), getString(R.string.new_schedule_toast_validation_no_period_selected),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // If the fragment was started through the list view's OnItemClick
                        // Pass true as the Edit Flag to tell the activity to update instead
                        // the occurrence item instead of adding a new item
                        boolean FLAG_EDIT = getArguments().containsKey("occurrence");
                        int rowId = getArguments().getInt("rowId");
                        daysSelectedListener.onDaysSelected("-1", -1, -1,
                                -1, -1, periods, FLAG_EDIT, rowId);
                        break;
                }
            }
        };
    }

    private String processPeriodsString(){
        // Creates the convertible period string
        // for database storage
        return isPeriodChecked[0] + ":"
                + isPeriodChecked[1] + ":"
                + isPeriodChecked[2] + ":"
                + isPeriodChecked[3];
    }

}
