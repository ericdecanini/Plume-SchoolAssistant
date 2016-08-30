package com.pdt.plume;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.io.IOException;
import java.util.ArrayList;

public class ScheduleDetailActivity extends AppCompatActivity {

    // Constantly used variables
    String LOG_TAG = ScheduleDetailActivity.class.getSimpleName();
    Utility utility = new Utility();

    String title;
    String teacher;
    String room;

    ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_detail);

        // Get references to the UI elements
        TextView teacherTextview = (TextView) findViewById(R.id.task_detail_class);
        TextView roomTextview = (TextView) findViewById(R.id.task_detail_type);

        // Set the attributes of the window
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setTitle("");

        // Get the class's data based on the title and fill in the fields
        Intent intent = getIntent();
        if (intent != null){
            title = intent.getStringExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE));
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor = dbHelper.getScheduleDataByTitle(title);

            // Set the values of the main UI elements
            if (cursor.moveToFirst()){
                teacher = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TEACHER));
                room = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ROOM));
                collapsingToolbar.setTitle(title);
                teacherTextview.setText(teacher);
                roomTextview.setText(room);

                String iconUriString = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));
                Uri iconUri = Uri.parse(iconUriString);
                Bitmap iconBitmap = null;
                try {
                    iconBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), iconUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Palette.generateAsync(iconBitmap, new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int mainColour = palette.getVibrantColor(getResources().getColor(R.color.colorPrimary));
                        int darkColour = palette.getDarkVibrantColor(getResources().getColor(R.color.colorPrimaryDark));
                        actionBar.setBackgroundDrawable(new ColorDrawable(mainColour));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            collapsingToolbar.setBackground(new ColorDrawable(mainColour));
                        } else collapsingToolbar.setBackgroundDrawable(new ColorDrawable(mainColour));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(darkColour);
                        }
                    }
                });

                // Inflate the listview of periods
                ArrayList<OccurrenceTimePeriod> periods = new ArrayList<>();
                for (int i = 0; i < cursor.getCount(); i ++){
                    String occurrence = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_OCCURRENCE));
                    if (!occurrence.equals("-1"))
                    periods.add(new OccurrenceTimePeriod(this,
                            utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN))),
                            utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEOUT))),
                            utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN_ALT))),
                            utility.secondsToTime(cursor.getFloat(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEOUT_ALT))),
                            cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_PERIODS)),
                            cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_OCCURRENCE))));
                    cursor.moveToNext();
                }

                OccurrenceTimePeriodAdapter adapter = new OccurrenceTimePeriodAdapter(this, R.layout.list_item_occurrence_time_period, periods);
                ListView periodListview = (ListView) findViewById(R.id.schedule_detail_periods_list);
                periodListview.setAdapter(adapter);

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                if (fab != null)
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Create an intent to NewScheduleActivity and include the selected
                            // item's id, title, and an edit flag as extras
                            Intent intent = new Intent(ScheduleDetailActivity.this, NewScheduleActivity.class);
                            intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_TITLE),title);
                            intent.putExtra(getResources().getString(R.string.SCHEDULE_FLAG_EDIT), true);
                            startActivity(intent);
                        }
                    });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.schedule_detail_dialog_delete_confirm))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbHelper dbHelper = new DbHelper(ScheduleDetailActivity.this);
                                dbHelper.deleteScheduleItemByTitle(title);
                                Intent intent = new Intent(ScheduleDetailActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
                break;

            case R.id.action_share:
                String shareString = title
                        + "\n" + getString(R.string.new_schedule_teacher) + ": " + teacher
                        + "\n" + getString(R.string.new_schedule_room) + ": " + room;
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);
                shareIntent.setType("text/plain");
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(shareIntent);
                }
                startActivity(shareIntent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
