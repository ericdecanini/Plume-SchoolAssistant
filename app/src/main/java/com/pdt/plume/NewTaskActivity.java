package com.pdt.plume;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pdt.plume.StaticRequestCodes.REQUEST_FILE_GET;
import static com.pdt.plume.StaticRequestCodes.REQUEST_IMAGE_CAPTURE;
import static com.pdt.plume.StaticRequestCodes.REQUEST_IMAGE_GET_ICON;
import static com.pdt.plume.StaticRequestCodes.REQUEST_IMAGE_GET_PHOTO;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ALARM;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_INTENT;
import static com.pdt.plume.StaticRequestCodes.REQUEST_PERMISSION_CAMERA;
import static com.pdt.plume.StaticRequestCodes.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE;

public class NewTaskActivity extends AppCompatActivity
        implements IconPromptDialog.iconDialogListener {

    // Constantly used variables
    String LOG_TAG = NewTaskActivity.class.getSimpleName();
    Utility utility = new Utility();
    Handler handler = new Handler();
    int i = 0;
    boolean active = true;
    boolean isTablet = false;

    // UI Elements
    EditText fieldTitle;
    View fieldShared;
    CheckBox fieldSharedCheckbox;
    EditText fieldDescription;
    ImageView fieldIcon;

    View fieldClassDropdown;
    TextView fieldClassTextview;
    View fieldTypeDropdown;
    TextView fieldTypeTextview;

    TextView fieldTakePhotoText;
    ImageView fieldTakePhotoIcon;
    View fieldDueDate;
    TextView fieldDueDateTextView;
    TextView fieldAttachFile;
    View fieldSetReminderDate;
    TextView fieldSetReminderDateTextview;
    View fieldSetReminderTime;
    TextView fieldSetReminderTimeTextview;

    // UI Data
    String iconUriString = "android.resource://com.pdt.plume/drawable/art_task_64dp";
    ArrayList<String> classTitleArray = new ArrayList<>();
    ArrayList<String> classTypeArray = new ArrayList<>();
    String classTitle = "None";
    String classType = "None";
    float dueDateMillis;
    ArrayList<Uri> photoUriList = new ArrayList<>();
    Uri mTempPhotoUri;

    long reminderDateMillis;
    long reminderTimeMillis;

    // Theme Variables
    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    String attachedFileUriString = "";

    // Firebase Variables
    String name;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;

    // Built-in Icons
    private Integer[] mThumbIds = {
            R.drawable.art_arts_64dp,
            R.drawable.art_biology_64dp,
            R.drawable.art_business_64dp,
            R.drawable.art_chemistry_64dp,
            R.drawable.art_childdevelopment_64dp,
            R.drawable.art_class_64dp,
            R.drawable.art_computing_64dp,
            R.drawable.art_cooking_64dp,
            R.drawable.art_creativestudies_64dp,
            R.drawable.art_drama_64dp,
            R.drawable.art_engineering_64dp,
            R.drawable.art_english_64dp,
            R.drawable.art_french_64dp,
            R.drawable.art_geography_64dp,
            R.drawable.art_graphics_64dp,
            R.drawable.art_hospitality_64dp,
            R.drawable.art_ict_64dp,
            R.drawable.art_maths_64dp,
            R.drawable.art_media_64dp,
            R.drawable.art_music_64dp,
            R.drawable.art_pe_64dp,
            R.drawable.art_physics_64dp,
            R.drawable.art_psychology_64dp,
            R.drawable.art_re_64dp,
            R.drawable.art_science_64dp,
            R.drawable.art_spanish_64dp,
            R.drawable.art_task_64dp,
            R.drawable.art_woodwork_64dp
    };


    // Intent Data
    boolean FLAG_EDIT = false;
    int editId = -1;
    String firebaseEditId = "";
    boolean customIconUploaded = false;

    boolean LAUNCHED_NEW_CLASS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        getSupportActionBar().setElevation(0f);
        isTablet = getResources().getBoolean(R.bool.isTablet);
        int minHeight = getWindowManager().getDefaultDisplay().getHeight();
        if (isTablet) findViewById(R.id.master_layout).setMinimumHeight(minHeight);

        // Initialise Firebase and SQLite
        DbHelper dbHelper = new DbHelper(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            mUserId = mFirebaseUser.getUid();
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("nickname").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name = dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else name = "";

        // Get references to the UI elements
        fieldTitle = (EditText) findViewById(R.id.field_new_task_title);
        fieldIcon = (ImageView) findViewById(R.id.field_new_task_icon);
        fieldClassDropdown = findViewById(R.id.field_class_dropdown);
        fieldClassTextview = (TextView) findViewById(R.id.field_class_textview);
        fieldShared = findViewById(R.id.field_shared_layout);
        fieldSharedCheckbox = (CheckBox) findViewById(R.id.field_shared_checkbox);
        fieldTypeDropdown = findViewById(R.id.field_type_dropdown);
        fieldTypeTextview = (TextView) findViewById(R.id.field_type_textview);
        fieldDescription = (EditText) findViewById(R.id.field_new_task_description);
        fieldTakePhotoText = (TextView) findViewById(R.id.take_photo_text);
        fieldTakePhotoIcon = (ImageView) findViewById(R.id.take_photo_icon);
        if (!isTablet) fieldDueDate = findViewById(R.id.field_new_task_duedate);
        else fieldDueDate = findViewById(R.id.field_new_task_duedate_textview);
        fieldDueDateTextView = (TextView) findViewById(R.id.field_new_task_duedate_textview);
        if (!isTablet) fieldSetReminderDate = findViewById(R.id.field_new_task_reminder_date);
        else fieldSetReminderDate = findViewById(R.id.field_new_task_reminder_date_textview);
        fieldSetReminderDateTextview = (TextView) findViewById(R.id.field_new_task_reminder_date_textview);
        if (!isTablet) fieldSetReminderTime = findViewById(R.id.field_new_task_reminder_time);
        else fieldSetReminderTime = findViewById(R.id.field_new_task_reminder_time_textview);
        fieldSetReminderTimeTextview = (TextView) findViewById(R.id.field_new_task_reminder_time_textview);

        if (mFirebaseUser == null)
            if (!isTablet) fieldSharedCheckbox.setVisibility(View.GONE);
            else fieldShared.setVisibility(View.GONE);
        if (isTablet)
            fieldShared.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fieldSharedCheckbox.toggle();
                }
            });

        // Initialise the dropdown box default data
        classTitle = getString(R.string.none);
        classType = getString(R.string.none);

        // Set the listeners of the UI
        fieldIcon.setOnClickListener(showIconPrompt());
        fieldClassDropdown.setOnClickListener(listener());
        fieldTypeDropdown.setOnClickListener(listener());
        fieldTakePhotoText.setOnClickListener(listener());
        if (fieldTakePhotoIcon != null)
        fieldTakePhotoIcon.setOnClickListener(listener());
