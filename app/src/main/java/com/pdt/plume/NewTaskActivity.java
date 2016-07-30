package com.pdt.plume;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class NewTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_done:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS), getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS));
                startActivity(intent);
                break;
        }

        return true;
    }
}
