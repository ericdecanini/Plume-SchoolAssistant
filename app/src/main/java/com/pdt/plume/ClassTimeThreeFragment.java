package com.pdt.plume;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class ClassTimeThreeFragment extends Fragment {

    int[] isButtonChecked = {0, 0, 0, 0, 0, 0, 0};


    public ClassTimeThreeFragment() {
        // Required empty public constructor
    }

    public interface onDaysSelectedListener {
        //Pass all data through input params here
        public void onDaysSelected(String classDays);
    }

    onDaysSelectedListener daysSelectedListener;

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
        View rootView = inflater.inflate(R.layout.class_time_three, container, false);
        Button sunday = (Button) rootView.findViewById(R.id.class_three_sunday);
        Button monday = (Button) rootView.findViewById(R.id.class_three_monday);
        Button tuesday = (Button) rootView.findViewById(R.id.class_three_tuesday);
        Button wednesday = (Button) rootView.findViewById(R.id.class_three_wednesday);
        Button thursday = (Button) rootView.findViewById(R.id.class_three_thursday);
        Button friday = (Button) rootView.findViewById(R.id.class_three_friday);
        Button saturday = (Button) rootView.findViewById(R.id.class_three_saturday);
        Button done = (Button) rootView.findViewById(R.id.class_three_done);

        sunday.setOnClickListener(listener());
        monday.setOnClickListener(listener());
        tuesday.setOnClickListener(listener());
        wednesday.setOnClickListener(listener());
        thursday.setOnClickListener(listener());
        friday.setOnClickListener(listener());
        saturday.setOnClickListener(listener());
        done.setOnClickListener(listener());
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
                        daysSelectedListener.onDaysSelected(classDays);
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

}
