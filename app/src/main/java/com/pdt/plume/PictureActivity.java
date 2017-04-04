package com.pdt.plume;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import static android.R.attr.id;

public class PictureActivity extends AppCompatActivity {

    String LOG_TAG = PictureActivity.class.getSimpleName();
    Handler handler = new Handler();
    boolean actionBarIsShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        // Reference the views
        RelativeLayout masterLayout = (RelativeLayout) findViewById(R.id.master_layout);
        final ImageView backButton = (ImageView) findViewById(R.id.back);
        final View scrim = findViewById(R.id.scrim);
        TouchImageView image = (TouchImageView) findViewById(R.id.image);

        // Apply the intent data
        Intent intent = getIntent();
        String uri = intent.getStringExtra(getString(R.string.INTENT_EXTRA_PATH));
        image.setImageURI(Uri.parse(uri));

        // Set the listeners
        masterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacksAndMessages(null);

                if (actionBarIsShowing) {
                    actionBarIsShowing = false;
                    backButton.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .start();
                    scrim.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .start();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            backButton.setVisibility(View.GONE);
                            scrim.setVisibility(View.GONE);
                        }
                    }, 200);
                } else {
                    actionBarIsShowing = true;
                    backButton.setVisibility(View.VISIBLE);
                    backButton.setAlpha(0f);
                    backButton.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                    scrim.setVisibility(View.VISIBLE);
                    scrim.setAlpha(0f);
                    scrim.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            actionBarIsShowing = false;
                            backButton.animate()
                                    .alpha(0f)
                                    .setDuration(200)
                                    .start();
                            scrim.animate()
                                    .alpha(0f)
                                    .setDuration(200)
                                    .start();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    backButton.setVisibility(View.GONE);
                                    scrim.setVisibility(View.GONE);
                                }
                            }, 200);
                        }
                    }, 2300);
                }
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacksAndMessages(null);

                if (actionBarIsShowing) {
                    actionBarIsShowing = false;
                    backButton.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .start();
                    scrim.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .start();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            backButton.setVisibility(View.GONE);
                            scrim.setVisibility(View.GONE);
                        }
                    }, 200);
                } else {
                    actionBarIsShowing = true;
                    backButton.setVisibility(View.VISIBLE);
                    backButton.setAlpha(0f);
                    backButton.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                    scrim.setVisibility(View.VISIBLE);
                    scrim.setAlpha(0f);
                    scrim.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            actionBarIsShowing = false;
                            backButton.animate()
                                    .alpha(0f)
                                    .setDuration(200)
                                    .start();
                            scrim.animate()
                                    .alpha(0f)
                                    .setDuration(200)
                                    .start();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    backButton.setVisibility(View.GONE);
                                    scrim.setVisibility(View.GONE);
                                }
                            }, 200);
                        }
                    }, 2300);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PictureActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                PictureActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setStatusBarColor(getColor(R.color.black_0_87));
    }

    @Override
    protected void onStop() {
        super.onStop();
        getWindow().setStatusBarColor(getColor(R.color.colorPrimaryDark));
    }
}
