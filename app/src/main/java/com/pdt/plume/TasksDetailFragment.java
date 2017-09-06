package com.pdt.plume;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import com.pdt.plume.data.DbContract.TasksEntry;


/**
 * A simple {@link Fragment} subclass.
 */
public class TasksDetailFragment extends Fragment {

    // Constantly used variables
    String LOG_TAG = TasksDetailActivity.class.getSimpleName();
    Utility utility = new Utility();
    ShareActionProvider mShareActionProvider;
    private static boolean active = false;
    int position = 0;

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
    long duedateValue;
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

    View rootview;

    // Required empty public constructor
    public TasksDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            taskCompleteListener = (OnTaskCompleteListener) context;
            taskDeleteListener = (OnTaskDeleteListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onTaskActionListener");
        }
    }

    OnTaskCompleteListener taskCompleteListener;
    public interface OnTaskCompleteListener {
        void OnTaskComplete(int ID, String fID);
    }

    OnTaskDeleteListener taskDeleteListener;
    public interface OnTaskDeleteListener {
        void OnTaskDelete(int ID, String fID);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.task_detail_dialog_delete_confirm))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mFirebaseUser != null) {
                                    final DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(mUserId).child("tasks")
                                            .child(firebaseID);
                                    final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                                    // Delete icon/photos from storage if applicable
                                    StorageReference iconRef = storageRef.child(mUserId).child("tasks").child(firebaseID);
                                    iconRef.delete();
                                    taskRef.removeValue();
                                } else {
                                    // Delete from SQLite
                                    DbHelper dbHelper = new DbHelper(getContext());
                                    dbHelper.deleteTaskItem(TasksDetailFragment.this.id);
                                }

                                // Interface here
                                taskDeleteListener.OnTaskDelete(id, firebaseID);
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
                return true;

            case R.id.action_edit:
                final Intent intent = new Intent(getActivity(), NewTaskActivity.class);
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
                    intent.putExtra("photo", builder.toString());
//                    intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_ATTACHMENT), attachment);
                    intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DUEDATE), duedateValue);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_ID), id);
                    intent.putExtra(getString(R.string.INTENT_FLAG_EDIT), true);
                    startActivity(intent);
                }
                return true;

            case R.id.action_time:
