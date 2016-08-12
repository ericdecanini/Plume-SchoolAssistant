package com.pdt.plume;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


public class ClassTimeThreeFragmentBlock extends Fragment {

    String[] isPeriodChecked = {"0", "0", "0", "0"};
    onDaysSelectedListener daysSelectedListener;


    public ClassTimeThreeFragmentBlock() {
        // Required empty public constructor
    }

    public interface onDaysSelectedListener {
        //Pass all data through input params here
        public void onDaysSelected(String classDays, int timeInSeconds, int timeOutSeconds, int timeInAltSeconds, int timeOutAltSeconds, String periods);
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
        View rootView = inflater.inflate(R.layout.class_time_three_block, container, false);

        Button periodOneA = (Button) rootView.findViewById(R.id.class_three_period_one);
        Button periodTwoA = (Button) rootView.findViewById(R.id.class_three_period_two);
        Button periodThreeA = (Button) rootView.findViewById(R.id.class_three_period_three);
        Button periodFourA = (Button) rootView.findViewById(R.id.class_three_period_four);

        Button periodOneB = (Button) rootView.findViewById(R.id.class_three_period_one_alt);
        Button periodTwoB = (Button) rootView.findViewById(R.id.class_three_period_two_alt);
        Button periodThreeB = (Button) rootView.findViewById(R.id.class_three_period_three_alt);
        Button periodFourB = (Button) rootView.findViewById(R.id.class_three_period_four_alt);

        Button done = (Button) rootView.findViewById(R.id.class_three_done);

        periodOneA.setOnClickListener(listener());
        periodTwoA.setOnClickListener(listener());
        periodThreeA.setOnClickListener(listener());
        periodFourA.setOnClickListener(listener());

        periodOneB.setOnClickListener(listener());
        periodTwoB.setOnClickListener(listener());
        periodThreeB.setOnClickListener(listener());
        periodFourB.setOnClickListener(listener());

        done.setOnClickListener(listener());

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

                    case R.id.class_three_done:
                        String periods = processPeriodsString();
                        daysSelectedListener.onDaysSelected("-1", -1, -1, -1, -1, periods);
                        break;
                }
            }
        };
    }

    private String processPeriodsString(){
        return isPeriodChecked[0] + ":"
                + isPeriodChecked[1] + ":"
                + isPeriodChecked[2] + ":"
                + isPeriodChecked[3] + ":";
    }

}
