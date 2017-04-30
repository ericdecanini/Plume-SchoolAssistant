package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class NewPeriodOneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_period_one);

        View timebased = findViewById(R.id.class_one_timebased);
        View periodbased = findViewById(R.id.class_one_periodbased);
        View blockbased = findViewById(R.id.class_one_blockbased);

        timebased.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NewPeriodOneActivity.this);
                preferences.edit()
                        .putString(getString(R.string.KEY_PREFERENCE_BASIS), "0")
                        .apply();
                Intent intent = new Intent(NewPeriodOneActivity.this, NewPeriodTwoActivity.class);
                startActivity(intent);
            }
        });

        periodbased.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NewPeriodOneActivity.this);
                preferences.edit()
                        .putString(getString(R.string.KEY_PREFERENCE_BASIS), "1")
                        .apply();
                Intent intent = new Intent(NewPeriodOneActivity.this, NewPeriodTwoActivity.class);
                startActivity(intent);
            }
        });

        blockbased.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NewPeriodOneActivity.this);
                preferences.edit()
                        .putString(getString(R.string.KEY_PREFERENCE_WEEKTYPE), "2")
                        .apply();
                boolean FIRST_LAUNCH = preferences.getBoolean(getString(R.string.KEY_FIRST_LAUNCH), true);

                Intent intent;
                if (FIRST_LAUNCH) intent = new Intent(NewPeriodOneActivity.this, MainActivity.class);
                else intent = new Intent(NewPeriodOneActivity.this, NewScheduleActivity.class);

                preferences.edit().putBoolean(getString(R.string.KEY_FIRST_LAUNCH), false).apply();
                startActivity(intent);
            }
        });
    }
}
