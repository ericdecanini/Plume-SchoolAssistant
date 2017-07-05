package com.pdt.plume;


import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A blank fragment used the tablet's master-detail flow
 * if the Schedule/Tasks pane has no items
 */
public class BlankFragment extends Fragment {

    public BlankFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);
        int backgroundColor = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), getResources().getColor(R.color.backgroundColor));
        float[] hsv = new float[3];
        Color.colorToHSV(backgroundColor, hsv);
        hsv[2] *= 0.9f;
        int darkBackgroundColor = Color.HSVToColor(hsv);
        rootView.findViewById(R.id.container).setBackgroundColor(darkBackgroundColor);
        return rootView;
    }

}
