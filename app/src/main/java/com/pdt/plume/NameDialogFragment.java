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
import android.widget.LinearLayout;


public class NameDialogFragment extends DialogFragment {

    EditText nameField;

    // Public Constructor
    public static NameDialogFragment newInstance(String name) {
        NameDialogFragment fragment = new NameDialogFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        fragment.setArguments(args);
        return fragment;
    }

    public interface onNameSelectedListener {
        //Pass all data through input params here
        public void onNameSelected(String name);
    }

    onNameSelectedListener nameSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            nameSelectedListener = (onNameSelectedListener) context;
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
        View rootView = inflater.inflate(R.layout.dialog_name, container, false);

        nameField = (EditText) rootView.findViewById(R.id.editText);

        rootView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameSelectedListener.onNameSelected(nameField.getText().toString());
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
