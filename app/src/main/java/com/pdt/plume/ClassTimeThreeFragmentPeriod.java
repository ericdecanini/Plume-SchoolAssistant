package com.pdt.plume;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class ClassTimeThreeFragmentPeriod extends Fragment {

    int[] isButtonChecked = {0, 0, 0, 0, 0, 0, 0};
    String[] isPeriodChecked = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
    String[] isPeriodAltChecked = {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
    onDaysSelectedListener daysSelectedListener;


    public ClassTimeThreeFragmentPeriod() {
        // Required empty public constructor
    }

    public interface onDaysSelectedListener {
        //Pass all data through input params here
        public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds, int timeInAltSeconds, int timeOutAltSeconds, String periods, String periodsAlt);
    }

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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.class_time_three_period, container, false);

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

        Button periodOne = (Button) rootView.findViewById(R.id.class_three_period_one);
        Button periodTwo = (Button) rootView.findViewById(R.id.class_three_period_two);
        Button periodThree = (Button) rootView.findViewById(R.id.class_three_period_three);
        Button periodFour = (Button) rootView.findViewById(R.id.class_three_period_four);
        Button periodFive = (Button) rootView.findViewById(R.id.class_three_period_five);
        Button periodSix = (Button) rootView.findViewById(R.id.class_three_period_six);
        Button periodSeven = (Button) rootView.findViewById(R.id.class_three_period_seven);
        Button periodEight = (Button) rootView.findViewById(R.id.class_three_period_eight);
        Button periodNine = (Button) rootView.findViewById(R.id.class_three_period_nine);
        Button periodTen = (Button) rootView.findViewById(R.id.class_three_period_ten);
        Button periodEleven = (Button) rootView.findViewById(R.id.class_three_period_eleven);
        Button periodTwelve = (Button) rootView.findViewById(R.id.class_three_period_twelve);

        Button periodOneAlt = (Button) rootView.findViewById(R.id.class_three_period_one_alt);
        Button periodTwoAlt = (Button) rootView.findViewById(R.id.class_three_period_two_alt);
        Button periodThreeAlt = (Button) rootView.findViewById(R.id.class_three_period_three_alt);
        Button periodFourAlt = (Button) rootView.findViewById(R.id.class_three_period_four_alt);
        Button periodFiveAlt = (Button) rootView.findViewById(R.id.class_three_period_five_alt);
        Button periodSixAlt = (Button) rootView.findViewById(R.id.class_three_period_six_alt);
        Button periodSevenAlt = (Button) rootView.findViewById(R.id.class_three_period_seven_alt);
        Button periodEightAlt = (Button) rootView.findViewById(R.id.class_three_period_eight_alt);
        Button periodNineAlt = (Button) rootView.findViewById(R.id.class_three_period_nine_alt);
        Button periodTenAlt = (Button) rootView.findViewById(R.id.class_three_period_ten_alt);
        Button periodElevenAlt = (Button) rootView.findViewById(R.id.class_three_period_eleven_alt);
        Button periodTwelveAlt = (Button) rootView.findViewById(R.id.class_three_period_twelve_alt);

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

        Bundle args = getArguments();
        if (args != null){
            if (!args.getString("weekType", "-1").equals("1"))
                //Change the layout based on weekType
                rootView.findViewById(R.id.class_time_three_week_type_alt_layout).setVisibility(View.GONE);
        }

        return rootView;
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
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

                    case R.id.class_three_done:
                        String classDays = processClassDaysString();
                        String periods = processPeriodsString();
                        String periodsAlt = processPeriodsAltString();
                        daysSelectedListener.onDaysSelected(classDays, -1, -1, -1, -1, periods, periodsAlt);
                        break;
                }
            }
        };
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

    private String processPeriodsString(){
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

    private String processPeriodsAltString(){
        return isPeriodAltChecked[0] + ":"
                + isPeriodAltChecked[1] + ":"
                + isPeriodAltChecked[2] + ":"
                + isPeriodAltChecked[3] + ":"
                + isPeriodAltChecked[4] + ":"
                + isPeriodAltChecked[5] + ":"
                + isPeriodAltChecked[6] + ":"
                + isPeriodAltChecked[7] + ":"
                + isPeriodAltChecked[8] + ":"
                + isPeriodAltChecked[9] + ":"
                + isPeriodAltChecked[10] + ":"
                + isPeriodAltChecked[11];
    }

}
