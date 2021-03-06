package com.pdt.plume;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleDetailActivity extends AppCompatActivity {

    // Constantly used variables
    String LOG_TAG = ScheduleDetailActivity.class.getSimpleName();
    Utility utility = new Utility();

    // UI Variables
    private Menu mActionMenu;
    private int mOptionsMenuCount;
    String title;
    String teacher;
    String room;
    ListView listView;
    ListView notesList;
    Uri iconUri;

    // Theme Variables
    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;
    // Child listeners
    ChildEventListener childListener1;
    ChildEventListener childListener2;

    // List Arrays
    ArrayList<Integer> taskIDs = new ArrayList<>();
    ArrayList<String> taskFirebaseIDs = new ArrayList<>();
    ArrayList<Integer> notesIDs = new ArrayList<>();
    ArrayList<Task> mTasksList = new ArrayList<>();
    ArrayList<PeriodItem> mPeriodsList = new ArrayList<>();


    NotesAdapter mNotesAdapter;
    TaskAdapter mTasksAdapter;
    PeriodAdapter mPeriodsAdapter;

    // Transition Utensils
    private View mRevealView;
    private View mRevealBackgroundView;
    private View mRevealView2;
    private View mRevealBackgroundView2;
    private AppBarLayout mToolbar;

    // Flags
    boolean isLandscape;
    boolean transitioning = false;

    private void executeEnterTransition() {
        transitioning = false;
        mToolbar = (AppBarLayout) findViewById(R.id.appbar);

        // Explode the icon into the circle reveal
        mRevealView2 = findViewById(R.id.reveal2);
        mRevealBackgroundView2 = findViewById(R.id.temp_icon);

        Animator animator = ViewAnimationUtils.createCircularReveal(
                mRevealView2,
                mRevealBackgroundView2.getWidth() / 2,
                mRevealBackgroundView2.getHeight() / 2, 0,
                mRevealBackgroundView2.getWidth());

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

        animator.setDuration(123);
        animator.start();

        // Play the animation for the rest of the toolbar
        mRevealView = findViewById(R.id.reveal);
        mRevealBackgroundView = findViewById(R.id.revealBackground);

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
                findViewById(R.id.collapsingToolbar).setBackgroundColor(mPrimaryColor);
                mRevealView.setVisibility(View.GONE);
                mRevealBackgroundView.setVisibility(View.GONE);
            }
        });

        int primaryColor = PreferenceManager.getDefaultSharedPreferences(ScheduleDetailActivity.this)
                .getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        mRevealBackgroundView.setBackgroundColor(primaryColor);
        animator2.setDuration(450);
        animator2.start();
        mRevealView.setVisibility(View.VISIBLE);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isLandscape = getResources().getBoolean(R.bool.isLandscape);
        if (savedInstanceState != null) transitioning = false;
        else transitioning = true;
        if (isLandscape) transitioning = false;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            transitioning = false;
        if (getIntent().getBooleanExtra(getString(R.string.INTENT_FLAG_NO_TRANSITION), false))
            transitioning = false;

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Set enter transition and window features
        requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.activity_schedule_detail);
        final String icon = getIntent().getStringExtra("icon");
        ((ImageView) findViewById(R.id.temp_icon)).setImageURI(Uri.parse(icon));
        if (isLandscape) findViewById(R.id.temp_icon).setVisibility(View.INVISIBLE);

        if (!getResources().getBoolean(R.bool.isTablet)) {
            mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
            mRevealView = findViewById(R.id.reveal);
            mRevealBackgroundView = findViewById(R.id.revealBackground);
            mToolbar = (AppBarLayout) findViewById(R.id.appbar);
            mRevealView2 = findViewById(R.id.reveal2);

            mRevealView.setBackgroundColor(mPrimaryColor);
            mRevealBackgroundView.setBackgroundColor(mPrimaryColor);
            findViewById(R.id.collapsingToolbar).setBackgroundColor(mPrimaryColor);
        }

        // Add a listener to the shared transition
        findViewById(R.id.temp_icon).setAlpha(0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
            sharedElementEnterTransition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    findViewById(R.id.temp_icon).setAlpha(1);
                    transitioning = true;
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
        }

        // Get references to the UI elements
        final TextView teacherTextview = (TextView) findViewById(R.id.teacher);
        final TextView roomTextview = (TextView) findViewById(R.id.room);
        TextView addNoteTextview = (TextView) findViewById(R.id.schedule_detail_notes_textview);
        addNoteTextview.setOnClickListener(addNoteListener());

        // Set the attributes of the window
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setTitle("");

        // Get the class's data based on the category and fill in the fields
        // Also inflate the tasks list
        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getStringExtra(getString(R.string.INTENT_EXTRA_CLASS));

            // Initialise the tasks list
            listView = (ListView) findViewById(R.id.schedule_detail_tasks_list);
            mTasksAdapter = new TaskAdapter(this, R.layout.list_item_task, mTasksList);
            listView.setAdapter(mTasksAdapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new OccurrenceModeCallback());
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ScheduleDetailActivity.this, TasksDetailActivity.class);
                    intent.putExtra("notransition", "notransition");
                    if (mFirebaseUser != null)
                        intent.putExtra("id", taskFirebaseIDs.get(position));
                    else intent.putExtra("_ID", taskIDs.get(position));
                    intent.putExtra("icon", mTasksList.get(position).taskIcon);
                    startActivity(intent);
                }
            });

            // Initialise the periods list
            mPeriodsAdapter = new PeriodAdapter(this, R.layout.list_item_new_period, mPeriodsList);
            ListView periodListview = (ListView) findViewById(R.id.schedule_detail_periods_list);
            periodListview.setAdapter(mPeriodsAdapter);

            if (mFirebaseUser != null) {
                // Get the key data from Firebase
                DatabaseReference classRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("classes").child(title);
                classRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        teacher = dataSnapshot.child("teacher").getValue(String.class);
                        room = dataSnapshot.child("room").getValue(String.class);
                        iconUri = Uri.parse(dataSnapshot.child("icon").getValue(String.class));

                        // Apply data to the UI
                        collapsingToolbar.setTitle(title);
                        teacherTextview.setText(teacher);
                        roomTextview.setText(room);
                        if ((teacher == null || teacher.equals("")) && (room == null || room.equals("")))
                            findViewById(R.id.schedule_detail_keys_layout).setVisibility(View.GONE);

                        // Intialise the tasks list
                        final View tasksLayout = findViewById(R.id.schedule_detail_tasks_layout);
                        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(mUserId).child("tasks");
                        tasksLayout.setVisibility(View.GONE);

                        childListener1 = new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                String classTitle = dataSnapshot.child("class").getValue(String.class);
                                if (classTitle.equals(title)) {
                                    tasksLayout.setVisibility(View.VISIBLE);
                                    final String id = dataSnapshot.getKey();
                                    final String icon = dataSnapshot.child("icon").getValue(String.class);
                                    final String title = dataSnapshot.child("title").getValue(String.class);
                                    final String sharer = dataSnapshot.child("sharer").getValue(String.class);
                                    final String taskClass = dataSnapshot.child("class").getValue(String.class);
                                    final String tasktType = dataSnapshot.child("type").getValue(String.class);
                                    final String description = dataSnapshot.child("description").getValue(String.class);
                                    final float duedate = dataSnapshot.child("duedate").getValue(float.class);
                                    final boolean completed = dataSnapshot.child("completed").getValue(boolean.class);

                                    // Check for icon validity
                                    File file = new File(getFilesDir(), dataSnapshot.getKey() + ".jpg");
                                    if (file.exists()) {
                                        if (!completed) {
                                            taskFirebaseIDs.add(id);
                                            mTasksList.add(new Task(null, icon, title, sharer, taskClass, tasktType, description, "", duedate, -1, null));
                                            ScheduleDetailActivity.this.mTasksAdapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        // File is non existent, download from storage
                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                        StorageReference storageRef = storage.getReference();
                                        StorageReference iconsRef = storageRef.child(mUserId + "/tasks/" + dataSnapshot.getKey());

                                        iconsRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                if (!completed) {
                                                    taskFirebaseIDs.add(id);
                                                    mTasksList.add(new Task(null, icon, title, sharer, taskClass, tasktType, description, "", duedate, -1, null));
                                                    ScheduleDetailActivity.this.mTasksAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }


                                }
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        };
                        tasksRef.addChildEventListener(childListener1);

                        // Initialise the periods list
                        final DatabaseReference classRef = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(mUserId).child("classes")
                                .child(title);
                        final ArrayList<String> occurrences = new ArrayList<>();
                        final ArrayList<Integer> timeins = new ArrayList<>();
                        final ArrayList<Integer> timeouts = new ArrayList<>();
                        final ArrayList<Integer> timeinalts = new ArrayList<>();
                        final ArrayList<Integer> timeoutalts = new ArrayList<>();
                        final ArrayList<String> periods = new ArrayList<>();

                        classRef.child("occurrence").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    occurrences.add(occurrenceSnapshot.getValue(String.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++) {
                                        mPeriodsList.add(new PeriodItem(ScheduleDetailActivity.this,
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));
                                    }

                                mPeriodsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        classRef.child("timein").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    timeins.add(occurrenceSnapshot.getValue(int.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++) {
                                        mPeriodsList.add(new PeriodItem(ScheduleDetailActivity.this,
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));
                                    }

                                mPeriodsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        classRef.child("timeout").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    timeouts.add(occurrenceSnapshot.getValue(int.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++) {
                                        mPeriodsList.add(new PeriodItem(ScheduleDetailActivity.this,
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));
                                    }

                                mPeriodsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        classRef.child("timeinalt").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    timeinalts.add(occurrenceSnapshot.getValue(int.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++) {
                                        mPeriodsList.add(new PeriodItem(ScheduleDetailActivity.this,
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));
                                    }

                                mPeriodsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        classRef.child("timeoutalt").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    timeoutalts.add(occurrenceSnapshot.getValue(int.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++)
                                        mPeriodsList.add(new PeriodItem(ScheduleDetailActivity.this,
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));

                                mPeriodsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        classRef.child("periods").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    periods.add(occurrenceSnapshot.getValue(String.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++)
                                        mPeriodsList.add(new PeriodItem(ScheduleDetailActivity.this,
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));

                                mPeriodsAdapter.notifyDataSetChanged();

                                classRef.removeEventListener(this);

                                // Initialise the theme variables
                                mPrimaryColor = PreferenceManager.getDefaultSharedPreferences(ScheduleDetailActivity.this)
                                        .getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
                                Bitmap iconBitmap = null;
                                try {
                                    iconBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), iconUri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // Set the primary color to that of the icon
                                int mainColour = Utility.getColorFromIcon(ScheduleDetailActivity.this, iconUri.toString());

                                // Set the action bar colour according to the theme
                                ColorStateList a = ColorStateList.valueOf(mainColour);
                                ColorStateList b = ColorStateList.valueOf(preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR),
                                        getResources().getColor(R.color.colorPrimary)));
                                if (a.equals(b))
                                    findViewById(R.id.temp_icon).setVisibility(View.GONE);


                                if (!iconUri.toString().contains("art_"))
                                    mainColour = mPrimaryColor;
                                mPrimaryColor = mainColour;
                                float[] hsv = new float[3];
                                int color = mainColour;
                                Color.colorToHSV(color, hsv);
                                hsv[2] *= 0.8f; // value component
                                mDarkColor = Color.HSVToColor(hsv);
                                mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent));

                                int backgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), getResources().getColor(R.color.backgroundColor));
                                findViewById(R.id.main_content).setBackgroundColor(backgroundColor);

                                int textColor = preferences.getInt(getString(R.string.KEY_THEME_TEXT_COLOUR), getResources().getColor(R.color.gray_900));
                                ((TextView) findViewById(R.id.textView2)).setTextColor(textColor);
                                ((TextView) findViewById(R.id.notes_textview)).setTextColor(textColor);
                                ((TextView) findViewById(R.id.periods_textview)).setTextColor(textColor);
                                ((TextView) findViewById(R.id.teacher)).setTextColor(textColor);
                                ((TextView) findViewById(R.id.room)).setTextColor(textColor);


                                if (!transitioning) {
                                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));

                                    mRevealView = findViewById(R.id.reveal);
                                    mRevealView2 = findViewById(R.id.reveal2);
                                    mRevealBackgroundView2 = findViewById(R.id.temp_icon);
                                    mRevealBackgroundView = findViewById(R.id.revealBackground);

                                    mRevealView.setVisibility(View.INVISIBLE);
                                    mRevealView2.setVisibility(View.INVISIBLE);
                                    mRevealBackgroundView.setVisibility(View.INVISIBLE);
                                    mRevealBackgroundView2.setVisibility(View.INVISIBLE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        collapsingToolbar.setBackground(new ColorDrawable(mainColour));
                                    } else {
                                        collapsingToolbar.setBackgroundDrawable(new ColorDrawable(mainColour));
                                    }
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    getWindow().setStatusBarColor(mDarkColor);
                                }

                                // Initialise Notes and Tasks
                                TextView notesTextview = (TextView) findViewById(R.id.schedule_detail_notes_textview);
                                notesTextview.setTextColor(mPrimaryColor);


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            } else {
                // Get the data from SQLite
                DbHelper dbHelper = new DbHelper(this);
                Cursor cursor = dbHelper.getScheduleDataByTitle(title);

                if (cursor.moveToFirst()) {
                    teacher = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TEACHER));
                    room = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ROOM));
                    String iconUriString = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));
                    iconUri = Uri.parse(iconUriString);

                    // Apply data to the UI
                    collapsingToolbar.setTitle(title);
                    teacherTextview.setText(teacher);
                    roomTextview.setText(room);

                    if ((teacher == null || teacher.equals("")) && (room == null || room.equals("")))
                        findViewById(R.id.schedule_detail_keys_layout).setVisibility(View.GONE);

                    // Initialise the Tasks List
                    Cursor tasksCursor = dbHelper.getTaskDataByClass(title);

                    if (tasksCursor.moveToFirst()) {
                        for (int i = 0; i < tasksCursor.getCount(); i++) {
                            mTasksList.add(new Task(null,
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON)),
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE)),
                                    "",
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS)),
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE)),
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION)),
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT)),
                                    tasksCursor.getFloat(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE)),
                                    tasksCursor.getFloat(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE))
                                            + tasksCursor.getFloat(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME)),
                                    null
                            ));
                            taskIDs.add(tasksCursor.getInt(tasksCursor.getColumnIndex(DbContract.TasksEntry._ID)));
                            tasksCursor.moveToNext();
                        }
                    } else {
                        findViewById(R.id.schedule_detail_tasks_layout).setVisibility(View.GONE);
                    }

                    // Inflate the listview of periods
                    for (int i = 0; i < cursor.getCount(); i++) {
                        String occurrence = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_OCCURRENCE));
                        if (!occurrence.equals("-1"))
                            mPeriodsList.add(new PeriodItem(this,
                                    cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN)),
                                    cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEOUT)),
                                    cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN_ALT)),
                                    cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEOUT_ALT)),
                                    cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_PERIODS)),
                                    cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_OCCURRENCE))));
                        cursor.moveToNext();
                    }

                    // Initialise the theme variables
                    mPrimaryColor = PreferenceManager.getDefaultSharedPreferences(ScheduleDetailActivity.this)
                            .getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
                    Bitmap iconBitmap = null;
                    try {
                        iconBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), iconUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Set the primary color to that of the icon
                    int mainColour = Utility.getColorFromIcon(ScheduleDetailActivity.this, iconUri.toString());

                    if (!iconUri.toString().contains("art_"))
                        mainColour = mPrimaryColor;
                    mPrimaryColor = mainColour;
                    float[] hsv = new float[3];
                    int color = mainColour;
                    Color.colorToHSV(color, hsv);
                    hsv[2] *= 0.8f; // value component

                    mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent));
                    mDarkColor = Color.HSVToColor(hsv);

                    collapsingToolbar.setBackgroundColor(mPrimaryColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(mDarkColor);
                    }

                    if (!transitioning) {
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));

                        mRevealView = findViewById(R.id.reveal);
                        mRevealView2 = findViewById(R.id.reveal2);
                        mRevealBackgroundView2 = findViewById(R.id.temp_icon);
                        mRevealBackgroundView = findViewById(R.id.revealBackground);

                        mRevealView.setVisibility(View.INVISIBLE);
                        mRevealView2.setVisibility(View.INVISIBLE);
                        mRevealBackgroundView.setVisibility(View.INVISIBLE);
                        mRevealBackgroundView2.setVisibility(View.INVISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            collapsingToolbar.setBackground(new ColorDrawable(mainColour));
                        } else {
                            collapsingToolbar.setBackgroundDrawable(new ColorDrawable(mainColour));
                        }
                    }

                    // Initialise Notes
                    TextView notesTextview = (TextView) findViewById(R.id.schedule_detail_notes_textview);
                    notesTextview.setTextColor(mPrimaryColor);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Inflate the notes listview
        notesList = (ListView) findViewById(R.id.schedule_detail_notes_list);
        final ArrayList<String> notesArray = new ArrayList<>();


        mNotesAdapter = new NotesAdapter(this, android.R.layout.simple_list_item_1, notesArray);
        notesList.setAdapter(mNotesAdapter);
        notesList.setOnItemClickListener(addNoteItemClickListener());
        notesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        notesList.setMultiChoiceModeListener(new NotesModeCallback());


        if (mFirebaseUser != null) {
            // Get the data from Firebase
            DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("notes");
            notesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                        String classTitle = noteSnapshot.child("scheduletitle").getValue(String.class);
                        if (classTitle != null && classTitle.equals(title)) {
                            notesArray.add(noteSnapshot.getKey());
                            mNotesAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            // Get the data from SQLite
            DbHelper dbHelper = new DbHelper(this);
            Cursor notes_cursor = dbHelper.getNoteByScheduleTitle(title);

            // Add the note into the array list
            for (int i = 0; i < notes_cursor.getCount(); i++) {
                if (notes_cursor.moveToPosition(i)) {
                    notesArray.add(notes_cursor.getString(notes_cursor.getColumnIndex(DbContract.NotesEntry.COLUMN_TITLE)));
                    notesIDs.add(notes_cursor.getPosition());
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule_detail, menu);
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.schedule_detail_dialog_delete_confirm))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mFirebaseUser != null) {
                                    // Delete data from Firebase
                                    final DatabaseReference classRef = FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(mUserId).child("classes")
                                            .child(title);

                                    // Delete from storage
                                    StorageReference iconRef = FirebaseStorage.getInstance().getReference()
                                            .child(mUserId).child("classes").child(title);
                                    iconRef.delete();
                                    classRef.removeValue();

                                    // Navigate back to MainActivity
                                    Intent intent = new Intent(ScheduleDetailActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    // Delete data from SQLite
                                    DbHelper dbHelper = new DbHelper(ScheduleDetailActivity.this);
                                    dbHelper.deleteScheduleItemByTitle(title);

                                    // Navigate back to MainActivity
                                    Intent intent = new Intent(ScheduleDetailActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
                break;

            case R.id.action_edit:
                Intent intent = new Intent(this, NewScheduleActivity.class);
                intent.putExtra(getString(R.string.INTENT_EXTRA_TITLE), title);
                intent.putExtra(getString(R.string.INTENT_FLAG_EDIT), true);
                startActivity(intent);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUserId == null) return;
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId);
        if (childListener1 != null)
            rootRef.child("tasks").removeEventListener(childListener1);
        if (childListener2 != null)
            rootRef.child("notes").removeEventListener(childListener2);
    }

    private View.OnClickListener addNoteListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScheduleDetailActivity.this, NoteActivity.class);
                intent.putExtra(getString(R.string.INTENT_EXTRA_TITLE), title);
                startActivity(intent);
            }
        };
    }

    private AdapterView.OnItemClickListener addNoteItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DbHelper dbHelper = new DbHelper(ScheduleDetailActivity.this);
                Cursor cursor = dbHelper.getNoteByScheduleTitle(title);
                if (cursor.moveToPosition(i)) {
                    int _ID = cursor.getInt(cursor.getColumnIndex(DbContract.NotesEntry._ID));
                    Intent intent = new Intent(ScheduleDetailActivity.this, NoteActivity.class);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_ID), _ID);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_TITLE), title);
                    startActivity(intent);
                }
            }
        };
    }

    private class OccurrenceModeCallback implements ListView.MultiChoiceModeListener {

        List<Integer> CAMselectedItemsList = new ArrayList<>();

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            // Get the number of list items selected
            // and set the window subtitle based on that
            final int checkedCount = listView.getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;

                case 1:
                    mOptionsMenuCount = 0;
                    mode.setSubtitle("One item selected");
                    break;

                default:
                    mOptionsMenuCount = 1;
                    mode.setSubtitle("" + checkedCount + " items selected");
                    break;

            }

            // If the clicked item became selected, add it to
            // an array list of selected items
            if (checked)
                CAMselectedItemsList.add(position);

                // If the clicked item became deselected, get its item id
                // and remove it from the array list
            else {
                int itemId = -1;
                // Scan through the array list until the
                // item's value matches its position
                // When it does, set the itemId to the matched position
                // and then remove the item in that array list
                // matching that position
                ArrayList<Integer> indexes = new ArrayList<>();
                for (int i = CAMselectedItemsList.size() - 1; i > -1; i--)
                    indexes.add(CAMselectedItemsList.get(i));

                Collections.sort(indexes);
                for (int i = indexes.size() - 1; i > -1; i--)
                    if (position == CAMselectedItemsList.get(i)) {
                        itemId = i;
                    }

                if (itemId != -1)
                    CAMselectedItemsList.remove(itemId);
            }

            // Invalidating the Action Mode calls onPrepareActionMode
            // which will show or hide the edit menu action based on
            // the number of items selected
            mode.invalidate();
        }


        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            // Inflate the action menu and set the global menu variable
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode_single, menu);
            mActionMenu = menu;

            // Set the category and colour of the contextual action bar
            mode.setTitle("Select Items");

            int colorFrom = getResources().getColor(R.color.colorPrimary);
            int colorTo = getResources().getColor(R.color.gray_500);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(200); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    findViewById(R.id.collapsingToolbar).setBackgroundColor((int) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();

            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            // Checks the count of items selected.
            // If it is one, show the edit menu action.
            // If it is delete than one, hide the edit menu action.
            MenuItem menuItem = mActionMenu.findItem(R.id.action_edit);
            if (mOptionsMenuCount == 0)
                menuItem.setVisible(true);
            else
                menuItem.setVisible(false);

            return true;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteSelectedItems();
                    break;

                case R.id.action_edit:
                    editSelectedItem(CAMselectedItemsList.get(0));
                    break;

                default:
                    Toast.makeText(ScheduleDetailActivity.this, "Clicked " + item.getTitle(),
                            Toast.LENGTH_SHORT).show();
                    break;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            // Clear the array list of selected items and revert the window colour back to normal
            CAMselectedItemsList.clear();

            int colorFrom = getResources().getColor(R.color.gray_500);
            int colorTo = getResources().getColor(R.color.colorPrimary);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(800); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    findViewById(R.id.collapsingToolbar).setBackgroundColor((int) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();
        }

        private void deleteSelectedItems() {
            // Get a reference to the database
            DbHelper db = new DbHelper(ScheduleDetailActivity.this);

            // Get a cursor by getting the TaskData
            // Which should match the list view of the TasksFragment
            Cursor cursor = db.getUncompletedTaskData();

            // Delete all the selected items based on the itemIDs
            // Stored in the array list
            for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                if (cursor.moveToPosition(CAMselectedItemsList.get(i))) {
                    db.deleteTaskItem(cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID)));
                }
            }

            cursor.close();

            // Get the list view's current mTasksAdapter, clear it,
            // and query the database again for the current day
            // data, then notify the mTasksAdapter for the changes
            TaskAdapter adapter = (TaskAdapter) listView.getAdapter();
            adapter.clear();
            adapter.addAll(db.getTaskDataArray());
            adapter.notifyDataSetChanged();

            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        private void editSelectedItem(int position) {
            // Ensure that only one item is selected
            if (CAMselectedItemsList.size() == 1) {
                // Initialise intent data variables
                int id;
                String title;
                String classTitle;
                String classType;
                String sharer;
                String description;
                String attachment;
                float dueDate;
                float reminderDate;
                float reminderTime;

                // Get a reference to the database and
                // Get a cursor of the Task Data
                DbHelper db = new DbHelper(ScheduleDetailActivity.this);
                Cursor cursor = db.getUncompletedTaskData();

                // Move the cursor to the position of the selected item
                if (cursor.moveToPosition(CAMselectedItemsList.get(0))) {
                    // Get its Data
                    id = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID));
                    title = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
                    classTitle = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS));
                    classType = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE));
                    description = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION));
                    attachment = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
                    dueDate = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE));
                    reminderDate = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE));
                    reminderTime = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME));
                    cursor.close();

                    // Create an intent to NewScheduleActivity and include the selected
                    // item's id, category, and an edit flag as extras
                    Intent intent = new Intent(ScheduleDetailActivity.this, NewTaskActivity.class);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ID), id);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_TITLE), title);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), classTitle);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_TYPE), classType);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DESCRIPTION), description);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ATTACHMENT), attachment);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DUEDATE), dueDate);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ALARM_DATE), reminderDate);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ALARM_TIME), reminderTime);
                    intent.putExtra("position", position);
                    intent.putExtra(getResources().getString(R.string.INTENT_FLAG_EDIT), true);

                    // Clear the selected items list, exit the CAM and launch the activity
                    CAMselectedItemsList.clear();
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                    startActivity(intent);
                }
            }

            // If delete than one item was selected, throw a warning log
            else {
                Log.w(LOG_TAG, "Cancelling event due to delete than one item selected");
            }
        }

    }

    private class NotesModeCallback implements ListView.MultiChoiceModeListener {

        List<Integer> CAMselectedItemsList = new ArrayList<>();

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            // Get the number of list items selected
            // and set the window subtitle based on that
            final int checkedCount = notesList.getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;

                case 1:
                    mOptionsMenuCount = 0;
                    mode.setSubtitle("One item selected");
                    break;

                default:
                    mOptionsMenuCount = 1;
                    mode.setSubtitle("" + checkedCount + " items selected");
                    break;

            }

            // If the clicked item became selected, add it to
            // an array list of selected items
            if (checked)
                CAMselectedItemsList.add(position);

                // If the clicked item became deselected, get its item id
                // and remove it from the array list
            else {
                int itemId = -1;
                // Scan through the array list until the
                // item's value matches its position
                // When it does, set the itemId to the matched position
                // and then remove the item in that array list
                // matching that position
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    if (position == CAMselectedItemsList.get(i)) {
                        itemId = i;
                    }
                }
                if (itemId != -1)
                    CAMselectedItemsList.remove(itemId);
            }

            // Invalidating the Action Mode calls onPrepareActionMode
            // which will show or hide the edit menu action based on
            // the number of items selected
            mode.invalidate();
        }


        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            // Inflate the action menu and set the global menu variable
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode_single, menu);
            mActionMenu = menu;

            // Set the category and colour of the contextual action bar
            mode.setTitle("Select Items");

            int colorFrom = getResources().getColor(R.color.colorPrimary);
            int colorTo = getResources().getColor(R.color.gray_500);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(200); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    findViewById(R.id.collapsingToolbar).setBackgroundColor((int) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();

            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            // Checks the count of items selected.
            // If it is one, show the edit menu action.
            // If it is delete than one, hide the edit menu action.
            MenuItem menuItem = mActionMenu.findItem(R.id.action_edit);
            if (mOptionsMenuCount == 0)
                menuItem.setVisible(true);
            else
                menuItem.setVisible(false);

            return true;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteSelectedItems();
                    break;

                case R.id.action_edit:
                    editSelectedItem(CAMselectedItemsList.get(0));
                    break;

                default:
                    Toast.makeText(ScheduleDetailActivity.this, "Clicked " + item.getTitle(),
                            Toast.LENGTH_SHORT).show();
                    break;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            // Clear the array list of selected items and revert the window colour back to normal
            CAMselectedItemsList.clear();

            int colorFrom = getResources().getColor(R.color.gray_500);
            int colorTo = getResources().getColor(R.color.colorPrimary);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(800); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    findViewById(R.id.collapsingToolbar).setBackgroundColor((int) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();
        }

        private void deleteSelectedItems() {
            // Get a reference to the database
            DbHelper db = new DbHelper(ScheduleDetailActivity.this);

            // Get a cursor by getting the TaskData
            // Which should match the list view of the TasksFragment
            Cursor cursor = db.getAllNoteData();

            // Delete all the selected items based on the itemIDs
            // Stored in the array list
            for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                if (cursor.moveToPosition(CAMselectedItemsList.get(i))) {
                    db.deleteNoteItem(cursor.getInt(cursor.getColumnIndex(DbContract.NotesEntry._ID)));
                }
            }

            cursor.close();

            // Get the list view's current mTasksAdapter, clear it,
            // and query the database again for the current day
            // data, then notify the mTasksAdapter for the changes
            ArrayAdapter adapter = (ArrayAdapter) notesList.getAdapter();
            adapter.clear();
            adapter.addAll(db.getNoteDataArray());
            adapter.notifyDataSetChanged();

            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        private void editSelectedItem(int position) {
            // Ensure that only one item is selected
            if (CAMselectedItemsList.size() == 1) {
                // Initialise intent data variables
                int id;
                String title;
                String classTitle;
                String classType;
                String sharer;
                String description;
                String attachment;
                float dueDate;
                float reminderDate;
                float reminderTime;

                // Get a reference to the database and
                // Get a cursor of the Task Data
                DbHelper db = new DbHelper(ScheduleDetailActivity.this);
                Cursor cursor = db.getUncompletedTaskData();

                // Move the cursor to the position of the selected item
                if (cursor.moveToPosition(CAMselectedItemsList.get(0))) {
                    // Get its Data
                    id = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID));
                    title = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
                    classTitle = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS));
                    classType = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE));
                    description = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION));
                    attachment = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
                    dueDate = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE));
                    reminderDate = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE));
                    reminderTime = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME));
                    cursor.close();

                    // Create an intent to NewScheduleActivity and include the selected
                    // item's id, category, and an edit flag as extras
                    Intent intent = new Intent(ScheduleDetailActivity.this, NewTaskActivity.class);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ID), id);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_TITLE), title);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), classTitle);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_TYPE), classType);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DESCRIPTION), description);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ATTACHMENT), attachment);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DUEDATE), dueDate);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ALARM_DATE), reminderDate);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ALARM_TIME), reminderTime);
                    intent.putExtra("position", position);
                    intent.putExtra(getResources().getString(R.string.INTENT_FLAG_EDIT), true);

                    // Clear the selected items list, exit the CAM and launch the activity
                    CAMselectedItemsList.clear();
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                    startActivity(intent);
                }
            }

            // If delete than one item was selected, throw a warning log
            else {
                Log.w(LOG_TAG, "Cancelling event due to delete than one item selected");
            }
        }

    }

}
