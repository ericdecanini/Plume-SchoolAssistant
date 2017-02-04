package com.pdt.plume;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;


public class MismatchDialog extends DialogFragment {

    String LOG_TAG = MismatchDialog.class.getSimpleName();

    public interface MismatchDialogListener {
        void OnClassesMatchedListener(ArrayList<Bundle> matchedClasses);
    }

    MismatchDialogListener dialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            dialogListener = (MismatchDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    // Public Constructor
    public static MismatchDialog newInstance() {
        MismatchDialog fragment = new MismatchDialog();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_accept_peer_match, container);

        Bundle args = getArguments();
        ArrayList<Bundle> mismatchedClassesList = (ArrayList<Bundle>) args.getSerializable("mismatchedClassesList");

        final ListView listView = (ListView) rootView.findViewById(R.id.listView);
        final MismatchListAdapter adapter = new MismatchListAdapter(getContext(), R.layout.list_item_mismatch, mismatchedClassesList);
        listView.setAdapter(adapter);

        Button button = (Button) rootView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Bundle> matchedClasses = new ArrayList<>();
                for (int i = 0; i < adapter.getCount(); i++) {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", ((String) getViewByPosition(i, listView).findViewById(R.id.field_class_textview).getTag()));
                    bundle.putString("icon", ((String) getViewByPosition(i, listView).findViewById(R.id.field_class_dropdown).getTag()));
                    matchedClasses.add(bundle);
                }
                dialogListener.OnClassesMatchedListener(matchedClasses);
                dismiss();
            }
        });

        return rootView;
    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
    
}