//        fieldAttachFile.setOnClickListener(listener());
        fieldSetReminderDate.setOnClickListener(listener());
        fieldSetReminderTime.setOnClickListener(listener());
        fieldDueDate.setOnClickListener(listener());


        // Initialise the class dropdown data
        if (mFirebaseUser != null) {
            // Get schedule data from Firebase
            final DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("classes");
            classesRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    classTitleArray.add(dataSnapshot.getKey());
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
            });
        } else {
            // Get schedule data from SQLite
            Cursor scheduleCursor = dbHelper.getAllScheduleData();

            // Scan through the cursor and add in each class title into the array list
            if (scheduleCursor.moveToFirst()) {
                for (int i = 0; i < scheduleCursor.getCount(); i++) {
                    String classTitle = scheduleCursor.getString(scheduleCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
                    if (!classTitleArray.contains(classTitle))
                        classTitleArray.add(classTitle);
                    scheduleCursor.moveToNext();
                }
            }
            scheduleCursor.close();
        }

        // Initialise the taskType dropdown data
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_homework));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_test));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_revision));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_project));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_detention));
        classTypeArray.add(getString(R.string.field_dropdown_type_menu_item_other));

        // Check if the activity was started by an edit action
        // If the intent is not null the activity must have
        // been started through an edit action
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                // Get the task data sent through the intent
                String icon = extras.getString("icon");
                String title = extras.getString(getString(R.string.INTENT_EXTRA_TITLE));
                String classTitle = extras.getString(getString(R.string.INTENT_EXTRA_CLASS));
                String classType = extras.getString(getString(R.string.INTENT_EXTRA_TYPE));
                String description = extras.getString(getString(R.string.INTENT_EXTRA_DESCRIPTION));
                String attachment = "";
                long dueDate = extras.getLong(getString(R.string.INTENT_EXTRA_DUEDATE));
                long reminderDate = extras.getLong(getString(R.string.INTENT_EXTRA_ALARM_DATE));
                long reminderTime = extras.getLong(getString(R.string.INTENT_EXTRA_ALARM_TIME));

                int position = extras.getInt("position");
                FLAG_EDIT = extras.getBoolean(getString(R.string.INTENT_FLAG_EDIT), false);

                if (FLAG_EDIT) {
                    // Get the id depending on where the data came from
                    if (mFirebaseUser != null)
                        firebaseEditId = intent.getStringExtra("id");
                    else editId = intent.getIntExtra(getString(R.string.INTENT_EXTRA_ID), -1);

                    fieldIcon.setImageURI(Uri.parse(icon));
                    fieldTitle.setText(title);
                    fieldTitle.setSelection(fieldTitle.getText().length());
                    fieldDescription.setText(description);

                    // Get photo data
                    if (mFirebaseUser != null) {
                        DatabaseReference photosRef = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(mUserId).child("tasks").child(firebaseEditId)
                                .child("photos");
                        DatabaseReference localRef = photosRef.child("local");
                        localRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long childrenCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot uriSnapshot : dataSnapshot.getChildren())
                                    photoUriList.add(Uri.parse(uriSnapshot.getValue(String.class)));
                                if (childrenCount > 0) {
                                    // Add in the views for the photos
                                    for (int i = 0; i < photoUriList.size(); i++) {
                                        // Add the photo as a new image view
                                        final RelativeLayout relativeLayout = new RelativeLayout(NewTaskActivity.this);
                                        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

                                        ImageView photo = new ImageView(NewTaskActivity.this);
                                        photo.setImageURI(photoUriList.get(i));
                                        int width = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getResources().getDisplayMetrics()));
                                        photo.setLayoutParams(new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
                                        photo.setPadding(4, 0, 4, 0);
                                        photo.setId(Utility.generateViewId());

                                        final LinearLayout photosLayout = (LinearLayout) findViewById(R.id.photos_layout);
                                        photosLayout.setVisibility(View.VISIBLE);
                                        photosLayout.addView(relativeLayout);
                                        relativeLayout.addView(photo);

                                        // Add the cancel button to remove the photo
                                        final ImageView cancel = new ImageView(NewTaskActivity.this);
                                        cancel.setImageResource(R.drawable.ic_cancel);
                                        int wh = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
                                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(wh, wh);
                                        params.addRule(RelativeLayout.ALIGN_END, photo.getId());
                                        params.addRule(RelativeLayout.ALIGN_RIGHT, photo.getId());
                                        params.addRule(RelativeLayout.ALIGN_TOP, photo.getId());
                                        cancel.setLayoutParams(params);
                                        relativeLayout.addView(cancel);
                                        cancel.setVisibility(View.GONE);
                                        final int finalI = i;
                                        cancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                photosLayout.removeView(relativeLayout);
                                                photoUriList.remove(photoUriList.get(finalI));
                                            }
                                        });

                                        // Add the click listener of the photo
                                        cancel.setTag("null");
                                        final Runnable runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                cancel.setTag("null");
                                                cancel.setVisibility(View.GONE);
                                            }
                                        };
                                        photo.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (cancel.getTag().equals("cancelVisible")) {
                                                    cancel.setTag("null");
                                                    cancel.setVisibility(View.GONE);
                                                    handler.removeCallbacks(runnable);
                                                } else {
                                                    cancel.setVisibility(View.VISIBLE);
                                                    Runnable runnable1 = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            View otherCancels = photosLayout.findViewWithTag("cancelVisible");
                                                            if (otherCancels != null) {
                                                                otherCancels.setVisibility(View.GONE);
                                                                otherCancels.setTag("null");
                                                                handler.post(this);
                                                            } else cancel.setTag("cancelVisible");
                                                        }
                                                    };

                                                    handler.post(runnable1);
                                                    handler.postDelayed(runnable, 2000);
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    // Download the photos
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
                                                File file = new File(photoUriList.get(i).toString());
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
                                                photoUriList.add(Uri.fromFile(file));
                                                LinearLayout photosLayout = (LinearLayout) findViewById(R.id.photos_layout);
                                                ImageView photo = new ImageView(NewTaskActivity.this);
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
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        // Set the saved photo's bitmap
                        // PHOTO DISABLED FOR THE BETA
