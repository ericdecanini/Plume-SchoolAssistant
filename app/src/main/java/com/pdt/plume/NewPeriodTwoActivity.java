package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class NewPeriodTwoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_period_two);

        View weekSame = findViewById(R.id.class_two_weeksame);
        View weekAlt = findViewById(R.id.class_two_weekalt);

        weekSame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NewPeriodTwoActivity.this);
                preferences.edit()
                        .putString(getString(R.string.KEY_PREFERENCE_WEEKTYPE), "0")
                        .apply();
                Intent intent = new Intent(NewPeriodTwoActivity.this, NewScheduleActivity.class);
                startActivity(intent);
            }
        });

        weekAlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NewPeriodTwoActivity.this);
                preferences.edit()
                        .putString(getString(R.string.KEY_PREFERENCE_WEEKTYPE), "1")
                        .apply();
                Intent intent = new Intent(NewPeriodTwoActivity.this, NewScheduleActivity.class);
                startActivity(intent);
            }
        });


    }
}
