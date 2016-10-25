package com.pdt.plume;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class ColouredPreferenceCategory extends PreferenceCategory {

    Context c;

    public ColouredPreferenceCategory(Context context) {
        super(context);
        c = context;
    }

    public ColouredPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
    }

    public ColouredPreferenceCategory(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        c = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        int mSecondaryColor;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        mSecondaryColor = preferences.getInt(c.getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent);
        titleView.setTextColor(mSecondaryColor);
    }

}
