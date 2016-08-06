package com.pdt.plume;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class ClassTimeTwoFragment extends Fragment {


    public ClassTimeTwoFragment() {
        // Required empty public constructor
    }

    public interface onWeekTypeSelectedListener {
        //Pass all data through input params here
        public void onWeekTypeSelected(String weekType);
    }

    onWeekTypeSelectedListener weekTypeSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            weekTypeSelectedListener = (onWeekTypeSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.class_time_two, container, false);
        Button weekSameButton = (Button) rootView.findViewById(R.id.class_two_weeksame);
        Button weekAltButton = (Button) rootView.findViewById(R.id.class_two_weekalt);

        weekSameButton.setOnClickListener(listener());
        weekAltButton.setOnClickListener(listener());
        return rootView;
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.class_two_weeksame:
                        weekTypeSelectedListener.onWeekTypeSelected("0");
                        break;
                    case R.id.class_two_weekalt:
                        weekTypeSelectedListener.onWeekTypeSelected("1");
                        break;
                }
            }
        };
    }


}
