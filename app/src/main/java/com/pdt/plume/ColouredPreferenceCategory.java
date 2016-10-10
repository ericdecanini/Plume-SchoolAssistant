package com.pdt.plume;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by user on 24/09/2016.
 */
public class ColouredPreferenceCategory extends PreferenceCategory {

    public ColouredPreferenceCategory(Context context) {
        super(context);
    }

    public ColouredPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColouredPreferenceCategory(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            titleView.setTextColor(getContext().getColor(R.color.colorPrimary));
        }
    }

}
