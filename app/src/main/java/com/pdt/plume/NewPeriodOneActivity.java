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
                        .putString(getString(R.string.KEY_PREFERENCE_BASIS), "2")
                        .apply();
                Intent intent = new Intent(NewPeriodOneActivity.this, NewScheduleActivity.class);
                startActivity(intent);
            }
        });
    }
}
