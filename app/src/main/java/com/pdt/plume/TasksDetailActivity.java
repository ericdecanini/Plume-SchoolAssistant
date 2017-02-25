package com.pdt.plume;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.TasksEntry;
import com.pdt.plume.services.RevisionTimerService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class TasksDetailActivity extends AppCompatActivity {

    // Constantly used variables
    String LOG_TAG = ScheduleDetailActivity.class.getSimpleName();
    Utility utility = new Utility();
    ShareActionProvider mShareActionProvider;

    // Theme Variables
    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    boolean FLAG_TASK_COMPLETED;

    int id;
    String firebaseID;
    String title;
    String subtitle;
    String description;
    String duedate;
    String attachment;
    String photoPath;
    Uri photoUri;
    String attachmentPath;
    String iconUri;
    TextView markAsDoneView;

    TextView fieldTimer;
    Intent serviceIntent;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;

    // Transition Utensils
    private View mRevealView;
    private View mRevealBackgroundView;
    private View mRevealView2;
    private View mRevealBackgroundView2;
    private AppBarLayout mToolbar;

    private void executeEnterTransition() {
        mToolbar = (AppBarLayout) findViewById(R.id.appbar);
        final ImageView tempView = (ImageView) findViewById(R.id.temp_icon);

        // Explode the icon into the circle reveal
        mRevealView2 = findViewById(R.id.reveal2);
        mRevealBackgroundView2 = findViewById(R.id.temp_icon);

        Animator animator = ViewAnimationUtils.createCircularReveal(
                mRevealView2,
                tempView.getWidth() / 2,
                tempView.getHeight() / 2, 0,
                tempView.getWidth());

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRevealView2.setBackgroundColor(mPrimaryColor);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mRevealView2.setVisibility(View.GONE);
                mRevealBackgroundView2.setVisibility(View.GONE);
            }
        });

        animator.setStartDelay(290);
        animator.setDuration(100);
        animator.start();

        // Play the animation for the rest of the toolbar
        mRevealView = findViewById(R.id.reveal);
        mRevealBackgroundView = findViewById(R.id.revealBackground);

        Animator animator2 = ViewAnimationUtils.createCircularReveal(
                mRevealView,
                tempView.getWidth() / 2 + ((int) tempView.getX()),
                tempView.getHeight() / 2 + ((int) tempView.getY()), 0,
                mToolbar.getWidth());

        animator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRevealView.setBackgroundColor(mPrimaryColor);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
                findViewById(R.id.collapsingToolbar).setBackgroundColor(mPrimaryColor);
                mRevealView.setVisibility(View.GONE);
                mRevealBackgroundView.setVisibility(View.GONE);
            }
        });

        mRevealBackgroundView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        animator2.setStartDelay(300);
        animator2.setDuration(300);
        animator2.start();
        mRevealView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set window properties
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new AutoTransition());
        setContentView(R.layout.activity_tasks_detail);
        String icon = getIntent().getStringExtra("icon");
        ((ImageView) findViewById(R.id.temp_icon)).setImageURI(Uri.parse(icon));

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Set the attributes of the window
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setTitle("");

        // Set the mark as done button
        markAsDoneView = (TextView) findViewById(R.id.mark_as_done);
        markAsDoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptCompleteTask();
            }
        });

        // Get the class's data based on the id and fill in the fields
        // An ID is passed by the intent so we query using that
        Intent intent = getIntent();
        if (intent != null) {
            FLAG_TASK_COMPLETED = intent.getBooleanExtra(getString(R.string.FLAG_TASK_COMPLETED), false);
            int id = intent.getIntExtra(getString(R.string.KEY_TASKS_EXTRA_ID), 0);

            if (mFirebaseUser != null) {
                // Get the data from Firebase
                firebaseID = intent.getStringExtra("id");
                Log.v(LOG_TAG, "FirebaseID: " + firebaseID);
                final DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("tasks").child(firebaseID);
                taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.v(LOG_TAG, "Data Snapshot: " + dataSnapshot.getKey());
                        title = dataSnapshot.child("title").getValue(String.class);
                        iconUri = dataSnapshot.child("icon").getValue(String.class);
                        subtitle = getString(R.string.format_subtitle,
                                dataSnapshot.child("class").getValue(String.class),
                                dataSnapshot.child("type").getValue(String.class));
                        description = dataSnapshot.child("description").getValue(String.class);
                        Object duedatemillis = dataSnapshot.child("duedate").getValue();

                        // Format a string for the duedate
                        Calendar c = Calendar.getInstance();
//                        c.setTimeInMillis((long)duedatemillis);
                        duedate = utility.formatDateString(TasksDetailActivity.this, c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                        applyDataToUI();
                        taskRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else {
                // Get the data from SQLite
                DbHelper dbHelper = new DbHelper(this);
                Cursor cursor;
                if (intent.hasExtra("_ID")) {
                    Log.v(LOG_TAG, "Task ID result: " + intent.getIntExtra("_ID", -1));
                    cursor = dbHelper.getTaskById(intent.getIntExtra("_ID", 0));
                } else if (FLAG_TASK_COMPLETED) cursor = dbHelper.getTaskData();
                else cursor = dbHelper.getUncompletedTaskData();


                // Get the data from the cursor
                if (cursor.moveToPosition(id)) {
                    this.id = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID));
                    title = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
                    subtitle = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS))
                            + " " + cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE));
                    description = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION));
                    photoPath = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_PICTURE));
                    iconUri = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));

                    // Process the data for the duedate to a string
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis((long) cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE)));
                    duedate = utility.formatDateString(this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                    applyDataToUI();


                    // Set the attachment field data
                    // ATTACHMENTS DISABLED FOR THE BETA
//                attachmentPath = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
//                String fileName;
//                if (!attachmentPath.equals("")) {
//                    Uri attachmentUri = Uri.parse(attachmentPath);
//                    Cursor returnCursor = getContentResolver().query(attachmentUri, null, null, null, null);
//                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//                    returnCursor.moveToFirst();
//                    fileName = returnCursor.getString(nameIndex);
//                    attachmentTextview.setText(fileName);
//                } else findViewById(R.id.task_attachment_layout).setVisibility(View.GONE);

                    // Set the photo field data
                    // PHOTOS DISABLED FOR THE BETA
//                if (!photoPath.equals("")) {
//                    photoUri = Uri.parse(photoPath);
//                    try {
//                        float scale = getResources().getDisplayMetrics().density;
//                        final Bitmap photoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);

//                        final ImageView photo = (ImageView) findViewById(R.id.task_detail_photo);
//                        photo.setMaxWidth(((int) (getWindowManager().getDefaultDisplay().getWidth() - (140 * scale))));
//                        photo.setImageBitmap(Bitmap.createScaledBitmap(photoBitmap, ((int) (photoBitmap.getWidth() / 2.5)), ((int) (photoBitmap.getHeight() / 2.5)), false));
//                        photo.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent photoIntent = new Intent(TasksDetailActivity.this, PictureActivity.class);
//                                photoIntent.putExtra(getString(R.string.INTENT_EXTRA_TITLE), title);
//                                photoIntent.putExtra(getString(R.string.INTENT_EXTRA_PATH), photoPath);
//                                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(TasksDetailActivity.this,
//                                        photo, photo.getTransitionName()).toBundle();
//                                startActivity(photoIntent, bundle);
//                            }
//                        });
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } else findViewById(R.id.task_detail_photo_layout).setVisibility(View.GONE);


                }

            }
        }
    }

    private void applyDataToUI() {
        // Get references to the UI elements
        final ActionBar actionBar = getSupportActionBar();
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        TextView collapsingToolbarSubtitle = (TextView) findViewById(R.id.collapsingToolbarSubtitle);
        TextView duedateTextview = (TextView) findViewById(R.id.task_detail_duedate);
        TextView descriptionTextview = (TextView) findViewById(R.id.task_detail_description);
        TextView attachmentTextview = (TextView) findViewById(R.id.task_detail_attachment);

        // Apply the data to the UI
        collapsingToolbar.setTitle(title);
        collapsingToolbarSubtitle.setText(subtitle);
        duedateTextview.setText(getString(R.string.due, duedate));
        descriptionTextview.setText(description);

        final Uri ParsedIconUri = Uri.parse(iconUri);
        Bitmap iconBitmap = null;
        try {
            iconBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), ParsedIconUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialise the theme variables
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), R.color.colorPrimary);
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent);


        Palette.generateAsync(iconBitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int mainColour;

                if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_arts_64dp")))
                    mainColour = Color.parseColor("#29235C");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_business_64dp")))
                    mainColour = Color.parseColor("#575756");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_chemistry_64dp")))
                    mainColour = Color.parseColor("#006838");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_cooking_64dp")))
                    mainColour = Color.parseColor("#A48A7B");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_drama_64dp")))
                    mainColour = Color.parseColor("#7B6A58");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_engineering_64dp")))
                    mainColour = Color.parseColor("#9E9E9E");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_ict_64dp")))
                    mainColour = Color.parseColor("#936037");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_media_64dp")))
                    mainColour = Color.parseColor("#F39200");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_music_64dp")))
                    mainColour = Color.parseColor("#432918");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_re_64dp")))
                    mainColour = Color.parseColor("#D35095");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_science_64dp")))
                    mainColour = Color.parseColor("#1D1D1B");
                else if (ParsedIconUri.equals(Uri.parse("android.resource://com.pdt.plume/drawable/art_woodwork_64dp")))
                    mainColour = Color.parseColor("#424242");
                else {
                    // Set the action bar colour according to the theme
                    mainColour = palette.getVibrantColor(mPrimaryColor);
                }

                mPrimaryColor = mainColour;
                float[] hsv = new float[3];
                int color = mainColour;
                Color.colorToHSV(color, hsv);
                hsv[2] *= 0.8f; // value component
                mDarkColor = Color.HSVToColor(hsv);

                ViewTreeObserver viewTreeObserver = collapsingToolbar.getViewTreeObserver();
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            executeEnterTransition();
                            collapsingToolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                }

