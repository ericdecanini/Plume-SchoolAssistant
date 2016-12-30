package com.pdt.plume;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class IconPromptDialog extends DialogFragment {

    public interface iconDialogListener{
        void OnIconListItemSelected(int item);
    }

    iconDialogListener dialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            dialogListener = (iconDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    public IconPromptDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the title for the dialog
        builder.setTitle(getString(R.string.new_schedule_icon_list_title))
                // Set up the items for the list
                .setItems(R.array.new_schedule_icon_list_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialogListener.OnIconListItemSelected(i);
                    }
                });
        return builder.create();
    }
}
