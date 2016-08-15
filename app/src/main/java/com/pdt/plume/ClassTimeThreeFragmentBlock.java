package com.pdt.plume;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class ClassTimeThreeFragmentBlock extends DialogFragment {

    // Fragment input storage variables
    String[] isPeriodChecked = {"0", "0", "0", "0"};

    // Interface variables
    onDaysSelectedListener daysSelectedListener;
    onBasisTextviewSelectedListener basisTextviewSelectedListener;

    // Public Constructor
    public static ClassTimeThreeFragmentBlock newInstance(int title) {
        ClassTimeThreeFragmentBlock fragment = new ClassTimeThreeFragmentBlock();
        Bundle args = new Bundle();
        args.putInt("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    // Interfaces used to pass data to NewScheduleActivity
    public interface onDaysSelectedListener {
        //Pass all data through input params here
        public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds, int timeInAltSeconds, int timeOutAltSeconds, String periods);
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
        View rootView = inflater.inflate(R.layout.class_time_three_block, container, false);

        // Get references to each UI element
        TextView basisTextView = (TextView) rootView.findViewById(R.id.class_time_one_value);

        Button periodOneA = (Button) rootView.findViewById(R.id.class_three_period_one);
        Button periodTwoA = (Button) rootView.findViewById(R.id.class_three_period_two);
        Button periodThreeA = (Button) rootView.findViewById(R.id.class_three_period_three);
        Button periodFourA = (Button) rootView.findViewById(R.id.class_three_period_four);

        Button periodOneB = (Button) rootView.findViewById(R.id.class_three_period_one_alt);
        Button periodTwoB = (Button) rootView.findViewById(R.id.class_three_period_two_alt);
        Button periodThreeB = (Button) rootView.findViewById(R.id.class_three_period_three_alt);
        Button periodFourB = (Button) rootView.findViewById(R.id.class_three_period_four_alt);

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

        // Set text of hyperlink basis text
        basisTextView.setText(getString(R.string.class_time_one_blockbased));

        return rootView;
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    // In the case that a period button was selected
                    case R.id.class_three_period_one:
                        if (isPeriodChecked[0].equals("0"))
                            isPeriodChecked[0] = "1";
                        else if (isPeriodChecked[0].equals("1"))
                            isPeriodChecked[0] = "0";
                        else if (isPeriodChecked[0].equals("2"))
                            isPeriodChecked[0] = "3";
                        else if (isPeriodChecked[0].equals("3"))
                            isPeriodChecked[0] = "2";
                        break;
                    case R.id.class_three_period_two:
                        if (isPeriodChecked[1].equals("0"))
                            isPeriodChecked[1] = "1";
                        else if (isPeriodChecked[1].equals("1"))
                            isPeriodChecked[1] = "0";
                        else if (isPeriodChecked[1].equals("2"))
                            isPeriodChecked[1] = "3";
                        else if (isPeriodChecked[1].equals("3"))
                            isPeriodChecked[1] = "2";
                        break;
                    case R.id.class_three_period_three:
                        if (isPeriodChecked[2].equals("0"))
                            isPeriodChecked[2] = "1";
                        else if (isPeriodChecked[2].equals("1"))
                            isPeriodChecked[2] = "0";
                        else if (isPeriodChecked[2].equals("2"))
                            isPeriodChecked[2] = "3";
                        else if (isPeriodChecked[2].equals("3"))
                            isPeriodChecked[2] = "2";
                        break;
                    case R.id.class_three_period_four:
                        if (isPeriodChecked[3].equals("0"))
                            isPeriodChecked[3] = "1";
                        else if (isPeriodChecked[3].equals("1"))
                            isPeriodChecked[3] = "0";
                        else if (isPeriodChecked[3].equals("2"))
                            isPeriodChecked[3] = "3";
                        else if (isPeriodChecked[3].equals("3"))
                            isPeriodChecked[3] = "2";
                        break;

                    // In the case that an alternate period button was selected
                    case R.id.class_three_period_one_alt:
                        if (isPeriodChecked[0].equals("0"))
                            isPeriodChecked[0] = "2";
                        else if (isPeriodChecked[0].equals("1"))
                            isPeriodChecked[0] = "3";
                        else if (isPeriodChecked[0].equals("2"))
                            isPeriodChecked[0] = "0";
                        else if (isPeriodChecked[0].equals("3"))
                            isPeriodChecked[0] = "1";
                        break;
                    case R.id.class_three_period_two_alt:
                        if (isPeriodChecked[1].equals("0"))
                            isPeriodChecked[1] = "12";
                        else if (isPeriodChecked[1].equals("1"))
                            isPeriodChecked[1] = "3";
                        else if (isPeriodChecked[1].equals("2"))
                            isPeriodChecked[1] = "0";
                        else if (isPeriodChecked[1].equals("3"))
                            isPeriodChecked[1] = "1";
                        break;
                    case R.id.class_three_period_three_alt:
                        if (isPeriodChecked[2].equals("0"))
                            isPeriodChecked[2] = "2";
                        else if (isPeriodChecked[2].equals("1"))
                            isPeriodChecked[2] = "3";
                        else if (isPeriodChecked[2].equals("2"))
                            isPeriodChecked[2] = "0";
                        else if (isPeriodChecked[2].equals("3"))
                            isPeriodChecked[2] = "1";
                        break;
                    case R.id.class_three_period_four_alt:
                        if (isPeriodChecked[3].equals("0"))
                            isPeriodChecked[3] = "2";
                        else if (isPeriodChecked[3].equals("1"))
                            isPeriodChecked[3] = "3";
                        else if (isPeriodChecked[3].equals("2"))
                            isPeriodChecked[3] = "0";
                        else if (isPeriodChecked[3].equals("3"))
                            isPeriodChecked[3] = "1";
                        break;

                    // In the case that the hyperlink basis text view was selected
                    case R.id.class_time_one_value:
                        basisTextviewSelectedListener.onBasisTextviewSelected();
                        break;

                    case R.id.class_three_done:
                        String periods = processPeriodsString();
                        daysSelectedListener.onDaysSelected("-1", -1, -1, -1, -1, periods);
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
                + isPeriodChecked[3] + ":";
    }

}
