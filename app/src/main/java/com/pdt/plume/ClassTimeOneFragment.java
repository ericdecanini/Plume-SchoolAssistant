package com.pdt.plume;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class ClassTimeOneFragment extends Fragment {


    public ClassTimeOneFragment() {
        // Required empty public constructor
    }

    public interface onBasisSelectedListener {
        //Pass all data through input params here
        public void onBasisSelected(String basis);
    }

    onBasisSelectedListener basisSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            basisSelectedListener = (onBasisSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.class_time_one, container, false);
        Button timeBasedButton = (Button) rootView.findViewById(R.id.class_one_timebased);
        Button periodBasedButton = (Button) rootView.findViewById(R.id.class_one_periodbased);
        Button blockBasedButton = (Button) rootView.findViewById(R.id.class_one_blockbased);

        timeBasedButton.setOnClickListener(listener());
        periodBasedButton.setOnClickListener(listener());
        blockBasedButton.setOnClickListener(listener()
        );
        return rootView;
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.class_one_timebased:
                        basisSelectedListener.onBasisSelected("0");
                        break;
                    case R.id.class_one_periodbased:
                        basisSelectedListener.onBasisSelected("1");
                        break;
                    case R.id.class_one_blockbased:
                        basisSelectedListener.onBasisSelected("2");
                        break;
                }
            }
        };
    }

}
