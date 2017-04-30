package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Random;


public class FloatingIcon extends android.support.v7.widget.AppCompatImageView {

    String LOG_TAG = FloatingIcon.class.getSimpleName();

    boolean movingLeft = false;
    int anchorX = 0;
    int x = 0;
    int y = 0;

    public FloatingIcon(Context context) {
        super(context);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        y = height;
    }

    public void generateLocation(int xRange, int yRange, int height) {
        Random random = new Random();
        anchorX = random.nextInt(xRange);
        x = anchorX;
        y = random.nextInt(yRange) + height;
    }

}
