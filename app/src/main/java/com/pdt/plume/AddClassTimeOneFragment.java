package com.pdt.plume;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;




public class AddClassTimeOneFragment extends DialogFragment {

    boolean FLAG_EDIT = false;
    int rowID = 0;

    // Public Constructor
    public static AddClassTimeOneFragment newInstance(int title) {
        AddClassTimeOneFragment fragment = new AddClassTimeOneFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    public interface onBasisSelectedListener {
        //Pass all data through input params here
        public void onBasisSelected(String basis, boolean FLAG_EDIT, int rowID);
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
    public void onStart() {
        super.onStart();
        // Set the fragment's window size to match the screen
        Window window = this.getDialog().getWindow();
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_class_time_one, container, false);

        // Get references to each UI element
        LinearLayout timeBasedButton = (LinearLayout) rootView.findViewById(R.id.class_one_timebased);
        LinearLayout periodBasedButton = (LinearLayout) rootView.findViewById(R.id.class_one_periodbased);
        LinearLayout blockBasedButton = (LinearLayout) rootView.findViewById(R.id.class_one_blockbased);

        // Set the OnClickListener of each UI element
        timeBasedButton.setOnClickListener(listener());
        periodBasedButton.setOnClickListener(listener());
        blockBasedButton.setOnClickListener(listener());

        if (getArguments().containsKey("FLAG_EDIT")) {
            FLAG_EDIT = getArguments().getBoolean("FLAG_EDIT");
            rowID = getArguments().getInt("rowID");
        }

        return rootView;
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    // Run the interface sending the basis parameter
                    // based on the button selected
                    case R.id.class_one_timebased:
                        basisSelectedListener.onBasisSelected("0", FLAG_EDIT, rowID);
                        break;
                    case R.id.class_one_periodbased:
                        basisSelectedListener.onBasisSelected("1", FLAG_EDIT, rowID);
                        break;
                    case R.id.class_one_blockbased:
                        basisSelectedListener.onBasisSelected("2", FLAG_EDIT, rowID);
                        break;
                }
            }
        };
    }

}
