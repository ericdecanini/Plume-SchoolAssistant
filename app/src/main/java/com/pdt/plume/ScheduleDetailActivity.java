package com.pdt.plume;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ScheduleDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_detail);

        // Inflate the fragment into the activity
        ScheduleDetailFragment fragment = new ScheduleDetailFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }
}