//                actionBar.setBackgroundDrawable(new ColorDrawable(mainColour));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    collapsingToolbar.setBackground(new ColorDrawable(mainColour));
                } else {
//                    collapsingToolbar.setBackgroundDrawable(new ColorDrawable(mainColour));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(mDarkColor);
                }
            }
        });

        markAsDoneView.setTextColor(mPrimaryColor);

        if (FLAG_TASK_COMPLETED) {
            markAsDoneView.setText(getString(R.string.mark_as_undone));
            markAsDoneView.setTextColor(getResources().getColor(R.color.red_500));
        }

        // TODO: Bring back the revision timer
        fieldTimer = (TextView) findViewById(R.id.task_detail_timer);
    }

    private Uri getFileAbsolutePath(Uri filePath) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(filePath);
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);

        File targetFile = new File(filePath.toString());
        OutputStream outputStream = new FileOutputStream(targetFile);
        outputStream.write(buffer);
        Log.v(LOG_TAG, "File Uri: " + targetFile.getAbsolutePath());
        return Uri.parse(targetFile.getAbsolutePath());
    }

    private void promptCompleteTask() {
        if (FLAG_TASK_COMPLETED) {
            // ACTION RESTORE TASK
            new AlertDialog.Builder(TasksDetailActivity.this)
                    .setTitle(getString(R.string.activity_tasksDetail_restore_dialog_title))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Set the task status to completed
                            if (mFirebaseUser != null) {
                                // Set the data in Firebase
                                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(mUserId).child("tasks").child(firebaseID);
                                taskRef.child("completed").setValue(false);

                            } else {
                                // Set the data in SQLite
                                DbHelper dbHelper = new DbHelper(TasksDetailActivity.this);
                                Cursor cursorTasks = dbHelper.getTaskById(TasksDetailActivity.this.id);

                                if (cursorTasks.moveToFirst()) {
                                    String title = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_TITLE));
                                    String classTitle = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_CLASS));
                                    String classType = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_TYPE));
                                    String description = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION));
                                    String attachment = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT));
                                    int duedate = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_DUEDATE));
                                    int reminderdate = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE));
                                    int remindertime = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_REMINDER_TIME));
                                    String icon = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_ICON));
                                    String picture = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_PICTURE));
                                    dbHelper.updateTaskItem(TasksDetailActivity.this.id, title, classTitle, classType, description, attachment,
                                            duedate, reminderdate, remindertime,
                                            icon, picture, false);
                                }

                                cursorTasks.close();
                            }


                            Intent intent = new Intent(TasksDetailActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        }
                    }).show();
        } else {
            // ACTION COMPLETE TASK
            new AlertDialog.Builder(TasksDetailActivity.this)
                    .setMessage(getString(R.string.task_detail_dialog_completed_confirm))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Set the task status to completed
                            if (mFirebaseUser != null) {
                                // Set the data in Firebase
                                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(mUserId).child("tasks").child(firebaseID);
                                taskRef.child("completed").setValue(true);
                            } else {
                                // Set the data in SQLite
                                DbHelper dbHelper = new DbHelper(TasksDetailActivity.this);
                                Cursor cursorTasks = dbHelper.getTaskById(TasksDetailActivity.this.id);

                                if (cursorTasks.moveToFirst()) {
                                    String title = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_TITLE));
                                    String classTitle = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_CLASS));
                                    String classType = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_TYPE));
                                    String description = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION));
                                    String attachment = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_ATTACHMENT));
                                    int duedate = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_DUEDATE));
                                    int reminderdate = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_REMINDER_DATE));
                                    int remindertime = cursorTasks.getInt(cursorTasks.getColumnIndex(TasksEntry.COLUMN_REMINDER_TIME));
                                    String icon = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_ICON));
                                    String picture = cursorTasks.getString(cursorTasks.getColumnIndex(TasksEntry.COLUMN_PICTURE));
                                    dbHelper.updateTaskItem(TasksDetailActivity.this.id, title, classTitle, classType, description, attachment,
                                            duedate, reminderdate, remindertime,
                                            icon, picture, true);
                                }

                                cursorTasks.close();
                            }


                            Intent intent = new Intent(TasksDetailActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.putExtra(getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS), getString(R.string.EXTRA_TEXT_RETURN_TO_TASKS));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.task_detail_dialog_delete_confirm))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mFirebaseUser != null) {
                                    // Delete from Firebase
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(mUserId).child("tasks")
                                            .child(firebaseID).removeValue();
                                } else {
                                    // Delete from SQLite
                                    DbHelper dbHelper = new DbHelper(TasksDetailActivity.this);
                                    dbHelper.deleteTaskItem(TasksDetailActivity.this.id);
                                }


                                Intent intent = new Intent(TasksDetailActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
                break;

            case R.id.action_edit:
                final Intent intent = new Intent(this, NewTaskActivity.class);
                if (mFirebaseUser != null) {
                    // Get the data from Firebase
                    final DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserId).child("tasks").child(firebaseID);
                    taskRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String id = dataSnapshot.getKey();
                            String icon = dataSnapshot.child("icon").getValue(String.class);
                            String title = dataSnapshot.child("title").getValue(String.class);
                            String classTitle = dataSnapshot.child("class").getValue(String.class);
                            String classType = dataSnapshot.child("type").getValue(String.class);
                            String description = dataSnapshot.child("description").getValue(String.class);
                            String photo = dataSnapshot.child("photo").getValue(String.class);
                            String attachment = dataSnapshot.child("attachment").getValue(String.class);
                            Float dueDate = dataSnapshot.child("duedate").getValue(Float.class);

                            intent.putExtra("id", id);
                            intent.putExtra("icon", icon);
                            intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_TITLE), title);
                            intent.putExtra(getString(R.string.TASKS_EXTRA_CLASS), classTitle);
                            intent.putExtra(getString(R.string.TASKS_EXTRA_TYPE), classType);
                            intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_DESCRIPTION), description);
                            intent.putExtra("photo", photo);
                            intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_ATTACHMENT), attachment);
                            intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_DUEDATE), dueDate);

                            // Create an intent to NewScheduleActivity and include the selected
                            // item's id, title, and an edit flag as extras
                            intent.putExtra(getResources().getString(R.string.TASKS_FLAG_EDIT), true);
                            taskRef.removeEventListener(this);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    // Pass on data that will be read from SQLite
                    intent.putExtra(getString(R.string.TASKS_EXTRA_ID), id);
                    intent.putExtra(getString(R.string.TASKS_FLAG_EDIT), true);
                    startActivity(intent);
                }
                return true;

            case R.id.action_time:
                startTimer();
                return true;

            case android.R.id.home:
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startTimer() {
        // REVISION TIMER HERE
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_number_picker);
        Button buttonDone = (Button) dialog.findViewById(R.id.button_done);
        final NumberPicker picker = (NumberPicker) dialog.findViewById(R.id.number_picker);
        picker.setMinValue(1);
        picker.setMaxValue(10000);
        picker.setWrapSelectorWheel(false);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int countdown = picker.getValue();
                serviceIntent = new Intent(TasksDetailActivity.this, RevisionTimerService.class);
                serviceIntent.putExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE), title);
                serviceIntent.putExtra(getString(R.string.KEY_TASKS_EXTRA_REVISION_TIME), countdown);
                fieldTimer.setVisibility(View.VISIBLE);
                fieldTimer.setText(utility.secondsToMinuteTime(countdown * 60));
                startService(serviceIntent);
                registerReceiver(mMessageReceiver, new IntentFilter("com.pdt.plume.USER_ACTION"));
                dialog.dismiss();
                if (countdown == 1)
                    Toast.makeText(TasksDetailActivity.this,
                            getString(R.string.toast_timer_set) + " " + countdown + " " + getString(R.string.minute),
                            Toast.LENGTH_SHORT).show();
                else Toast.makeText(TasksDetailActivity.this,
                        getString(R.string.toast_timer_set) + " " + countdown + " " + getString(R.string.minutes),
                        Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("Status");
            if (message.equals("STOP_SERVICE")) {
                // COUNTDOWN REACHED
                LocalBroadcastManager.getInstance(TasksDetailActivity.this).unregisterReceiver(mMessageReceiver);
                fieldTimer.setVisibility(View.GONE);
                Toast.makeText(context, "service stopped", Toast.LENGTH_SHORT).show();

                promptCompleteTask();
            } else {
                fieldTimer.setVisibility(View.VISIBLE);
                fieldTimer.setText(message);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("GPSLocationUpdates"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
    }

}