//                    if (!photo.equals("")) {
//                        fieldPhotoSlot.setImageURI(Uri.parse(photo));
//                        mCurrentPhotoPathString = photo;
//                        mCurrentPhotoPath = Uri.parse(mCurrentPhotoPathString);
//                    }

                    }

                    // Photo data from SQLite
                    String photoString = intent.getStringExtra("photo");
                    if (photoString != null) {
                        String[] photos = photoString.split("#seperate#");
                        // Add in the views for the photos
                        for (int i = 0; i < photos.length; i++) {
                            // Add the photo as a new image view
                            final Uri photoUri = Uri.parse(photos[i]);
                            photoUriList.add(photoUri);
                            final RelativeLayout relativeLayout = new RelativeLayout(NewTaskActivity.this);
                            relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

                            ImageView photo = new ImageView(NewTaskActivity.this);
                            photo.setImageURI(photoUri);
                            int width = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getResources().getDisplayMetrics()));
                            photo.setLayoutParams(new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
                            photo.setPadding(4, 0, 4, 0);
                            photo.setId(Utility.generateViewId());

                            final LinearLayout photosLayout = (LinearLayout) findViewById(R.id.photos_layout);
                            photosLayout.setVisibility(View.VISIBLE);
                            photosLayout.addView(relativeLayout);
                            relativeLayout.addView(photo);

                            // Add the cancel button to remove the photo
                            final ImageView cancel = new ImageView(NewTaskActivity.this);
                            cancel.setImageResource(R.drawable.ic_cancel);
                            int wh = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(wh, wh);
                            params.addRule(RelativeLayout.ALIGN_END, photo.getId());
                            params.addRule(RelativeLayout.ALIGN_RIGHT, photo.getId());
                            params.addRule(RelativeLayout.ALIGN_TOP, photo.getId());
                            cancel.setLayoutParams(params);
                            relativeLayout.addView(cancel);
                            cancel.setVisibility(View.GONE);
                            final int finalI = i;
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    photosLayout.removeView(relativeLayout);
                                    photoUriList.remove(photoUri);
                                }
                            });

                            // Add the click listener of the photo
                            cancel.setTag("null");
                            final Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    cancel.setTag("null");
                                    cancel.setVisibility(View.GONE);
                                }
                            };
                            photo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (cancel.getTag().equals("cancelVisible")) {
                                        cancel.setTag("null");
                                        cancel.setVisibility(View.GONE);
                                        handler.removeCallbacks(runnable);
                                    } else {
                                        cancel.setVisibility(View.VISIBLE);
                                        Runnable runnable1 = new Runnable() {
                                            @Override
                                            public void run() {
                                                View otherCancels = photosLayout.findViewWithTag("cancelVisible");
                                                if (otherCancels != null) {
                                                    otherCancels.setVisibility(View.GONE);
                                                    otherCancels.setTag("null");
                                                    handler.post(this);
                                                } else cancel.setTag("cancelVisible");
                                            }
                                        };

                                        handler.post(runnable1);
                                        handler.postDelayed(runnable, 2000);
                                    }
                                }
                            });
                        }
                    }

                    // Set the current state of the dropdown text views
                    if (classTitle.equals(""))
                        fieldClassTextview.setText(getString(R.string.none));
                    else fieldClassTextview.setText(classTitle);
                    this.classTitle = classTitle;
                    if (classType.equals(""))
                        fieldTypeTextview.setText(getString(R.string.none));
                    else fieldTypeTextview.setText(classType);
                    this.classType = classType;
                    this.iconUriString = icon;

                    // Set the current state of the due date
                    if (dueDate != 0f) {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis((long) dueDate);
                        float dueDateYear = c.get(Calendar.YEAR);
                        float dueDateMonth = c.get(Calendar.MONTH);
                        float dueDateDay = c.get(Calendar.DAY_OF_MONTH);
                        fieldDueDateTextView.setText(utility.formatDateString(this, ((int) dueDateYear), ((int) dueDateMonth), ((int) dueDateDay)));
                        this.dueDateMillis = c.getTimeInMillis();
                    }

                    // Set the current state of the reminder date and time
                    if (reminderDate != 0f) {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis((long) reminderDate);
                        float reminderDateYear = c.get(Calendar.YEAR);
                        float reminderDateMonth = c.get(Calendar.MONTH);
                        float reminderDateDay = c.get(Calendar.DAY_OF_MONTH);

                        Calendar today = Calendar.getInstance();
                        if (today.get(Calendar.DAY_OF_MONTH) == reminderDateDay && today.get(Calendar.MONTH) == reminderDateMonth
                                && today.get(Calendar.YEAR) == reminderDateYear)
                            fieldSetReminderDateTextview.setText(getString(R.string.today));
                        else {
                            today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH) + 1);
                            if (today.get(Calendar.DAY_OF_MONTH) == reminderDateDay && today.get(Calendar.MONTH) == reminderDateMonth
                                    && today.get(Calendar.YEAR) == reminderDateYear)
                                fieldSetReminderDateTextview.setText(getString(R.string.tomorrow));
                            else
                                fieldSetReminderDateTextview.setText(utility.formatDateString(this, ((int) reminderDateYear),
                                        ((int) reminderDateMonth), ((int) reminderDateDay)));
                        }
                        this.reminderDateMillis = c.getTimeInMillis();
                    } else {
                        fieldSetReminderDateTextview.setText(getString(R.string.none));
                        fieldSetReminderTime.setEnabled(false);
                        fieldSetReminderTimeTextview.setTextColor(getResources().getColor(R.color.gray_400));
                    }


                    if (reminderTime != 0f) {
                        fieldSetReminderTimeTextview.setText(utility.millisToHourTime(reminderTime));
                        this.reminderTimeMillis = reminderTime;
                    } else {
                        fieldSetReminderTimeTextview.setText(getString(R.string.none));
                    }

                    // ATTACHMENT WILL NOT BE INCLUDED IN BETA
