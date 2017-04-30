package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.Random;

import static android.R.attr.max;
import static android.R.attr.width;
import static android.R.attr.x;
import static android.R.attr.y;


public class IntroSurface extends SurfaceView implements SurfaceHolder.Callback {

    String LOG_TAG = IntroSurface.class.getSimpleName();
    Handler h = new Handler();
    int FRAME_RATE = 3;

    private Integer[] mThumbIds = {
            R.drawable.art_arts_64dp,
            R.drawable.art_biology_64dp,
            R.drawable.art_business_64dp,
            R.drawable.art_chemistry_64dp,
            R.drawable.art_childdevelopment_64dp,
            R.drawable.art_class_64dp,
            R.drawable.art_computing_64dp,
            R.drawable.art_cooking_64dp,
            R.drawable.art_creativestudies_64dp,
            R.drawable.art_drama_64dp,
            R.drawable.art_engineering_64dp,
            R.drawable.art_english_64dp,
            R.drawable.art_french_64dp,
            R.drawable.art_geography_64dp,
            R.drawable.art_graphics_64dp,
            R.drawable.art_hospitality_64dp,
            R.drawable.art_ict_64dp,
            R.drawable.art_maths_64dp,
            R.drawable.art_media_64dp,
            R.drawable.art_music_64dp,
            R.drawable.art_pe_64dp,
            R.drawable.art_physics_64dp,
            R.drawable.art_psychology_64dp,
            R.drawable.art_re_64dp,
            R.drawable.art_science_64dp,
            R.drawable.art_spanish_64dp,
            R.drawable.art_task_64dp,
            R.drawable.art_woodwork_64dp
    };

    private Bitmap[] mBitmaps = new Bitmap[28];
    private FloatingIcon[] floatingIcons = new FloatingIcon[28];

    IntroThread _thread;

    public IntroSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float density = displayMetrics.density;
        final int width = displayMetrics.widthPixels;
        final int height = displayMetrics.heightPixels;

        final int[] i = {0};
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBitmaps[i[0]] = BitmapFactory.decodeResource(getResources(), mThumbIds[i[0]]);
                floatingIcons[i[0]] = new FloatingIcon(getContext());
                floatingIcons[i[0]].setImageResource(mThumbIds[i[0]]);
                floatingIcons[i[0]].generateLocation(width, height, 0);

                i[0]++;
                if (i[0] <= floatingIcons.length -1)
                    h.postDelayed(this, 50);
            }
        }, 50);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(50);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        float density = displayMetrics.density;
        float maxDistance = 72 * density;
        int height = ((int) (displayMetrics.heightPixels - (128 * density)));

        for (int i = 0; i < floatingIcons.length; i++) {
            if (floatingIcons[i] != null) {
                floatingIcons[i].y = floatingIcons[i].y - 5;

                float distanceFromAnchor = floatingIcons[i].x - floatingIcons[i].anchorX;

                double m = Math.pow(maxDistance, 2.0) - Math.pow(distanceFromAnchor, 2.0);
                if (m < 0)
                    m = m * -1;

                double speed = (Math.sqrt(m) / 40) + 1;

                if (speed < 1)
                    speed = 1;

                if (floatingIcons[i].movingLeft) {
                    floatingIcons[i].x = (int) (floatingIcons[i].x - speed);
                    if (distanceFromAnchor <= -maxDistance) {
                        floatingIcons[i].movingLeft = false;
                    }
                } else {
                    floatingIcons[i].x = (int) (floatingIcons[i].x + speed);
                    if (distanceFromAnchor >= maxDistance) {
                        floatingIcons[i].movingLeft = true;
                    }
                }

                if (i == 0)
                    Log.v(LOG_TAG, "Speed: " + speed + " Distance: " + distanceFromAnchor + " Max: " + maxDistance
                            + " Boolean: " + floatingIcons[i].movingLeft);

                if (floatingIcons[i].y <= 0 - (64 * density)) {
                    floatingIcons[i].generateLocation(width, height, height);
                }
                canvas.drawBitmap(mBitmaps[i], floatingIcons[i].x, floatingIcons[i].y, paint);
            }
        }

        h.postDelayed(r1, FRAME_RATE);
    }

    private Runnable r1 = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        setWillNotDraw(false); //Allows us to use invalidate() to call onDraw()


        _thread = new IntroThread(getHolder(), this); //Start the thread that
        _thread.setRunning(true);                     //will make calls to
        _thread.start();                              //onDraw()
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        try {
            _thread.setRunning(false);                //Tells thread to stop
            _thread.join();                           //Removes thread from mem.
        } catch (InterruptedException e) {}
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