//                startTimer();
                return true;

            default: return false;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (rootview != null && isAdded()) getData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_tasks_detail, container, false);

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Set the mark as done button listener
        markAsDoneView = (TextView) rootview.findViewById(R.id.mark_as_done);
        markAsDoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptCompleteTask();
            }
        });

        initialiseTheme();

        if (isAdded()) getData();

        return rootview;
    }

    private void getData() {
        Bundle args = getArguments();
        if (args != null) {
            position = args.getInt(getString(R.string.INTENT_EXTRA_POSITION));
            FLAG_TASK_COMPLETED = args.getBoolean(getString(R.string.INTENT_FLAG_COMPLETED), false);
            int id = args.getInt(getString(R.string.INTENT_EXTRA_ID), 0);

            if (mFirebaseUser != null) {
                // Get the data from Firebase
                firebaseID = args.getString("id");
                final DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("tasks").child(firebaseID);
                taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!isAdded())
                            return;
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
                        duedate = utility.formatDateString(getContext(), c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                        // Get the photo data
                        long photoCount = dataSnapshot.child("photos").child("local").getChildrenCount();
                        for (DataSnapshot photoSnapshot : dataSnapshot.child("photos").child("local").getChildren()) {
                            photoUris.add(Uri.parse(photoSnapshot.getValue(String.class)));
                        }
                        if (photoCount > 0) {
                            // Add in the views for the photos
                            for (int i = 0; i < photoUris.size(); i++) {
                                int wh = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 156, getResources().getDisplayMetrics()));
                                LinearLayout photosLayout = (LinearLayout) rootview.findViewById(R.id.photos_layout);

                                final CardView cardView = new CardView(getContext());
                                cardView.setLayoutParams(new LinearLayout.LayoutParams(wh, wh));
                                cardView.setElevation(24f);
                                photosLayout.addView(cardView);

                                final ImageView photo = new ImageView(getContext());
                                photo.setImageURI(photoUris.get(i));

                                photo.setLayoutParams(new CardView.LayoutParams
                                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                photo.setId(Utility.generateUniqueId());
                                cardView.addView(photo);

                                photosLayout.setVisibility(View.VISIBLE);

                                // Add the listener
                                final int finalI = i;
                                cardView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent pictureIntent = new Intent(getContext(), PictureActivity.class);
                                        pictureIntent.putExtra(getString(R.string.INTENT_EXTRA_PATH), photoUris.get(finalI).toString());
                                        photo.setTransitionName("transition");
                                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation
                                                (getActivity(), photo, photo.getTransitionName()).toBundle();
                                        startActivity(pictureIntent, bundle);
                                    }
                                });
                            }
                        } else {
                            // Download the photo data
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();
                            i = 0;
                            for (final DataSnapshot photoUriSnapshot : dataSnapshot.child("photos").child("cloud").getChildren()) {
                                StorageReference photosRef = storageRef.child(photoUriSnapshot.getValue(String.class));
                                final long ONE_MEGABYTE = 1024 * 1024;
                                photosRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        // Save the file locally
                                        File file = new File(photoUris.get(i).toString());
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
                                        LinearLayout photosLayout = (LinearLayout) rootview.findViewById(R.id.photos_layout);
                                        ImageView photo = new ImageView(getContext());
                                        photo.setImageURI(Uri.fromFile(file));
                                        int width = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getResources().getDisplayMetrics()));
                                        photo.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
                                        photo.setPadding(4, 0, 4, 0);
                                        photo.setId(Utility.generateUniqueId());
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
                DbHelper dbHelper = new DbHelper(getContext());
                Cursor cursor;
                if (args.containsKey("_ID")) {
                    cursor = dbHelper.getTaskById(args.getInt("_ID", 0));
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
                    c.setTimeInMillis((long) cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE)));
                    duedate = utility.formatDateString(getContext(), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                    applyDataToUI();

                    // Set the photo field data
                    LinearLayout photosLayout = (LinearLayout) rootview.findViewById(R.id.photos_layout);
                    for (int i = 0; i < photos.length; i++) {
                        if (photos[i].length() > 1) {
                            final Uri photoUri = Uri.parse(photos[i]);
                            photoUris.add(photoUri);

                            // Add in the views for the photos
                            int wh = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 156, getResources().getDisplayMetrics()));

                            final CardView cardView = new CardView(getContext());
                            cardView.setLayoutParams(new LinearLayout.LayoutParams(wh, wh));
                            cardView.setElevation(24f);
                            photosLayout.addView(cardView);

                            final ImageView photo = new ImageView(getContext());
                            photo.setImageURI(photoUri);

                            photo.setLayoutParams(new CardView.LayoutParams
                                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            photo.setId(Utility.generateUniqueId());
                            cardView.addView(photo);

                            photosLayout.setVisibility(View.VISIBLE);

                            // Add the listener
                            cardView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent pictureIntent = new Intent(getContext(), PictureActivity.class);
                                    pictureIntent.putExtra(getString(R.string.INTENT_EXTRA_PATH), photoUri.toString());
                                    photo.setTransitionName("transition");
                                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation
                                            (getActivity(), photo, photo.getTransitionName()).toBundle();
                                    startActivity(pictureIntent, bundle);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    private void applyDataToUI() {
        // Get references to the UI elements
        ImageView icon = (ImageView) rootview.findViewById(R.id.icon);
        ImageView icon2 = (ImageView) rootview.findViewById(R.id.icon2);
        TextView titleTextview = (TextView) rootview.findViewById(R.id.title);
        TextView subtitleTextview = (TextView) rootview.findViewById(R.id.collapsingToolbarSubtitle);
        TextView duedateTextview = (TextView) rootview.findViewById(R.id.task_detail_duedate);
        TextView descriptionTextview = (TextView) rootview.findViewById(R.id.task_detail_description);
//        TextView attachmentTextview = (TextView) rootview.findViewById(R.id.task_detail_attachment);

        // Apply the data to the UI
        if (iconUri != null)
            if (iconUri.contains("android.resource://com.pdt.plume"))
                icon.setImageURI(Uri.parse(iconUri));
            else icon2.setImageURI(Uri.parse(iconUri));
        titleTextview.setText(title);
        if (subtitle.equals("") || subtitle.equals(" "))
            subtitleTextview.setVisibility(View.GONE);
        else subtitleTextview.setText(subtitle);
        duedateTextview.setText(getString(R.string.due, duedate));
        descriptionTextview.setText(description);

        if (description.length() == 0)
            rootview.findViewById(R.id.task_detail_description).setVisibility(View.GONE);
        else rootview.findViewById(R.id.task_detail_description).setVisibility(View.VISIBLE);

        final Uri ParsedIconUri = Uri.parse(iconUri);

        // TODO: Bring back the revision timer
        fieldTimer = (TextView) rootview.findViewById(R.id.task_detail_timer);
    }

    private void initialiseTheme() {
        TextView titleTextview = (TextView) rootview.findViewById(R.id.title);
        TextView subtitleTextview = (TextView) rootview.findViewById(R.id.collapsingToolbarSubtitle);
        TextView duedateTextview = (TextView) rootview.findViewById(R.id.task_detail_duedate);
        TextView descriptionTextview = (TextView) rootview.findViewById(R.id.task_detail_description);

        // Initialise the theme variables
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent));

        int backgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), getResources().getColor(R.color.backgroundColor));
        Color.colorToHSV(backgroundColor, hsv);
        hsv[2] *= 0.9f;
        int darkenedBackgroundColor = Color.HSVToColor(hsv);
        rootview.findViewById(R.id.container).setBackgroundColor(darkenedBackgroundColor);

        int textColor = preferences.getInt(getString(R.string.KEY_THEME_TITLE_COLOUR), getResources().getColor(R.color.gray_900));
        Color.colorToHSV(textColor, hsv);
        hsv[2] *= 0.8f;
        int darkTextColor = Color.HSVToColor(hsv);
        titleTextview.setTextColor(textColor);
        subtitleTextview.setTextColor(darkTextColor);
        duedateTextview.setTextColor(darkTextColor);
        descriptionTextview.setTextColor(darkTextColor);

        markAsDoneView.setTextColor(mPrimaryColor);

        if (FLAG_TASK_COMPLETED) {
            markAsDoneView.setText(getString(R.string.mark_as_undone));
            markAsDoneView.setTextColor(getResources().getColor(R.color.red_500));
        }
    }

    private void promptCompleteTask() {
        if (FLAG_TASK_COMPLETED) {
            // ACTION RESTORE TASK
            new AlertDialog.Builder(getContext())
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
                                DbHelper dbHelper = new DbHelper(getContext());
                                Cursor cursorTasks = dbHelper.getTaskById(TasksDetailFragment.this.id);

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

                                    dbHelper.updateTaskItem(getContext(), TasksDetailFragment.this.id, title, classTitle, classType, description, attachment,
                                            duedate, reminderdate, remindertime,
                                            icon, pictureStringList, false);
                                }

                                cursorTasks.close();
                            }


                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        }
                    }).show();
        } else {
            // ACTION COMPLETE TASK
            new AlertDialog.Builder(getContext())
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
                                DbHelper dbHelper = new DbHelper(getContext());
                                Cursor cursorTasks = dbHelper.getTaskById(TasksDetailFragment.this.id);

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

                                    dbHelper.updateTaskItem(getContext(), TasksDetailFragment.this.id, title, classTitle, classType, description, attachment,
                                            duedate, reminderdate, remindertime,
                                            icon, pictureStringList, true);
                                }

                                cursorTasks.close();
                            }


                            taskCompleteListener.OnTaskComplete(id, firebaseID);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

}
