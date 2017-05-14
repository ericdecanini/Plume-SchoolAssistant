package com.pdt.plume;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityOptions;
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
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pdt.plume.data.DbContract.TasksEntry;
import com.pdt.plume.data.DbHelper;
import com.pdt.plume.services.RevisionTimerService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import static com.pdt.plume.R.id.collapsingToolbar;

public class TasksDetailActivityTablet extends AppCompatActivity {

    // Constantly used variables
    String LOG_TAG = TasksDetailActivityTablet.class.getSimpleName();
    Utility utility = new Utility();
    ShareActionProvider mShareActionProvider;
    private static boolean active = false;

    // Theme Variables
    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    boolean FLAG_TASK_COMPLETED;

    int id;
    String firebaseID;
    String title;
    String classTitle;
    String classType;
    String subtitle;
    String description;
    float duedateValue;
    String duedate;
    String iconUri;
    ArrayList<Uri> photoUris = new ArrayList<>();


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

    int i = 0;
    boolean isTablet;

    private void executeEnterTransition() {
        mToolbar = (AppBarLayout) findViewById(R.id.appbar);

        // Explode the icon into the circle reveal
        mRevealView2 = findViewById(R.id.reveal2);
        mRevealBackgroundView2 = findViewById(R.id.temp_icon);

        Animator animator = ViewAnimationUtils.createCircularReveal(
                mRevealView2,
                mRevealBackgroundView2.getWidth() / 2,
                mRevealBackgroundView2.getHeight() / 2, 0,
                mRevealBackgroundView2.getWidth() / 3);

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

        animator.setDuration(50);
        animator.start();

        // Play the animation for the rest of the toolbar
        mRevealView = findViewById(R.id.reveal);
        mRevealBackgroundView = findViewById(R.id.revealBackground);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animator animator2 = ViewAnimationUtils.createCircularReveal(
                        mRevealView,
                        mRevealBackgroundView2.getWidth() / 2 + ((int) mRevealBackgroundView2.getX()),
                        mRevealBackgroundView2.getHeight() / 2 + ((int) mRevealBackgroundView2.getY()), 0,
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
                        findViewById(collapsingToolbar).setBackgroundColor(mPrimaryColor);
                        mRevealView.setVisibility(View.GONE);
                        mRevealBackgroundView.setVisibility(View.GONE);
                    }
                });

                mRevealBackgroundView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                animator2.setDuration(450);
                animator2.start();
                mRevealView.setVisibility(View.VISIBLE);
            }
        }, 0);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set window properties
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new AutoTransition());
        setContentView(R.layout.activity_tasks_detail);
        isTablet = getResources().getBoolean(R.bool.isTablet);

        // Add a listener to the shared transition
        Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
        sharedElementEnterTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                executeEnterTransition();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Set the attributes of the window
        if (!isTablet) {
            final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.task));
            collapsingToolbar.setTitle("");
        }

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
            FLAG_TASK_COMPLETED = intent.getBooleanExtra(getString(R.string.INTENT_FLAG_COMPLETED), false);
            int id = intent.getIntExtra(getString(R.string.INTENT_EXTRA_ID), 0);

            if (mFirebaseUser != null) {
                // Get the data from Firebase
                firebaseID = intent.getStringExtra("id");
                final DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("tasks").child(firebaseID);
                taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        title = dataSnapshot.child("title").getValue(String.class);
                        iconUri = dataSnapshot.child("icon").getValue(String.class);
                        classTitle = dataSnapshot.child("class").getValue(String.class);
                        classType = dataSnapshot.child("type").getValue(String.class);
                        subtitle = getString(R.string.format_subtitle,
                                classTitle, classType);
                        description = dataSnapshot.child("description").getValue(String.class);
                        Object duedatemillis = dataSnapshot.child("duedate").getValue();
                        duedateValue = ((long) duedatemillis);

                        // Format a string for the duedate
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis((long) duedatemillis);
                        duedate = utility.formatDateString(TasksDetailActivityTablet.this, c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                        // Get the photo data
                        long photoCount = dataSnapshot.child("photos").getChildrenCount();
                        for (DataSnapshot photoSnapshot : dataSnapshot.child("photos").getChildren()) {
                            String photoPath = photoSnapshot.getKey()
                                    .replace("'dot'", ".")
                                    .replace("'slash'", "/")
                                    .replace("'hash'", "#")
                                    .replace("'ampers'", "&");
                            photoUris.add(Uri.parse(photoPath));
                        }
                        // Add in the views for the photos
                        for (int i = 0; i < photoUris.size(); i++) {
                            // Check validity of URI
                            File file = new File(photoUris.get(i).getPath());
                            if (file.exists()) {
                                int wh = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 156, getResources().getDisplayMetrics()));
                                LinearLayout photosLayout = (LinearLayout) findViewById(R.id.photos_layout);

                                final CardView cardView = new CardView(TasksDetailActivityTablet.this);
                                cardView.setLayoutParams(new LinearLayout.LayoutParams(wh, wh));
                                cardView.setElevation(24f);
                                photosLayout.addView(cardView);

                                final ImageView photo = new ImageView(TasksDetailActivityTablet.this);
                                photo.setImageURI(photoUris.get(i));

                                photo.setLayoutParams(new CardView.LayoutParams
                                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                photo.setId(Utility.generateViewId());
                                cardView.addView(photo);

                                photosLayout.setVisibility(View.VISIBLE);

                                // Add the listener
                                final int finalI = i;
                                cardView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent pictureIntent = new Intent(TasksDetailActivityTablet.this, PictureActivity.class);
                                        pictureIntent.putExtra(getString(R.string.INTENT_EXTRA_PATH), photoUris.get(finalI).toString());
                                        photo.setTransitionName("transition");
                                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation
                                                (TasksDetailActivityTablet.this, photo, photo.getTransitionName()).toBundle();
                                        startActivity(pictureIntent, bundle);
                                    }
                                });
                            } else {
                                // Download the photo data
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageRef = storage.getReference();
                                StorageReference photosRef = storageRef.child(dataSnapshot.child("photos")
                                        .child(String.valueOf(i)).getKey());
                                final long ONE_MEGABYTE = 1024 * 1024;
                                final int finalI1 = i;
                                photosRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        // Save the file locally
                                        File file = new File(photoUris.get(finalI1).toString());
                                        try {
                                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                                            bos.write(bytes);
                                            bos.flush();
                                            bos.close();
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        // Add the view
                                        if (!active) return;
                                        photoUris.add(Uri.fromFile(file));
                                        LinearLayout photosLayout = (LinearLayout) findViewById(R.id.photos_layout);
                                        ImageView photo = new ImageView(TasksDetailActivityTablet.this);
                                        photo.setImageURI(Uri.fromFile(file));
                                        int width = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getResources().getDisplayMetrics()));
                                        photo.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
                                        photo.setPadding(4, 0, 4, 0);
                                        photo.setId(Utility.generateViewId());
                                        photosLayout.addView(photo);
                                        photosLayout.setVisibility(View.VISIBLE);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // TODO: Handle Unsuccessful Download
                                    }
                                });
                                i++;

                            }
                        }

                        applyDataToUI();
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
                    cursor = dbHelper.getTaskById(intent.getIntExtra("_ID", 0));
                } else if (FLAG_TASK_COMPLETED) cursor = dbHelper.getTaskData();
                else cursor = dbHelper.getUncompletedTaskData();


                // Get the data from the cursor
                if (cursor.moveToPosition(id)) {
                    this.id = cursor.getInt(cursor.getColumnIndex(TasksEntry._ID));
                    title = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TITLE));
                    classTitle = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_CLASS));
                    classType = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_TYPE));
                    subtitle = getString(R.string.format_subtitle, classTitle, classType);
                    description = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_DESCRIPTION));
                    iconUri = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_ICON));
                    String photoLine = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_PICTURE));
                    final String[] photos = cursor.getString(cursor.getColumnIndex(TasksEntry.COLUMN_PICTURE)).split("#seperate#");

                    // Process the data for the duedate to a string
                    Calendar c = Calendar.getInstance();
                    duedateValue = (long) cursor.getFloat(cursor.getColumnIndex(TasksEntry.COLUMN_DUEDATE));
                    c.setTimeInMillis((long)duedateValue);
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
                    LinearLayout photosLayout = (LinearLayout) findViewById(R.id.photos_layout);
                    for (int i = 0; i < photos.length; i++) {
                        if (photos[i].length() > 1) {
                            final Uri photoUri = Uri.parse(photos[i]);
                            photoUris.add(photoUri);

                            // Add in the views for the photos
                            int wh = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 156, getResources().getDisplayMetrics()));

                            final CardView cardView = new CardView(TasksDetailActivityTablet.this);
                            cardView.setLayoutParams(new LinearLayout.LayoutParams(wh, wh));
                            cardView.setElevation(24f);
                            photosLayout.addView(cardView);

                            final ImageView photo = new ImageView(TasksDetailActivityTablet.this);
                            photo.setImageURI(photoUri);

                            photo.setLayoutParams(new CardView.LayoutParams
                                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            photo.setId(Utility.generateViewId());
                            cardView.addView(photo);

                            photosLayout.setVisibility(View.VISIBLE);

                            // Add the listener
                            cardView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent pictureIntent = new Intent(TasksDetailActivityTablet.this, PictureActivity.class);
                                    pictureIntent.putExtra(getString(R.string.INTENT_EXTRA_PATH), photoUri.toString());
                                    photo.setTransitionName("transition");
                                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation
                                            (TasksDetailActivityTablet.this, photo, photo.getTransitionName()).toBundle();
                                    startActivity(pictureIntent, bundle);
                                }
                            });
                        }
                    }

                }

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
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
        if (isTablet) actionBar.setTitle(title);
        else collapsingToolbar.setTitle(title);
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

        if (FLAG_TASK_COMPLETED) {
            markAsDoneView.setText(getString(R.string.mark_as_undone));
            markAsDoneView.setTextColor(getResources().getColor(R.color.red_500));
        }

        // TODO: Bring back the revision timer
        fieldTimer = (TextView) findViewById(R.id.task_detail_timer);
    }

    private void promptCompleteTask() {
        if (FLAG_TASK_COMPLETED) {
            // ACTION RESTORE TASK
            new AlertDialog.Builder(TasksDetailActivityTablet.this)
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
                                DbHelper dbHelper = new DbHelper(TasksDetailActivityTablet.this);
                                Cursor cursorTasks = dbHelper.getTaskById(TasksDetailActivityTablet.this.id);

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
                                    String[] pictureString = picture.split("#seperate#");
                                    ArrayList<Uri> pictureStringList = new ArrayList<>();
                                    for (int i = 0; i < pictureString.length; i++) {
                                        pictureStringList.add(Uri.parse(pictureString[i]));
                                    }

                                    dbHelper.updateTaskItem(TasksDetailActivityTablet.this, TasksDetailActivityTablet.this.id, title, classTitle, classType, description, attachment,
                                            duedate, reminderdate, remindertime,
                                            icon, pictureStringList, false);
                                }

                                cursorTasks.close();
                            }


                            Intent intent = new Intent(TasksDetailActivityTablet.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        }
                    }).show();
        } else {
            // ACTION COMPLETE TASK
            new AlertDialog.Builder(TasksDetailActivityTablet.this)
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
                                DbHelper dbHelper = new DbHelper(TasksDetailActivityTablet.this);
                                Cursor cursorTasks = dbHelper.getTaskById(TasksDetailActivityTablet.this.id);

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
                                    String[] pictureString = picture.split("#seperate#");
                                    ArrayList<Uri> pictureStringList = new ArrayList<>();
                                    for (int i = 0; i < pictureString.length; i++) {
                                        pictureStringList.add(Uri.parse(pictureString[i]));
                                    }

                                    dbHelper.updateTaskItem(TasksDetailActivityTablet.this, TasksDetailActivityTablet.this.id, title, classTitle, classType, description, attachment,
                                            duedate, reminderdate, remindertime,
                                            icon, pictureStringList, true);
                                }

                                cursorTasks.close();
                            }


                            Intent intent = new Intent(TasksDetailActivityTablet.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.putExtra(getString(R.string.INTENT_FLAG_RETURN_TO_TASKS), getString(R.string.INTENT_FLAG_RETURN_TO_TASKS));
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
                                    DbHelper dbHelper = new DbHelper(TasksDetailActivityTablet.this);
                                    dbHelper.deleteTaskItem(TasksDetailActivityTablet.this.id);
                                }


                                Intent intent = new Intent(TasksDetailActivityTablet.this, MainActivity.class);
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
                    taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                            intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_TITLE), title);
                            intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), classTitle);
                            intent.putExtra(getString(R.string.INTENT_EXTRA_TYPE), classType);
                            intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DESCRIPTION), description);
                            intent.putExtra("photo", photo);
                            intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ATTACHMENT), attachment);
                            intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DUEDATE), dueDate);

                            // Create an intent to NewScheduleActivity and include the selected
                            // item's id, title, and an edit flag as extras
                            intent.putExtra(getResources().getString(R.string.INTENT_FLAG_EDIT), true);
                            taskRef.removeEventListener(this);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    // Pass on data through an intent
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < photoUris.size(); i++) {
                        builder.append(photoUris.get(i));
                        if (i < photoUris.size() - 1)
                            builder.append("#seperate#");
                    }

                    intent.putExtra("id", id);
                    intent.putExtra("icon", iconUri);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_TITLE), title);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), classTitle);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_TYPE), classType);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DESCRIPTION), description);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_DUEDATE), duedateValue);
                    intent.putExtra("photo", builder.toString());
//                    intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_ATTACHMENT), attachment);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_ID), id);
                    intent.putExtra(getString(R.string.INTENT_FLAG_EDIT), true);
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
                serviceIntent = new Intent(TasksDetailActivityTablet.this, RevisionTimerService.class);
                serviceIntent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title);
                serviceIntent.putExtra(getString(R.string.INTENT_EXTRA_DURATION), countdown);
                fieldTimer.setVisibility(View.VISIBLE);
                fieldTimer.setText(utility.secondsToMinuteTime(countdown * 60));
                startService(serviceIntent);
                registerReceiver(mMessageReceiver, new IntentFilter("com.pdt.plume.USER_ACTION"));
                dialog.dismiss();
                if (countdown == 1)
                    Toast.makeText(TasksDetailActivityTablet.this,
                            getString(R.string.toast_timer_set) + " " + countdown + " " + getString(R.string.minute),
                            Toast.LENGTH_SHORT).show();
                else Toast.makeText(TasksDetailActivityTablet.this,
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
                LocalBroadcastManager.getInstance(TasksDetailActivityTablet.this).unregisterReceiver(mMessageReceiver);
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
