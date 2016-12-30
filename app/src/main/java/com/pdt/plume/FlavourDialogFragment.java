package com.pdt.plume;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;


public class FlavourDialogFragment extends DialogFragment {

    EditText flavourField;

    // Public Constructor
    public static FlavourDialogFragment newInstance(String flavour) {
        FlavourDialogFragment fragment = new FlavourDialogFragment();
        Bundle args = new Bundle();
        args.putString("flavour", flavour);
        fragment.setArguments(args);
        return fragment;
    }

    public interface onFlavourSelectedListener {
        //Pass all data through input params here
        public void onFlavourSelected(String flavour);
    }

    onFlavourSelectedListener flavourSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            flavourSelectedListener = (onFlavourSelectedListener) context;
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
        View rootView = inflater.inflate(R.layout.dialog_flavour, container, false);
        flavourField = (EditText) rootView.findViewById(R.id.editText);

        // Automatically inflate the EditText with the name
        Bundle args = getArguments();
        if (args != null) {
            flavourField.setText(args.getString("name"));
        }

        // Set the ItemClickListener on the buttons
        rootView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flavourSelectedListener.onFlavourSelected(flavourField.getText().toString());
                dismiss();
            }
        });

        rootView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return rootView;
    }

}