//                    attachment = cursorEdit.getString(cursorEdit.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
//                    Uri filePathUri = Uri.parse(attachment);
//                    if (!attachment.equals("")) {
//                        Cursor returnCursor = getContentResolver().query(filePathUri, null, null, null, null);
//                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//                        if (returnCursor.moveToFirst()) {
//                            String fileName = returnCursor.getString(nameIndex);
//                            returnCursor.close();
//                            fieldAttachFile.setText(fileName);
//                            this.attachedFileUriString = filePathUri.toString();
//                        }
//                    }
                } else {
                    setDefaultData();
                    // Check if there is any data sent through the intent
                    classTitle = intent.getStringExtra(getString(R.string.INTENT_EXTRA_CLASS));
                    if (classTitle != null) {
                        this.classTitle = classTitle;
                        fieldClassTextview.setText(classTitle);
                        fieldTitle.setText(classTitle);
                        fieldTitle.setSelection(0, classTitle.length());
                    }
                }
            } else setDefaultData();
        } else setDefaultData();
    }

    private void setDefaultData() {
        // Set any default data
        Resources resources = getResources();
        int resId = R.drawable.art_task_64dp;
        Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId)
                + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId));
        iconUriString = drawableUri.toString();

        // Reminder Date and Time
        fieldSetReminderDateTextview.setText(getString(R.string.none));
        if (!isTablet)
        fieldSetReminderTime.setEnabled(false);
        else fieldSetReminderTimeTextview.setEnabled(false);
        fieldSetReminderTimeTextview.setTextColor(getResources().getColor(R.color.gray_400));
        fieldSetReminderTimeTextview.setText(getString(R.string.none));

        // Dates
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 7);
        dueDateMillis = c.getTimeInMillis();
        fieldDueDateTextView.setText(utility.formatDateString(this,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent));

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }
        if (!isTablet)
            fieldTitle.setBackgroundColor(mPrimaryColor);
        else fieldTitle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_700)));

        if (LAUNCHED_NEW_CLASS) {
            DbHelper dbHelper = new DbHelper(this);
            Cursor cursor = dbHelper.getAllScheduleData();
            if (cursor.moveToLast()) {
                String newClassTitle = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
                fieldClassTextview.setText(newClassTitle);
                classTitle = newClassTitle;
                LAUNCHED_NEW_CLASS = false;
            }

            // Auto-fill the title editText if there isn't any user-inputted title yet
            String titleText = fieldTitle.getText().toString();
            if (titleText.equals(""))
                if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                    fieldTitle.setText("");
                else fieldTitle.setText(NewTaskActivity.this.classTitle);
            if (titleText.equals(classType) && NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                fieldTitle.setText(classType);
                // Check if another class was set before
            else if (classTitleArray.contains(titleText))
                if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                    fieldTitle.setText("");
                else fieldTitle.setText(NewTaskActivity.this.classTitle);
                // Check if the taskType was set before the class
            else if (classTypeArray.contains(titleText))
                fieldTitle.setText(NewTaskActivity.this.classTitle + " " + titleText);
                // Check if the title editText contains text as a result
                // of previously using the dropdown lists
            else {
                String[] splitFieldTitle = titleText.split(" ");
                if (splitFieldTitle.length == 2 && classTitleArray.contains(splitFieldTitle[0]) && classTypeArray.contains(splitFieldTitle[1])) {
                    if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                        fieldTitle.setText(splitFieldTitle[1]);
                    else
                        fieldTitle.setText(NewTaskActivity.this.classTitle + " " + splitFieldTitle[1]);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Without this, the up button will not do anything and return the error 'Cancelling event due to no window focus'
            case android.R.id.home:
                finish();
                break;

            // Insert inputted data into the database and terminate the activity
            case R.id.action_done:
                // Validate that the title field is not empty
                if (fieldTitle.getText().toString().equals("")) {
                    Toast.makeText(NewTaskActivity.this, getString(R.string.new_tasks_toast_validation_title_not_found), Toast.LENGTH_SHORT).show();
                    return false;
                }
                Intent intent = new Intent(this, MainActivity.class);

                // Determine where the NewTaskActivity was started from and return to the corresponding activity
                if (getIntent().hasExtra(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE))) {
                    intent.putExtra(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE), getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE));
                    intent.putExtra(getString(R.string.INTENT_EXTRA_POSITION),
                            getIntent().getIntExtra(getString(R.string.INTENT_EXTRA_POSITION), 0));
                } else intent.putExtra(getString(R.string.INTENT_FLAG_RETURN_TO_TASKS), getString(R.string.INTENT_FLAG_RETURN_TO_TASKS));
                try {
                    if (insertTaskDataIntoDatabase()) {
                        // Upload the icon to the cloud if applicable
                        if (mFirebaseUser != null && customIconUploaded) {
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();
                            StorageReference iconRef = storageRef.child(mUserId + "/tasks/" + fieldTitle.getText().toString());

                            fieldIcon.setDrawingCacheEnabled(true);
                            fieldIcon.buildDrawingCache();
                            Bitmap bitmap = fieldIcon.getDrawingCache();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();

                            UploadTask uploadTask = iconRef.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // TODO: Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // TODO: Handle successful uploads if action is required
                                }
                            });

                        }

                        startActivity(intent);
                        return true;
                    } else {
                        Log.w(LOG_TAG, "Error creating new task");
                        finish();
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewTaskActivity.this);
                    builder.setTitle(getString(R.string.field_new_photo_dialog_title))
                            .setItems(R.array.field_new_photo_dialog_items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            try {
                                                dispatchTakePictureIntent();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                        case 1:
                                            dispatchSelectPhotoIntent();
                                            break;
                                    }
                                }
                            }).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;

            case REQUEST_PERMISSION_CAMERA:
                try {
                    dispatchTakePictureIntent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    // This method is called when a file is selected after the ACTION_GET
    // intent was called from the attach file action
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Attachment Upload
        if (requestCode == REQUEST_FILE_GET && resultCode == RESULT_OK) {
            // Get the Uri and UriString from the intent and save its global variable
            Uri filePathUri = data.getData();
            attachedFileUriString = data.getDataString();

            // Get the filename of the file and set the field's text to that
            Cursor returnCursor = getContentResolver().query(filePathUri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String fileName = returnCursor.getString(nameIndex);
            returnCursor.close();
            fieldAttachFile.setText(fileName);
        }

        // Take Photo
        if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_GET_PHOTO && resultCode == RESULT_OK) {
            // Add the uri to the array list
            final Uri imageData = data.getData();
            photoUriList.add(imageData);

            // Add the photo as a new image view
            final RelativeLayout relativeLayout = new RelativeLayout(this);
            relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

            ImageView photo = new ImageView(this);
            photo.setImageURI(imageData);
            int width = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getResources().getDisplayMetrics()));
            photo.setLayoutParams(new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
            photo.setPadding(4, 0, 4, 0);
            photo.setId(Utility.generateViewId());

            final LinearLayout photosLayout = (LinearLayout) findViewById(R.id.photos_layout);
            photosLayout.setVisibility(View.VISIBLE);
            photosLayout.addView(relativeLayout);
            relativeLayout.addView(photo);

            // Add the cancel button to remove the photo
            final ImageView cancel = new ImageView(this);
            cancel.setImageResource(R.drawable.ic_cancel);
            int wh = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(wh, wh);
            params.addRule(RelativeLayout.ALIGN_END, photo.getId());
            params.addRule(RelativeLayout.ALIGN_RIGHT, photo.getId());
            params.addRule(RelativeLayout.ALIGN_TOP, photo.getId());
            cancel.setLayoutParams(params);
            relativeLayout.addView(cancel);
            cancel.setVisibility(View.GONE);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    photosLayout.removeView(relativeLayout);
                    photoUriList.remove(imageData);
                }
            });

            // Add the click listener of the photo
            cancel.setTag("null");
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    cancel.setTag("null");
                    cancel.setVisibility(View.GONE);
                }
            };
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cancel.getTag().equals("cancelVisible")) {
                        cancel.setTag("null");
                        cancel.setVisibility(View.GONE);
                        handler.removeCallbacks(runnable);
                    } else {
                        cancel.setVisibility(View.VISIBLE);
                        Runnable runnable1 = new Runnable() {
                            @Override
                            public void run() {
                                View otherCancels = photosLayout.findViewWithTag("cancelVisible");
                                if (otherCancels != null) {
                                    otherCancels.setVisibility(View.GONE);
                                    otherCancels.setTag("null");
                                    handler.post(this);
                                } else cancel.setTag("cancelVisible");
                            }
                        };

                        handler.post(runnable1);
                        handler.postDelayed(runnable, 2000);
                    }
                }
            });
        }

        // Custom Icon Upload
        if (requestCode == REQUEST_IMAGE_GET_ICON && resultCode == RESULT_OK) {
            Uri dataUri = data.getData();
            Bitmap setImageBitmap = null;

            iconUriString = dataUri.toString();

            try {
                setImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dataUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            fieldIcon.setImageBitmap(setImageBitmap);
            customIconUploaded = true;
        }
    }

    private void showClassDropdownMenu() {
        // Set up the dropdown menu on both views
        // Set up the class dropdown menu
        PopupMenu popupMenu = new PopupMenu(this, fieldClassDropdown);

        // Add the titles to the menu as well as the item to add a new class
        popupMenu.getMenu().add(getString(R.string.none));
        for (int i = 0; i < classTitleArray.size(); i++)
            popupMenu.getMenu().add(classTitleArray.get(i));
        popupMenu.getMenu().add(getString(R.string.add_new_class));

        // Set the ItemClickListener for the menu items
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Set the data to be later saved into the database
                NewTaskActivity.this.classTitle = item.getTitle().toString();

                // If Add New Class was selected, start NewScheduleActivity
                if (NewTaskActivity.this.classTitle.equals(getString(R.string.add_new_class))) {
                    LAUNCHED_NEW_CLASS = true;
                    Intent intent = new Intent(NewTaskActivity.this, NewScheduleActivity.class);
                    intent.putExtra("STARTED_BY_NEWTASKACTIVITY", true);
                    startActivity(intent);
                    return true;
                }

                // Auto-fill the title editText if there isn't any user-inputted title yet
                String titleText = fieldTitle.getText().toString();
                if (titleText.equals(""))
                    if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                        fieldTitle.setText("");
                    else fieldTitle.setText(NewTaskActivity.this.classTitle);
                if (titleText.equals(classType) && NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                    fieldTitle.setText(classType);
                    // Check if another class was set before
                else if (classTitleArray.contains(titleText))
                    if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                        fieldTitle.setText("");
                    else fieldTitle.setText(NewTaskActivity.this.classTitle);
                    // Check if the taskType was set before the class
                else if (classTypeArray.contains(titleText))
                    fieldTitle.setText(NewTaskActivity.this.classTitle + " " + titleText);
                    // Check if the title editText contains text as a result
                    // of previously using the dropdown lists
                else {
                    String[] splitFieldTitle = titleText.split(" ");
                    if (splitFieldTitle.length == 2 && classTitleArray.contains(splitFieldTitle[0]) && classTypeArray.contains(splitFieldTitle[1])) {
                        if (NewTaskActivity.this.classTitle.equals(getString(R.string.none)))
                            fieldTitle.setText(splitFieldTitle[1]);
                        else
                            fieldTitle.setText(NewTaskActivity.this.classTitle + " " + splitFieldTitle[1]);
                    }
                }
                // Set the dropdown list text to the selected item
                fieldClassTextview.setText(NewTaskActivity.this.classTitle);

                return true;
            }
        });

        popupMenu.show();
    }

    private void showTypeDropdownMenu() {
        // Initialise and inflate the menu
        PopupMenu popupMenu = new PopupMenu(NewTaskActivity.this, fieldTypeDropdown);
        popupMenu.getMenuInflater().inflate(R.menu.menu_popup_type, popupMenu.getMenu());

        // Set the menu's ItemClickListener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Set the data to be later saved into the database
                NewTaskActivity.this.classType = item.getTitle().toString();

                // Auto-fill the title editText if there isn't any user-inputted title yet
                String titleText = fieldTitle.getText().toString();
                if (titleText.equals("") && !NewTaskActivity.this.classType.equals(getString(R.string.none)))
                    fieldTitle.setText(NewTaskActivity.this.classType);
                if (titleText.contains(classTitle) && NewTaskActivity.this.classType.equals(getString(R.string.none)))
                    fieldTitle.setText(classTitle);
                    // Check if another taskType was set before
                else if (classTypeArray.contains(titleText))
                    if (NewTaskActivity.this.classType.equals(getString(R.string.none)))
                        fieldTitle.setText("");
                    else fieldTitle.setText(NewTaskActivity.this.classType);
                    // Check if the taskType was set before the class
                else if (classTitleArray.contains(titleText)) {
                    fieldTitle.setText(titleText + " " + NewTaskActivity.this.classType);
                }
                // Check if the title editText contains text as a result
                // of previously using the dropdown lists
                else {
                    String[] splitFieldTitle = titleText.split(" ");
                    if (splitFieldTitle.length == 2 && classTitleArray.contains(splitFieldTitle[0]) && classTypeArray.contains(splitFieldTitle[1])) {
                        if (NewTaskActivity.this.classType.equals(getString(R.string.none)))
                            fieldTitle.setText(splitFieldTitle[0]);
                        else
                            fieldTitle.setText(splitFieldTitle[0] + " " + NewTaskActivity.this.classType);
                    }
                }

                // Set the dropdown list text to the selected item
                fieldTypeTextview.setText(NewTaskActivity.this.classType);

                return true;
            }
        });

        popupMenu.show();
    }

    private void showReminderDateDropdownMenu() {
        PopupMenu popupMenu = new PopupMenu(this, fieldSetReminderDate);
        popupMenu.getMenuInflater().inflate(R.menu.popup_reminder_date, popupMenu.getMenu());
        final Calendar c = Calendar.getInstance();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.dropdown_reminder_date_none:
                        fieldSetReminderDateTextview.setText(getString(R.string.none));
                        reminderDateMillis = 0;
                        fieldSetReminderTime.setEnabled(false);
                        fieldSetReminderTimeTextview.setTextColor(getResources().getColor(R.color.gray_400));
                        break;
                    case R.id.dropdown_reminder_date_today:
                        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                        fieldSetReminderDateTextview.setText(getString(R.string.today));
                        reminderDateMillis = c.getTimeInMillis();
                        fieldSetReminderTime.setEnabled(true);
                        if (!isTablet) fieldSetReminderTimeTextview.setTextColor(getResources().getColor(R.color.black_0_54));
                        else fieldSetReminderTimeTextview.setTextColor(getResources().getColor(R.color.gray_900));
                        if (reminderTimeMillis == 0) {
                            Calendar toGetTime = Calendar.getInstance();
                            int hourOfDay = toGetTime.get(Calendar.HOUR_OF_DAY) + 1;
                            int minute = toGetTime.get(Calendar.MINUTE);
                            reminderTimeMillis = utility.timeToMillis(hourOfDay, minute);
                            if (minute < 10)
                                fieldSetReminderTimeTextview.setText(hourOfDay + ":0" + minute);
                            else
                                fieldSetReminderTimeTextview.setText(hourOfDay + ":" + minute);
                        }
                        break;
                    case R.id.dropdown_reminder_date_tomorrow:
                        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH) + 1);
                        fieldSetReminderDateTextview.setText(getString(R.string.tomorrow));
                        reminderDateMillis = c.getTimeInMillis();
                        fieldSetReminderTime.setEnabled(true);
                        if (!isTablet) fieldSetReminderTimeTextview.setTextColor(getResources().getColor(R.color.black_0_54));
                        else fieldSetReminderTimeTextview.setTextColor(getResources().getColor(R.color.gray_900));
                        if (reminderTimeMillis == 0) {
                            Calendar toGetTime = Calendar.getInstance();
                            int hourOfDay = toGetTime.get(Calendar.HOUR_OF_DAY) + 1;
                            int minute = toGetTime.get(Calendar.MINUTE);
                            reminderTimeMillis = utility.timeToMillis(hourOfDay, minute) * 1000;
                            if (minute < 10)
                                fieldSetReminderTimeTextview.setText(hourOfDay + ":0" + minute);
                            else
                                fieldSetReminderTimeTextview.setText(hourOfDay + ":" + minute);
                        }
                        break;
                    case R.id.dropdown_reminder_date_setdate:
                        fieldSetReminderTime.setEnabled(true);
                        if (!isTablet) fieldSetReminderTimeTextview.setTextColor(getResources().getColor(R.color.black_0_54));
                        else fieldSetReminderTimeTextview.setTextColor(getResources().getColor(R.color.gray_900));
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH) + 1;
                        DatePickerDialog datePickerDialog =
                                new DatePickerDialog(NewTaskActivity.this, reminderDateSetListener(),
                                        year, month, day);
                        datePickerDialog.show();
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void showReminderTimeDropdownMenu() {
        PopupMenu popupMenu = new PopupMenu(this, fieldSetReminderTime);
        popupMenu.getMenuInflater().inflate(R.menu.popup_reminder_time, popupMenu.getMenu());
        final Calendar c = Calendar.getInstance();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.none:
                        fieldSetReminderTimeTextview.setText(getString(R.string.none));
                        reminderTimeMillis = 0;
                        break;
                    case R.id.morning:
                        fieldSetReminderTimeTextview.setText(getString(R.string.morning));
                        reminderTimeMillis = utility.timeToMillis(9, 0);
                        break;
                    case R.id.afternoon:
                        fieldSetReminderTimeTextview.setText(getString(R.string.afternoon));
                        reminderTimeMillis = utility.timeToMillis(14, 0);
                        break;
                    case R.id.evening:
                        fieldSetReminderTimeTextview.setText(getString(R.string.evening));
                        reminderTimeMillis = utility.timeToMillis(18, 0);
                        break;
                    case R.id.custom:
                        int hour = (int) (reminderTimeMillis / 1000) / 3600;
                        int minute = (int) (((reminderTimeMillis / 1000) - (hour * 3600)) / 60);
                        TimePickerDialog timePickerFragment = new TimePickerDialog(NewTaskActivity.this, onTimeSetListener(), hour, minute, true);
                        timePickerFragment.show();
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private View.OnClickListener listener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.field_class_dropdown:
                        showClassDropdownMenu();
                        break;
                    case R.id.field_type_dropdown:
                        showTypeDropdownMenu();
                        break;
                    case R.id.field_new_task_duedate:
                    case R.id.field_new_task_duedate_textview:
                        Calendar c_duedate = Calendar.getInstance();
                        Date date_duedate = new Date();
                        c_duedate.setTime(date_duedate);
                        int year_duedate = c_duedate.get(Calendar.YEAR);
                        int month_duedate = c_duedate.get(Calendar.MONTH);
                        int day_duedate = c_duedate.get(Calendar.DAY_OF_MONTH) + 1;
                        DatePickerDialog datePickerDialog_duedate = new DatePickerDialog(NewTaskActivity.this, dueDateSetListener(), year_duedate, month_duedate, day_duedate);
                        datePickerDialog_duedate.show();
                        break;
//                    case R.id.field_new_task_attach:
//                        if (mFirebaseUser != null)
//                            Toast.makeText(NewTaskActivity.this, "Coming soon", Toast.LENGTH_SHORT).show();
//                        else {
//                            Intent attach_intent = new Intent(Intent.ACTION_PICK);
//                            attach_intent.setType("*/*");
//                            attach_intent.setAction(Intent.ACTION_GET_CONTENT);
//                            attach_intent.addCategory(Intent.CATEGORY_OPENABLE);
//                            startActivityForResult(attach_intent, REQUEST_FILE_GET);
//                        }
//                        break;
                    case R.id.take_photo_text:
                    case R.id.take_photo_icon:
                        // Request all permissions (for API 23+)
                        int permissionCheck = ContextCompat.checkSelfPermission(NewTaskActivity.this,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(NewTaskActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                //Show permission explanation dialog...
                                new AlertDialog.Builder(NewTaskActivity.this)
                                        .setMessage(getString(R.string.dialog_permission_rationale_take_photo))
                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ActivityCompat.requestPermissions(NewTaskActivity.this,
                                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW},
                                                        REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                                            }
                                        })
                                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                return;
                                            }
                                        })
                                        .show();
                            }
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(NewTaskActivity.this);
                        builder.setTitle(getString(R.string.field_new_photo_dialog_title))
                                .setItems(R.array.field_new_photo_dialog_items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                try {
                                                    dispatchTakePictureIntent();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case 1:
                                                dispatchSelectPhotoIntent();
                                                break;
                                        }
                                    }
                                }).show();
                        break;
                    case R.id.field_new_task_reminder_date:
                    case R.id.field_new_task_reminder_date_textview:
                        showReminderDateDropdownMenu();
                        break;
                    case R.id.field_new_task_reminder_time_textview:
                    case R.id.field_new_task_reminder_time:
                        showReminderTimeDropdownMenu();
                        break;
                }
            }
        };
    }

    // Special ItemClickListener for when dueDate is set from dialog
    private DatePickerDialog.OnDateSetListener dueDateSetListener() {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth);
                dueDateMillis = c.getTimeInMillis();
                fieldDueDateTextView.setText(utility.formatDateString(NewTaskActivity.this, year, monthOfYear, dayOfMonth));
            }
        };
    }

    // This method is called when a date for the reminding notification is set
    private DatePickerDialog.OnDateSetListener reminderDateSetListener() {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth);
                reminderDateMillis = c.getTimeInMillis();
                Calendar currentDate = Calendar.getInstance();
                if (currentDate.getTimeInMillis() == reminderDateMillis)
                    fieldSetReminderDateTextview.setText(getString(R.string.today));
                else {
                    int currentYear = currentDate.get(Calendar.YEAR);
                    int currentMonth = currentDate.get(Calendar.MONTH);
                    int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);
                    currentDate.set(currentYear, currentMonth, currentDay + 1);
                    if (currentDate.getTimeInMillis() == reminderDateMillis)
                        fieldSetReminderDateTextview.setText(getString(R.string.tomorrow));
                    else
                        fieldSetReminderDateTextview.setText(utility.formatDateString(NewTaskActivity.this, year, monthOfYear, dayOfMonth));
                }
            }
        };
    }

    // Special ItemClickListener used for reminder time
    public TimePickerDialog.OnTimeSetListener onTimeSetListener() {
        return new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                reminderTimeMillis = utility.timeToMillis(hourOfDay, minute) / 1000;
                fieldSetReminderTimeTextview.setText(utility.millisToHourTime(reminderTimeMillis * 1000));
            }
        };
    }

    // Action called when the Icon ImageView is clicked
    private View.OnClickListener showIconPrompt() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new IconPromptDialog();
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        };
    }

    // Action called when previewed photo is clicked
    private View.OnClickListener photoListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewTaskActivity.this);
                builder.setTitle(getString(R.string.field_existing_photo_dialog_title))
                        .setItems(R.array.field_existing_photo_dialog_items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        try {
                                            dispatchTakePictureIntent();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 1:
                                        dispatchSelectPhotoIntent();
                                        break;
                                    case 2:
                                        break;
                                }
                            }
                        }).show();
            }
        };
    }

    private void showBuiltInIconsDialog() {
        // Prepare grid view
        GridView gridView = new GridView(this);
        final AlertDialog dialog;

        int[] builtinIcons = getResources().getIntArray(R.array.builtin_icons);
        List<Integer> mList = new ArrayList<>();
        for (int i = 1; i < builtinIcons.length; i++) {
            mList.add(builtinIcons[i]);
        }

        gridView.setAdapter(new BuiltInSubjectIconsAdapter(this));
        gridView.setNumColumns(4);
        gridView.setPadding(0, 16, 0, 16);
        gridView.setGravity(Gravity.CENTER);
        // Set grid view to alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(gridView);
        builder.setTitle(getString(R.string.new_schedule_icon_builtin_title));
        dialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int resId = mThumbIds[position];
                fieldIcon.setImageResource(resId);
                Resources resources = getResources();
                Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId)
                        + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId));
                iconUriString = drawableUri.toString();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private boolean insertTaskDataIntoDatabase() throws IOException {
        // Get the data
        final String title = fieldTitle.getText().toString();
        final String description = fieldDescription.getText().toString();

        if (classTitle.equals(getString(R.string.none)))
            classTitle = "";
        if (classType.equals(getString(R.string.none)))
            classType = "";

        // Insert the data based on the user
        if (mFirebaseUser != null) {
            // Insert into Firebase
            final DatabaseReference taskRef;
            if (FLAG_EDIT)
                taskRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("tasks").child(firebaseEditId);
            else taskRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("tasks").push();
            Map<String, Float> duedateMap = new HashMap<>();
            duedateMap.put("duedate", dueDateMillis);
            taskRef.setValue(duedateMap);
            taskRef.child("title").setValue(title);
            taskRef.child("class").setValue(classTitle);
            taskRef.child("type").setValue(classType);
            taskRef.child("sharer").setValue("");
            taskRef.child("description").setValue(description);
            taskRef.child("completed").setValue(false);

            if (customIconUploaded) {
                // Copy the file to the app's directory
                Uri imageUri = Uri.parse(iconUriString);
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                String filename = imageUri.getLastPathSegment();
                byte[] data = getBytes(inputStream);
                File file = new File(getFilesDir(), filename);
                FileOutputStream outputStream;
                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(data);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Image compression
                Bitmap decodedBitmap = decodeFile(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                decodedBitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
                byte[] bitmapData = baos.toByteArray();
                file.delete();
                File file1 = new File(getFilesDir(), filename);
                FileOutputStream fos = new FileOutputStream(file1);
                fos.write(bitmapData);
                fos.flush();
                fos.close();

                // Get the uri of the file so it can be saved
                iconUriString = Uri.fromFile(file1).toString();
            }

            taskRef.child("icon").setValue(iconUriString);

            // Upload the custom icon and photos
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference iconRef = storageRef.child(mUserId + "/tasks/" + title);
            if (customIconUploaded) {
                fieldIcon.setDrawingCacheEnabled(true);
                fieldIcon.buildDrawingCache();
                Bitmap bitmap = fieldIcon.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = iconRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO: Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // TODO: Handle successful upload if any action is required
                    }
                });
            }

            // Photos upload
            StorageReference photosRef = iconRef.child("photos");
            for (int i = 0; i < photoUriList.size(); i++) {
                // Save the photos uri in the database
                taskRef.child("photos").child("local").child(String.valueOf(i)).setValue(photoUriList.get(i).toString());

                // Create a path and an input stream to upload the photo
                StorageReference uploadPathRef = photosRef.child(Integer.toString(i));
                InputStream iStream = getContentResolver().openInputStream(photoUriList.get(i));
                byte[] data = getBytes(iStream);
                UploadTask uploadTask = uploadPathRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO: Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        // TODO: Handle successful upload if any action is required
                        taskRef.child("photos").child("cloud").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                ArrayList<String> currentData = (ArrayList<String>) dataSnapshot.getValue();
                                if (currentData != null)
                                    currentData.add(taskSnapshot.getDownloadUrl().toString());
                                else {
                                    currentData = new ArrayList<>();
                                    currentData.add(taskSnapshot.getDownloadUrl().toString());
                                }
                                taskRef.child("photos").child("cloud").setValue(currentData);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }

            // Set the alarm for the notification
            {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(reminderDateMillis);
                int hour = (int) reminderTimeMillis / 3600;
                int minute = (int) (reminderTimeMillis - hour * 3600) / 60;
                c.set(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, minute);
                long notificationMillis = (c.getTimeInMillis());
                if (reminderDateMillis > 0)
                    ScheduleNotification(new Date(notificationMillis), -1, taskRef.getKey(), getString(R.string.notification_message_reminder), title);
            }

            // Share the task to peers if checked shared
            if (fieldSharedCheckbox.isChecked()) {
                final ArrayList<String> peerUIDs = new ArrayList<>();
                DatabaseReference classPeersRef;
                if (classTitle.equals(""))
                    classPeersRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserId).child("peers");
                else classPeersRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("classes").child(classTitle).child("peers");
                classPeersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long snapshotCount = dataSnapshot.getChildrenCount();
                        for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                            peerUIDs.add(classSnapshot.getKey());
                            for (int i = 0; i < peerUIDs.size(); i++) {
                                // All the values are set here per peer
                                String peerUid = peerUIDs.get(i);
                                DatabaseReference peerTask = FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(peerUid).child("tasks").push();
                                peerTask.child("title").setValue(title);
                                peerTask.child("class").setValue(classTitle);
                                peerTask.child("type").setValue(classType);
                                peerTask.child("sharer").setValue(name);
                                peerTask.child("description").setValue(description);
                                peerTask.child("duedate").setValue(dueDateMillis);
                                peerTask.child("icon").setValue(iconUriString);
                                peerTask.child("completed").setValue(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
            return true;
        } else {
            // Copy the icon into a seperate file if custom
            if (customIconUploaded) {
                Uri imageUri = Uri.parse(iconUriString);
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                String filename = imageUri.getLastPathSegment();
                byte[] data = getBytes(inputStream);
                File file = new File(getFilesDir(), filename);
                FileOutputStream outputStream;
                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(data);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Image compression
                Bitmap decodedBitmap = decodeFile(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                decodedBitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
                byte[] bitmapData = baos.toByteArray();
                file.delete();
                File file1 = new File(getFilesDir(), filename);
                FileOutputStream fos = new FileOutputStream(file1);
                fos.write(bitmapData);
                fos.flush();
                fos.close();

                // Get the uri of the file so it can be saved
                iconUriString = Uri.fromFile(file1).toString();
            }

            // Insert into SQLite
            DbHelper dbHelper = new DbHelper(this);

            if (FLAG_EDIT) {
                // Update database row
                if (dbHelper.updateTaskItem(this, editId, title, classTitle, classType, description, attachedFileUriString,
                        dueDateMillis, reminderDateMillis, reminderTimeMillis, iconUriString, photoUriList, false))
                    return true;
            } else {
                // Insert a new database row
                int id = (int) dbHelper.insertTask(this, title, classTitle, classType, description, attachedFileUriString,
                        dueDateMillis, reminderDateMillis, reminderTimeMillis, iconUriString, photoUriList, false);

                // Schedule a notification
                {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(reminderDateMillis);
                    int hour = (int) reminderTimeMillis / 3600;
                    int minute = (int) (reminderTimeMillis - hour * 3600) / 60;
                    c.set(Calendar.HOUR_OF_DAY, hour);
                    c.set(Calendar.MINUTE, minute);
                    long notificationMillis = (c.getTimeInMillis());
                    if (reminderDateMillis > 0)
                        ScheduleNotification(new Date(notificationMillis), id, "", getString(R.string.notification_message_reminder), title);
                }

                if (id > -1)
                    return true;
                else Log.d(LOG_TAG, "Task not inserted");
            }

            return false;
        }
    }

    private Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE=300;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }


    // Used for Attach File
    void saveFile(Uri sourceuri) {
        String sourceFilename = sourceuri.getPath();
        String destinationFilename = android.os.Environment.getExternalStorageDirectory().getPath() + File.separatorChar + "abc.mp3";

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
        } catch (IOException e) {

        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {

            }
        }
    }

    private void ScheduleNotification(final Date dateTime, final int ID, final String firebaseID, final String title, final String message) {
        final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Bitmap largeIcon = null;
        try {
            largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(iconUriString));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setBackground(largeIcon);

        Intent contentIntent = new Intent(this, TasksDetailActivity.class);
        if (ID > -1)
            contentIntent.putExtra("_ID", ID);
        else contentIntent.putExtra("id", firebaseID);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TasksDetailActivity.class);
        stackBuilder.addNextIntent(contentIntent);
        final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent
                (REQUEST_NOTIFICATION_INTENT, PendingIntent.FLAG_UPDATE_CURRENT);

        Palette.generateAsync(largeIcon, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                builder.setContentIntent(contentPendingIntent)
                        .setSmallIcon(R.drawable.ic_assignment)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .extend(wearableExtender)
                        .setDefaults(Notification.DEFAULT_ALL);

                Notification notification = builder.build();

                Intent notificationIntent = new Intent(NewTaskActivity.this, TaskNotificationPublisher.class);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(NewTaskActivity.this, REQUEST_NOTIFICATION_ALARM,
                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC, dateTime.getTime(), pendingIntent);
            }
        });
    }

    private void dispatchTakePictureIntent() throws IOException {
        // Check if the CAMERA permission has been granted
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_PERMISSION_CAMERA);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = createImageFile();
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.pdt.plume.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    mTempPhotoUri = photoURI;
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private void dispatchSelectPhotoIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, REQUEST_IMAGE_GET_PHOTO);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    // Interface method when icon is selected from built-in icons
    @Override
    public void OnIconListItemSelected(int item) {
        switch (item) {
            case 0:
                showBuiltInIconsDialog();
                break;
            case 1:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivityForResult(intent, REQUEST_IMAGE_GET_ICON);
                break;
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

}
