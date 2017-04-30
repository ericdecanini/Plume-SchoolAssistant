package com.pdt.plume;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;


public class DaysDialog extends DialogFragment {

    String LOG_TAG = DaysDialog.class.getSimpleName();
    int position = 0;
    boolean alternate = false;

    // UI Elements
    CheckBox[] checkbox = {null, null, null, null, null, null, null};

    OnDaysSelectedListener onDaysSelectedListener;
    public interface OnDaysSelectedListener {
        void OnDaysSelected(boolean alternate, int position, String days);
    }

    public static DaysDialog newInstance() {
        DaysDialog fragment = new DaysDialog();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onDaysSelectedListener = (OnDaysSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onDaysSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.dialog_days, container);

        View[] days = {null, null, null, null, null, null, null};
        days[0] = rootview.findViewById(R.id.sunday);
        days[1] = rootview.findViewById(R.id.monday);
        days[2] = rootview.findViewById(R.id.tuesday);
        days[3] = rootview.findViewById(R.id.wednesday);
        days[4] = rootview.findViewById(R.id.thursday);
        days[5] = rootview.findViewById(R.id.friday);
        days[6] = rootview.findViewById(R.id.saturday);
        View cancel = rootview.findViewById(R.id.cancel);
        View ok = rootview.findViewById(R.id.ok);

        checkbox[0] = (CheckBox) rootview.findViewById(R.id.sunday_checkbox);
        checkbox[1] = (CheckBox) rootview.findViewById(R.id.monday_checkbox);
        checkbox[2] = (CheckBox) rootview.findViewById(R.id.tuesday_checkbox);
        checkbox[3] = (CheckBox) rootview.findViewById(R.id.wednesday_checkbox);
        checkbox[4] = (CheckBox) rootview.findViewById(R.id.thursday_checkbox);
        checkbox[5] = (CheckBox) rootview.findViewById(R.id.friday_checkbox);
        checkbox[6] = (CheckBox) rootview.findViewById(R.id.saturday_checkbox);

        // Attach the listeners to each of the list items
        for (int i = 0; i < days.length; i++)
            days[i].setOnClickListener(listener);
        cancel.setOnClickListener(listener);
        ok.setOnClickListener(listener);

        Bundle args = getArguments();
        if (args != null) {
            alternate = args.getBoolean(getString(R.string.ARGUMENT_ALTERNATE), false);
            position = args.getInt(getString(R.string.ARGUMENT_POSITION), 0);

            // Automatically check fields based on arguments passed
            String[] daysArray = args.getString(getString(R.string.ARGUMENT_DAYS)).split(":");
            for (int i = 0; i < daysArray.length; i++)
                if (daysArray[i].equals("1"))
                    checkbox[i].toggle();

        }

        return rootview;
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.sunday:
                    checkbox[0].toggle();
                    break;
                case R.id.monday:
                    checkbox[1].toggle();
                    break;
                case R.id.tuesday:
                    checkbox[2].toggle();
                    break;
                case R.id.wednesday:
                    checkbox[3].toggle();
                    break;
                case R.id.thursday:
                    checkbox[4].toggle();
                    break;
                case R.id.friday:
                    checkbox[5].toggle();
                    break;
                case R.id.saturday:
                    checkbox[6].toggle();
                    break;
                case R.id.cancel:
                    dismiss();
                    break;
                case R.id.ok:
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < checkbox.length; i++) {
                        if (i != 0) builder.append(":");
                        if (checkbox[i].isChecked()) builder.append("1");
                        else builder.append("0");
                    }
                    onDaysSelectedListener.OnDaysSelected(alternate, position , builder.toString());
                    dismiss();
                    break;
            }
        }
    };

}
