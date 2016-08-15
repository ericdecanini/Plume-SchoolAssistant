package com.pdt.plume;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TasksDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_detail);

        // Inflate the fragment into the activity
        TasksDetailFragment fragment = new TasksDetailFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }
}
