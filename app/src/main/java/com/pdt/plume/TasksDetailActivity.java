package com.pdt.plume;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.TasksEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import static com.pdt.plume.R.id.fab;

public class TasksDetailActivity extends AppCompatActivity {

    // Constantly used variables
    String LOG_TAG = ScheduleDetailActivity.class.getSimpleName();
    Utility utility = new Utility();
    ShareActionProvider mShareActionProvider;

    boolean FLAG_TASK_COMPLETD;

    int id;
    String title;
    String subtitle;
    String description;
    String duedate;
    String attachment;
    String photoPath;
    Uri photoUri;
    String attachmentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_detail);

        // Get references to the UI elements
        TextView collapsingToolbarSubtitle = (TextView) findViewById(R.id.collapsingToolbarSubtitle);
        TextView duedateTextview = (TextView) findViewById(R.id.task_detail_duedate);
        TextView descriptionTextview = (TextView) findViewById(R.id.task_detail_description);
        TextView attachmentTextview = (TextView) findViewById(R.id.task_detail_attachment);

        // Set the attributes of the window
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setTitle("");

        // Get the class's data based on the id and fill in the fields
        Intent intent = getIntent();
        if (intent != null){
            FLAG_TASK_COMPLETD = intent.getBooleanExtra(getString(R.string.FLAG_TASK_COMPLETED), false);
            int id = intent.getIntExtra(getString(R.string.KEY_TASKS_EXTRA_ID), 0);
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor = null;
            if (intent.hasExtra("_ID")){
                cursor = dbHelper.getTaskById(intent.getIntExtra("_ID", 0));
            } else cursor = dbHelper.getTaskData();



            // Set the values of the main UI elements
            // First get the data from the cursor
            if (cursor.moveToPosition(id)){
                this.id = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID));
                title = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
                subtitle = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS))
                        + " " + cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE));
                description = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION));
                photoPath = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_PICTURE));

                // Process the data for the duedate to a string
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis((long) cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE)));
                duedate = utility.formatDateString(this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                // Set the attachment field data
                attachmentPath = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
                String fileName = "";
                if (!attachmentPath.equals("")) {
                    Uri attachmentUri = Uri.parse(attachmentPath);
                    Cursor returnCursor = getContentResolver().query(attachmentUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    fileName = returnCursor.getString(nameIndex);
                    attachmentTextview.setText(fileName);
                } else findViewById(R.id.task_attachment_layout).setVisibility(View.GONE);

                // Set the photo field data
                if (!photoPath.equals("")) {
                    photoUri = Uri.parse(photoPath);
                    try {
                        Bitmap photoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                        ImageView photo = (ImageView) findViewById(R.id.task_detail_photo);
                        photo.setImageBitmap(photoBitmap);
                        photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(photoUri, "image/*");
                                if (intent.resolveActivity(getPackageManager()) != null)
                                    startActivity(intent);
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else findViewById(R.id.task_detail_photo_layout).setVisibility(View.GONE);


                collapsingToolbar.setTitle(title);
                collapsingToolbarSubtitle.setText(subtitle);
                duedateTextview.setText(duedate);
                descriptionTextview.setText(description);

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
                        float[] hsv = new float[3];
                        int color = mainColour;
                        Color.colorToHSV(color, hsv);
                        hsv[2] *= 0.8f; // value component
                        int darkColour = Color.HSVToColor(hsv);
                        actionBar.setBackgroundDrawable(new ColorDrawable(mainColour));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            collapsingToolbar.setBackground(new ColorDrawable(mainColour));
                        } else collapsingToolbar.setBackgroundDrawable(new ColorDrawable(mainColour));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(darkColour);
                        }
                    }
                });

                // Initialise the FAB
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                if (fab != null)
                    if (FLAG_TASK_COMPLETD) {
                        fab.setImageResource(R.drawable.ic_refresh_white_24dp);
                        // ACTION RESTORE TASK
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(TasksDetailActivity.this)
                                        .setTitle(getString(R.string.activity_tasksDetail_restore_dialog_title))
                                        .setNegativeButton(getString(R.string.cancel), null)
                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Set the task status to completed
                                                DbHelper dbHelper = new DbHelper(TasksDetailActivity.this);
                                                Cursor cursorTasks = dbHelper.getTaskById(TasksDetailActivity.this.id);

                                                if (cursorTasks.moveToFirst()) {
                                                    String title = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_TITLE));
                                                    String classTitle = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_CLASS));
                                                    String classType = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_TYPE));
                                                    String sharer = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_SHARER));
                                                    String description = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION));
                                                    String attachment = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT));
                                                    int duedate = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_DUEDATE));
                                                    int reminderdate = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE));
                                                    int remindertime = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_REMINDER_TIME));
                                                    String icon = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_ICON));
                                                    String picture = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_PICTURE));
                                                    dbHelper.updateTaskItem(TasksDetailActivity.this.id, title, classTitle, classType,
                                                            sharer, description, attachment,
                                                            duedate, reminderdate, remindertime,
                                                            icon, picture, false);
                                                }

                                                cursorTasks.close();
                                                Intent intent = new Intent(TasksDetailActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                startActivity(intent);
                                            }
                                        }).show();
                            }
                        });
                    }
                else {
                        // ACTION COMPLETE TASK
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(TasksDetailActivity.this)
                                        .setMessage(getString(R.string.task_detail_dialog_completed_confirm))
                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Set the task status to completed
                                                DbHelper dbHelper = new DbHelper(TasksDetailActivity.this);
                                                Cursor cursorTasks = dbHelper.getTaskById(TasksDetailActivity.this.id);

                                                if (cursorTasks.moveToFirst()) {
                                                    String title = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_TITLE));
                                                    String classTitle = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_CLASS));
                                                    String classType = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_TYPE));
                                                    String sharer = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_SHARER));
                                                    String description = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION));
                                                    String attachment = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT));
                                                    int duedate = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_DUEDATE));
                                                    int reminderdate = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE));
                                                    int remindertime = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_REMINDER_TIME));
                                                    String icon = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_ICON));
                                                    String picture = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_PICTURE));
                                                    dbHelper.updateTaskItem(TasksDetailActivity.this.id, title, classTitle, classType,
                                                            sharer, description, attachment,
                                                            duedate, reminderdate, remindertime,
                                                            icon, picture, true);
                                                }

                                                cursorTasks.close();
                                                Intent intent = new Intent(TasksDetailActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                startActivity(intent);
                                            }
                                        })
                                        .setNegativeButton(getString(R.string.cancel), null)
                                        .show();
                            }
                        });
                    }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.task_detail_dialog_delete_confirm))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbHelper dbHelper = new DbHelper(TasksDetailActivity.this);
                                dbHelper.deleteTaskItem(TasksDetailActivity.this.id);
                                Intent intent = new Intent(TasksDetailActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
                break;

//            case R.id.action_share:
//                String shareString = title
//                    + "\n\n" + description
//                    + "\n\n" + getString(R.string.due) + " " + duedate;
//                Intent shareIntent = new Intent();
//                shareIntent.setAction(Intent.ACTION_SEND);
//                shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);
//                shareIntent.setType("text/plain");
//                if (mShareActionProvider != null) {
//                    mShareActionProvider.setShareIntent(shareIntent);
//                }
//                startActivity(shareIntent);
//                break;

            case R.id.action_edit:
                Intent intent = new Intent(this, NewTaskActivity.class);
                intent.putExtra(getString(R.string.TASKS_EXTRA_ID), id);
                intent.putExtra(getString(R.string.TASKS_FLAG_EDIT), true);
                startActivity(intent);
                return true;

            case android.R.id.home:
                Intent startMainActivity = new Intent(this, MainActivity.class);
                startMainActivity.putExtra(getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS), getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS));
                startActivity(startMainActivity);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
